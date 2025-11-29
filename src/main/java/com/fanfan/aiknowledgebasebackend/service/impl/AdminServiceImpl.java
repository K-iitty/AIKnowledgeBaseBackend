package com.fanfan.aiknowledgebasebackend.service.impl;

import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.common.util.JwtUtil;
import com.fanfan.aiknowledgebasebackend.domain.entity.Admin;
import com.fanfan.aiknowledgebasebackend.mapper.AdminMapper;
import com.fanfan.aiknowledgebasebackend.service.AdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 管理员服务实现
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;
    
    public AdminServiceImpl(AdminMapper adminMapper, PasswordEncoder passwordEncoder, 
                           JwtUtil jwtUtil, CaptchaService captchaService) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.captchaService = captchaService;
    }
    
    @Override
    public Admin findByUsername(String username) {
        return adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username));
    }
    
    @Override
    public String login(String username, String password, String captchaVerification) {
        // 验证验证码
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaVerification(captchaVerification);
        ResponseModel response = captchaService.verification(captchaVO);
        if (!response.isSuccess()) {
            throw new RuntimeException("验证码校验失败");
        }
        
        // 查询管理员
        Admin admin = findByUsername(username);
        if (admin == null || !passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 更新最后登录时间
        admin.setLastLoginAt(LocalDateTime.now());
        adminMapper.updateById(admin);
        
        // 生成JWT Token（添加admin:前缀区分管理员）
        return jwtUtil.generateToken("admin:" + username, admin.getId(), admin.getRole());
    }
    
    @Override
    public void updateLastLogin(Long adminId, String ip) {
        Admin admin = adminMapper.selectById(adminId);
        if (admin != null) {
            admin.setLastLoginAt(LocalDateTime.now());
            admin.setLastLoginIp(ip);
            adminMapper.updateById(admin);
        }
    }
}
