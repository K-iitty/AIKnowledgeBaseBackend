package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.Mindmap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MindmapService {
    Mindmap create(Long userId, Long categoryId, String title, String description, String coverKey, String visibility);
    Mindmap importFile(Long userId, Long categoryId, MultipartFile file, String visibility);
    void delete(Long id);
    List<Mindmap> search(Long userId, String keyword, Long categoryId);
    Mindmap update(Long id, String title, String description, String coverKey, String visibility);
    void like(Long id);
    java.io.InputStream download(Long id);
    Mindmap getById(Long id);
}
