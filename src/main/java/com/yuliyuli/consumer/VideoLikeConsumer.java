package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.consumer.support.ConsumerRetrySupport;
import com.yuliyuli.entity.video.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VideoLikeConsumer {

  private static final String RETRY_HEADER = "video-like-retry-count";
  private final String DELAY_KEY = "video:like:delay";
  private final long DELAY_TIME_MS = 5000L;
  private final String LOCK_KEY_PREFIX = "video:like:lock:";
  private final String USER_KEY_PREFIX = "user:like:";
  private final int LOCK_WAIT = 3; // 3秒
  private final int LOCK_RELEASE = 10; // 10秒
  private final int MAX_RETRY_COUNT = 3; // 最大重试次数

  @Resource private RedissonClient redissonClient;

  @Resource private ConsumerRetrySupport consumerRetrySupport;

  @RabbitListener(queues = RabbitMqConfig.LIKE_QUEUE_NAME)
  public void videoLike(VideoLike videoLike, Channel channel, Message mqMessage) throws Exception {
    long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    int retryCount = consumerRetrySupport.getRetryCount(mqMessage, RETRY_HEADER);
    // 参数校验
    if (videoLike == null || videoLike.getVideoId() == null || videoLike.getUserId() == null) {
      log.error("点赞失败");
      channel.basicReject(deliveryTag, false);
      return;
    }
    // 构建分布式锁Key：视频ID + 用户ID
    String videoId = videoLike.getVideoId();
    String userId = videoLike.getUserId().toString();
    String lockKey = LOCK_KEY_PREFIX + videoId + ":" + userId;
    RLock lock = redissonClient.getLock(lockKey);
    try {
      boolean isLocked = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLocked) {
        log.info("用户{}点赞视频{}失败，获取分布式锁失败,已重新放入队列", userId, videoId);
        consumerRetrySupport.handleRetry(
            deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "点赞");
        return;
      }
      String userKey = USER_KEY_PREFIX + videoLike.getVideoId();
      RSet<String> userSet = redissonClient.getSet(userKey);
      userSet.add(userId);
      redissonClient
          .getScoredSortedSet(DELAY_KEY)
          .add(System.currentTimeMillis() + DELAY_TIME_MS, videoLike);
      // 6. 手动ACK：确认消息消费成功（关键：防止重复消费）
      channel.basicAck(deliveryTag, false);
      log.info("用户{}点赞视频{}成功", userId, videoId);
    } catch (Exception e) {
      log.error("点赞消费异常,重试次数:{}", retryCount, e);
      consumerRetrySupport.handleRetry(
          deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "点赞");
    } finally {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.LIKE_DEAD_QUEUE_NAME)
  public void videoLikeDeadConsumer(VideoLike videoLike, Channel channel, Message mqMessage) {
    log.info("点赞视频死信消费者,视频ID:{}", videoLike.getVideoId());
    Long diliverTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      consumerRetrySupport.ackDeadLetter(diliverTag, channel, "点赞");
    } catch (Exception e) {
      log.error("死信队列点赞视频失败,视频ID:{}", videoLike.getVideoId(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列点赞视频失败");
    }
  }
}
