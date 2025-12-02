package com.fanfan.aiknowledgebasebackend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成管理员密码的BCrypt哈希
 */
public class PasswordGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // 生成默认管理员密码：sheep14
        String password = "sheep14";
        String hash = encoder.encode(password);
        
        System.out.println("=================================");
        System.out.println("管理员密码生成工具");
        System.out.println("=================================");
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码哈希: " + hash);
        System.out.println("=================================");
        System.out.println("\n请将上面的密码哈希复制到SQL中：");
        System.out.println("UPDATE admins SET password_hash = '" + hash + "' WHERE username = 'admin';");
        System.out.println("=================================");
    }
}
