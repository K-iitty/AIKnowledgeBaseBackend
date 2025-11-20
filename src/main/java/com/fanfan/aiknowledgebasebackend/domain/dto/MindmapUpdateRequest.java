package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

/**
 * 思维导图更新请求
 */
@Data
public class MindmapUpdateRequest {
    private String title;
    private String description;
    private String coverKey;
    private String visibility;
}
