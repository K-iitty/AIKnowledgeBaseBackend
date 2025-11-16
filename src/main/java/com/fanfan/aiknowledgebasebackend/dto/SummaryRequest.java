package com.fanfan.aiknowledgebasebackend.dto;

import lombok.Data;

/**
 * 摘要生成请求
 */
@Data
public class SummaryRequest {
    private String content;
}
