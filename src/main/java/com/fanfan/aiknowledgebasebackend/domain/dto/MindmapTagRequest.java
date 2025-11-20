package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

/**
 * 思维导图标签请求
 */
@Data
public class MindmapTagRequest {
    private String name;
    private String color;
}
