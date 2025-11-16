package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

/**
 * 链接创建/更新请求
 */
@Data
public class LinkRequest {
    private Long categoryId;
    private String title;
    private String url;
    private String remark;
    private String icon;
    private Integer orderIndex;
}
