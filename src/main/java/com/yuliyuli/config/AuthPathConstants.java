package com.yuliyuli.config;

public final class AuthPathConstants {

  private AuthPathConstants() {}

  public static final String[] PUBLIC_PATHS = {
    "/api/user/login",
    "/api/user/register",
    "/api/user/logout",
    "/api/user/getCode",
    "/api/video/list",
    "/api/video/videoList",
    "/api/comment/list",
    "/api/video/videoTypeList",
    "/api/video/clickVideo/**",
    "/api/info/authorPage/**",
    "/api/search/video",
    "/api/search/topTenVideo",
    "/doc.html",
    "/webjars/**",
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/static/**",
    "/favicon.ico"
  };
}
