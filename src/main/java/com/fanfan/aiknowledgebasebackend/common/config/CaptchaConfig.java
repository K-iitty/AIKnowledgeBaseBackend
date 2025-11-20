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

    @Bean
    public CaptchaService captchaService() {
        Properties config = new Properties();
        // 注意：这些配置项的 key 必须完全匹配 AJ-Captcha 的要求
        config.setProperty("captcha.type", "blockPuzzle");
        config.setProperty("captcha.water.mark", "");
        config.setProperty("captcha.slip.offset", "5");
        config.setProperty("captcha.aes.status", "false");
        config.setProperty("captcha.interference.options", "2");
        config.setProperty("captcha.cache.type", "redis");
        config.setProperty("captcha.timing.waterMark", "");
        config.setProperty("captcha.type.default", "blockPuzzle");
        config.setProperty("captcha.font.type", "");
        config.setProperty("captcha.font.style", "1");
        
        System.out.println("========== Captcha Config ==========");
        config.forEach((key, value) -> System.out.println(key + " = " + value));
        System.out.println("====================================");
        
        return CaptchaServiceFactory.getInstance(config);
    }
}
