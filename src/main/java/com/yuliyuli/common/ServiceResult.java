package com.yuliyuli.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServiceResult {
  private final boolean success;
  private final String message;

  public static ServiceResult success(String message) {
    return new ServiceResult(true, message);
  }

  public static ServiceResult fail(String message) {
    return new ServiceResult(false, message);
  }
}
