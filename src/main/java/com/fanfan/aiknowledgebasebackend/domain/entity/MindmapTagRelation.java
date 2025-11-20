package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mindmap_tag_relations")
public class MindmapTagRelation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long mindmapId;
    private Long tagId;
    private LocalDateTime createdAt;
}