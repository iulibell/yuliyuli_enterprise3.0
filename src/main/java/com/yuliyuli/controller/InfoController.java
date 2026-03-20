package com.yuliyuli.controller;

import com.yuliyuli.common.Result;
import com.yuliyuli.service.InfoService;
import com.yuliyuli.vo.VideoVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    try {
      return Result.success(infoService.getAuthorPageVideo(userId));
    } catch (Exception e) {
      log.error("获取作者页面视频失败", e);
      return Result.fail("请重试打开该页面!");
    }
  }

  /**
   * 获取用户信息
   *
   * @param userId 用户ID
   * @return 用户信息
   */
  @GetMapping("/userInfo/{userId}")
  public Result<Object> getUserInfo(@PathVariable Long userId) {
    if (userId == null) {
      return Result.fail("该用户不存在!");
    }
    try {
      return Result.success(infoService.getUserInfo(userId));
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
      return Result.fail("请重试打开该页面!");
    }
  }

  /**
   * 根据作者名字获取用户信息
   *
   * @param authorName 作者名字
   * @return 用户信息
   */
  @GetMapping("/userInfoByName/{authorName}")
  public Result<Object> getUserInfoByName(@PathVariable String authorName) {
    if (authorName == null || authorName.isEmpty()) {
      return Result.fail("该作者不存在!");
    }
    try {
      return Result.success(infoService.getUserInfoByAuthorName(authorName));
    } catch (Exception e) {
      log.error("获取用户信息失败", e);
      return Result.fail("请重试打开该页面!");
    }
  }

  @PostMapping("/videoDelete")
  public Result<Object> videoDelete(@RequestBody Map<String, Object> params) {
    try {
      String videoUrl = (String) params.get("videoUrl");
      Long userId = Long.valueOf(params.get("userId").toString());
      String result = infoService.videoDelete(videoUrl, userId);
      if (result.equals("删除视频成功")) {
        return Result.success("删除成功");
      }
      return Result.fail(result);
    } catch (Exception e) {
      log.error("删除视频失败", e);
      return Result.fail("请重试删除该视频!");
    }
  }

  @PostMapping("/follow")
  public Result<String> userfollow(@RequestBody Map<String, Object> params) {
    try {
      String operation = (String) params.get("operation");
      Long followUserId = (Long) params.get("followUserId");
      Long userId = (Long) params.get("userId");

      if ("unfollow".equals(operation)) {
        String result = infoService.userUnfollow(followUserId, userId);
        if ("取消关注成功".equals(result)) {
          return Result.success("取消关注成功!");
        }
        return Result.fail(result);
      } else {
        String result = infoService.userFollow(followUserId, userId);
        if ("关注成功".equals(result)) {
          return Result.success("关注成功!");
        }
        return Result.fail(result);
      }
    } catch (Exception e) {
      log.error("关注用户失败", e);
      return Result.fail("请重试关注该用户!");
    }
  }
}
