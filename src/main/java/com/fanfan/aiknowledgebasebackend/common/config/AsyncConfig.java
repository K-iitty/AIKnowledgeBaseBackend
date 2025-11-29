/**
 * 异步任务配置类
 * 用于配置应用程序中的异步任务执行器
 * 主要包括RAG索引重建等耗时操作的线程池配置
 */
package com.fanfan.aiknowledgebasebackend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 用于 RAG 索引重建等耗时操作
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * RAG 索引重建专用线程池
     * 配置参数根据CPU核心数动态调整，以优化性能
     * @return Executor 线程池执行器
     */
    @Bean(name = "ragIndexExecutor")
    public Executor ragIndexExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：CPU 核心数
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        
        // 最大线程数：CPU 核心数 * 2
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        
        // 队列容量：100 个任务，用于缓冲等待执行的任务
        executor.setQueueCapacity(100);
        
        // 线程名称前缀，便于识别和调试
        executor.setThreadNamePrefix("rag-index-");
        
        // 拒绝策略：由调用线程执行，当线程池无法处理更多任务时的备用方案
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池，确保不丢失任务
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间：60秒，关闭线程池前等待任务完成的时间
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 通用异步任务线程池
     * 用于处理一般的异步任务
     * @return Executor 线程池执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：5个线程常驻
        executor.setCorePoolSize(5);
        // 最大线程数：最多扩展到10个线程
        executor.setMaxPoolSize(10);
        // 队列容量：200个任务
        executor.setQueueCapacity(200);
        // 线程名称前缀
        executor.setThreadNamePrefix("async-task-");
        // 拒绝策略：由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间：60秒
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}