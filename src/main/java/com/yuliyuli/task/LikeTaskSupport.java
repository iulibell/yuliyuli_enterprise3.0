package com.yuliyuli.task;

import com.yuliyuli.dto.query.VideoLikeWrapper;
import com.yuliyuli.entity.video.VideoLike;
import com.yuliyuli.mapper.VideoLikeMapper;
import com.yuliyuli.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LikeTaskSupport {

  private final RedissonClient redissonClient;
  private final VideoMapper videoMapper;
  private final VideoLikeWrapper videoLikeWrapper;
  private final VideoLikeMapper videoLikeMapper;

  public void processLike(VideoLike videoLike) {
    String counterKey = TaskKeyConstants.LIKE_COUNTER_KEY_PREFIX + videoLike.getVideoId();
    String userKey = TaskKeyConstants.USER_KEY_PREFIX + videoLike.getVideoId();
    Long userId = videoLike.getUserId();
    String videoId = videoLike.getVideoId().toString();

    try {
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      RSet<Long> userSet = redissonClient.getSet(userKey);
      boolean isLikedInCache = userSet.contains(userId);

      if (isLikedInCache) {
        userSet.remove(userId);
        counter.decrementAndGet();
        videoMapper.deleteVideoLike(videoId, userId);
        log.info("取消点赞：用户{}，视频{}", userId, videoId);
      } else if (videoLikeMapper.selectOne(videoLikeWrapper.getVideoLike(videoId, userId)) != null) {
        counter.decrementAndGet();
        videoMapper.deleteVideoLike(videoId, userId);
        log.info("取消点赞(DB存在)：用户{}，视频{}", userId, videoId);
      } else {
        userSet.add(userId);
        counter.incrementAndGet();
        videoMapper.insertVideoLike(videoLike);
        log.info("添加点赞：用户{}，视频{}", userId, videoId);
      }

      long changeCount = counter.getAndSet(0);
      if (changeCount != 0) {
        videoMapper.addVideoLikeCount(changeCount, videoId);
      }
    } catch (Exception e) {
      log.error("处理延时点赞失败: {}", videoLike, e);
    }
  }
}
