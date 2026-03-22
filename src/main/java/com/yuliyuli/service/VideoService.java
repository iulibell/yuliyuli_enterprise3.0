package com.yuliyuli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.dto.vo.HotRecommendVideoVO;
import com.yuliyuli.dto.vo.SearchVideoVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.delivery.VideoDeliveryWithoutFile;
import com.yuliyuli.entity.video.Comment;
import com.yuliyuli.entity.video.VideoCollection;
import com.yuliyuli.entity.video.VideoLike;

import java.util.List;

public interface VideoService {
  /**
   * 插入视频
   *
   * @param video 视频对象
   */
  String videoDeliver(VideoDeliveryWithoutFile videoDelivery);

  /**
   * 视频点赞
   *
   * @param videoLike 视频点赞对象
   */
  String videoLike(VideoLike videoLike);

  /**
   * 视频收藏
   *
   * @param videoCollect 视频收藏对象
   */
  String videoCollect(VideoCollection videoCollect);

  /**
   * 视频评论
   *
   * @param comment 视频评论对象
   */
  String videoComment(Comment comment);

  /**
   * 获取视频列表
   *
   * @param pageNum 页码
   * @param pageSize 每页数量
   * @return 视频列表
   */
  Page<VideoVO> getVideoList(int pageNum, int pageSize);

  /**
   * 用户点击搜索后根据传过来的标题来返回一堆相关的视频
   *
   * @param title 视频标题
   * @return 视频详情
   */
  Page<SearchVideoVO> getSearchVideoResults(String title);

  /**
   * 用户点击视频后打开视频详细页后来返回推荐热门视频
   *
   * @return 推荐热门视频，即右边的视频栏
   */
  List<HotRecommendVideoVO> getRecommendHotVideo();

  /**
   * 用户点击顶部导航后根据视频类型id获取视频列表
   *
   * @param typeId 视频类型id
   * @return 视频列表
   */
  Page<VideoVO> getVideoAccordingTypeId(int typeId, int pageNum, int pageSize);

  /**
   * 用户点击热门视频后播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  String hotVideoPlay(String videoUrl);

  /**
   * 用户点击普通视频播放视频，发送至消费者进行播放统计
   *
   * @param videoUrl 视频URL
   */
  String videoPlay(String videoUrl);
}
