package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
}

