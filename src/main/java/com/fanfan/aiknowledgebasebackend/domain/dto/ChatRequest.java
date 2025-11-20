package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 聊天请求
 */
@Data
public class ChatRequest {
    private List<Map<String, String>> messages;
}
