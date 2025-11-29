/**
 * 接口限流注解
 * 使用Redis实现分布式限流
 */
package com.fanfan.aiknowledgebasebackend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解
 * 用于标记需要限流的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    
    /**
     * 限流key前缀
     */
    String key() default "ratelimit:api";
    
    /**
     * 时间窗口（秒）
     */
    long window() default 60;
    
    /**
     * 最大请求次数
     */
    long maxCount() default 100;
    
    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，请稍后再试";
}
