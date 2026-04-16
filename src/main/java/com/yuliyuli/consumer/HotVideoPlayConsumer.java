package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.consumer.support.ConsumerRetrySupport;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class HotVideoPlayConsumer {

  private final String RETRY_HEADER = "hot-play-retry-count";
  private final int MAX_RETRY_COUNT = 3;
  private final String LOCK_KEY_PREFIX = "hot:video:play:lock:";
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
  private final String DELAY_KEY = "hot:video:play:delay";
  private final String HOT_PLAY_COUNTER_KEY_PREFIX = "video:hot:play:";
  private final Long DELAY_TIME = 1000 * 3L; // 3秒

  @Resource private RedissonClient redissonClient;

  @Resource private ConsumerRetrySupport consumerRetrySupport;

  @RabbitListener(queues = RabbitMqConfig.HOT_PLAY_QUEUE_NAME)
  public void videoPlay(String videoUrl, Channel channel, Message mqMessage) throws Exception {

    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    int retryCount = consumerRetrySupport.getRetryCount(mqMessage, RETRY_HEADER);
    // 参数校验
    if (videoUrl == null) {
      log.error("视频URL为空");
      channel.basicReject(deliveryTag, false);
      return;
    }
    final String LOCK_KEY = LOCK_KEY_PREFIX + videoUrl;
    RLock lock = redissonClient.getLock(LOCK_KEY);
    try {
      // 获取视频播放锁
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.warn("热门视频播放锁获取失败");
        consumerRetrySupport.handleRetry(
            deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "热门播放");
        return;
      }
      redissonClient.getAtomicLong(HOT_PLAY_COUNTER_KEY_PREFIX + videoUrl).incrementAndGet();
      // 放入延时有序集合
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);
      // 播放完成后，手动确认消息
      channel.basicAck(deliveryTag, false);
      log.debug("热门视频播放消息入延时队列成功,视频URL:{}", videoUrl);
    } catch (Exception e) {
      log.error("热门视频播放消费异常,retry={}", retryCount, e);
      consumerRetrySupport.handleRetry(
          deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "热门播放");
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.HOT_PLAY_DEAD_QUEUE_NAME)
  public void videoPlayDeadConsumer(String videoUrl, Channel channel, Message mqMessage) {
    log.info("播放死信消费者,视频URL:{}", videoUrl);
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      consumerRetrySupport.ackDeadLetter(diliverTag, channel, "热门播放");
    } catch (Exception e) {
      log.error("死信队列丢弃热门播放失败,视频URL:{}", videoUrl, e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃热门播放失败");
    }
  }
}
