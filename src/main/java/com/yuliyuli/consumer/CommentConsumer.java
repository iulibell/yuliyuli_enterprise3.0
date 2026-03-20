package com.yuliyuli.consumer;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.rabbitmq.client.Channel;
import com.yuliyuli.config.RabbitMqConfig;
import com.yuliyuli.entity.Comment;
import com.yuliyuli.exception.GlobalExceptionHandler;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.query.VideoWrapper;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.ScriptType;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CommentConsumer {

  private final String RETRY_HEADER = "comment-retry-count";
  private static final String LOCK_KEY_PREFIX = "comment:lock:";
  private static final int MAX_RETRY_COUNT = 3;
  private static final int LOCK_WAIT = 3; // 3秒
  private static final int LOCK_RELEASE = 10; // 10秒

  @Resource private RedissonClient redissonClient;

  @Resource private CommentMapper commentMapper;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoWrapper videoWrapper;

  @Resource private SnowflakeIdGenerator snowflakeIdGenerator;

  @Resource private ElasticsearchOperations elasticsearchOperations;

  @RabbitListener(queues = RabbitMqConfig.COMMENT_QUEUE_NAME)
  public void commentConsumer(Comment comment, Channel channel, Message mqMessage)
      throws Exception {
    log.info("进入评论消费者");
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    // 参数校验
    if(comment == null || comment.getVideoId() == null || comment.getUserId() == null) {
      channel.basicReject(deliveryTag, false);
      log.warn("评论消息为空,在评论消费者丢弃");
      return;
    }
    //重试次数,从消息头中获取重试次数,如果没有则默认0
    Map<String, Object> headers = mqMessage.getMessageProperties().getHeaders();
    Integer retryCount = (Integer) headers.getOrDefault(RETRY_HEADER, 0);
    // 锁: 每个用户评论时,加锁,防止并发评论
    String lockKey = LOCK_KEY_PREFIX + comment.getUserId();
    RLock lock = redissonClient.getLock(lockKey);
    // 使用雪花算法生成评论id
    Long commentId = snowflakeIdGenerator.nextId();

    try {
      // 尝试加锁,如果3秒内没有加锁成功,则重新放入队列
      boolean isLock = lock.tryLock(LOCK_WAIT, LOCK_RELEASE, TimeUnit.SECONDS);
      if (!isLock) {
        channel.basicNack(deliveryTag, false, true);
        log.info("用户{}评论锁被其他线程占用,已重新放入队列", comment.getUserId());
        return;
      }
      log.info("用户{}评论锁已获取", comment.getUserId());
      comment.setCommentId(commentId);
      log.info("开始插入评论到数据库");
      commentMapper.insertComment(comment);
      log.info("评论插入数据库成功,评论ID:{}", commentId);
      // 直接用 SQL 累加评论数
      videoMapper.addVideoCommentCount(comment.getVideoId());
      // 更新ES中的评论数
      updateCommentCountToES(comment.getVideoId());
      // 手动确认消息，彻底解决重复消费
      channel.basicAck(deliveryTag, false);
      log.info("评论成功,评论ID:{}", commentId);
    } catch (Exception e) {
      log.error("评论消费异常,retry={}", retryCount, e);
      handleRetry(deliveryTag, channel, retryCount, headers);
    } finally {
      if (lock != null && lock.isHeldByCurrentThread()) {
        lock.unlock();
        log.info("用户{}评论锁已释放", comment.getUserId());
      }
    }
  }

  /**
   * 更新视频评论数到ES
   * @param videoUrl 视频URL
   */
  private void updateCommentCountToES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      // 定义核心参数（文档ID直接用videoUrl，无需替换特殊字符，ES支持特殊字符作为文档ID）
      String docId = videoUrl; // 要更新的ES文档ID
      String scriptSource = "ctx._source.commentCount = (ctx._source.commentCount ?: 0) + 1"; // 原子更新脚本
      // 构建 UpdateQuery（适配所有 Spring Data Elasticsearch 版本的通用写法）
      UpdateQuery updateQuery = 
          UpdateQuery.builder(docId)
              .withScript(scriptSource) // 传入内联脚本内容（字符串）
              .withScriptType(ScriptType.INLINE) // 明确指定脚本类型为内联（关键！）
              .withRetryOnConflict(3) // 冲突时重试3次
              .build();
      // 批量更新ES文档
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (org.springframework.data.elasticsearch.ResourceNotFoundException e) {
      // 文档不存在，忽略此错误
      log.debug("ES文档不存在，忽略评论数更新: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理评论数同步到ES失败: {}", videoUrl, e);
    }
  }

  /**
   * 死信队列（记录+警告）
   * @param comment
   * @param channel
   * @param mqMessage
   * @throws Exception
   */
  @RabbitListener(queues = RabbitMqConfig.COMMENT_DEAD_QUEUE_NAME)
  public void commentDeadConsumer(Comment comment, Channel channel, Message mqMessage)
      throws Exception {
    Long deliveryTag = mqMessage.getMessageProperties().getDeliveryTag();
    log.info("死信队列收到失败评论,评论ID:{}", comment.getCommentId());
    try {
      channel.basicAck(deliveryTag, false);
    } catch (Exception e) {
      log.error("死信队列丢弃失败评论失败,评论ID:{}", comment.getCommentId(), e);
      throw new GlobalExceptionHandler.BusinessException("死信队列丢弃失败评论失败");
    }
  }

  /**
   * 处理重试
   * @param deliveryTag 消息标签
   * @param channel 通道
   * @param retryCount 重试次数
   * @param headers 消息头
   */
  private void handleRetry(Long deliveryTag, Channel channel, Integer retryCount, Map<String, Object> headers) {
    if (retryCount < MAX_RETRY_COUNT) {
      headers.put(RETRY_HEADER, retryCount + 1);
      try {
        channel.basicNack(deliveryTag, false, true);
      } catch (Exception e) {
        log.error("重试评论消息失败,重试次数:{}", retryCount + 1, e);
      }
    } else {
      try {
        channel.basicReject(deliveryTag, false);
      } catch (Exception e) {
        log.error("评论消息重试次数超过最大重试次数,已丢弃,重试次数:{}", retryCount, e);
      }
    }
  }
}
