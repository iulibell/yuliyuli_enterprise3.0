package com.yuliyuli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import lombok.Data;

@Data
@TableName("comment")
public class Comment {
  @TableId(type = IdType.AUTO)
  @Parameter(name = "评论id")
  private Long id;

  @TableField("video_id")
  @Parameter(name = "评论所属视频id")
  private String videoId;

  @TableField("avatar")
  @Parameter(name = "评论用户头像")
  private String avatar;

  @TableField("username")
  @Parameter(name = "评论用户昵称")
  private String username;

  @TableField("user_id")
  @Parameter(name = "评论用户id")
  private Long userId;

  @TableField("content")
  @NotBlank(message = "评论内容不能为空")
  @Parameter(name = "评论内容")
  private String content;

  @TableField("parent_id")
  @Parameter(name = "父评论id")
  private Long parentId;

  @TableField("create_time")
  @Parameter(name = "评论时间")
  private Date createTime;

  @TableField("is_deleted")
  @Parameter(name = "是否被删除,0-未删除,1-已删除")
  private short isDeleted;

  @TableField("comment_id")
  @Parameter(name = "评论id")
  private Long commentId;
}
