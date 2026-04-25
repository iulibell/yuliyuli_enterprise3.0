package com.yuliyuli.controller;

import com.yuliyuli.annotation.OperationLog;
import com.yuliyuli.annotation.RateLimit;
import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.common.LoginServiceResult;
import com.yuliyuli.common.Result;
import com.yuliyuli.common.ServiceResult;
import com.yuliyuli.dto.request.GetCodeRequest;
import com.yuliyuli.dto.request.LoginRequest;
import com.yuliyuli.dto.request.ModifyUserInfoRequest;
import com.yuliyuli.dto.request.RegisterRequest;
import com.yuliyuli.dto.vo.LoginVO;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.UserService;
import com.yuliyuli.util.TransferUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 用户控制器 提供用户登录、校验、注册等接口，校验已在业务层实现 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户模块")
@Slf4j
@Validated
public class UserController {

  @Value(
      "${upload.avatarPath:C\\\\\\\\Users\\\\\\\\Administrator\\\\\\\\Desktop\\\\\\\\yuliyuli_enterprise\\\\\\\\yuliyuli-frontend\\\\\\\\static\\\\\\\\avatarUrl}")
  private String avatarPath;

  @Resource private UserService userService;

  @Resource private TransferUtil transferUtil;

  /**
   * 用户登录接口
   *
   * @return 登录结果（Token+用户信息）
   */
  @OperationLog(value = "用户登录", type = "LOGIN")
  @RateLimit(key = "login", limit = 10, window = 60)
  @Operation(summary = "用户登录")
  @PostMapping("/login")
  public Result<LoginVO> login(
      @Parameter(description = "登录参数（账号+密码）", required = true) @Validated @RequestBody
          LoginRequest loginDto) {
    log.info("【用户登录】手机号：{}", loginDto.getPhone());
    try {
      LoginServiceResult result = userService.login(loginDto.getPhone(), loginDto.getPassword());
      if (!result.isSuccess()) {
        log.info("【用户登录】失败: {}", result.getMessage());
        return Result.fail(result.getMessage());
      }
      return Result.success(result.getData());
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("登录异常!", e);
      return Result.fail("登录失败,请稍后重试");
    }
  }

  /**
   * 校验模块
   *
   * @return 校验结果（验证码）
   */
  @RateLimit(key = "getCode", limit = 5, window = 60)
  @Operation(summary = "校验模块")
  @PostMapping("/getCode")
  public Result<String> getCode(
      @Parameter(description = "校验参数（手机号）", required = true) @Validated @RequestBody
          GetCodeRequest request) {
    String phone = request.getPhone();
    log.info("手机号：{}", phone);
    try {
      ServiceResult result = userService.getCode(phone);
      if (result.isSuccess()) {
        log.info("验证码发送成功");
      }
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("获取验证码异常!", e);
      return Result.fail("获取验证码失败,请稍后重试");
    }
  }

  /**
   * 注册模块,已获取验证码的情况下,用户注册
   *
   * @param registerDto 注册参数（账号+验证码+密码）
   * @param code 校验参数（验证码）
   * @return 注册结果（用户信息）
   */
  @OperationLog(value = "用户注册", type = "REGISTER")
  @RateLimit(key = "register", limit = 5, window = 60)
  @Operation(summary = "注册模块")
  @PostMapping("/register")
  public Result<String> register(
      @Parameter(description = "注册参数（账号+密码）", required = true) @Validated @RequestBody
          RegisterRequest registerDto,
      @Parameter(description = "校验参数（验证码）", required = true) @RequestParam
          @NotBlank(message = "验证码不能为空")
          @Pattern(regexp = "^\\d{6}$", message = "验证码需为6位数字")
          String code) {
    try {
      ServiceResult result =
          userService.register(registerDto.getPhone(), code, registerDto.getPassword());
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("【注册模块】注册异常!", e);
      return Result.fail("注册失败,请稍后重试");
    }
  }

  /**
   * 修改模块,用户修改个人信息
   *
   * @param userInfoDto 修改参数（性别+生日+签名）
   * @return 修改结果（用户信息）
   */
  @Operation(summary = "修改模块")
  @PostMapping("/modifyInfo")
  public Result<String> modifyInfo(
      @Parameter(description = "修改参数（性别+生日+签名）", required = true) @Validated @RequestBody
          ModifyUserInfoRequest userInfoDto) {
    // 修改用户信息
    try {
      ServiceResult result =
          userService.modifyInfo(
              userInfoDto.getGender(), userInfoDto.getBirthday(), userInfoDto.getSign());
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("【修改模块】修改异常!", e);
      return Result.fail("修改失败,请稍后重试");
    }
  }

  /**
   * 修改模块,用户修改头像
   *
   * @return 修改结果（用户信息）
   */
  @Operation(summary = "修改模块")
  @PostMapping("/modifyAvatar")
  public Result<String> modifyAvatar(@RequestParam MultipartFile avatar) {
    try {
      User currentUser = CurrentUserHolder.getUser();
      if (currentUser == null) {
        return Result.fail("请先完成登录!");
      }
      Long userId = currentUser.getUserId();
      // 上传头像,返回头像URL
      String avatarUrl = transferUtil.uploadAvatar(avatar, avatarPath);
      ServiceResult result = userService.modifyAvatar(avatarUrl, userId);
      if (result.isSuccess()) {
        return Result.success(avatarUrl);
      }
      return Result.fail(result.getMessage());
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("修改头像失败", e);
      return Result.fail("请重试打开该页面!");
    }
  }

  private Result<String> toResult(ServiceResult result) {
    return result.isSuccess() ? Result.success(result.getMessage()) : Result.fail(result.getMessage());
  }
}
