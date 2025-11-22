package com.fanfan.aiknowledgebasebackend.common.config;

import com.fanfan.aiknowledgebasebackend.common.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")  // 对所有 API 接口限流
                .excludePathPatterns(
                    "/api/auth/login",      // 登录接口不限流
                    "/api/auth/register",   // 注册接口不限流
                    "/swagger-ui/**",       // Swagger 不限流
                    "/v3/api-docs/**"       // API 文档不限流
                );
    }
}
