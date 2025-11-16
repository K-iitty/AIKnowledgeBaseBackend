package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

/**
 * 笔记创建/更新请求
 */
@Data
public class NoteRequest {
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private String visibility;
    private String tags;
    private String coverKey;
}
