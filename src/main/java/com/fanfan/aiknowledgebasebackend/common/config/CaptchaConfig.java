/**
 * AJ-Captcha 验证码配置类
 * 用于配置滑动拼图验证码的相关参数
 * 包括验证码类型、干扰选项、缓存方式等配置
 */
package com.fanfan.aiknowledgebasebackend.common.config;

import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * AJ-Captcha 验证码配置
 */
@Configuration
public class CaptchaConfig {

    /**
     * 创建验证码服务实例
     * 配置验证码的各种参数，包括类型、水印、偏移量等
     * @return CaptchaService 验证码服务实例
     */
    @Bean
    public CaptchaService captchaService() {
        Properties config = new Properties();
        // 注意：这些配置项的 key 必须完全匹配 AJ-Captcha 的要求
        // 验证码类型：blockPuzzle表示滑动拼图验证码
        config.setProperty("captcha.type", "blockPuzzle");
        // 水印设置：空字符串表示不添加水印
        config.setProperty("captcha.water.mark", "");
        // 滑动容错偏移量：像素级别，5表示允许5像素的误差
        config.setProperty("captcha.slip.offset", "5");
        // AES加密状态：false表示不启用AES加密
        config.setProperty("captcha.aes.status", "false");
        // 干扰选项：2表示中等干扰程度
        config.setProperty("captcha.interference.options", "2");
        // 缓存类型：使用redis作为缓存存储
        config.setProperty("captcha.cache.type", "redis");
        // 定时水印：空字符串表示不使用定时水印
        config.setProperty("captcha.timing.waterMark", "");
        // 默认验证码类型：blockPuzzle滑动拼图
        config.setProperty("captcha.type.default", "blockPuzzle");
        // 字体类型：空字符串表示使用默认字体
        config.setProperty("captcha.font.type", "");
        // 字体样式：1表示常规样式
        config.setProperty("captcha.font.style", "1");
        
        System.out.println("========== Captcha Config ==========");
        config.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("====================================");
        
        return CaptchaServiceFactory.getInstance(config);
    }
}