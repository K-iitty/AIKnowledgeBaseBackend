package com.fanfan.aiknowledgebasebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mindmap_resources")
public class MindmapResource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long mindmapId;
    private String nodeId;
    private String ossKey;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}