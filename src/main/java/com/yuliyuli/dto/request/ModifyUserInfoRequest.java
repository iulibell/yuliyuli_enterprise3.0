package com.yuliyuli.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Data;

@Data
public class ModifyUserInfoRequest {
  @NotNull(message = "性别不能为空")
  @Min(value = 0, message = "性别参数不合法")
  @Max(value = 2, message = "性别参数不合法")
  private Short gender;

  @Past(message = "生日必须早于当前时间")
  private Date birthday;

  @Size(max = 255, message = "签名长度不能超过255个字符")
  private String sign;
}
