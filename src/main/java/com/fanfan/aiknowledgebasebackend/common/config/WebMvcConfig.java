/**
 * Web MVC配置类
 * 用于配置Spring MVC相关组件
 * 主要用于注册拦截器和其他Web相关配置
 */
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

    /**
     * 添加拦截器到注册表
     * 配置拦截器的应用路径和排除路径
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                // 对所有以/api/开头的接口进行限流
                .addPathPatterns("/api/**")  
                // 排除不需要限流的路径
                .excludePathPatterns(
                    "/api/auth/login",      // 登录接口不限流
                    "/api/auth/register",   // 注册接口不限流
                    "/swagger-ui/**",       // Swagger 不限流
                    "/v3/api-docs/**"       // API 文档不限流
                );
    }
}