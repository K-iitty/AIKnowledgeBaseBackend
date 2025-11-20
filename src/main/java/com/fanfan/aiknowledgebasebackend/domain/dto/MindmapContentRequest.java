package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

/**
 * 思维导图内容保存请求
 */
@Data
public class MindmapContentRequest {
    private String content;
    private Integer nodeCount;
}
