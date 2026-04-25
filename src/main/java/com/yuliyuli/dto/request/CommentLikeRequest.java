package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentLikeRequest {
  @NotNull(message = "评论ID不能为空")
  private Long commentId;

  @NotNull(message = "用户ID不能为空")
  private Long userId;
}
