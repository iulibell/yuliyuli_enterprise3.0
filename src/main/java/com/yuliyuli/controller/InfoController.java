package com.yuliyuli.controller;

import com.yuliyuli.common.Result;
import com.yuliyuli.common.ServiceResult;
import com.yuliyuli.dto.request.FollowRequest;
import com.yuliyuli.dto.request.VideoDeleteRequest;
import com.yuliyuli.dto.vo.UserProfileVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.InfoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 信息返回控制器 提供获取作者、视频公开信息的接口 */
@RestController
@RequestMapping("/api/info")
@Tag(name = "信息接口")
@Slf4j
public class InfoController {

  @Resource private InfoService infoService;

  /**
   * 获取作者页面信息,传视制作的视频
   *
   * @param userId 作者ID
   * @return 作者页面信息
   */
  @GetMapping("/authorPage/{userId}")
  public Result<List<VideoVO>> getAuthorPageInfo(@PathVariable Long userId) {
    if (userId == null) {
      return Result.fail("该作者不存在!");
    }
    return Result.success(infoService.getAuthorPageVideo(userId));
  }

  /**
   * 获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  @GetMapping("/userInfo/{userId}")
  public Result<UserProfileVO> getUserInfo(@PathVariable Long userId) {
    if (userId == null) {
      return Result.fail("该用户不存在!");
    }
    return Result.success(infoService.getUserInfo(userId));
  }

  /**
   * 根据作者名字获取用户信息
   *
   * @param authorName 作者名字
   * @return 用户信息
   */
  @GetMapping("/userInfoByName/{authorName}")
  public Result<UserProfileVO> getUserInfoByName(@PathVariable String authorName) {
    if (authorName == null || authorName.isEmpty()) {
      return Result.fail("该作者不存在!");
    }
    return Result.success(infoService.getUserInfoByAuthorName(authorName));
  }

  @PostMapping("/videoDelete")
  public Result<String> videoDelete(@Validated @RequestBody VideoDeleteRequest request) {
    try {
      ServiceResult result = infoService.videoDelete(request.getVideoUrl(), request.getUserId());
      return toResult(result, "删除成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("删除视频失败", e);
      return Result.fail("请重试删除该视频!");
    }
  }

  @PostMapping("/follow")
  public Result<String> userfollow(@Validated @RequestBody FollowRequest request) {
    try {
      String operation = request.getOperation();
      if ("unfollow".equals(operation)) {
        ServiceResult result = infoService.userUnfollow(request.getFollowUserId(), request.getUserId());
        return toResult(result, "取消关注成功!");
      }
      ServiceResult result = infoService.userFollow(request.getFollowUserId(), request.getUserId());
      return toResult(result, "关注成功!");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("关注用户失败", e);
      return Result.fail("请重试关注该用户!");
    }
  }

  @GetMapping("/followStatus")
  public Result<Boolean> followStatus(
      @RequestParam Long followUserId, @RequestParam Long fanUserId) {
    try {
      if (followUserId == null || fanUserId == null) {
        return Result.success(false);
      }
      return Result.success(infoService.isFollowed(followUserId, fanUserId));
    } catch (Exception e) {
      log.error("获取关注状态失败, followUserId:{}, fanUserId:{}", followUserId, fanUserId, e);
      return Result.success(false);
    }
  }

  private Result<String> toResult(ServiceResult result, String successMessage) {
    return result.isSuccess() ? Result.success(successMessage) : Result.fail(result.getMessage());
  }
}
