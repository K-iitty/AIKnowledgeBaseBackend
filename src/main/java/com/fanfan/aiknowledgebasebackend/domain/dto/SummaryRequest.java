package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;

/**
 * 摘要生成请求
 */
@Data
public class SummaryRequest {
    private String content;
}
