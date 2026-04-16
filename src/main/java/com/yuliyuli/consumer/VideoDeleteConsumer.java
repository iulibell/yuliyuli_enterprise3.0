package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.consumer.support.ConsumerRetrySupport;
import com.yuliyuli.dto.command.VideoDeleteCommand;
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
public class VideoDeleteConsumer {

  private final String VIDEO_DELETE_LOCK_PREFIX = "video:delete:lock:";
  private final int DELAY_TIME = 1000 * 5;
  private final String DELAY_KEY = "video:delete:delay";
  private final String RETRY_HEADER = "video-collect-retry-count";
  private final int MAX_RETRY_COUNT = 3;
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒

  @Resource private RedissonClient redissonClient;

  @Resource private ConsumerRetrySupport consumerRetrySupport;

  @RabbitListener(queues = RabbitMqConfig.DELETE_QUEUE_NAME)
  public void videoDelete(VideoDeleteCommand command, Channel channel, Message mqMessage)
      throws Exception {
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    int retryCount = consumerRetrySupport.getRetryCount(mqMessage, RETRY_HEADER);

    if (command == null || command.getVideoUrl() == null || command.getUserId() == null) {
      log.error("视频删除消息videoUrl或userId为空");
      channel.basicReject(diliverTag, false);
      return;
    }
    String lockKey = VIDEO_DELETE_LOCK_PREFIX + command.getVideoUrl();
    // 获取删除视频锁
    RLock lock = redissonClient.getLock(lockKey);
    try {
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.error("视频删除锁已被占用");
        consumerRetrySupport.handleRetry(
            diliverTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "视频删除");
        return;
      }
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, command);
      // 播放完成后，手动确认消息
      channel.basicAck(diliverTag, false);
      log.info("视频删除消息确认成功,视频URL:{}", command.getVideoUrl());
    } catch (Exception e) {
      log.error("视频删除消费者异常，重试次数:{}", retryCount, e);
      consumerRetrySupport.handleRetry(
          diliverTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "视频删除");
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.DELETE_DEAD_QUEUE_NAME)
  public void videoDeleteDeadConsumer(
      VideoDeleteCommand command, Channel channel, Message mqMessage) {
    log.info("删除视频死信消费者,视频URL:{}", command != null ? command.getVideoUrl() : null);
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      consumerRetrySupport.ackDeadLetter(diliverTag, channel, "视频删除");
    } catch (Exception e) {
      log.error("死信队列丢弃删除视频失败,视频URL:{}", command != null ? command.getVideoUrl() : null, e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃删除视频失败");
    }
  }
}
