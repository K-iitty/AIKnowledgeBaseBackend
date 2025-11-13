package com.fanfan.aiknowledgebasebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("profile_items")
public class ProfileItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long profileId;
    private String type;
    private String title;
    private String content;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private Integer orderIndex;
}
