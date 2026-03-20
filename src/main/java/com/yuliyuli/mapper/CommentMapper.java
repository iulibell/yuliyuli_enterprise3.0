package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Comment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

public interface CommentMapper extends BaseMapper<Comment> {
  @Insert(
      "INSERT INTO comment (`video_id`, `avatar`, `username`, `user_id`, `content`, `parent_id`, `is_deleted`, `comment_id`)"
          + "VALUES (#{videoId}, #{avatar}, #{username}, #{userId}, #{content}, #{parentId}, #{isDeleted}, #{commentId})")
  int insertComment(Comment comment);

  @Delete("DELETE FROM comment WHERE video_id = #{videoId}")
  int deleteComment(String videoId);
}
