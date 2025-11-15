package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.entity.MindmapResource;
import com.fanfan.aiknowledgebasebackend.entity.MindmapTag;
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

    // 新增：保存思维导图内容
    @PostMapping("/{id}/content")
    public void saveContent(@PathVariable Long id, @RequestBody SaveContentReq req) {
        mindmapService.saveContent(id, req.getContent(), req.getNodeCount());
    }

    // 新增：更新节点备注
    @PutMapping("/{id}/node/{nodeId}/note")
    public void updateNodeNote(@PathVariable Long id, 
                               @PathVariable String nodeId,
                               @RequestBody UpdateNodeNoteReq req) {
        mindmapService.updateNodeNote(id, nodeId, req.getNote());
    }
    
    // 新增：更新整个思维导图数据（包括节点结构、备注、图片等）
    @PutMapping("/{id}/data")
    public void updateMindmapData(@PathVariable Long id,
                                  @RequestBody UpdateMindmapDataReq req) {
        mindmapService.updateMindmapData(id, req.getContent(), req.getNodeCount());
    }

    // 新增：获取思维导图内容
    @GetMapping("/{id}/content")
    public String getContent(@PathVariable Long id) {
        return mindmapService.getContent(id);
    }

    // 新增：上传图片
    @PostMapping("/{id}/images")
    public UploadImageResp uploadImage(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable Long id,
            @RequestParam String nodeId,
            @RequestParam("file") MultipartFile file) {
        User u = userService.findByUsername(principal.getUsername());
        String ossKey = mindmapService.uploadImage(u.getId(), id, nodeId, file);
        UploadImageResp resp = new UploadImageResp();
        resp.setOssKey(ossKey);
        resp.setUrl(mindmapService.getPublicUrl(ossKey));
        return resp;
    }

    // 新增：获取节点图片
    @GetMapping("/{id}/nodes/{nodeId}/images")
    public List<MindmapResource> getImagesByNodeId(@PathVariable Long id, @PathVariable String nodeId) {
        return mindmapService.getImagesByNodeId(id, nodeId);
    }

    // 新增：删除图片
    @DeleteMapping("/images/{resourceId}")
    public void deleteImage(@PathVariable Long resourceId) {
        mindmapService.deleteImage(resourceId);
    }

    // 新增：创建标签
    @PostMapping("/tags")
    public MindmapTag createTag(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestBody CreateTagReq req) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapService.createTag(u.getId(), req.getName(), req.getColor());
    }

    // 新增：获取用户所有标签
    @GetMapping("/tags")
    public List<MindmapTag> getUserTags(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapService.getUserTags(u.getId());
    }

    // 新增：为思维导图添加标签
    @PostMapping("/{id}/tags/{tagId}")
    public void addTagToMindmap(@PathVariable Long id, @PathVariable Long tagId) {
        mindmapService.addTagToMindmap(id, tagId);
    }

    // 新增：移除思维导图标签
    @DeleteMapping("/{id}/tags/{tagId}")
    public void removeTagFromMindmap(@PathVariable Long id, @PathVariable Long tagId) {
        mindmapService.removeTagFromMindmap(id, tagId);
    }

    // 新增：获取思维导图所有标签
    @GetMapping("/{id}/tags")
    public List<MindmapTag> getTagsByMindmapId(@PathVariable Long id) {
        return mindmapService.getTagsByMindmapId(id);
    }

    // 新增：获取思维导图所有资源
    @GetMapping("/{id}/resources")
    public List<MindmapResource> getResourcesByMindmapId(@PathVariable Long id) {
        return mindmapService.getResourcesByMindmapId(id);
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
    
    @Data
    public static class SaveContentReq {
        private String content;
        private Integer nodeCount;
    }
    
    @Data
    public static class UploadImageResp {
        private String ossKey;
        private String url;
    }
    
    @Data
    public static class CreateTagReq {
        private String name;
        private String color;
    }
    
    @Data
    public static class UpdateNodeNoteReq {
        private String note;
    }
    
    @Data
    public static class UpdateMindmapDataReq {
        private String content;
        private Integer nodeCount;
    }
    
    @Data
    public static class AddNodeImageReq {
        private java.util.Map<String, Object> image;
    }
    
    // 新增：更新节点图片
    @PostMapping("/{id}/node/{nodeId}/images")
    public void addNodeImage(@PathVariable Long id,
                             @PathVariable String nodeId,
                             @RequestBody AddNodeImageReq req) {
        mindmapService.addNodeImage(id, nodeId, req.getImage());
    }
    
    // 新增：获取节点图片
    @GetMapping("/{id}/node/{nodeId}/images")
    public java.util.List<java.util.Map<String, Object>> getNodeImages(@PathVariable Long id,
                                                                       @PathVariable String nodeId) {
        return mindmapService.getNodeImages(id, nodeId);
    }
}