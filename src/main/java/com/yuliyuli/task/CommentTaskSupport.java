package com.yuliyuli.task;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.yuliyuli.entity.video.Comment;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.elasticsearch.ResourceNotFoundException;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommentTaskSupport {

  private static final String LOCK_KEY_PREFIX = "comment:lock:";
  private static final int LOCK_WAIT = 3;
  private static final int LOCK_RELEASE = 10;

  private final RedissonClient redissonClient;
  private final CommentMapper commentMapper;
  private final VideoMapper videoMapper;
  private final SnowflakeIdGenerator snowflakeIdGenerator;
  private final ElasticsearchOperations elasticsearchOperations;

  public void processComment(Comment comment) {
    if (comment == null || comment.getVideoId() == null || comment.getUserId() == null) {
      throw new IllegalArgumentException("评论参数不完整");
    }

    String lockKey = LOCK_KEY_PREFIX + comment.getUserId();
    RLock lock = redissonClient.getLock(lockKey);
    try {
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        throw new IllegalStateException("评论提交频繁，请稍后重试");
      }

      Long commentId = snowflakeIdGenerator.nextId();
      comment.setCommentId(commentId);
      commentMapper.insertComment(comment);
      videoMapper.addVideoCommentCount(comment.getVideoId());
      updateCommentCountToES(comment.getVideoId());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("评论处理被中断", e);
    } finally {
      if (lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }

  private void updateCommentCountToES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      String scriptSource = "ctx._source.commentCount = (ctx._source.commentCount ?: 0) + 1";
      UpdateQuery updateQuery =
          UpdateQuery.builder(videoUrl)
              .withScript(scriptSource)
              .withScriptType(ScriptType.INLINE)
              .withRetryOnConflict(3)
              .build();
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (ResourceNotFoundException e) {
      log.debug("ES文档不存在，忽略评论数更新: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理评论数同步到ES失败: {}", videoUrl, e);
    }
  }
}
