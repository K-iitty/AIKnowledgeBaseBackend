package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.Admin;

/**
 * 管理员服务接口
 */
public interface AdminService {
    
    /**
     * 根据用户名查找管理员
     */
    Admin findByUsername(String username);
    
    /**
     * 管理员登录
     */
    String login(String username, String password, String captchaVerification);
    
    /**
     * 更新最后登录信息
     */
    void updateLastLogin(Long adminId, String ip);
}
