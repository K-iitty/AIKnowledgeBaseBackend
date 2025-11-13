package com.fanfan.aiknowledgebasebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("notes")
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private String ossKey;
    private String format;
    private String visibility;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String tags;
    private String coverKey;
    private Integer likes;
    private Integer views;
    private Integer wordCount;
}
