package com.jl.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jl.springbootinit.model.entity.Chart;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;

@Service
public class RedisCacheManager {
    @Resource
    private RedissonClient redissonClient;

    public Page<Chart> getCachedResult(String cacheKey) {
        // 从缓存中获取数据
        RMap<String, Object> cache = redissonClient.getMap(cacheKey);
        return (Page<Chart>) cache.get(cacheKey);
    }

    public void putCachedResult(String cacheKey, Page<Chart> chartPage) {
        // 放入缓存
        RMap<String, Object> cache = redissonClient.getMap(cacheKey);
        cache.put(cacheKey, chartPage);
        // 设置缓存过期时间为60秒
        cache.expire(Duration.ofSeconds(60));
    }

    @Async
    public void asyncPutCachedResult(String cacheKey, Page<Chart> chartPage){
        putCachedResult(cacheKey, chartPage);
    }
}
