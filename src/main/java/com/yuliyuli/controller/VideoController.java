package com.yuliyuli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.annotation.RateLimit;
import com.yuliyuli.common.Result;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.entity.CurrentUserHolder;
import com.yuliyuli.entity.User;
import com.yuliyuli.entity.VideoCollection;
import com.yuliyuli.entity.VideoDeliveryWithoutFile;
import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.FollowMapper;
import com.yuliyuli.query.CommentWrapper;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.util.TransferUtil;
import com.yuliyuli.vo.HotRecommendVideoVO;
import com.yuliyuli.vo.SearchVideoVO;
import com.yuliyuli.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频模块
 *
 * @author Dima
 * @date 2026-03-02
 */
@RestController
@RequestMapping("/api/video")
@Tag(name = "视频模块")
@Slf4j
public class VideoController {

  @Value(
      "${upload.videopath:C\\\\Users\\\\Administrator\\\\Desktop\\\\yuliyuli_enterprise\\\\yuliyuli-frontend\\\\static\\\\videoUrl}")
  private String VIDEOIR;

  @Value(
      "${upload.coverPath:C\\\\Users\\\\Administrator\\\\Desktop\\\\yuliyuli_enterprise\\\\yuliyuli-frontend\\\\static\\\\coverUrl}")
  private String COVERDIR;

  @Resource private CommentMapper commentMapper;

  @Resource private CommentWrapper commentWrapper;

  @Resource private FollowMapper followMapper;

  @Resource private VideoService videoService;

  @Resource private SearchService searchService;

  @Resource private RedisTemplate<String, Object> redisTemplate;

  @Resource private TransferUtil transferUtil;

  // 检查用户是否登录
  private boolean checkLogin() {
    User user = CurrentUserHolder.getUser();
    if (user == null) {
      return false;
    }
    return true;
  }

  /**
   * 视频投递
   *
   * @param video
   * @return 处理结果
   */
  @PostMapping("/delivery")
  @Operation(summary = "视频投递")
  public Result<Object> deliveryVideo(
      @RequestParam("file") MultipartFile file,
      @RequestParam("video.title") String title,
      @RequestParam("video.type") String type,
      @RequestParam(value = "video.cover", required = false) MultipartFile cover,
      @RequestParam(value = "video.intro", required = false) String intro) {
    if (!checkLogin()) {
      return Result.fail("请完成登录");
    }
    try {
      // 从登录用户获取userId
      User currentUser = CurrentUserHolder.getUser();
      if (currentUser == null) {
        return Result.fail("请完成登录");
      }

      // 保存视频文件，使用视频ID作为文件名
      String videoPath = transferUtil.uploadVideo(file, VIDEOIR);

      // 保存封面文件，使用视频ID作为文件名
      String coverPath = transferUtil.uploadVideoCover(cover, COVERDIR);
      // 传递视频信息到服务层
      VideoDeliveryWithoutFile videoDelivery = new VideoDeliveryWithoutFile();
      videoDelivery.setUserId(currentUser.getUserId());
      videoDelivery.setTitle(title);
      videoDelivery.setIntro(intro);
      videoDelivery.setUrl(videoPath);
      videoDelivery.setCoverUrl(coverPath);
      videoDelivery.setTypeId(Integer.parseInt(type));
      videoDelivery.setAuthorName(currentUser.getUsername());
      videoDelivery.setIsDelete(0);
      videoDelivery.setAuthorAvatar(currentUser.getAvatar());
      String message = videoService.videoDeliver(videoDelivery);
      log.info("视频投递成功,视频标题:{}, 视频路径:{}, 封面路径:{}", title, videoPath, coverPath);
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频投递失败", e);
      return Result.fail("视频上传失败,请稍后重试");
    }
  }

  @RateLimit(limit = 10, window = 60, key = "like")
  @PostMapping("/like")
  @Operation(summary = "视频点赞")
  public Result<Object> likeVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoLike videoLike) {
    checkLogin();
    try {
      String message = videoService.videoLike(videoLike);
      log.info("视频点赞成功,视频ID:{},用户ID:{}", videoLike.getVideoId(), videoLike.getUserId());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频点赞失败", e);
      return Result.fail("视频点赞失败,请稍后重试");
    }
  }

  /**
   * 视频收藏
   *
   * @param videoCollect
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "collect")
  @PostMapping("/collect")
  @Operation(summary = "视频收藏")
  public Result<Object> collectVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoCollection videoCollect) {
    checkLogin();
    try {
      String message = videoService.videoCollect(videoCollect);
      log.info("视频收藏成功,视频ID:{},用户ID:{}", videoCollect.getVideoId(), videoCollect.getUserId());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频收藏失败", e);
      return Result.fail("视频收藏失败,请稍后重试");
    }
  }

  /**
   * 视频评论
   *
   * @param comment
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "comment")
  @PostMapping("/comment")
  @Operation(summary = "视频评论")
  public Result<Object> commentVideo(
      @Parameter(description = "传递的评论对象", required = true) @RequestBody Comment comment) {
    checkLogin();
    try {
      log.info("进入视频评论接口");
      String message = videoService.videoComment(comment);
      log.info("视频评论成功,视频ID:{},用户ID:{}", comment.getVideoId(), comment.getUserId());
      return Result.success(message);
    } catch (Exception e) {
      log.error("视频评论失败", e);
      return Result.fail("视频评论失败,请稍后重试");
    }
  }

  /**
   * 获取视频列表
   *
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @return 视频列表
   */
  @GetMapping("/videoList")
  @Operation(summary = "获取视频列表")
  public Result<Page<VideoVO>> getVideoList(
      @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
      @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
    try {
      Page<VideoVO> page = videoService.getVideoList(pageNum, pageSize);
      return Result.success(page);
    } catch (Exception e) {
      log.error("获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("获取视频列表失败");
    }
  }

  /**
   * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
   *
   * @param title 视频标题
   * @return 视频详情
   */
  @GetMapping("/clickSearch")
  @Operation(summary = "根据视频标题获取相关视频")
  public Result<Page<SearchVideoVO>> getVideoDetail(
      @Parameter(description = "视频标题") @RequestParam String title) {
    try {
      Page<SearchVideoVO> page = videoService.getSearchVideoResults(title);
      return Result.success(page);
    } catch (Exception e) {
      log.error("根据标题搜索视频失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据标题搜索视频失败");
    }
  }

  /**
   * 固定返回15个从100个热门缓存中获取的视频
   *
   * @param videoId 视频ID
   * @return 相关视频，即右边的视频栏
   */
  @GetMapping("/clickVideo")
  @Operation(summary = "根据视频ID获取相关视频")
  public Result<Map<String, Object>> getRelatedVideo(
      @Parameter(description = "视频URL") @RequestParam String videoUrl,
      @Parameter(description = "关注用户ID") @RequestParam Long followUserId,
      @Parameter(description = "粉丝用户ID") @RequestParam Long fanUserId,
      @Parameter(description = "上一页最后一条评论的id") @RequestParam(required = false) Long lastId) {
    // 先对热门视频进行播放计数，再返回相关视频
    if (redisTemplate.opsForValue().get(videoUrl) != null) {
      videoService.hotVideoPlay(videoUrl);
      // 返回的右侧热门推荐视频栏
      List<HotRecommendVideoVO> hotVideoVOList = videoService.getRecommendHotVideo();
      // 分页获取评论列表
      Page<Comment> commentPage = new Page<>(1, 10);
      commentMapper.selectPage(
          commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 10));
      Map<String, Object> response = new HashMap<>();
      response.put("hotVideoVOList", hotVideoVOList);
      // 传递评论列表
      response.put("commentList", commentPage.getRecords());
      response.put("isFollow", followMapper.getFollow(followUserId, fanUserId) != null);
      return Result.success(response);
    } else {
      // 先对视频进行播放计数，再返回相关视频
      videoService.videoPlay(videoUrl);
      // 返回的右侧热门推荐视频栏
      List<HotRecommendVideoVO> hotVideoVOList = videoService.getRecommendHotVideo();
      // 分页获取评论列表
      Page<Comment> commentPage = new Page<>(1, 10);
      commentMapper.selectPage(
          commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 10));
      Map<String, Object> response = new HashMap<>();
      response.put("hotVideoVOList", hotVideoVOList);
      // 传递评论列表
      response.put("commentList", commentPage.getRecords());
      response.put("isFollow", followMapper.getFollow(followUserId, fanUserId) != null);
      return Result.success(response);
    }
  }

  /**
   * 用户点击顶部导航后根据视频类型id获取视频列表
   *
   * @param typeId 视频类型id
   * @return 视频列表
   */
  @GetMapping("/videoTypeList")
  @Operation(summary = "根据视频类型id获取视频列表")
  public Result<Page<VideoVO>> getVideoTypeList(
      @Parameter(description = "视频类型id") @RequestParam int typeId,
      @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
      @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
    try {
      Page<VideoVO> page = videoService.getVideoAccordingTypeId(typeId, pageNum, pageSize);
      return Result.success(page);
    } catch (Exception e) {
      log.error("根据视频类型id获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据视频类型id获取视频列表失败");
    }
  }
}
