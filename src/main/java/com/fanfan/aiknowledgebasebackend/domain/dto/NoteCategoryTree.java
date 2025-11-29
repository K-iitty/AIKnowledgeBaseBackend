package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteCategoryTree {
    private Long id;
    private Long userId;
    private String name;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer sortOrder;
    private String icon;
    private String coverKey;
    private String description;
    private String visibility;
    private Integer itemCount;
    private String backgroundStyle;
    private String badgeText;
    
    // 树形结构字段
    private List<NoteCategoryTree> children;
}
