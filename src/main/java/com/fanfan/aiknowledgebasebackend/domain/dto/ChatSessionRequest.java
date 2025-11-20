package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

/**
 * 会话聊天请求
 */
@Data
public class ChatSessionRequest {
    private Long sessionId;
    private String question;
    private String mode; // default | local
    private Long categoryId;
}
