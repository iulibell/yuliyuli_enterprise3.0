package com.yuliyuli.exception;

import com.yuliyuli.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /** 处理参数校验异常(@Validated失败) */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    log.error("参数校验异常: {}", e.getMessage());
    return Result.fail(e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
  }

  /** 处理简单参数校验异常（如@RequestParam上的校验） */
  @ExceptionHandler(ConstraintViolationException.class)
  public Result<?> handleConstraintViolationException(ConstraintViolationException e) {
    log.error("参数校验异常: {}", e.getMessage());
    String message =
        e.getConstraintViolations().isEmpty()
            ? "参数校验失败"
            : e.getConstraintViolations().iterator().next().getMessage();
    return Result.fail(message);
  }

  /** 处理业务异常 */
  @ExceptionHandler(BusinessException.class)
  public Result<?> handleBusinessException(BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    return Result.fail(e.getMessage());
  }

  /** 处理非法参数（如资源不存在等明确提示） */
  @ExceptionHandler(IllegalArgumentException.class)
  public Result<?> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("非法参数: {}", e.getMessage());
    return Result.fail(e.getMessage());
  }

  /** 处理空指针异常 */
  @ExceptionHandler(NullPointerException.class)
  public Result<?> handleNullPointerException(NullPointerException e) {
    log.error("空指针异常: {}", e.getMessage());
    return Result.fail(e.getMessage());
  }

  /** 处理所有未捕获的通用异常 */
  @ExceptionHandler(Exception.class)
  public Result<?> handleGlobalException(Exception e) {
    String errorMsg = "系统繁忙，请稍后再试";
    log.error("全局通用异常", e);
    return Result.fail(errorMsg);
  }

  /** 自定义业务异常类 */
  public static class BusinessException extends RuntimeException {
    public BusinessException(String message) {
      super(message);
    }

    public BusinessException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
