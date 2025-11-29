package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    
    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String categoryName;
    
    @TableField(exist = false)
    private String username;
}
