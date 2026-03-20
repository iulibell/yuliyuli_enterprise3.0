package com.yuliyuli.timer;

import com.yuliyuli.entity.VideoLike;
import com.yuliyuli.mapper.CommentMapper;
import com.yuliyuli.mapper.FollowMapper;
import com.yuliyuli.mapper.VideoLikeMapper;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.query.VideoLikeWrapper;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DelayedTaskProcessor {

  private static final String FOLLOW_DELAY_KEY = "follow:delay";
  private static final String DELETE_DELAY_KEY = "video:delete:delay";
  private static final String PLAY_DELAY_KEY = "video:play:delay";
  private static final String LIKE_COUNTER_KEY_PREFIX = "video:like:";
  private static final String USER_KEY_PREFIX = "user:like:";
  private static final String LIKE_DELAY_KEY = "video:like:delay";
  private static final String HOT_PLAY_DELAY_KEY = "hot:video:play:delay";
  private static final String HOT_PLAY_COUNTER_KEY_PREFIX = "video:hot:play:";
  private static final String PLAY_COUNTER_KEY_PREFIX = "video:play:";

  @Resource private RedisTemplate<String, Object> redisTemplate;

  @Resource private RedissonClient redissonClient;

  @Resource private VideoMapper videoMapper;

  @Resource private VideoLikeWrapper videoLikeWrapper;

  @Resource private VideoLikeMapper videoLikeMapper;

  @Resource private CommentMapper commentMapper;

  @Resource private FollowMapper followMapper;

  @Resource private ElasticsearchOperations elasticsearchOperations;

  // 处理延时点赞
  @Scheduled(fixedRate = 1000) // 每秒检查一次
  @Async
  public void processDelayLikes() {

    long currentTime = System.currentTimeMillis();

    // 获取所有到时间的点赞操作
    RScoredSortedSet<VideoLike> sortedSet = redissonClient.getScoredSortedSet(LIKE_DELAY_KEY);
    Collection<VideoLike> expiredLikes =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(ScoredEntry::getValue)
            .collect(Collectors.toList());

    for (VideoLike videoLike : expiredLikes) {
      try {
        processLike(videoLike);
        sortedSet.remove(videoLike);
      } catch (Exception e) {
        log.error("处理延时点赞失败", e);
      }
    }
  }

  // 处理延时点赞同步到数据库
  // 处理延时点赞同步到数据库【最终修复版】
  private void processLike(VideoLike videoLike) {
    String counterKey = LIKE_COUNTER_KEY_PREFIX + videoLike.getVideoId();
    String userKey = USER_KEY_PREFIX + videoLike.getVideoId();
    Long userId = videoLike.getUserId();
    String videoId = videoLike.getVideoId().toString();

    try {
        RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
        RSet<Long> userSet = redissonClient.getSet(userKey);
        boolean isLikedInCache = userSet.contains(userId);

        // 缓存里有点赞 → 执行 取消点赞
        if (isLikedInCache) {
            userSet.remove(userId);        // 删缓存
            counter.decrementAndGet();     // 计数 -1
            videoMapper.deleteVideoLike(videoId, userId); // 删DB点赞记录
            log.info("取消点赞：用户{}，视频{}", userId, videoId);
        }
        // 缓存没点赞，但DB有点赞记录 → 也执行 取消点赞
        else if (videoLikeMapper.selectOne(videoLikeWrapper.getVideoLike(videoId, userId)) != null) {
            counter.decrementAndGet();
            videoMapper.deleteVideoLike(videoId, userId);
            log.info("取消点赞(DB存在)：用户{}，视频{}", userId, videoId);
        }
        // 既没缓存也没DB → 执行 点赞
        else {
            userSet.add(userId);          // 加缓存
            counter.incrementAndGet();    // 计数 +1
            videoMapper.insertVideoLike(videoLike); // 加DB点赞记录
            log.info("添加点赞：用户{}，视频{}", userId, videoId);
        }

        // 核心修复：把 Redis 计数 同步到 DB
        long changeCount = counter.getAndSet(0);
        if (changeCount != 0) {
            // 调用你已有的 SQL：like_count = like_count + #{addCount}
            videoMapper.addVideoLikeCount(changeCount, videoId);
        }

    } catch (Exception e) {
        log.error("处理延时点赞失败: {}", videoLike, e);
    }
  }

  // 处理延时热门视频播放
  @Scheduled(fixedRate = 15000) // 每15秒检查一次
  @Async
  public void processDelayHotPlay() {

    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(HOT_PLAY_DELAY_KEY);
    Collection<String> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (String videoUrl : expiredVideoUrls) {
      try {
        processHotPlay(videoUrl);
        sortedSet.remove(videoUrl);
      } catch (Exception e) {
        log.error("处理延时热门视频播放失败: {}", videoUrl, e);
      }
    }
  }

  // 处理延时热门视频播放同步到数据库
  private void processHotPlay(String videoUrl) {
    String counterKey = HOT_PLAY_COUNTER_KEY_PREFIX + videoUrl;
    try {
      playCommonProcess(videoUrl, counterKey);
    } catch (Exception e) {
      log.error("处理延时热门视频播放失败: {}", videoUrl, e);
    }
  }

  // 处理延时视频播放
  @Scheduled(fixedRate = 5000) // 每5秒检查一次
  public void processDelayPlay() {

    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<String> sortedSet = redissonClient.getScoredSortedSet(PLAY_DELAY_KEY);
    Collection<String> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (String videoUrl : expiredVideoUrls) {
      try {
        processPlay(videoUrl);
        sortedSet.remove(videoUrl);
      } catch (Exception e) {
        log.error("处理延时视频播放失败: {}", videoUrl, e);
      }
    }
  }

  // 处理延时视频播放同步到数据库
  private void processPlay(String videoUrl) {
    String counterKey = PLAY_COUNTER_KEY_PREFIX + videoUrl;
    try {
      playCommonProcess(videoUrl, counterKey);
    } catch (Exception e) {
      log.error("处理延时视频播放失败: {}", videoUrl, e);
    }
  }

  // 处理延时视频删除
  @Scheduled(fixedRate = 5000) // 每5秒检查一次
  @Async
  protected void processDelayDelete() {
    long currentTime = System.currentTimeMillis();
    // 从有序集合获取所有到期的 videoUrl
    RScoredSortedSet<Map<String, Object>> sortedSet = redissonClient.getScoredSortedSet(DELETE_DELAY_KEY);
    Collection<Map<String, Object>> expiredVideoUrls =
        sortedSet.entryRange(0, true, currentTime, true).stream()
            .map(entry -> entry.getValue())
            .collect(Collectors.toList());
    // 遍历处理每个到期的 videoUrl
    for (Map<String, Object> map : expiredVideoUrls) {
      try {
        processDelete(map);
        sortedSet.remove(map);
      } catch (Exception e) {
        log.error("处理延时视频删除失败: {}", map, e);
      }
    }
  }

  // 处理延时视频删除同步到数据库
  private void processDelete(Map<String, Object> map) {
    log.info("处理延时视频删除: {}", map);
    // 从map中获取videoUrl和userId
    String videoUrl = map.get("videoUrl").toString();
    String userId = map.get("userId").toString();
    String hotVideoKey = "video:hot:all";
    String hotTopKey = "video:hot:top";
    String videoKey = "video:info:" + videoUrl;
    String videoListKeyPattern = "video:info:list*";
    try {
      // 从热门视频ZSet中移除该视频
      // 注意：这里需要从ZSet中移除，而不是删除key
      // 由于ZSet中存储的是VideoDocument对象，我们需要先获取再移除
      // 简化处理：直接删除整个热门缓存，让它下次自动重建
      if (redisTemplate.hasKey(hotVideoKey)) {
        redisTemplate.delete(hotVideoKey);
        log.info("删除热门视频缓存成功: {}", hotVideoKey);
      }
      // 删除Top10热门视频缓存
      if (redisTemplate.hasKey(hotTopKey)) {
        redisTemplate.delete(hotTopKey);
        log.info("删除Top10热门视频缓存成功: {}", hotTopKey);
      }
      // 删除视频信息缓存
      if (redisTemplate.hasKey(videoKey)) {
        redisTemplate.delete(videoKey);
        log.info("删除视频信息缓存成功: {}", videoKey);
      }
      // 删除视频列表缓存（使用通配符删除所有分页缓存）
      try {
        Set<String> listKeys = redisTemplate.keys(videoListKeyPattern);
        if (listKeys != null && !listKeys.isEmpty()) {
          redisTemplate.delete(listKeys);
          log.info("删除视频列表缓存成功，共 {} 个", listKeys.size());
        }
      } catch (Exception e) {
        log.warn("删除视频列表缓存失败: {}", e.getMessage());
      }
      // 删除数据库中的视频记录
      videoMapper.deleteVideo(videoUrl, Long.parseLong(userId));
      log.info("删除数据库中的视频记录成功: {}", videoUrl);
      // 删除数据库中的评论记录
      commentMapper.deleteComment(videoUrl);
      log.info("删除数据库中的评论记录成功: {}", videoUrl);
      // 删除ES中的视频记录
      processDeleteES(videoUrl);
      log.info("处理延时视频删除成功: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理延时视频删除失败: {}", videoUrl, e);
    }
  }

  // 处理延时关注同步到数据库
  @Scheduled(fixedRate = 5000) // 每5秒检查一次
    public void processFollow() {
        try {
            RScoredSortedSet<Map<String, Object>> sortedSet = redissonClient.getScoredSortedSet(FOLLOW_DELAY_KEY);
            long now = System.currentTimeMillis();

            // 每次最多拉取 200 条，防止OOM
            Collection<ScoredEntry<Map<String, Object>>> entries =
                    sortedSet.entryRange(0, true, now, true, 0, 200);

            for (ScoredEntry<Map<String, Object>> entry : entries) {
                Map<String, Object> task = entry.getValue();
                boolean removed = sortedSet.remove(task);
                if (!removed) {
                    // 已被其他线程处理，跳过
                    continue;
                }

                try {
                    doProcess(task);
                } catch (Exception e) {
                    log.error("处理关注任务失败: {}", task, e);
                }
            }

        } catch (Exception e) {
            log.error("延时任务执行异常", e);
        }
    }

    // 真正处理关注逻辑
    private void doProcess(Map<String, Object> map) {
        String operation = map.getOrDefault("operation", "follow").toString();
        Object fanUserIdObj = map.get("fanUserId");
        Object followUserIdObj = map.get("followUserId");

        if (fanUserIdObj == null || followUserIdObj == null) {
            log.error("任务数据不完整: {}", map);
            return;
        }

        try {
            long fanUserId = Long.parseLong(fanUserIdObj.toString());
            long followUserId = Long.parseLong(followUserIdObj.toString());
            String hotUserKey = "user:hot:" + followUserId;
            RScoredSortedSet<Long> hotUserSet = redissonClient.getScoredSortedSet(hotUserKey);

            // 判断是否是热门用户：看缓存是否存在
            boolean isHotUser = !hotUserSet.isEmpty();

            if ("unfollow".equals(operation)) {
                handleUnfollow(fanUserId, followUserId, hotUserSet, isHotUser);
            } else {
                handleFollow(fanUserId, followUserId, hotUserSet, isHotUser);
            }

        } catch (Exception e) {
            log.error("处理任务异常: {}", map, e);
        }
    }

    // 关注逻辑
    private void handleFollow(long fanUserId, long followUserId,
                              RScoredSortedSet<Long> hotUserSet, boolean isHotUser) {
        // 幂等：不存在才插入
        if (followMapper.getFollow(followUserId, fanUserId) == null) {
            followMapper.insertFollow(followUserId, fanUserId);
            followMapper.updateFansCount(followUserId);

            if (isHotUser) {
                hotUserSet.add(System.currentTimeMillis(), fanUserId);
                log.info("关注热门UP主成功: fan={}, up={}", fanUserId, followUserId);
            } else {
                log.info("关注普通UP主成功: fan={}, up={}", fanUserId, followUserId);
            }
        }
    }

    // 取消关注逻辑
    private void handleUnfollow(long fanUserId, long followUserId,
                                RScoredSortedSet<Long> hotUserSet, boolean isHotUser) {
        if (followMapper.getFollow(followUserId, fanUserId) != null) {
            followMapper.deleteFollow(followUserId, fanUserId);
            followMapper.decrementFansCount(followUserId);

            if (isHotUser) {
                hotUserSet.remove(fanUserId);
                log.info("取消热门UP主关注: fan={}, up={}", fanUserId, followUserId);
            } else {
                log.info("取消普通UP主关注: fan={}, up={}", fanUserId, followUserId);
            }
        }
    }

  // 处理延时视频删除同步到数据库
  @Scheduled(fixedRate = 10000) // 每10秒检查一次
  @Async
  protected void processDeleteIsDelete(){
    try {
      int deletedCount = videoMapper.deleteVideoIsDelete();
      if (deletedCount > 0) {
        log.info("处理延时视频删除成功，删除了{}个视频", deletedCount);
      }
    } catch (Exception e) {
      log.error("处理延时视频删除失败: {}", e);
    }
  }

  private void processPlayCountToES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      // 定义核心参数（文档ID直接用videoUrl，无需替换特殊字符，ES支持特殊字符作为文档ID）
      String docId = videoUrl; // 要更新的ES文档ID
      String scriptSource = "ctx._source.playCount = (ctx._source.playCount ?: 0) + 1"; // 原子更新脚本
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
      log.debug("ES文档不存在，忽略播放量更新: {}", videoUrl);
    } catch (Exception e) {
      log.error("处理延时视频播放同步到ES失败: {}", videoUrl, e);
    }
  }

  // 处理延时视频删除同步到ES
  private void processDeleteES(String videoUrl) {
    if (videoUrl == null) {
      return;
    }
    try {
      // 定义核心参数（文档ID直接用videoUrl，无需替换特殊字符，ES支持特殊字符作为文档ID）
      String docId = videoUrl; // 要更新的ES文档ID
      String scriptSource = "ctx._source.playCount = (ctx._source.playCount ?: 0) - 1"; // 原子更新脚本
      // 构建 UpdateQuery（适配所有 Spring Data Elasticsearch 版本的通用写法）
      UpdateQuery updateQuery =
          UpdateQuery.builder(docId)
              .withScript(scriptSource) // 传入内联脚本内容（字符串）
              .withScriptType(ScriptType.INLINE) // 明确指定脚本类型为内联（关键！）
              .build();
      // 批量更新ES文档
      elasticsearchOperations.update(updateQuery, IndexCoordinates.of("video"));
    } catch (Exception e) {
      log.error("处理延时视频删除同步到ES失败: {}", videoUrl, e);
    }
  }

  // 处理延时视频播放的公用方法
  private void playCommonProcess(String videoUrl, String counterKey){
      // 计数器，记录播放次数
      RAtomicLong counter = redissonClient.getAtomicLong(counterKey);
      counter.incrementAndGet();
      processPlayCountToES(videoUrl);
      videoMapper.addVideoPlayCount(1, videoUrl);
      log.info("延时处理视频播放：视频{}", videoUrl);
  }
}
