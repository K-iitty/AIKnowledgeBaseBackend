package com.fanfan.aiknowledgebasebackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("profiles")
public class Profile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String contact;
    private String bio;
    private String avatarKey;
    private String location;
    private String jobTitle;
    private String website;
}
