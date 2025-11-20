package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

@Data
public class MindmapImportRequest {
    private Long categoryId;
    private String title;
    private String content;  // JSON格式的思维导图数据
    private Integer nodeCount;
    private String visibility;
}
