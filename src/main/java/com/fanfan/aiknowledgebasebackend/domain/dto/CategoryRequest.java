package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
    private String description;
    private String visibility;
    private String badgeText;
    private String backgroundStyle;
}