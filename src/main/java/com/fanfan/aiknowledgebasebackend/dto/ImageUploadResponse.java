package com.fanfan.aiknowledgebasebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片上传响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    private String ossKey;
    private String url;
}
