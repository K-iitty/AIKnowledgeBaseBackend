package com.fanfan.aiknowledgebasebackend.service.impl;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.User;
import com.fanfan.aiknowledgebasebackend.mapper.UserMapper;
import com.fanfan.aiknowledgebasebackend.service.UserCacheService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import com.fanfan.aiknowledgebasebackend.common.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    private final UserCacheService userCacheService;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, 
                          CaptchaService captchaService, UserCacheService userCacheService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.captchaService = captchaService;
        this.userCacheService = userCacheService;
    }

    @Override
    public User register(String username, String rawPassword, String nickname, String email, String phone) {
        if (rawPassword == null || !rawPassword.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            throw new RuntimeException("密码至少6位且需包含英文和数字");
        }
        User exist = findByUsername(username);
        if (exist != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 验证邮箱格式
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new RuntimeException("邮箱格式不正确");
        }
        
        // 验证手机号格式
        if (phone != null && !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }
        
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setNickname(nickname);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole("USER");
        u.setStatus(1);
        u.setCreatedAt(LocalDateTime.now());
        u.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(u);
        return u;
    }

    @Override
    public String login(String username, String rawPassword, String captchaVerification) {
        // 验证验证码
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if (!response.isSuccess()) {
            throw new RuntimeException("验证码校验失败");
        }
        
        User user = findByUsername(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("账号或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被停用");
        }
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);
        
        // 登录成功后，将用户信息写入缓存
        userCacheService.updateUserCache(user);
        
        return jwtUtil.generateToken(username, user.getId(), user.getRole());
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }
}
