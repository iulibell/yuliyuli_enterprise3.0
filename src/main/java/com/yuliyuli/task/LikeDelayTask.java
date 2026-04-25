package com.yuliyuli.task;

import com.yuliyuli.entity.video.VideoLike;
import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LikeDelayTask {

  private final RedissonClient redissonClient;
  private final LikeTaskSupport likeTaskSupport;

  @Scheduled(fixedRate = 1000)
  @Async
  public void processDelayLikes() {
    long currentTime = System.currentTimeMillis();
    RScoredSortedSet<VideoLike> sortedSet =
        redissonClient.getScoredSortedSet(TaskKeyConstants.LIKE_DELAY_KEY);
    Collection<VideoLike> expiredLikes =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(ScoredEntry::getValue)
            .collect(Collectors.toList());

    for (VideoLike videoLike : expiredLikes) {
      try {
        likeTaskSupport.processLike(videoLike);
        sortedSet.remove(videoLike);
      } catch (Exception e) {
        log.error("处理延时点赞失败", e);
      }
    }
  }
}
