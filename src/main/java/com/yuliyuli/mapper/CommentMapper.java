package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.dto.vo.ReplyNoticeVO;
import com.yuliyuli.entity.video.Comment;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CommentMapper extends BaseMapper<Comment> {
  @Insert(
      "INSERT INTO comment (`video_id`, `avatar`, `username`, `user_id`, `content`, `parent_id`, `is_deleted`, `comment_id`)"
          + "VALUES (#{videoId}, #{avatar}, #{username}, #{userId}, #{content}, #{parentId}, #{isDeleted}, #{commentId})")
  int insertComment(Comment comment);

  @Delete("DELETE FROM comment WHERE video_id = #{videoId}")
  int deleteComment(String videoId);

  @Select(
      "select c.id as replyCommentId, c.video_id as videoId, c.content as replyContent, p.content as parentContent,"
          + " c.create_time as createTime, c.username as replyUsername, c.avatar as replyAvatar"
          + " from comment c join comment p on c.parent_id = p.id"
          + " where p.user_id = #{userId} and c.user_id <> #{userId} and c.is_deleted = 0 and p.is_deleted = 0"
          + " order by c.create_time desc limit #{limit}")
  List<ReplyNoticeVO> listReplyNotices(@Param("userId") Long userId, @Param("limit") Integer limit);
}
