package com.yuliyuli.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解 用于在Controller方法上添加限流功能，限制单位时间内的请求次数
 *
 * @author yuliyuli
 * @date 2024-03-20
 * @param limit 限制次数
 * @param window 时间窗口(秒)
 * @param key 限流键
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
  int limit() default 10; // 限制次数

  int window() default 60; // 时间窗口(秒)

  String key() default ""; // 限流键
}
