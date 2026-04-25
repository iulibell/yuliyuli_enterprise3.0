package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.video.CommentLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentLikeMapper extends BaseMapper<CommentLike> {

  @Select("select * from comment_like where comment_id = #{commentId} and user_id = #{userId} limit 1")
  CommentLike getLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

  @Insert("insert into comment_like (comment_id, user_id, create_time) values (#{commentId}, #{userId}, now())")
  int insertLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

  @Delete("delete from comment_like where comment_id = #{commentId} and user_id = #{userId}")
  int deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

  @Select("select count(1) from comment_like where comment_id = #{commentId}")
  int countLikeByCommentId(@Param("commentId") Long commentId);
}
