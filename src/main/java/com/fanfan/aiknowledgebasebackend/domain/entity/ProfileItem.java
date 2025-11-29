package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("profile_items")
public class ProfileItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long profileId;
    private String type;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer orderIndex;
}
