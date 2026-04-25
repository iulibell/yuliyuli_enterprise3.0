package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class FollowRequest {
  @NotBlank(message = "操作类型不能为空")
  @Pattern(regexp = "follow|unfollow", message = "操作类型不合法")
  private String operation;

  @NotNull(message = "被关注用户ID不能为空")
  private Long followUserId;

  @NotNull(message = "用户ID不能为空")
  private Long userId;
}
