package com.yuliyuli.service;

import com.yuliyuli.vo.VideoVO;
import java.util.List;
import java.util.Map;

public interface InfoService {
  /**
   * 获取作者页面信息,传视频的信息，包括制作的视频
   *
   * @param userId 作者ID
   * @return 作者页面所有视频
   */
  public List<VideoVO> getAuthorPageVideo(Long userId);

  /**
   * 获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息，包括用户名、头像、关注数、粉丝数等
   */
  public Map<String, Object> getUserInfo(Long userId);

  /**
   * 根据作者名字获取用户信息
   *
   * @param authorName 作者名字
   * @return 用户信息，包括用户ID、用户名、头像、关注数、粉丝数等
   */
  public Map<String, Object> getUserInfoByAuthorName(String authorName);

  /**
   * 删除视频
   *
   * @param videoIUrl 视频ID
   * @param coverUrl 封面URL
   * @return 删除结果
   */
  public String videoDelete(String videoIUrl, Long userId);

  /**
   * 用户关注
   *
   * @param followUserId 关注用户ID
   * @param fanUserId 粉丝用户ID
   */
  public String userFollow(Long followUserId, Long fanUserId);

  /**
   * 用户取消关注
   *
   * @param followUserId 关注用户ID
   * @param fanUserId 粉丝用户ID
   */
  public String userUnfollow(Long followUserId, Long fanUserId);
}
