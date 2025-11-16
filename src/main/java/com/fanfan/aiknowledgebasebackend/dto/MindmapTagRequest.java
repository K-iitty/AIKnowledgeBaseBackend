package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

/**
 * 思维导图标签请求
 */
@Data
public class MindmapTagRequest {
    private String name;
    private String color;
}
