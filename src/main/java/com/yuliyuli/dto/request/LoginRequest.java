package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
  @NotBlank(message = "手机号不能为空")
  @Pattern(regexp = "^\\d{11}$", message = "请输入有效的11位手机号")
  private String phone;

  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 12, message = "密码长度必须大于等于6位且小于等于12位")
  private String password;
}
