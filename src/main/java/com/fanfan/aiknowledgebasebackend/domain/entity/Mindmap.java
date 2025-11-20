package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mindmaps")
public class Mindmap {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    @TableField("oss_key")
    private String ossKey;
    private String format;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("cover_key")
    private String coverKey;
    private Integer likes;
    private Integer views;
    @TableField("node_count")
    private Integer nodeCount;
    @TableField("content")
    private String content; // 存储思维导图的JSON格式节点数据
    private String status; // 状态: active/archived/deleted
    private String version; // 版本号
}