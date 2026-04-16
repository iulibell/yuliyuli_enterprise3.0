package com.yuliyuli.task;

import com.yuliyuli.dto.command.VideoDeleteCommand;
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
public class DeleteDelayTask {

  private final RedissonClient redissonClient;
  private final DeleteTaskSupport deleteTaskSupport;

  @Scheduled(fixedRate = 5000)
  @Async
  public void processDelayDelete() {
    long currentTime = System.currentTimeMillis();
    RScoredSortedSet<VideoDeleteCommand> sortedSet =
        redissonClient.getScoredSortedSet(TaskKeyConstants.DELETE_DELAY_KEY);
    Collection<VideoDeleteCommand> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());

    for (VideoDeleteCommand task : expiredVideoUrls) {
      try {
        deleteTaskSupport.processDelete(task);
        sortedSet.remove(task);
      } catch (Exception e) {
        log.error("处理延时视频删除失败: {}", task, e);
      }
    }
  }

  @Scheduled(fixedRate = 10000)
  @Async
  public void processDeleteIsDelete() {
    deleteTaskSupport.cleanupSoftDeletedVideos();
  }
}
