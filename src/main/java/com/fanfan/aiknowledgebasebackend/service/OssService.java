package com.fanfan.aiknowledgebasebackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String upload(String dir, MultipartFile file);
    String uploadBytes(String dir, String filename, byte[] data);
    void delete(String objectKey);
    java.io.InputStream get(String objectKey);
    String getPublicUrl(String objectKey);
    String downloadAndUpload(String imageUrl, String dir);
}
