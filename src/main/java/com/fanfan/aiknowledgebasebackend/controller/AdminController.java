package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.domain.dto.AdminLoginRequest;
import com.fanfan.aiknowledgebasebackend.domain.entity.Admin;
import com.fanfan.aiknowledgebasebackend.domain.vo.DashboardStatsVO;
import com.fanfan.aiknowledgebasebackend.service.AdminService;
import com.fanfan.aiknowledgebasebackend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理后台控制器
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理后台", description = "管理员登录、数据统计等")
public class AdminController {
    
    private final AdminService adminService;
    private final DashboardService dashboardService;
    
    public AdminController(AdminService adminService, DashboardService dashboardService) {
        this.adminService = adminService;
        this.dashboardService = dashboardService;
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "使用用户名、密码和验证码登录")
    public Map<String, Object> login(@Valid @RequestBody AdminLoginRequest request, 
                                     HttpServletRequest httpRequest) {
        String token = adminService.login(request.getUsername(), request.getPassword(), 
                                         request.getCaptchaVerification());
        
        // 获取IP地址
        String ip = getClientIp(httpRequest);
        Admin admin = adminService.findByUsername(request.getUsername());
        adminService.updateLastLogin(admin.getId(), ip);
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("admin", admin);
        return result;
    }
    
    /**
     * 获取管理员信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取管理员信息", description = "获取当前登录管理员的详细信息")
    public Admin getProfile(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        String username = principal.getUsername();
        // 移除admin:前缀
        if (username.startsWith("admin:")) {
            username = username.substring(6);
        }
        return adminService.findByUsername(username);
    }
    
    /**
     * 获取仪表板统计数据
     */
    @GetMapping("/dashboard/stats")
    @Operation(summary = "获取统计数据", description = "获取管理后台仪表板的所有统计数据")
    public DashboardStatsVO getStats() {
        return dashboardService.getStatistics();
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
