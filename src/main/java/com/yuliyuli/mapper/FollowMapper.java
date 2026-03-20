package com.yuliyuli.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Follow;

public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 根据关注用户ID和粉丝用户ID查询关注关系
     * @param followUserId
     * @param fanUserId
     * @return
     */
    @Select("select * from follow where follow_user_id = #{followUserId} and fan_user_id = #{fanUserId}")
    Follow getFollow(Long followUserId, Long fanUserId);

    /**
     * 插入关注关系
     * @param followUserId
     * @param fanUserId
     */
    @Insert("insert into follow (follow_user_id, fan_user_id) values (#{followUserId}, #{fanUserId})")
    void insertFollow(Long followUserId, Long fanUserId);

    /**
     * 更新关注用户的关注数
     * @param followUserId
     * @param followCount
     */
    @Update("UPDATE user SET fans_count = fans_count + 1 WHERE user_id = #{followUserId}")
    int updateFansCount(Long followUserId);

    /**
     * 取消关注扣除粉丝数
     * @param followUserId
     * @return
     */
    @Update("UPDATE user SET fans_count = fans_count - 1 WHERE user_id = #{followUserId}")
    int decrementFansCount(Long followUserId);

    /**
     * 根据关注用户ID和粉丝用户ID删除关注关系
     * @param followUserId
     * @param fanUserId
     */
    @Delete("delete from follow where follow_user_id = #{followUserId} and fan_user_id = #{fanUserId}")
    void deleteFollow(Long followUserId, Long fanUserId);
}
