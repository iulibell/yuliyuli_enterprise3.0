package com.yuliyuli.task;

import com.yuliyuli.dto.command.FollowCommand;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FollowDelayTask {

  private final RedissonClient redissonClient;
  private final FollowTaskSupport followTaskSupport;

  @Scheduled(fixedRate = 5000)
  public void processFollow() {
    try {
      RScoredSortedSet<FollowCommand> sortedSet =
          redissonClient.getScoredSortedSet(TaskKeyConstants.FOLLOW_DELAY_KEY);
      long now = System.currentTimeMillis();
      Collection<ScoredEntry<FollowCommand>> entries =
          sortedSet.entryRange(0, true, now, true, 0, 200);

      for (ScoredEntry<FollowCommand> entry : entries) {
        FollowCommand task = entry.getValue();
        boolean removed = sortedSet.remove(task);
        if (!removed) {
          continue;
        }

        try {
          followTaskSupport.processFollowTask(task);
        } catch (Exception e) {
          log.error("处理关注任务失败: {}", task, e);
        }
      }
    } catch (Exception e) {
      log.error("延时任务执行异常", e);
    }
  }
}
