/**
 * AI配置类
 * 用于配置DashScope API相关参数
 * 包括API密钥、模型名称和温度参数
 */
package com.fanfan.aiknowledgebasebackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * DashScope API密钥
     * 从配置文件中读取spring.ai.dashscope.api-key属性
     */
    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    /**
     * 使用的模型名称
     * 默认为qwen3-max模型
     * 可在配置文件中通过spring.ai.dashscope.chat.options.model属性修改
     */
    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String model;

    /**
     * 温度参数，控制生成文本的随机性
     * 值越高结果越随机，值越低结果越确定
     * 默认值为0.8
     */
    @Value("${spring.ai.dashscope.chat.options.temperature:0.8}")
    private Double temperature;

    /**
     * 获取API密钥
     * @return API密钥字符串
     * @throws IllegalStateException 当API密钥未配置或无效时抛出异常
     */
    public String getApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("${")) {
            throw new IllegalStateException("DashScope API Key 未配置或无效");
        }
        return apiKey;
    }

    /**
     * 获取模型名称
     * @return 模型名称字符串
     */
    public String getModel() {
        return model;
    }

    /**
     * 获取温度参数
     * @return 温度参数值
     */
    public Double getTemperature() {
        return temperature;
    }
}