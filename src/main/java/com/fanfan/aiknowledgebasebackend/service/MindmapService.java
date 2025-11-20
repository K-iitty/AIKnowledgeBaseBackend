package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapResource;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapTag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MindmapService {
    Mindmap create(Long userId, Long categoryId, String title, String description, String coverKey, String visibility);
    Mindmap importFile(Long userId, Long categoryId, MultipartFile file, String visibility);
    Mindmap importFromJson(Long userId, Long categoryId, String title, String content, Integer nodeCount, String visibility);
    void delete(Long id);
    List<Mindmap> search(Long userId, String keyword, Long categoryId);
    Mindmap update(Long id, String title, String description, String coverKey, String visibility);
    void like(Long id);
    java.io.InputStream download(Long id);
    Mindmap getById(Long id);
    
    // 新增方法：保存思维导图内容
    void saveContent(Long id, String content, Integer nodeCount);
    
    // 新增方法：获取思维导图内容
    String getContent(Long id);
    
    // 新增方法：上传图片资源
    String uploadImage(Long userId, Long mindmapId, String nodeId, MultipartFile file);
    
    // 新增方法：获取节点图片资源
    List<MindmapResource> getImagesByNodeId(Long mindmapId, String nodeId);
    
    // 新增方法：删除图片资源
    void deleteImage(Long resourceId);
    
    // 新增方法：创建标签
    MindmapTag createTag(Long userId, String name, String color);
    
    // 新增方法：获取用户所有标签
    List<MindmapTag> getUserTags(Long userId);
    
    // 新增方法：为思维导图添加标签
    void addTagToMindmap(Long mindmapId, Long tagId);
    
    // 新增方法：移除思维导图标签
    void removeTagFromMindmap(Long mindmapId, Long tagId);
    
    // 新增方法：获取思维导图所有标签
    List<MindmapTag> getTagsByMindmapId(Long mindmapId);
    
    // 新增方法：获取思维导图所有资源
    List<MindmapResource> getResourcesByMindmapId(Long mindmapId);
    
    // 新增方法：根据OSS Key获取资源
    MindmapResource getResourceByOssKey(String ossKey);
    
    // 新增方法：更新节点备注
    void updateNodeNote(Long id, String nodeId, String note);
    
    // 新增方法：更新整个思维导图数据
    void updateMindmapData(Long id, String content, Integer nodeCount);
    
    // 新增方法：为节点添加图片
    void addNodeImage(Long id, String nodeId, java.util.Map<String, Object> image);
    
    // 新增方法：获取节点图片
    java.util.List<java.util.Map<String, Object>> getNodeImages(Long id, String nodeId);
    
    // 新增方法：获取OSS公开URL
    String getPublicUrl(String ossKey);
}