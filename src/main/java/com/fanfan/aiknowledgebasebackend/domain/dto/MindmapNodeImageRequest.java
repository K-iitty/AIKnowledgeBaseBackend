package com.fanfan.aiknowledgebasebackend.domain.dto;

import lombok.Data;
import java.util.Map;

/**
 * 思维导图节点图片请求
 */
@Data
public class MindmapNodeImageRequest {
    private Map<String, Object> image;
}
