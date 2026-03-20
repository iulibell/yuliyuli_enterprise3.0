package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.UserInfo;
import org.apache.ibatis.annotations.Update;

public interface UserInfoMapper extends BaseMapper<UserInfo> {
  @Update(
      "update user_info set gender = #{gender}, birthday = #{birthday}, sign = #{sign} where user_id = #{userId}")
  int updateUserInfo(Long userId, short gender, String birthday, String sign);
}
