package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应（包含 Token）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
    private String token;
}
