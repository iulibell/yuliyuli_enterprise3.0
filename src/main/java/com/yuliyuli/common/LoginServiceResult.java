package com.yuliyuli.common;

import com.yuliyuli.dto.vo.LoginVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginServiceResult {
  private final boolean success;
  private final String message;
  private final LoginVO data;

  public static LoginServiceResult success(LoginVO data) {
    return new LoginServiceResult(true, "登录成功", data);
  }

  public static LoginServiceResult fail(String message) {
    return new LoginServiceResult(false, message, null);
  }
}
