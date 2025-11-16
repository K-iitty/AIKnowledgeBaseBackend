package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

/**
 * 思维导图创建请求
 */
@Data
public class MindmapCreateRequest {
    private Long categoryId;
    private String title;
    private String description;
    private String coverKey;
    private String visibility;
}
