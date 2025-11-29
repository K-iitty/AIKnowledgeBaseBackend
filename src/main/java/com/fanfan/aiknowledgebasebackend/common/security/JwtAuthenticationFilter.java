/**
 * JWT认证过滤器
 * 用于拦截请求并验证JWT Token的有效性
 * 如果Token有效，则构建认证信息并放入安全上下文
 * 继承OncePerRequestFilter确保每个请求只过滤一次
 */
package com.fanfan.aiknowledgebasebackend.common.security;

import com.fanfan.aiknowledgebasebackend.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 过滤请求，验证JWT Token
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取Authorization字段
        String authHeader = request.getHeader("Authorization");
        // 检查是否有Bearer Token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // 提取Token（去掉"Bearer "前缀）
            String token = authHeader.substring(7);
            try {
                // 检查Token是否过期
                if (jwtUtil.isTokenExpired(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token已过期");
                    return;
                }
                
                // 解析Token获取声明信息
                Claims claims = jwtUtil.parseToken(token);
                String username = claims.getSubject();
                String role = jwtUtil.getRoleFromToken(token);
                
                // 如果用户名存在且当前没有认证信息，则构建认证对象
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                    // 如果角色信息存在，则添加角色权限
                    if (role != null) {
                        authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    
                    // 创建用户主体对象
                    User principal = new User(username, "", authorities);
                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 将认证信息放入安全上下文
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // Token验证失败处理
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token验证失败");
                return;
            }
        }
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}