package com.yuliyuli.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.dto.vo.HotRecommendVideoVO;
import com.yuliyuli.dto.vo.SearchVideoVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.document.VideoDocument;
import com.yuliyuli.entity.video.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class VideoConvertUtil {

  /** 单个视频实体类转换为视频VO类 */
  public static VideoVO convertVideoToVideoVO(Video video) {
    VideoVO vo = new VideoVO();
    if (video != null) {
      BeanUtils.copyProperties(video, vo);
    }
    return vo;
  }

  /** 视频List列表转换为视频VO类列表 */
  public static List<VideoVO> convertVideoListToVideoVOList(List<?> videoList) {
    if (videoList == null || videoList.isEmpty()) {
      return new ArrayList<>();
    }

    return videoList.stream()
        .filter(Objects::nonNull)
        .map(
            item -> {
              if (item instanceof Video video) {
                return convertVideoToVideoVO(video);
              }
              if (item instanceof VideoVO vo) {
                return vo;
              }
              return new VideoVO();
            })
        .collect(Collectors.toList());
  }

  /** 视频分页对象转换为视频VO类分页对象 */
  public static Page<VideoVO> converPageToVideoVOList(Page<?> pageVideoList) {
    if (pageVideoList == null) {
      return new Page<>();
    }

    List<VideoVO> voList = convertVideoListToVideoVOList(pageVideoList.getRecords());

    Page<VideoVO> resultPage =
        new Page<>(pageVideoList.getCurrent(), pageVideoList.getSize(), pageVideoList.getTotal());
    resultPage.setRecords(voList);
    return resultPage;
  }

  /** Map对象转换为视频VO类 */
  public static VideoVO convertMapToVideoVO(Map<Object, Object> map) {
    VideoVO vo = new VideoVO();
    BeanUtils.copyProperties(map, vo);
    return vo;
  }

  /** 视频文档List列表转换为搜索视频VO类列表 */
  public static List<SearchVideoVO> convertVideoDocumentListToSearchVideoVOList(
      List<VideoDocument> videoDocumentList) {
    if (videoDocumentList == null || videoDocumentList.isEmpty()) {
      return new ArrayList<>();
    }
    return videoDocumentList.stream()
        .map(
            videoDocument -> {
              SearchVideoVO searchVideoVO = new SearchVideoVO();
              searchVideoVO.setId(videoDocument.getId());
              searchVideoVO.setTitle(videoDocument.getTitle());
              searchVideoVO.setUserId(videoDocument.getUserId());
              searchVideoVO.setAuthorName(videoDocument.getAuthorName());
              searchVideoVO.setUrl(videoDocument.getUrl());
              searchVideoVO.setCoverUrl(videoDocument.getCoverUrl());
              searchVideoVO.setTypeId(videoDocument.getTypeId());
              searchVideoVO.setPlayCount(videoDocument.getPlayCount());
              searchVideoVO.setLikeCount(videoDocument.getLikeCount());
              searchVideoVO.setCommentCount(videoDocument.getCommentCount());
              searchVideoVO.setCollectionCount(videoDocument.getCollectionCount());
              searchVideoVO.setCreateTime(videoDocument.getCreateTime());
              searchVideoVO.setIntro(videoDocument.getIntro());
              searchVideoVO.setAuthorAvatar(videoDocument.getAuthorAvatar());
              return searchVideoVO;
            })
        .collect(Collectors.toList());
  }

  /** 视频文档List列表转换为热门推荐视频VO类列表 */
  public static List<HotRecommendVideoVO> convertVideoDocumentToHotRecommendVideoVO(
      List<VideoDocument> videoDocumentList) {
    if (videoDocumentList == null || videoDocumentList.isEmpty()) {
      return new ArrayList<>();
    }
    return videoDocumentList.stream()
        .map(
            videoDocument -> {
              HotRecommendVideoVO hotRecommendVideoVO = new HotRecommendVideoVO();
              hotRecommendVideoVO.setId(videoDocument.getId());
              hotRecommendVideoVO.setTitle(videoDocument.getTitle());
              hotRecommendVideoVO.setCoverUrl(videoDocument.getCoverUrl());
              hotRecommendVideoVO.setPlayCount(videoDocument.getPlayCount());
              hotRecommendVideoVO.setCommentCount(videoDocument.getCommentCount());
              hotRecommendVideoVO.setAuthorName(videoDocument.getAuthorName());
              hotRecommendVideoVO.setUrl(videoDocument.getUrl());
              hotRecommendVideoVO.setTypeId(videoDocument.getTypeId());
              hotRecommendVideoVO.setLikeCount(videoDocument.getLikeCount());
              hotRecommendVideoVO.setCollectionCount(videoDocument.getCollectionCount());
              hotRecommendVideoVO.setCreateTime(videoDocument.getCreateTime());
              hotRecommendVideoVO.setIntro(videoDocument.getIntro());
              hotRecommendVideoVO.setAuthorAvatar(videoDocument.getAuthorAvatar());
              hotRecommendVideoVO.setUserId(videoDocument.getUserId());
              return hotRecommendVideoVO;
            })
        .collect(Collectors.toList());
  }

  /** 视频List列表转换为视频VO类分页对象 */
  public static Page<VideoVO> convertToVOPage(List<?> videoList, int pageNum, int pageSize) {
    List<VideoVO> voList = convertVideoListToVideoVOList(videoList);

    Page<VideoVO> page = new Page<>(pageNum, pageSize);
    page.setRecords(voList);
    return page;
  }
}
