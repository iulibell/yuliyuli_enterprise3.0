package com.yuliyuli.service;

import com.yuliyuli.common.LoginServiceResult;
import com.yuliyuli.common.ServiceResult;
import java.util.Date;

// 用户服务接口
public interface UserService {
  /**
   * 登录
   *
   * @param account 账号
   * @param password 密码
   * @return 用户信息
   */
  LoginServiceResult login(String account, String password);

  /**
   * 注册
   *
   * @param phone 手机号
   * @return 用户信息
   */
  ServiceResult getCode(String phone);

  /**
   * 注册
   *
   * @param phone 手机号
   * @param code 验证码
   * @return 用户信息
   */
  ServiceResult register(String phone, String code, String password);

  /**
   * 修改信息
   *
   * @param gender 性别
   * @param birthday 生日
   * @param sign 签名
   * @return 用户信息
   */
  ServiceResult modifyInfo(short gender, Date birthday, String sign);

  /**
   * 修改用户头像
   *
   * @param avatar 头像文件
   * @param userId 用户ID
   * @return 修改结果
   */
  ServiceResult modifyAvatar(String avatarUrl, Long userId);
}
