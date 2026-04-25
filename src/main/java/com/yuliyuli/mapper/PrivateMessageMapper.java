package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.message.PrivateMessage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PrivateMessageMapper extends BaseMapper<PrivateMessage> {

  @Select(
      "select count(1) from private_message where from_user_id = #{fromUserId} and to_user_id = #{toUserId} and is_read = 0")
  int countUnread(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);
}
