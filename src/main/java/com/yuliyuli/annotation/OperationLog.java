package com.yuliyuli.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

  /**
   * 操作描述
   */
  String value() default "";

  /**
   * 操作类型
   */
  String type() default "";

  /**
   * 是否记录请求参数
   */
  boolean recordParams() default true;

  /**
   * 是否记录返回值
   */
  boolean recordResult() default false;
}
