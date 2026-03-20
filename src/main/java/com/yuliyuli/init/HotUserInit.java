package com.yuliyuli.init;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.yuliyuli.entity.User;
import com.yuliyuli.mapper.FollowMapper;
import com.yuliyuli.mapper.UserMapper;
import com.yuliyuli.query.FollowWrapper;
import com.yuliyuli.query.UserWrapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HotUserInit {

    private final String HOT_USER_KEY_PREFIX = "user:hot:";
    private final int EXPIRE_TIME = 11;

    @Resource private RedissonClient redissonClient;

    @Resource private UserWrapper userWrapper;

    @Resource private UserMapper userMapper;

    @Resource private FollowWrapper followWrapper;

    @Resource private FollowMapper followMapper;

    @PostConstruct
    public void init() {
        log.info("初始化热门用户");
        asynInitHotUser();
    }

    @Async("asyncThreadPoolExecutor")
    /** 初始化热门用户 */
    private CompletableFuture<Void> asynInitHotUser() {
     log.info("进行异步初始化热门用户");
     List<Long> hotUserIds = userMapper.selectList(userWrapper.buildHotUserList()).stream()
      .map(User::getUserId)
      .collect(Collectors.toList());
      return CompletableFuture.runAsync(
        () -> 
         followMapper.selectList(followWrapper.buildFollowListByUserId(hotUserIds.get(0))).forEach(
          follow -> {
            redissonClient
             .getScoredSortedSet(HOT_USER_KEY_PREFIX + follow.getFollowUserId())
             .add(follow.getFanUserId(), Duration.ofHours(EXPIRE_TIME));
          }
         )
        );
    }
}