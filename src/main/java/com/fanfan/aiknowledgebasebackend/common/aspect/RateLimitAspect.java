/**
 * 限流切面
 * 使用Redis实现接口限流，防止接口被滥用
 */
package com.fanfan.aiknowledgebasebackend.common.aspect;

import com.fanfan.aiknowledgebasebackend.common.annotation.RateLimit;
import com.fanfan.aiknowledgebasebackend.common.util.RedisUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 * 拦截带有@RateLimit注解的方法，实现限流功能
 */
@Aspect
@Component
public class RateLimitAspect {

    private final RedisUtil redisUtil;

    public RateLimitAspect(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Around("@annotation(com.fanfan.aiknowledgebasebackend.common.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 获取当前用户ID
        String userId = getCurrentUserId();
        if (userId == null) {
            userId = "anonymous";
        }

        // 构建限流Key
        String key = rateLimit.key() + ":userId:" + userId + ":" + method.getName();
        
        // 获取当前计数
        Object countObj = redisUtil.get(key);
        long count = countObj == null ? 0 : Long.parseLong(countObj.toString());

        if (count >= rateLimit.maxCount()) {
            // 超过限流阈值
            System.out.println("[限流拦截] 用户: " + userId + ", 接口: " + method.getName() + 
                             ", 当前请求数: " + count + ", 限制: " + rateLimit.maxCount());
            throw new RuntimeException(rateLimit.message());
        }

        // 增加计数
        Long newCount = redisUtil.increment(key, 1);
        
        // 如果是第一次请求，设置过期时间
        if (newCount == 1) {
            redisUtil.expire(key, rateLimit.window(), TimeUnit.SECONDS);
        }

        System.out.println("[限流检查] 用户: " + userId + ", 接口: " + method.getName() + 
                         ", 当前请求数: " + newCount + "/" + rateLimit.maxCount());

        // 继续执行方法
        return joinPoint.proceed();
    }

    /**
     * 获取当前登录用户ID
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.User) {
                    return ((org.springframework.security.core.userdetails.User) principal).getUsername();
                }
            }
        } catch (Exception e) {
            // 获取用户信息失败，返回null
        }
        return null;
    }
}
