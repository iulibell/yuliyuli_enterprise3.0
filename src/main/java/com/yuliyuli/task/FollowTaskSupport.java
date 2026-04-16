package com.yuliyuli.task;

import com.yuliyuli.dto.command.FollowCommand;
import com.yuliyuli.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowTaskSupport {

  private final RedissonClient redissonClient;
  private final FollowMapper followMapper;

  public void processFollowTask(FollowCommand command) {
    if (command == null || command.getFanUserId() == null || command.getFollowUserId() == null) {
      log.error("任务数据不完整: {}", command);
      return;
    }

    try {
      long fanUserId = command.getFanUserId();
      long followUserId = command.getFollowUserId();
      String hotUserKey = "user:hot:" + followUserId;
      RScoredSortedSet<Long> hotUserSet = redissonClient.getScoredSortedSet(hotUserKey);
      boolean isHotUser = !hotUserSet.isEmpty();

      if ("unfollow".equals(command.getOperation())) {
        handleUnfollow(fanUserId, followUserId, hotUserSet, isHotUser);
      } else {
        handleFollow(fanUserId, followUserId, hotUserSet, isHotUser);
      }
    } catch (Exception e) {
      log.error("处理任务异常: {}", command, e);
    }
  }

  private void handleFollow(
      long fanUserId, long followUserId, RScoredSortedSet<Long> hotUserSet, boolean isHotUser) {
    if (followMapper.getFollow(followUserId, fanUserId) == null) {
      followMapper.insertFollow(followUserId, fanUserId);
      followMapper.updateFansCount(followUserId);

      if (isHotUser) {
        hotUserSet.add(System.currentTimeMillis(), fanUserId);
        log.info("关注热门UP主成功: fan={}, up={}", fanUserId, followUserId);
      } else {
        log.info("关注普通UP主成功: fan={}, up={}", fanUserId, followUserId);
      }
    }
  }

  private void handleUnfollow(
      long fanUserId, long followUserId, RScoredSortedSet<Long> hotUserSet, boolean isHotUser) {
    if (followMapper.getFollow(followUserId, fanUserId) != null) {
      followMapper.deleteFollow(followUserId, fanUserId);
      followMapper.decrementFansCount(followUserId);

      if (isHotUser) {
        hotUserSet.remove(fanUserId);
        log.info("取消热门UP主关注: fan={}, up={}", fanUserId, followUserId);
      } else {
        log.info("取消普通UP主关注: fan={}, up={}", fanUserId, followUserId);
      }
    }
  }
}
