package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
  @NotBlank(message = "视频ID不能为空")
  private String videoId;
  private String avatar;
  private String username;

  @NotNull(message = "用户ID不能为空")
  private Long userId;

  @NotBlank(message = "评论内容不能为空")
  private String content;

  private Long parentId;
  private Long commentId;
}
