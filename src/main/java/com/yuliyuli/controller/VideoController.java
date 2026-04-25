package com.yuliyuli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuliyuli.annotation.OperationLog;
import com.yuliyuli.annotation.RateLimit;
import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.common.Result;
import com.yuliyuli.common.ServiceResult;
import com.yuliyuli.dto.request.CommentLikeRequest;
import com.yuliyuli.dto.request.CommentRequest;
import com.yuliyuli.dto.request.VideoCollectionRequest;
import com.yuliyuli.dto.request.VideoLikeRequest;
import com.yuliyuli.dto.query.CommentWrapper;
import com.yuliyuli.dto.vo.CommentLikeResultVO;
import com.yuliyuli.dto.vo.HotRecommendVideoVO;
import com.yuliyuli.dto.vo.SearchVideoVO;
import com.yuliyuli.dto.vo.VideoDetailPageVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.delivery.VideoDeliveryWithoutFile;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.entity.video.Comment;
import com.yuliyuli.entity.video.VideoCollection;
import com.yuliyuli.entity.video.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.CommentLikeMapper;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.FollowMapper;
import com.yuliyuli.mapper.UserMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.util.TransferUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
 * &#064;date  2026-03-02
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

  @Resource private CommentLikeMapper commentLikeMapper;

  @Resource private CommentWrapper commentWrapper;

  @Resource private FollowMapper followMapper;

  @Resource private UserMapper userMapper;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoService videoService;

  @Resource private SearchService searchService;

  @Resource private RedisTemplate<String, Object> redisTemplate;

  @Resource private TransferUtil transferUtil;

  // 检查用户是否登录
  private boolean checkLogin() {
    return CurrentUserHolder.getUser() == null;
  }

  /**
   * 视频投递
   *
   * @return 处理结果
   */
  @OperationLog(value = "视频投递", type = "VIDEO_UPLOAD")
  @PostMapping("/delivery")
  @Operation(summary = "视频投递")
  public Result<String> deliveryVideo(
      @RequestParam("file") MultipartFile file,
      @RequestParam("video.title") String title,
      @RequestParam("video.type") String type,
      @RequestParam(value = "video.cover", required = false) MultipartFile cover,
      @RequestParam(value = "video.intro", required = false) String intro) {
    if (checkLogin()) {
      return Result.fail("请完成登录");
    }
    try {
      // 从登录用户获取userId
      User currentUser = CurrentUserHolder.getUser();

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
      ServiceResult result = videoService.videoDeliver(videoDelivery);
      log.info("视频投递请求处理完成,结果:{},视频标题:{}, 视频路径:{}, 封面路径:{}", result.getMessage(), title, videoPath, coverPath);
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频投递失败", e);
      return Result.fail("视频上传失败,请稍后重试");
    }
  }

  @OperationLog(value = "视频点赞", type = "VIDEO_LIKE")
  @RateLimit(limit = 10, window = 60, key = "like")
  @PostMapping("/like")
  @Operation(summary = "视频点赞")
  public Result<String> likeVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoLikeRequest request) {
    if(checkLogin()){
      return Result.fail("请完成登录");
    }
    try {
      VideoLike videoLike = new VideoLike();
      videoLike.setVideoId(request.getVideoId());
      videoLike.setUserId(request.getUserId());
      ServiceResult result = videoService.videoLike(videoLike);
      log.info("视频点赞请求处理完成,结果:{},视频ID:{},用户ID:{}", result.getMessage(), videoLike.getVideoId(), videoLike.getUserId());
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频点赞失败", e);
      return Result.fail("视频点赞失败,请稍后重试");
    }
  }

  /**
   * 视频收藏
   *
   * @return 处理结果
   */
  @OperationLog(value = "视频收藏", type = "VIDEO_COLLECT")
  @RateLimit(limit = 10, window = 60, key = "collect")
  @PostMapping("/collect")
  @Operation(summary = "视频收藏")
  public Result<String> collectVideo(
      @Parameter(description = "传递的视频对象", required = true) @Validated @RequestBody
          VideoCollectionRequest request) {
    if(checkLogin()){
      return Result.fail("请完成登录");
    }
    try {
      VideoCollection videoCollect = new VideoCollection();
      videoCollect.setVideoId(request.getVideoId());
      videoCollect.setUserId(request.getUserId());
      ServiceResult result = videoService.videoCollect(videoCollect);
      log.info("视频收藏请求已受理,结果:{},视频ID:{},用户ID:{}", result.getMessage(), videoCollect.getVideoId(), videoCollect.getUserId());
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频收藏失败", e);
      return Result.fail("视频收藏失败,请稍后重试");
    }
  }

  /**
   * 视频评论
   *
   * @return 处理结果
   */
  @RateLimit(limit = 10, window = 60, key = "comment")
  @PostMapping("/comment")
  @Operation(summary = "视频评论")
  public Result<String> commentVideo(
      @Parameter(description = "传递的评论对象", required = true) @Validated @RequestBody
          CommentRequest request) {
    if(checkLogin()){
      return Result.fail("请完成登录");
    }
    try {
      Comment comment = new Comment();
      comment.setVideoId(request.getVideoId());
      comment.setAvatar(request.getAvatar());
      comment.setUsername(request.getUsername());
      comment.setUserId(request.getUserId());
      comment.setContent(request.getContent());
      comment.setParentId(request.getParentId());
      comment.setCommentId(request.getCommentId());
      log.info("进入视频评论接口");
      ServiceResult result = videoService.videoComment(comment);
      log.info("视频评论请求已受理,结果:{},视频ID:{},用户ID:{}", result.getMessage(), comment.getVideoId(), comment.getUserId());
      return toResult(result);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频评论失败", e);
      return Result.fail("视频评论失败,请稍后重试");
    }
  }

  @RateLimit(limit = 20, window = 60, key = "comment-like")
  @PostMapping("/comment/like")
  @Operation(summary = "评论点赞/取消点赞")
  public Result<CommentLikeResultVO> likeComment(
      @Parameter(description = "评论点赞请求", required = true) @Validated @RequestBody
          CommentLikeRequest request) {
    if (checkLogin()) {
      return Result.fail("请完成登录");
    }
    try {
      boolean liked;
      if (commentLikeMapper.getLike(request.getCommentId(), request.getUserId()) == null) {
        commentLikeMapper.insertLike(request.getCommentId(), request.getUserId());
        liked = true;
      } else {
        commentLikeMapper.deleteLike(request.getCommentId(), request.getUserId());
        liked = false;
      }
      int likeCount = commentLikeMapper.countLikeByCommentId(request.getCommentId());
      return Result.success(new CommentLikeResultVO(liked, likeCount));
    } catch (Exception e) {
      log.error("评论点赞失败, commentId:{}, userId:{}", request.getCommentId(), request.getUserId(), e);
      return Result.fail("评论点赞失败,请稍后重试");
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
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
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
    return Result.success(videoService.getSearchVideoResults(title));
  }

  @GetMapping("/detail")
  @Operation(summary = "根据视频URL获取视频详情")
  public Result<VideoVO> getVideoDetailByUrl(
      @Parameter(description = "视频URL") @RequestParam String videoUrl) {
    try {
      if (videoUrl == null || videoUrl.isBlank()) {
        return Result.fail("视频参数不能为空");
      }
      com.yuliyuli.entity.video.Video video =
          videoMapper.selectOne(
              new LambdaQueryWrapper<com.yuliyuli.entity.video.Video>()
                  .eq(com.yuliyuli.entity.video.Video::getUrl, videoUrl)
                  .eq(com.yuliyuli.entity.video.Video::getIsDelete, (short) 0)
                  .last("LIMIT 1"));
      if (video == null) {
        return Result.fail("视频不存在或已删除");
      }
      return Result.success(com.yuliyuli.util.VideoConvertUtil.convertVideoToVideoVO(video));
    } catch (Exception e) {
      log.error("获取视频详情失败, videoUrl:{}", videoUrl, e);
      return Result.fail("获取视频详情失败");
    }
  }

  /**
   * 固定返回15个从100个热门缓存中获取的视频
   *
   * @param videoUrl 视频的路径
   * @param followUserId 作者Id
   * @param fanUserId 登录用户Id
   * @param lastId 上一页最后一条评论的Id
   * @return 右侧推荐视频（热门视频），评论列表，是否关注了该作者
   */
  @GetMapping("/clickVideo")
  @Operation(summary = "根据视频ID获取相关视频")
  public Result<VideoDetailPageVO> getRelatedVideo(
      @Parameter(description = "视频URL") @RequestParam String videoUrl,
      @Parameter(description = "关注用户ID") @RequestParam Long followUserId,
      @Parameter(description = "粉丝用户ID") @RequestParam Long fanUserId,
      @Parameter(description = "上一页最后一条评论的id") @RequestParam(required = false) Long lastId) {
    // 先对热门视频进行播放计数，再返回相关视频
    if (redisTemplate.opsForValue().get(videoUrl) != null) {
      videoService.hotVideoPlay(videoUrl);
      return clickCommonProcess(videoUrl, followUserId, fanUserId, lastId);
    } else {
      // 先对视频进行播放计数，再返回相关视频
      videoService.videoPlay(videoUrl);
      return clickCommonProcess(videoUrl, followUserId, fanUserId, lastId);
    }
  }

  @GetMapping("/comment/list")
  @Operation(summary = "获取视频评论列表")
  public Result<List<Comment>> getCommentList(
      @Parameter(description = "视频URL") @RequestParam String videoUrl,
      @Parameter(description = "当前登录用户ID") @RequestParam(required = false) Long fanUserId,
      @Parameter(description = "上一页最后一条评论的id") @RequestParam(required = false) Long lastId) {
    try {
      Page<Comment> commentPage = new Page<>(1, 30);
      commentMapper.selectPage(commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 30));
      enrichCommentLikeInfo(commentPage.getRecords(), fanUserId);
      return Result.success(commentPage.getRecords());
    } catch (Exception e) {
      log.error("获取评论列表失败, videoUrl:{}", videoUrl, e);
      return Result.fail("获取评论列表失败");
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
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("根据视频类型id获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据视频类型id获取视频列表失败");
    }
  }

  /**
   * 点击打开视频后的通用处理方法
   * @param videoUrl 视频路径
   * @param followUserId 视频作者
   * @param fanUserId 登录用户
   * @param lastId 上一页最后一条评论的Id
   * @return 右侧推荐视频（热门视频），评论列表，是否关注了该作者
   */
  private Result<VideoDetailPageVO> clickCommonProcess(
      String videoUrl, Long followUserId, Long fanUserId, Long lastId) {
      // 返回的右侧热门推荐视频栏
      List<HotRecommendVideoVO> hotVideoVOList = videoService.getRecommendHotVideo();
      // 分页获取评论列表
      Page<Comment> commentPage = new Page<>(1, 10);
      commentMapper.selectPage(
          commentPage, commentWrapper.getCommentListByCursor(videoUrl, lastId, 10));
      enrichCommentLikeInfo(commentPage.getRecords(), fanUserId);
      VideoDetailPageVO response =
          new VideoDetailPageVO(
              hotVideoVOList,
              commentPage.getRecords(),
              hasFollowRelation(followUserId, fanUserId));
      return Result.success(response);
  }

  private Long normalizeToUserId(Long idOrUserId) {
    if (idOrUserId == null) {
      return null;
    }
    User byUserId =
        userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUserId, idOrUserId).last("LIMIT 1"));
    if (byUserId != null) {
      return idOrUserId;
    }
    User byPrimaryKey = userMapper.selectById(idOrUserId);
    if (byPrimaryKey != null && byPrimaryKey.getUserId() != null) {
      return byPrimaryKey.getUserId();
    }
    return idOrUserId;
  }

  private boolean hasFollowRelation(Long followUserId, Long fanUserId) {
    if (followUserId == null || fanUserId == null) {
      return false;
    }
    Set<Long> followCandidates = buildIdCandidates(followUserId);
    Set<Long> fanCandidates = buildIdCandidates(fanUserId);
    for (Long followCandidate : followCandidates) {
      for (Long fanCandidate : fanCandidates) {
        if (followMapper.getFollow(followCandidate, fanCandidate) != null) {
          return true;
        }
      }
    }
    return false;
  }

  private Set<Long> buildIdCandidates(Long rawId) {
    Set<Long> candidates = new LinkedHashSet<>();
    if (rawId == null) {
      return candidates;
    }
    candidates.add(rawId);
    Long normalized = normalizeToUserId(rawId);
    if (normalized != null) {
      candidates.add(normalized);
    }
    return candidates;
  }

  private void enrichCommentLikeInfo(List<Comment> comments, Long currentUserId) {
    if (comments == null || comments.isEmpty()) {
      return;
    }
    for (Comment comment : comments) {
      if (comment == null || comment.getId() == null) {
        continue;
      }
      int likeCount = commentLikeMapper.countLikeByCommentId(comment.getId());
      comment.setLikeCount(likeCount);
      boolean liked =
          currentUserId != null
              && commentLikeMapper.getLike(comment.getId(), currentUserId) != null;
      comment.setLiked(liked);
    }
  }

  private Result<String> toResult(ServiceResult result) {
    return result.isSuccess() ? Result.success(result.getMessage()) : Result.fail(result.getMessage());
  }
}
