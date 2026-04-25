package com.yuliyuli.consumer;

import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.consumer.support.ConsumerRetrySupport;
import com.yuliyuli.dto.command.FollowCommand;
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
public class FollowConsumer {

  private static final int MAX_RETRY_COUNT = 3;
  private static final String RETRY_HEADER = "follow-retry-count";
  private static final String LOCK_KEY_PREFIX = "follow_lock:";
  private static final String DELAY_KEY = "follow:delay";
  private static final String FOLLOW_KEY_PREFIX = "follow:";
  private static final int LOCK_WAIT = 3;
  private static final int LOCK_LEASE = 3;
  private static final long DELAY_TIME = 1000 * 5;

  @Resource RedissonClient redissonClient;

  @Resource private ConsumerRetrySupport consumerRetrySupport;

  @RabbitListener(queues = RabbitMqConfig.FOLLOW_QUEUE_NAME)
  public void followConsumer(FollowCommand command, Channel channel, Message mqMessage)
      throws Exception {
    long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 校验参数
    if (command == null || command.getFanUserId() == null || command.getFollowUserId() == null) {
      log.error("关注消费参数校验失败,fanUserId:{}", command != null ? command.getFanUserId() : null);
      // 拒绝消息,不重新入队
      channel.basicReject(deliveryTag, false);
      return;
    }

    // 获取操作类型
    String operation = command.getOperation() == null ? "follow" : command.getOperation();

    // 重试次数,从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    int retryCount = consumerRetrySupport.getRetryCount(mqMessage, RETRY_HEADER);
    // 进行加锁操作
    RLock lock =
        redissonClient.getLock(
            LOCK_KEY_PREFIX + command.getFanUserId() + ":" + command.getFollowUserId());
    try {
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS);
      if (!isLock) {
        log.error("关注操作加锁失败,fanUserId:{}", command.getFanUserId());
        consumerRetrySupport.handleRetry(
            deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "关注");
        return;
      }

      if ("unfollow".equals(operation)) {
        // 取消关注操作
        String followKey = FOLLOW_KEY_PREFIX + command.getFanUserId();
        RSet<String> followSet = redissonClient.getSet(followKey);
        followSet.remove(command.getFollowUserId().toString());
        redissonClient.getScoredSortedSet(DELAY_KEY).add(System.currentTimeMillis() + DELAY_TIME, command);
        // 手动确认消息
        channel.basicAck(deliveryTag, false);
        log.info("取消关注操作成功,fanUserId:{}", command.getFanUserId());
      } else {
        // 关注操作
        // 将主动关注的用户ID添加到关注集合中当主键，值为被关注的用户ID
        String followKey = FOLLOW_KEY_PREFIX + command.getFanUserId();
        RSet<String> followSet = redissonClient.getSet(followKey);
        followSet.add(command.getFollowUserId().toString());
        redissonClient.getScoredSortedSet(DELAY_KEY).add(System.currentTimeMillis() + DELAY_TIME, command);
        // 手动确认消息
        channel.basicAck(deliveryTag, false);
        log.info("关注操作成功,fanUserId:{}", command.getFanUserId());
      }
    } catch (InterruptedException e) {
      log.error("关注操作加锁失败,重试次数:{}", retryCount, e);
      // 拒绝消息,不重新入队
      consumerRetrySupport.handleRetry(
          deliveryTag, channel, retryCount, MAX_RETRY_COUNT, headers, RETRY_HEADER, "关注");
      return;
    } finally {
      // 释放锁
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  @RabbitListener(queues = RabbitMqConfig.FOLLOW_DEAD_QUEUE_NAME)
  public void followDeadConsumer(FollowCommand command, Channel channel, Message mqMessage) {
    log.info("关注操作死信队列消费,fanUserId:{}", command != null ? command.getFanUserId() : null);
    long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    try {
      consumerRetrySupport.ackDeadLetter(deliveryTag, channel, "关注");
    } catch (Exception e) {
      log.error("关注死信队列消费异常", e);
      throw new GlobalExceptionHandler.BusinessException("关注死信队列消费异常");
    }
  }
}
