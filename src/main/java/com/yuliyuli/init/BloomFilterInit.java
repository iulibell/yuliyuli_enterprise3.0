package com.yuliyuli.init;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuliyuli.entity.Video;
import com.yuliyuli.mapper.VideoMapper;
import com.yuliyuli.query.VideoWrapper;
import com.yuliyuli.util.BloomFilterUtil;

import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class BloomFilterInit implements CommandLineRunner {

    @Resource private VideoMapper videoMapper;

    @Resource private BloomFilterUtil bloomFilterUtil;

    @Resource private VideoWrapper videoWrapper;

    @Override
    public void run(String... args) {
        // 初始化布隆过滤器
        initBloomFilter();
    }

    /**
     * 初始化布隆过滤器
     */
    @Async
    private void initBloomFilter() {
        Page<Video> videoPage = videoMapper.selectPage(
            new Page<>(1, Integer.MAX_VALUE), videoWrapper.getVideoUrlForBloom());
        List<Video> videoList = videoPage.getRecords();
        videoList.forEach(video -> bloomFilterUtil.addVideoUrl(video.getUrl()));
    }
}
