package com.yuliyuli.task;

import com.yuliyuli.mapper.VideoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayTaskSupport {

  private final RedissonClient redissonClient;
  private final VideoMapper videoMapper;
  private final ElasticsearchOperations elasticsearchOperations;

  public void processPlay(String videoUrl) {
    processPlayInternal(videoUrl, TaskKeyConstants.PLAY_COUNTER_KEY_PREFIX + videoUrl);
  }

  public void processHotPlay(String videoUrl) {
    processPlayInternal(videoUrl, TaskKeyConstants.HOT_PLAY_COUNTER_KEY_PREFIX + videoUrl);
  }

  private void processPlayInternal(String videoUrl, String counterKey) {
    if (videoUrl == null) {
      return;
    }
    try {
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      long delta = counter.getAndSet(0);
      if (delta <= 0) {
        return;
      }
      processPlayCountToES(videoUrl, delta);
      videoMapper.addVideoPlayCount(delta, videoUrl);
      log.debug("延时批量处理视频播放：视频{}，增量{}", videoUrl, delta);
    } catch (Exception e) {
      log.error("处理延时视频播放失败: {}", videoUrl, e);
    }
  }

  private void processPlayCountToES(String videoUrl, long delta) {
    if (videoUrl == null || delta <= 0) {
      return;
    }
    try {
      UpdateQuery updateQuery =
          UpdateQuery.builder(videoUrl)
              .withScript("ctx._source.playCount = (ctx._source.playCount ?: 0) + " + delta)
              .withScriptType(ScriptType.INLINE)
              .withRetryOnConflict(3)
              .build();
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (org.springframework.data.elasticsearch.ResourceNotFoundException e) {
      log.debug("ES文档不存在，忽略播放量更新: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理延时视频播放同步到ES失败: {}", videoUrl, e);
    }
  }
}
