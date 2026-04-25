package com.yuliyuli.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.common.CurrentUserHolder;
import com.yuliyuli.common.ServiceResult;
import com.yuliyuli.dto.vo.HotRecommendVideoVO;
import com.yuliyuli.dto.vo.SearchVideoVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.delivery.VideoDeliveryWithoutFile;
import com.yuliyuli.entity.user.User;
import com.yuliyuli.entity.video.Comment;
import com.yuliyuli.entity.video.VideoCollection;
import com.yuliyuli.entity.video.VideoLike;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.service.SearchService;
import com.yuliyuli.service.VideoService;
import com.yuliyuli.service.support.VideoEventPublisher;
import com.yuliyuli.service.support.VideoQuerySupport;
import com.yuliyuli.task.CommentTaskSupport;
import com.yuliyuli.util.BloomFilterUtil;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

  @Resource private VideoEventPublisher videoEventPublisher;

  // 视频分发线程池
  @Resource private ExecutorService threadPoolExecutor;

  @Resource private SearchService searchService;

  @Resource private VideoQuerySupport videoQuerySupport;

  @Resource private BloomFilterUtil bloomFilterUtil;

  @Resource private CommentTaskSupport commentTaskSupport;

  /*=======================================================👇消息发布者============================================================= */

  /**
   * 视频分发
   *
   * @param video 视频信息
   */
  @Override
  public ServiceResult videoDeliver(VideoDeliveryWithoutFile videoDelivery) {
    User user = CurrentUserHolder.getUser();
    if (user == null) {
      return ServiceResult.fail("请完成登录");
    }
    try {
      videoEventPublisher.publishVideoDeliver(videoDelivery);
      return ServiceResult.success("视频分发成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频分发失败", e);
      return ServiceResult.fail("视频分发失败");
    }
  }

  /**
   * 视频点赞
   *
   * @param videoLike 视频点赞对象
   */
  @Override
  public ServiceResult videoLike(VideoLike videoLike) {
    User user = CurrentUserHolder.getUser();
    if (user == null) {
      return ServiceResult.fail("请完成登录");
    }
    try {
      if (bloomFilterUtil.checkVideoExists(videoLike.getVideoId())) {
        return ServiceResult.fail("视频不存在");
      }
      videoEventPublisher.publishVideoLike(videoLike);
      return ServiceResult.success("点赞成功");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("视频点赞失败", e);
      return ServiceResult.fail("视频点赞失败");
    }
  }

  /**
   * 视频收藏
   *
   * @param videoCollect 视频收藏对象
   */
  @Override
  public ServiceResult videoCollect(VideoCollection videoCollection) {
    User user = CurrentUserHolder.getUser();
    if (user == null) {
      return ServiceResult.fail("请完成登录");
    }
    if (bloomFilterUtil.checkVideoExists(videoCollection.getVideoId())) {
      return ServiceResult.fail("视频不存在");
    }
    threadPoolExecutor.submit(
        () -> {
          try {
            videoEventPublisher.publishVideoCollect(videoCollection);
          } catch (Exception e) {
            log.error("视频收藏失败", e);
          }
        });
    return ServiceResult.success("收藏请求已提交");
  }

  /**
   * 视频评论
   *
   * @param comment 视频评论对象
   */
  @Override
  public ServiceResult videoComment(Comment comment) {
    User user = CurrentUserHolder.getUser();
    if (user == null) {
      return ServiceResult.fail("请完成登录");
    }
    if (bloomFilterUtil.checkVideoExists(comment.getVideoId())) {
      return ServiceResult.fail("视频不存在");
    }
    try {
      commentTaskSupport.processComment(comment);
      return ServiceResult.success("评论成功");
    } catch (Exception e) {
      log.error("视频评论失败", e);
      return ServiceResult.fail("评论失败，请稍后重试");
    }
  }

  /**
   * 用户点击视频后播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  @Override
  public ServiceResult hotVideoPlay(String videoUrl) {
    if (bloomFilterUtil.checkVideoExists(videoUrl)) {
      return ServiceResult.fail("视频不存在");
    }
    threadPoolExecutor.submit(
        () -> {
          try {
            videoEventPublisher.publishHotVideoPlay(videoUrl);
          } catch (Exception e) {
            log.error("视频播放失败", e);
          }
        });
    return ServiceResult.success("热门视频播放请求已提交");
  }

  /**
   * 用户点击普通视频播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  @Override
  public ServiceResult videoPlay(String videoUrl) {
    if (bloomFilterUtil.checkVideoExists(videoUrl)) {
      return ServiceResult.fail("视频不存在");
    }
    threadPoolExecutor.submit(
        () -> {
          try {
            videoEventPublisher.publishVideoPlay(videoUrl);
          } catch (Exception e) {
            log.error("视频播放失败", e);
          }
        });
    return ServiceResult.success("视频播放请求已提交");
  }

  /*=======================================================👇get方法============================================================= */

  /**
   * 获取视频列表,让前端获取视频，用于主页懒加载视频
   *
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @return 视频列表
   */
  @Override
  public Page<VideoVO> getVideoList(int pageNum, int pageSize) {
    return videoQuerySupport.getVideoList(pageNum, pageSize);
  }

  /**
   * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
   *
   * @param title 视频标题
   * @return 视频详情
   */
  @Override
  public Page<SearchVideoVO> getSearchVideoResults(String title) {
    try {
      List<SearchVideoVO> searchVideoResults = searchService.findByTitleSuggest(title);
      Page<SearchVideoVO> page = new Page<>(1, 20);
      page.setRecords(searchVideoResults);
      return page;
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("根据标题搜索视频失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据标题搜索视频失败");
    }
  }

  /**
   * 用户点击顶部导航后根据视频类型id获取视频列表
   * @param typeId 视频类型id
   * @return 视频列表
   */
  @Override
  @Cacheable(value = "videoByType", key = "#typeId + ':' + #pageNum", unless = "#result == null")
  public Page<VideoVO> getVideoAccordingTypeId(int typeId, int pageNum, int pageSize) {
    return videoQuerySupport.getVideoAccordingTypeId(typeId, pageNum, pageSize);
  }

  /**
   * 用户点击视频后打开视频详细页后来返回推荐热门视频
   *
   * @return 推荐热门视频，即右边的视频栏
   */
  @Override
  public List<HotRecommendVideoVO> getRecommendHotVideo() {
    return videoQuerySupport.getRecommendHotVideo();
  }
}
