package com.fanfan.aiknowledgebasebackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    private final OSS oss;

    @Value("${aliyun.oss.bucket-name}")
    private String bucket;

    public OssServiceImpl(OSS oss) {
        this.oss = oss;
    }

    @Override
    public String upload(String dir, MultipartFile file) {
        String ext = "";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.'));
        }
        String objectKey = dir + "/" + UUID.randomUUID() + ext;
        try {
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file.getInputStream());
            oss.putObject(request);
            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("上传失败");
        }
    }

    @Override
    public String uploadBytes(String dir, String filename, byte[] data) {
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf('.'));
        }
        String objectKey = dir + "/" + UUID.randomUUID() + ext;
        PutObjectRequest request = new PutObjectRequest(bucket, objectKey, new java.io.ByteArrayInputStream(data));
        oss.putObject(request);
        return objectKey;
    }

    @Override
    public void delete(String objectKey) {
        oss.deleteObject(bucket, objectKey);
    }

    @Override
    public java.io.InputStream get(String objectKey) {
        return oss.getObject(bucket, objectKey).getObjectContent();
    }
}
