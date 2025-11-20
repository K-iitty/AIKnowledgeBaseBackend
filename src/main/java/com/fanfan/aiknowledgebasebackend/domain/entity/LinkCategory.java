package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("link_categories")
public class LinkCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private Long parentId;
    private Integer sortOrder;
    private String icon;
}
