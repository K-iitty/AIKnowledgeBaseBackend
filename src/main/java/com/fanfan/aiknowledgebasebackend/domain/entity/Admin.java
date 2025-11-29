package com.fanfan.aiknowledgebasebackend.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员实体类
 */
@Data
@TableName("admins")
public class Admin {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String passwordHash;
    
    private String nickname;
    
    private String email;
    
    private String phone;
    
    private String avatarUrl;
    
    /**
     * 角色：super_admin-超级管理员, admin-普通管理员
     */
    private String role;
    
    /**
     * 状态：0-禁用, 1-启用
     */
    private Integer status;
    
    private LocalDateTime lastLoginAt;
    
    private String lastLoginIp;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
