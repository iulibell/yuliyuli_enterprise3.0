package com.yuliyuli.annotation;

import com.yuliyuli.enums.DesensitizeType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Desensitize {
  DesensitizeType value() default DesensitizeType.DEFAULT;

  int prefixKeep() default 0;

  int suffixKeep() default 0;
}
