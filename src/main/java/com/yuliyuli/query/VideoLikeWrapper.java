package com.yuliyuli.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.VideoLike;
import org.springframework.stereotype.Component;

@Component
public class VideoLikeWrapper {

  /**
   * 获取视频点赞信息
   *
   * @param videoId 视频id
   * @param userId 用户id
   * @return LambdaQueryWrapper<VideoLike>获取视频点赞信息
   */
  public LambdaQueryWrapper<VideoLike> getVideoLike(String videoId, Long userId) {
    return new LambdaQueryWrapper<VideoLike>()
        .eq(VideoLike::getVideoId, videoId)
        .eq(VideoLike::getUserId, userId);
  }
}
