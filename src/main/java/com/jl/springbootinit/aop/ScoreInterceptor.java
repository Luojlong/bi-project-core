package com.jl.springbootinit.aop;

import com.jl.springbootinit.common.ErrorCode;
import com.jl.springbootinit.exception.BusinessException;
import com.jl.springbootinit.model.entity.User;
import com.jl.springbootinit.service.ScoreService;
import com.jl.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class ScoreInterceptor {
    @Resource
    private UserService userService;

    @Resource
    private ScoreService scoreService;

    /**
     * 执行拦截
     */
    @Around("execution(* com.jl.springbootinit.service.impl.OpenaiServiceImpl.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 执行原方法
        Object result;
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        try {
            // 执行原方法
            result = point.proceed();
            // 在方法成功执行后，扣除积分
            System.out.println("扣分");
            scoreService.deductPoints(loginUser.getId(), 1L);
        } catch (Exception e) {
            // 在方法抛出异常时，
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
        }
        return result;
    }
}
