package com.fanfan.aiknowledgebasebackend.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

    @Override
    public String getPublicUrl(String objectKey) {
        // 生成阿里云OSS公开访问URL
        // 格式: https://{bucket-name}.{endpoint}/{objectKey}
        return "https://" + bucket + ".oss-cn-beijing.aliyuncs.com/" + objectKey;
    }

    @Override
    public String downloadAndUpload(String imageUrl, String dir) {
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        
        try {
            // 1. 下载图片
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            // 设置 User-Agent 避免某些网站拒绝请求
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.connect();
            
            // 检查响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("下载图片失败，HTTP状态码: " + responseCode);
            }
            
            inputStream = connection.getInputStream();
            
            // 2. 获取文件扩展名
            String ext = "";
            String contentType = connection.getContentType();
            
            // 从 URL 获取扩展名
            if (imageUrl.contains(".")) {
                String urlExt = imageUrl.substring(imageUrl.lastIndexOf('.'));
                // 只取扩展名部分，去掉可能的查询参数
                if (urlExt.contains("?")) {
                    urlExt = urlExt.substring(0, urlExt.indexOf("?"));
                }
                if (urlExt.matches("\\.(jpg|jpeg|png|gif|webp|bmp|svg)")) {
                    ext = urlExt;
                }
            }
            
            // 如果从 URL 无法获取，从 Content-Type 推断
            if (ext.isEmpty() && contentType != null) {
                if (contentType.contains("jpeg") || contentType.contains("jpg")) {
                    ext = ".jpg";
                } else if (contentType.contains("png")) {
                    ext = ".png";
                } else if (contentType.contains("gif")) {
                    ext = ".gif";
                } else if (contentType.contains("webp")) {
                    ext = ".webp";
                } else if (contentType.contains("bmp")) {
                    ext = ".bmp";
                } else if (contentType.contains("svg")) {
                    ext = ".svg";
                }
            }
            
            // 默认使用 .jpg
            if (ext.isEmpty()) {
                ext = ".jpg";
            }
            
            // 3. 生成 OSS 对象键
            String objectKey = dir + "/" + UUID.randomUUID() + ext;
            
            // 4. 上传到 OSS
            PutObjectRequest request = new PutObjectRequest(bucket, objectKey, inputStream);
            oss.putObject(request);
            
            return objectKey;
            
        } catch (IOException e) {
            throw new RuntimeException("下载或上传图片失败: " + e.getMessage(), e);
        } finally {
            // 关闭资源
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // 忽略关闭异常
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
