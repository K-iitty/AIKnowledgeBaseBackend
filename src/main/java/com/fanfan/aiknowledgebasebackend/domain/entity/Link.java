package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("links")
public class Link {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String url;
    private String remark;
    private String icon;
    private Integer orderIndex;
    private java.time.LocalDateTime createdAt;
}
