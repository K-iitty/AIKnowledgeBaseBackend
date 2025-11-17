package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.User;

public interface UserService {
    User register(String username, String rawPassword, String nickname, String email, String phone);
    String login(String username, String rawPassword, String captchaVerification);
    User findByUsername(String username);
}

