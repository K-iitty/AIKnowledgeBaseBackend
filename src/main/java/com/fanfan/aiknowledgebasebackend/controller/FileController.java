package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final OssService ossService;

    public FileController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/upload")
    public String upload(@RequestPart("file") MultipartFile file, @RequestParam(defaultValue = "images") String dir) {
        return ossService.upload(dir, file);
    }

    @PostMapping("/download-and-upload")
    public String downloadAndUpload(@RequestBody Map<String, String> params) {
        String url = params.get("url");
        String dir = params.getOrDefault("dir", "images");
        
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("图片URL不能为空");
        }
        
        return ossService.downloadAndUpload(url, dir);
    }

    @GetMapping("/proxy-pdf")
    public void proxyPdf(@RequestParam String key, jakarta.servlet.http.HttpServletResponse response) {
        try {
            // 从 OSS 获取文件
            java.io.InputStream inputStream = ossService.get(key);
            
            // 设置响应头
            response.setContentType("application/pdf");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            
            // 将文件流写入响应
            java.io.OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            inputStream.close();
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException("PDF 代理失败: " + e.getMessage());
        }
    }
}

