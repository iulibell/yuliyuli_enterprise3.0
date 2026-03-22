package com.yuliyuli.dto.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.entity.user.ExistingPhone;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.entity.user.UserInfo;

import org.springframework.stereotype.Component;

/** 用户相关查询/更新条件构造器 统一封装User、UserInfo的MyBatis-Plus条件，避免业务层重复写Wrapper逻辑 */
@Component
public class UserWrapper {

  /**
   * 根据用户名构造查询条件（用于登录/校验用户名是否存在）
   *
   * @param userName 用户名
   * @return LambdaQueryWrapper<User> 查询条件
   */
  public LambdaQueryWrapper<User> buildUserByUsername(String userName) {
    return new LambdaQueryWrapper<User>().eq(User::getUsername, userName).last("LIMIT 1");
  }

  /**
   * 根据账号构造查询条件（账号可等价于用户名/手机号）
   *
   * @param account 账号（用户名/手机号）
   * @return LambdaQueryWrapper<User> 查询条件
   */
  public LambdaQueryWrapper<User> buildUserByAccount(String account) {
    return new LambdaQueryWrapper<User>().eq(User::getPhone, account).last("LIMIT 1");
  }

  /**
   * 根据手机号构造查询条件（校验手机号是否存在）
   *
   * @param phone 手机号
   * @return LambdaQueryWrapper<ExistingPhone> 查询条件
   */
  public LambdaQueryWrapper<ExistingPhone> buildUserByPhone(String phone) {
    return new LambdaQueryWrapper<ExistingPhone>()
        .eq(ExistingPhone::getPhone, phone)
        .select(ExistingPhone::getUsername)
        .last("LIMIT 1");
  }

  /**
   * 根据用户ID查询用户详情（UserInfo）
   *
   * @param userId 用户ID
   * @return LambdaQueryWrapper<UserInfo> 查询条件
   */
  public LambdaQueryWrapper<UserInfo> buildUserInfoByUserId(Long userId) {
    return new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getUserId, userId).last("LIMIT 1");
  }

  public LambdaQueryWrapper<User> buildHotUserList() {
    return new LambdaQueryWrapper<User>().lt(User::getFollowCount, 1000000);
  }
}
