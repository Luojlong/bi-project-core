package com.jl.springbootinit.controller;

import com.jl.springbootinit.common.BaseResponse;
import com.jl.springbootinit.common.ResultUtils;
import com.jl.springbootinit.model.entity.User;
import com.jl.springbootinit.service.ScoreService;
import com.jl.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/score")
@Slf4j
public class ScoreController {
    @Resource
    private UserService userService;

    @Resource
    private ScoreService scoreService;

    /**
     * 用于签到时添加积分
     * @param request
     * @return
     */
    @PostMapping("/checkIn")
    public BaseResponse<String> checkIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        scoreService.checkIn(loginUser.getId());
        return ResultUtils.success("签到成功");
    }

    /**
     * 查询积分
     *
     * @param request
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Long> getUserById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long totalPoints = scoreService.getUserPoints(loginUser.getId());
        return ResultUtils.success(totalPoints);
    }

    /**
     * 查询签到状态
     *
     * @param request
     * @return
     */
    @GetMapping("/getSign")
    public BaseResponse<Integer> getSignById(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        int isSign = scoreService.getIsSign(loginUser.getId());
        return ResultUtils.success(isSign);
    }
}


