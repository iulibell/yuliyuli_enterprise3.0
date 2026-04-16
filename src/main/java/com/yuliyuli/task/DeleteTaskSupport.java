package com.yuliyuli.task;

import com.yuliyuli.dto.command.VideoDeleteCommand;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteTaskSupport {

  private final RedisTemplate<String, Object> redisTemplate;
  private final VideoMapper videoMapper;
  private final CommentMapper commentMapper;
  private final ElasticsearchOperations elasticsearchOperations;

  public void processDelete(VideoDeleteCommand command) {
    log.info("处理延时视频删除: {}", command);
    String videoUrl = command.getVideoUrl();
    Long userId = command.getUserId();
    String hotVideoKey = "video:hot:all";
    String hotTopKey = "video:hot:top";
    String videoKey = "video:info:" + videoUrl;
    String videoListKeyPattern = "video:info:list*";
    try {
      if (redisTemplate.hasKey(hotVideoKey)) {
        redisTemplate.delete(hotVideoKey);
        log.info("删除热门视频缓存成功: {}", hotVideoKey);
      }
      if (redisTemplate.hasKey(hotTopKey)) {
        redisTemplate.delete(hotTopKey);
        log.info("删除Top10热门视频缓存成功: {}", hotTopKey);
      }
      if (redisTemplate.hasKey(videoKey)) {
        redisTemplate.delete(videoKey);
        log.info("删除视频信息缓存成功: {}", videoKey);
      }
      try {
        long deletedCount = deleteKeysByPattern(videoListKeyPattern);
        if (deletedCount > 0) {
          log.info("删除视频列表缓存成功，共 {} 个", deletedCount);
        }
      } catch (Exception e) {
        log.warn("删除视频列表缓存失败: {}", e.getMessage());
      }
      videoMapper.deleteVideo(videoUrl, userId);
      log.info("删除数据库中的视频记录成功: {}", videoUrl);
      commentMapper.deleteComment(videoUrl);
      log.info("删除数据库中的评论记录成功: {}", videoUrl);
      processDeleteES(videoUrl);
      log.info("处理延时视频删除成功: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理延时视频删除失败: {}", videoUrl, e);
    }
  }

  public void cleanupSoftDeletedVideos() {
    try {
      int deletedCount = videoMapper.deleteVideoIsDelete();
      if (deletedCount > 0) {
        log.info("处理延时视频删除成功，删除了{}个视频", deletedCount);
      }
    } catch (Exception e) {
      log.error("处理延时视频删除失败: {}", e.getMessage(), e);
    }
  }

  private void processDeleteES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      UpdateQuery updateQuery =
          UpdateQuery.builder(videoUrl)
              .withScript("ctx._source.playCount = (ctx._source.playCount ?: 0) - 1")
              .withScriptType(ScriptType.INLINE)
              .build();
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (Exception e) {
      log.error("处理延时视频删除同步到ES失败: {}", videoUrl, e);
    }
  }

  private long deleteKeysByPattern(String pattern) {
    return redisTemplate.execute(
        (RedisCallback<Long>)
            connection -> {
              long deleted = 0;
              ScanOptions options = ScanOptions.scanOptions().match(pattern).count(200).build();
              try (Cursor<byte[]> cursor = connection.scan(options)) {
                while (cursor.hasNext()) {
                  String key = new String(cursor.next(), StandardCharsets.UTF_8);
                  Boolean removed = redisTemplate.delete(key);
                  if (Boolean.TRUE.equals(removed)) {
                    deleted++;
                  }
                }
              }
              return deleted;
            });
  }
}
