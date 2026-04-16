package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VideoCollectionRequest {
  @NotBlank(message = "视频ID不能为空")
  private String videoId;

  @NotNull(message = "用户ID不能为空")
  private Long userId;
}
