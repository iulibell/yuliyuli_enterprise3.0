package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoDeleteRequest {
  @NotBlank(message = "视频地址不能为空")
  private String videoUrl;

  @NotNull(message = "用户ID不能为空")
  private Long userId;
}
