/**
 * 阿里云OSS配置类
 * 用于配置阿里云对象存储服务的相关参数
 * 包括终端节点、访问密钥等信息
 */
package com.fanfan.aiknowledgebasebackend.common.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    /**
     * 阿里云OSS终端节点
     * 从配置文件中读取aliyun.oss.endpoint属性
     */
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    /**
     * 阿里云访问密钥ID
     * 从配置文件中读取aliyun.oss.access-key-id属性
     */
    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    /**
     * 阿里云访问密钥Secret
     * 从配置文件中读取aliyun.oss.access-key-secret属性
     */
    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    /**
     * 创建OSS客户端实例
     * 使用配置的凭证信息初始化OSS客户端
     * @return OSS 阿里云OSS客户端实例
     */
    @Bean(destroyMethod = "shutdown")
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}