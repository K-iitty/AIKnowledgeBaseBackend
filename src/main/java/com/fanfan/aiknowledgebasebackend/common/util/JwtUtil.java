/**
 * JWT工具类
 * 用于生成和解析JWT Token
 * 包括Token的创建、解析、过期检查等功能
 */
package com.fanfan.aiknowledgebasebackend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    /**
     * JWT密钥
     * 从配置文件中读取jwt.secret属性
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token过期时间（毫秒）
     * 从配置文件中读取jwt.expiration属性
     */
    @Value("${jwt.expiration}")
    private long expirationMillis;

    /**
     * 获取签名密钥
     * 将字符串密钥转换为Key对象
     * @return Key 密钥对象
     */
    private Key getKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成基础Token
     * 仅包含用户名信息
     * @param username 用户名
     * @return String JWT Token字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * 生成包含用户详细信息的Token
     * 包含用户名、用户ID和角色信息
     * @param username 用户名
     * @param userId 用户ID
     * @param role 用户角色
     * @return String JWT Token字符串
     */
    public String generateToken(String username, Long userId, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析Token获取声明信息
     * @param token JWT Token字符串
     * @return Claims 声明信息对象
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 从Token中获取用户ID
     * @param token JWT Token字符串
     * @return Long 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从Token中获取用户角色
     * @param token JWT Token字符串
     * @return String 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 检查Token是否过期
     * @param token JWT Token字符串
     * @return boolean true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().before(new Date());
    }
}