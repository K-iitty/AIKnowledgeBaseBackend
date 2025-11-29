/**
 * Spring Security配置类
 * 用于配置应用程序的安全策略
 * 包括认证、授权、密码加密、JWT过滤器等安全相关配置
 */
package com.fanfan.aiknowledgebasebackend.common.config;

import com.fanfan.aiknowledgebasebackend.common.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 配置安全过滤器链
     * 定义哪些路径需要认证，哪些路径可以匿名访问
     * @param http HttpSecurity对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保护，因为使用JWT Token方式进行认证
                .csrf(csrf -> csrf.disable())
                // 设置会话管理策略为无状态，不创建HttpSession
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 允许匿名访问的路径
                        .requestMatchers(
                                "/api/auth/**",          // 认证相关接口
                                "/api/ai/**",            // AI相关接口
                                "/api/files/proxy-pdf",  // PDF代理接口
                                "/swagger-ui.html",      // Swagger UI页面
                                "/swagger-ui/**",        // Swagger UI资源
                                "/v3/api-docs/**"        // API文档资源
                        ).permitAll()
                        // 其他所有请求都需要认证
                        .anyRequest().authenticated()
                )
                // 添加JWT认证过滤器，在用户名密码认证过滤器之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 禁用 X-Frame-Options 以允许跨端口 iframe 嵌入（用于 PDF 显示）
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                );
        return http.build();
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法对密码进行加密
     * @return PasswordEncoder 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置认证管理器
     * 用于处理认证请求
     * @param configuration 认证配置
     * @return AuthenticationManager 认证管理器
     * @throws Exception 异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}