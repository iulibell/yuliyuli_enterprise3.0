package com.yuliyuli.service.support;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuliyuli.dto.query.VideoWrapper;
import com.yuliyuli.dto.vo.HotRecommendVideoVO;
import com.yuliyuli.dto.vo.VideoVO;
import com.yuliyuli.entity.document.VideoDocument;
import com.yuliyuli.entity.video.Video;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.init.SearchVideoInit;
import com.yuliyuli.init.VideoInfoInit;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.util.VideoConvertUtil;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VideoQuerySupport {

  private final RedissonClient redissonClient;
  private final VideoMapper videoMapper;
  private final VideoWrapper videoWrapper;
  private final RedisTemplate<String, Object> redisTemplate;

  public Page<VideoVO> getVideoList(int pageNum, int pageSize) {
    String listKey = VideoInfoInit.VIDEO_LIST_CACHE_KEY + pageNum;
    RBucket<List<Video>> listBucket = redissonClient.getBucket(listKey);

    try {
      if (listBucket.isExists()) {
        List<Video> videoList = listBucket.get();
        log.info("从缓存中获取视频列表成功,视频数量:{}", videoList.size());
        return convertToVOPage(videoList, pageNum, pageSize);
      }

      String lockKey = "lock:video:list:" + pageNum;
      RLock lock = redissonClient.getLock(lockKey);
      boolean isLock = lock.tryLock(3, 10, TimeUnit.SECONDS);

      if (isLock) {
        try {
          if (listBucket.isExists()) {
            return convertToVOPage(listBucket.get(), pageNum, pageSize);
          }

          log.info("从数据库中获取视频列表,页码:{}", pageNum);
          Page<Video> page =
              videoMapper.selectPage(new Page<>(pageNum, pageSize), videoWrapper.getInitVideo());

          Duration expireDuration = Duration.ofHours(VideoInfoInit.EXPIRE_TIME);
          if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            listBucket.set(page.getRecords(), expireDuration);
          } else {
            listBucket.set(new ArrayList<>(), Duration.ofMinutes(5));
          }

          return VideoConvertUtil.converPageToVideoVOList(page);
        } finally {
          if (lock.isHeldByCurrentThread()) {
            lock.unlock();
          }
        }
      }

      log.warn("获取视频列表锁失败,页码:{}", pageNum);
      Page<Video> page =
          videoMapper.selectPage(new Page<>(pageNum, pageSize), videoWrapper.getInitVideo());
      return VideoConvertUtil.converPageToVideoVOList(page);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new GlobalExceptionHandler.BusinessException("获取锁被中断");
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("从数据库中获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("从数据库中获取视频列表失败");
    }
  }

  public Page<VideoVO> getVideoAccordingTypeId(int typeId, int pageNum, int pageSize) {
    try {
      Page<Video> videoPage =
          videoMapper.selectPage(
              new Page<>(pageNum, pageSize), videoWrapper.getVideoAccordingTypeId(typeId));
      log.info("已根据typeId:{}进行筛选视频,视频数量:{}", typeId, videoPage.getRecords().size());
      return VideoConvertUtil.converPageToVideoVOList(videoPage);
    } catch (GlobalExceptionHandler.BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("根据视频类型id获取视频列表失败", e);
      throw new GlobalExceptionHandler.BusinessException("根据视频类型id获取视频列表失败");
    }
  }

  public List<HotRecommendVideoVO> getRecommendHotVideo() {
    log.info("开始获取推荐热门视频");
    Set<Object> top15Videos =
        redisTemplate.opsForZSet().reverseRange(SearchVideoInit.HOT_ALL_KEY, 0, 14);

    log.info("从ZSet获取的视频数量: {}", top15Videos != null ? top15Videos.size() : 0);

    List<VideoDocument> hotVideoList = new ArrayList<>();
    ObjectMapper objectMapper = new ObjectMapper();

    if (top15Videos != null && !top15Videos.isEmpty()) {
      for (Object videoObj : top15Videos) {
        try {
          VideoDocument videoDocument = null;

          if (videoObj instanceof VideoDocument) {
            videoDocument = (VideoDocument) videoObj;
          } else if (videoObj instanceof Map) {
            videoDocument = objectMapper.convertValue(videoObj, VideoDocument.class);
          } else if (videoObj instanceof String) {
            videoDocument = objectMapper.readValue((String) videoObj, VideoDocument.class);
          }

          if (videoDocument != null) {
            hotVideoList.add(videoDocument);
            log.info("添加视频到推荐列表: {}", videoDocument.getTitle());
          } else {
            log.warn("无法转换视频数据: {}", videoObj);
          }
        } catch (Exception e) {
          log.error("处理视频数据失败: {}", videoObj, e);
        }
      }
    } else {
      log.warn("ZSet中没有视频数据: {}", SearchVideoInit.HOT_ALL_KEY);
    }

    log.info("获取推荐热门视频成功，数量: {}", hotVideoList.size());
    return VideoConvertUtil.convertVideoDocumentToHotRecommendVideoVO(hotVideoList);
  }

  private Page<VideoVO> convertToVOPage(List<Video> videoList, int pageNum, int pageSize) {
    Page<Video> page = new Page<>(pageNum, pageSize);
    page.setRecords(videoList);
    return VideoConvertUtil.converPageToVideoVOList(page);
  }
}
