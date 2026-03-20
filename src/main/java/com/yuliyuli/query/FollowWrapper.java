package com.yuliyuli.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.Follow;
import org.springframework.stereotype.Component;

@Component
public class FollowWrapper {

  /**
   * 根据用户ID查询关注列表
   *
   * @param userId
   * @return
   */
  public LambdaQueryWrapper<Follow> buildFollowListByUserId(Long userId) {
    return new LambdaQueryWrapper<Follow>().eq(Follow::getFollowUserId, userId);
  }
}
