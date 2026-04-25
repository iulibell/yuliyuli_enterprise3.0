package com.yuliyuli.config;

import com.yuliyuli.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final LoginInterceptor loginInterceptor;

  public WebConfig(LoginInterceptor loginInterceptor) {
    this.loginInterceptor = loginInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(loginInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(AuthPathConstants.PUBLIC_PATHS);
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // 配置静态资源访问
    registry
        .addResourceHandler("/static/**")
        .addResourceLocations("file:C:/Users/Administrator/Desktop/yuliyuli_enterprise/static/");
  }
}
