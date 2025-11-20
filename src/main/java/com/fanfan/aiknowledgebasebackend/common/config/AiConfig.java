package com.fanfan.aiknowledgebasebackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String model;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.8}")
    private Double temperature;

    public String getApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("${")) {
            throw new IllegalStateException("DashScope API Key 未配置或无效");
        }
        return apiKey;
    }

    public String getModel() {
        return model;
    }

    public Double getTemperature() {
        return temperature;
    }
}