package com.yuliyuli.consumer.support;

import com.rabbitmq.client.Channel;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsumerRetrySupport {

  public int getRetryCount(Message mqMessage, String retryHeader) {
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    return (Integer) headers.getOrDefault(retryHeader, 0);
  }

  public void handleRetry(
      long deliveryTag,
      Channel channel,
      int retryCount,
      int maxRetryCount,
      Map<String, Object> headers,
      String retryHeader,
      String actionName) {
    if (retryCount < maxRetryCount) {
      headers.put(retryHeader, retryCount + 1);
      try {
        channel.basicNack(deliveryTag, false, true);
      } catch (Exception e) {
        log.error("重试{}消息失败,重试次数:{}", actionName, retryCount + 1, e);
      }
      return;
    }

    try {
      channel.basicReject(deliveryTag, false);
    } catch (Exception e) {
      log.error("{}消息重试次数超过最大重试次数,已丢弃,重试次数:{}", actionName, retryCount, e);
    }
  }

  public void ackDeadLetter(long deliveryTag, Channel channel, String actionName) {
    try {
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      throw new IllegalStateException("死信队列处理失败:" + actionName, e);
    }
  }
}
