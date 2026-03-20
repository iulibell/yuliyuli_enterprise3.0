package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.User;

public interface UserMapper extends BaseMapper<User> {
    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param avatar 头像URL
     * @return 更新结果，成功返回1，失败返回0或负数
     */
    @Update("update user set avatar = #{avatar} where user_id = #{userId}")
    int updateAvatar(Long userId, String avatar);
}
