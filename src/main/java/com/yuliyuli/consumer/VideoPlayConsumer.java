package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.consumer.support.ConsumerRetrySupport;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoPlayConsumer {

  private final int MAX_RETRY_COUNT = 3;
  private final String RETRY_HEADER = "play-retry-count";
  private final String DELAY_KEY = "video:play:delay";
  private final String PLAY_COUNTER_KEY_PREFIX = "video:play:";
  private final int DELAY_TIME = 1000 * 2; // 2秒

  @Resource private RedissonClient redissonClient;

  @Resource private ConsumerRetrySupport consumerRetrySupport;

  @RabbitListener(queues = RabbitMqConfig.PLAY_QUEUE_NAME)
  public void videoPlay(String videoUrl, Channel channel, Message mqMessage) throws Exception {

    long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    int retryCount = consumerRetrySupport.getRetryCount(mqMessage, RETRY_HEADER);

    if (videoUrl == null) {
      log.error("视频URL为空");
      channel.basicReject(deliveryTag, false);
      return;
    }

    try {
      redissonClient.getAtomicLong(PLAY_COUNTER_KEY_PREFIX + videoUrl).incrementAndGet();
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME, videoUrl);
      // 播放完成后，手动确认消息
      channel.basicAck(deliveryTag, false);
      log.debug("视频播放消息入延时队列成功,视频URL:{}", videoUrl);
    } catch (Exception e) {
      log.error("视频播放消费异常,重试次数:{}", retryCount, e);
      consumerRetrySupport.handleRetry(
          deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "播放");
    }
  }

  @RabbitListener(queues = RabbitMqConfig.PLAY_DEAD_QUEUE_NAME)
  public void videoPlayDeadConsumer(String videoUrl, Channel channel, Message mqMessage) {
    log.info("播放视频死信消费者,视频URL:{}", videoUrl);
    long deliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      consumerRetrySupport.ackDeadLetter(deliverTag, channel, "播放");
    } catch (Exception e) {
      log.error("死信队列播放视频失败,视频URL:{}", videoUrl, e);
      throw new GlobalExceptionHandler.BusinessException("死信队列播放视频失败");
    }
  }
}
