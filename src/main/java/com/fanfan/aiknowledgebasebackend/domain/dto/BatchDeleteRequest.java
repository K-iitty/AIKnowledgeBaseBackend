package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteRequest {
    private List<Long> ids;
}