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
}

