package com.yuliyuli.service.support;

import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.delivery.VideoDeliveryWithoutFile;
import com.yuliyuli.entity.video.Comment;
import com.yuliyuli.entity.video.VideoCollection;
import com.yuliyuli.entity.video.VideoLike;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VideoEventPublisher {

  private final RabbitTemplate rabbitTemplate;

  public void publishVideoDeliver(VideoDeliveryWithoutFile videoDelivery) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.VIDEO_QUEUE_NAME, videoDelivery);
  }

  public void publishVideoLike(VideoLike videoLike) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.LIKE_QUEUE_NAME, videoLike);
  }

  public void publishVideoCollect(VideoCollection videoCollection) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.COLLECT_QUEUE_NAME, videoCollection);
  }

  public void publishVideoComment(Comment comment) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.COMMENT_QUEUE_NAME, comment);
  }

  public void publishHotVideoPlay(String videoUrl) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.HOT_PLAY_QUEUE_NAME, videoUrl);
  }

  public void publishVideoPlay(String videoUrl) {
    rabbitTemplate.convertAndSend(RabbitMqConfig.PLAY_QUEUE_NAME, videoUrl);
  }
}
