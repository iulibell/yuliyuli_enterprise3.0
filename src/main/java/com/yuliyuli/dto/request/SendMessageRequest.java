package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendMessageRequest {
  @NotNull(message = "接收用户ID不能为空")
  private Long toUserId;

  @NotBlank(message = "消息内容不能为空")
  private String content;
}
