package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 个人资料项请求
 */
@Data
public class ProfileItemRequest {
    private Long profileId;
    private String type;
    private String title;
    private String content;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer orderIndex;
}
