package com.yuliyuli.aspect;

import com.yuliyuli.annotation.OperationLog;
import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.entity.user.User;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面
 * 用于记录用户操作日志
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {

  /** 定义切点：所有带有 @OperationLog 注解的方法 */
  @Pointcut("@annotation(com.yuliyuli.annotation.OperationLog)")
  public void operationLogPointcut() {}

  /**
   * 环绕通知：记录操作日志
   */
  @Around("operationLogPointcut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    // 获取方法签名
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    OperationLog operationLog = signature.getMethod().getAnnotation(OperationLog.class);

    // 获取当前用户
    User currentUser = CurrentUserHolder.getUser();
    Long userId = currentUser != null ? currentUser.getUserId() : null;
    String username = currentUser != null ? currentUser.getUsername() : "anonymous";

    // 获取请求信息
    HttpServletRequest request = getRequest();
    String ip = request != null ? getClientIp(request) : "unknown";
    String uri = request != null ? request.getRequestURI() : "unknown";
    String method = request != null ? request.getMethod() : "unknown";

    // 记录操作开始时间
    LocalDateTime startTime = LocalDateTime.now();
    long startMillis = System.currentTimeMillis();

    // 记录请求参数
    Object[] args = joinPoint.getArgs();
    String params = operationLog.recordParams() ? Arrays.toString(args) : "[参数已隐藏]";

    // 操作描述
    String operationDesc = operationLog.value();
    String operationType = operationLog.type();

    log.info(
        "[操作日志] 开始 - 用户:{}({}), 操作:{}, 类型:{}, URI:{}, Method:{}, IP:{}, 参数:{}, 时间:{}",
        username,
        userId,
        operationDesc,
        operationType,
        uri,
        method,
        ip,
        params,
        startTime);

    Object result = null;
    boolean success = true;
    String errorMsg = null;

    try {
      // 执行目标方法
      result = joinPoint.proceed();
      return result;
    } catch (Exception e) {
      success = false;
      errorMsg = e.getMessage();
      throw e;
    } finally {
      // 记录操作结束
      long costTime = System.currentTimeMillis() - startMillis;
      LocalDateTime endTime = LocalDateTime.now();

      String resultStr =
          operationLog.recordResult() ? String.valueOf(result) : "[返回值已隐藏]";

      if (success) {
        log.info(
            "[操作日志] 成功 - 用户:{}({}), 操作:{}, 耗时:{}ms, 时间:{}, 结果:{}",
            username,
            userId,
            operationDesc,
            costTime,
            endTime,
            resultStr);
      } else {
        log.error(
            "[操作日志] 失败 - 用户:{}({}), 操作:{}, 耗时:{}ms, 时间:{}, 错误:{}",
            username,
            userId,
            operationDesc,
            costTime,
            endTime,
            errorMsg);
      }
    }
  }

  /**
   * 获取当前请求
   */
  private HttpServletRequest getRequest() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attributes != null ? attributes.getRequest() : null;
  }

  /**
   * 获取客户端IP地址
   */
  private String getClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    // 如果是多级代理，取第一个IP
    if (ip != null && ip.contains(",")) {
      ip = ip.split(",")[0].trim();
    }
    return ip;
  }
}
