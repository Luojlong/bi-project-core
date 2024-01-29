package com.jl.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jl.springbootinit.model.entity.Score;

public interface ScoreService extends IService<Score> {
    /**
     * 签到
     * @param userId
     * @return
     */
    void checkIn(Long userId);

    /**
     * 消耗积分
     * @param userId
     * @param points 积分数
     * @return
     */
    void deductPoints(Long userId, Long points);

    /**
     *获取积分
     * @param userId
     * @return
     */
    Long getUserPoints(Long userId);
}
