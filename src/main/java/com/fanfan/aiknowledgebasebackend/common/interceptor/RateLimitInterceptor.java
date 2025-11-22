package com.fanfan.aiknowledgebasebackend.common.interceptor;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API 限流拦截器
 * 使用 Guava RateLimiter 实现 QPS 限制
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // 全局限流器：每秒 1000 个请求（提高阈值，避免误伤）
    private final RateLimiter globalRateLimiter = RateLimiter.create(1000.0);
    
    // AI 接口限流器：每秒 50 个请求（提高阈值）
    private final RateLimiter aiRateLimiter = RateLimiter.create(50.0);
    
    // 用户级限流器：每个用户每秒 100 个请求（提高阈值）
    private final ConcurrentHashMap<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String userId = getUserIdFromRequest(request);
        
        // 1. 全局限流检查
        if (!globalRateLimiter.tryAcquire()) {
            sendRateLimitError(response, "系统繁忙，请稍后重试");
            return false;
        }
        
        // 2. AI 接口特殊限流
        if (uri.startsWith("/api/ai/")) {
            if (!aiRateLimiter.tryAcquire()) {
                sendRateLimitError(response, "AI 服务请求过于频繁，请稍后重试");
                return false;
            }
        }
        
        // 3. 用户级限流（如果已登录）
        if (userId != null && !userId.isEmpty()) {
            RateLimiter userLimiter = userRateLimiters.computeIfAbsent(
                userId, 
                k -> RateLimiter.create(100.0)  // 每个用户每秒 100 个请求
            );
            
            if (!userLimiter.tryAcquire()) {
                sendRateLimitError(response, "您的请求过于频繁，请稍后重试");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 从请求中获取用户ID
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        // 从 JWT token 中提取用户名作为用户ID
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                // 这里简化处理，实际应该解析 JWT 获取用户ID
                // 为了避免循环依赖，我们使用 token 的 hash 作为标识
                return String.valueOf(token.hashCode());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 发送限流错误响应
     */
    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
            "{\"code\": 429, \"message\": \"%s\"}", 
            message
        ));
    }
}
