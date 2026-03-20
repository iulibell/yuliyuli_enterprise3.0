package com.yuliyuli.init;

import com.yuliyuli.document.VideoDocument;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SearchVideoInit implements CommandLineRunner {

    public static final String HOT_TOP_KEY = "video:hot:top";
    public static final String HOT_ALL_KEY = "video:hot:all";
    private static final int EXPIRE_TIME = 11;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        try {
            IndexOperations indexOps = elasticsearchOperations.indexOps(VideoDocument.class);

            if (!indexOps.exists()) {
                indexOps.createWithMapping();
                log.info("搜索视频索引创建成功");
            } else {
                log.info("搜索视频索引已存在");
            }

            syncInitHotVideoDataToRedis();

            refreshTopTenCache();

        } catch (Exception e) {
            log.error("初始化搜索视频失败", e);
        }
    }

    /**
     * 同步热门视频到 Redis（正确版）
     */
    private void syncInitHotVideoDataToRedis() {
        try {
            log.info("开始同步热门视频到 Redis");

            // 查询播放量 > 10000 的视频，取前100
            Criteria criteria = Criteria.where("playCount").greaterThan(10000);
            CriteriaQuery query = CriteriaQuery.builder(criteria)
                    .withPageable(PageRequest.of(0, 100))
                    .withSort(Sort.by(Sort.Direction.DESC, "playCount"))
                    .build();

            SearchHits<VideoDocument> hits = elasticsearchOperations.search(query, VideoDocument.class);
            List<VideoDocument> list = hits.stream().map(h -> h.getContent()).toList();

            if (list.isEmpty()) {
                log.warn("没有查询到热门视频");
                return;
            }

            // 先清空旧缓存
            redisTemplate.delete(HOT_ALL_KEY);
            redisTemplate.delete(HOT_TOP_KEY);

            // 存入 ZSet：score = 播放量
            for (VideoDocument videoDocument : list) {
                redisTemplate.opsForZSet().add(HOT_ALL_KEY, videoDocument, videoDocument.getPlayCount());
            }

            // 设置过期时间
            redisTemplate.expire(HOT_ALL_KEY, EXPIRE_TIME + new Random().nextInt(3), TimeUnit.HOURS);

            log.info("同步热门视频到 Redis 成功，共 {} 条", list.size());

        } catch (Exception e) {
            log.error("同步热门视频失败", e);
        }
    }

    /**
     * 刷新 Top10 热门视频（正确版）
     */
    public void refreshTopTenCache() {
        try {
            // 从 ZSet 取前 10
            Set<Object> top10 = redisTemplate.opsForZSet().reverseRange(HOT_ALL_KEY, 0, 9);

            if (top10 == null || top10.isEmpty()) {
                log.warn("暂无热门视频可刷新");
                return;
            }

            redisTemplate.delete(HOT_TOP_KEY);

            // 存入 List
            redisTemplate.opsForList().leftPushAll(HOT_TOP_KEY, top10);
            redisTemplate.expire(HOT_TOP_KEY, EXPIRE_TIME + new Random().nextInt(3), TimeUnit.HOURS);

            log.info("刷新前10热门视频成功,共 {} 条", top10.size());

        } catch (Exception e) {
            log.error("刷新Top10失败", e);
        }
    }
}
