//package com.yupi.springbootinit.aop;
//
//import com.yupi.springbootinit.service.UserService;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.springframework.web.context.request.RequestAttributes;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
//public class ScoreInterceptor {
//    @Resource
//    private UserService userService;
//
//    @Resource
//    private ScoreService scoreService;
//
//    /**
//     * 执行拦截
//     */
//    @Around("execution(* com.sing.init.manager.AiManager.*(..))")
//    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
//        // 执行原方法
//        Object result;
//        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
//        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
//        // 当前登录用户
//        User loginUser = userService.getLoginUser(request);
//        try {
//            // 执行原方法
//            result = point.proceed();
//            // 在方法成功执行后，扣除积分
//            scoreService.deductPoints(loginUser.getId(), 5L);
//        } catch (Exception e) {
//            // 在方法抛出异常时，
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成好像出问题了呦");
//        }
//        // 通过权限校验，放行
//        return result;
//    }
//}
