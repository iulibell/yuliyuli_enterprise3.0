package com.yuliyuli.task;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlayDelayTask {

  private final RedissonClient redissonClient;
  private final PlayTaskSupport playTaskSupport;

  @Scheduled(fixedRate = 15000)
  @Async
  public void processDelayHotPlay() {
    processVideoPlaySet(TaskKeyConstants.HOT_PLAY_DELAY_KEY, true);
  }

  @Scheduled(fixedRate = 5000)
  public void processDelayPlay() {
    processVideoPlaySet(TaskKeyConstants.PLAY_DELAY_KEY, false);
  }

  private void processVideoPlaySet(String key, boolean hotPlay) {
    long currentTime = System.currentTimeMillis();
    RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(key);
    Collection<String> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());

    for (String videoUrl : expiredVideoUrls) {
      try {
        if (hotPlay) {
          playTaskSupport.processHotPlay(videoUrl);
        } else {
          playTaskSupport.processPlay(videoUrl);
        }
        sortedSet.remove(videoUrl);
      } catch (Exception e) {
        log.error("处理延时{}视频播放失败: {}", hotPlay ? "热门" : "", videoUrl, e);
      }
    }
  }
}
