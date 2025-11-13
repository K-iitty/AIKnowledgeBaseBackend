package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.entity.User;
import com.fanfan.aiknowledgebasebackend.service.MindmapService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

import java.util.List;

@RestController
@RequestMapping("/api/mindmaps")
public class MindmapController {

    private final MindmapService mindmapService;
    private final UserService userService;

    public MindmapController(MindmapService mindmapService, UserService userService) {
        this.mindmapService = mindmapService;
        this.userService = userService;
    }

    @PostMapping
    public Mindmap create(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                          @RequestBody CreateReq req) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapService.create(u.getId(), req.getCategoryId(), req.getTitle(), req.getDescription(), req.getCoverKey(), req.getVisibility());
    }

    @PostMapping("/import")
    public Mindmap importFile(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestParam Long categoryId, @RequestParam String visibility, @RequestPart("file") MultipartFile file) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapService.importFile(u.getId(), categoryId, file, visibility);
    }

    @GetMapping("/list")
    public List<Mindmap> list(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapService.search(u.getId(), keyword, categoryId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        mindmapService.delete(id);
    }

    @PutMapping("/{id}")
    public Mindmap update(@PathVariable Long id, @RequestBody UpdateReq req) {
        return mindmapService.update(id, req.getTitle(), req.getDescription(), req.getCoverKey(), req.getVisibility());
    }

    @GetMapping("/{id}/download")
    public org.springframework.http.ResponseEntity<byte[]> download(@PathVariable Long id) throws java.io.IOException {
        java.io.InputStream is = mindmapService.download(id);
        byte[] data = is.readAllBytes();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mindmap.xmind")
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    

    @PostMapping("/{id}/like")
    public void like(@PathVariable Long id) {
        mindmapService.like(id);
    }

    @Data
    public static class UpdateReq {
        private String title;
        private String description;
        private String coverKey;
        private String visibility;
    }
    @Data
    public static class CreateReq {
        private Long categoryId;
        private String title;
        private String description;
        private String coverKey;
        private String visibility;
    }
}
