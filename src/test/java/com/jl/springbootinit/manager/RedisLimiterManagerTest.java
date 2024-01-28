package com.jl.springbootinit.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimit() throws InterruptedException {
        String userid = "1";
        for (int i=0; i< 2; i++){
            redisLimiterManager.doRateLimit(userid);
            System.out.println("succeed");
        }
        Thread.sleep(1000);
        for (int i = 0;i<5;i++){
            redisLimiterManager.doRateLimit(userid);
            System.out.println("succeed");
        }
    }
}