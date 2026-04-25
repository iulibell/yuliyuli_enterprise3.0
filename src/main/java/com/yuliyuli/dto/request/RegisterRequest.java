package com.yuliyuli.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
  @NotBlank(message = "手机号不能为空")
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的11位手机号")
  private String phone;

  @NotBlank(message = "密码不能为空")
  @Pattern(regexp = "^\\S{8,12}$", message = "密码需为8-12位")
  private String password;
}
