package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.entity.MindmapResource;
import com.fanfan.aiknowledgebasebackend.entity.MindmapTag;
import com.fanfan.aiknowledgebasebackend.entity.MindmapTagRelation;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapResourceMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapTagMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapTagRelationMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapService;
import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MindmapServiceImpl implements MindmapService {

    private final MindmapMapper mindmapMapper;
    private final MindmapResourceMapper mindmapResourceMapper;
    private final MindmapTagMapper mindmapTagMapper;
    private final MindmapTagRelationMapper mindmapTagRelationMapper;
    private final OssService ossService;

    public MindmapServiceImpl(MindmapMapper mindmapMapper, 
                              MindmapResourceMapper mindmapResourceMapper,
                              MindmapTagMapper mindmapTagMapper,
                              MindmapTagRelationMapper mindmapTagRelationMapper,
                              OssService ossService) {
        this.mindmapMapper = mindmapMapper;
        this.mindmapResourceMapper = mindmapResourceMapper;
        this.mindmapTagMapper = mindmapTagMapper;
        this.mindmapTagRelationMapper = mindmapTagRelationMapper;
        this.ossService = ossService;
    }

    @Override
    public Mindmap create(Long userId, Long categoryId, String title, String description, String coverKey, String visibility) {
        Mindmap m = new Mindmap();
        m.setUserId(userId);
        m.setCategoryId(categoryId);
        String mindmapTitle = title != null ? title : "思维导图";
        m.setTitle(mindmapTitle);
        m.setDescription(description);
        m.setCoverKey(coverKey);
        m.setOssKey(""); // 设置默认空字符串，避免数据库约束错误
        m.setVisibility(visibility != null ? visibility : "private");
        m.setFormat("xmind");
        m.setLikes(0);
        m.setViews(0);
        m.setNodeCount(3); // 默认三个节点（1个父节点+2个子节点）
        m.setCreatedAt(java.time.LocalDateTime.now());
        m.setUpdatedAt(java.time.LocalDateTime.now());
        
        // 创建默认的思维导图内容（一个父节点和两个子节点），使用标题作为根节点
        String defaultContent = createDefaultMindmapContent(mindmapTitle);
        m.setContent(defaultContent);
        
        mindmapMapper.insert(m);
        return m;
    }

    @Override
    public Mindmap importFile(Long userId, Long categoryId, MultipartFile file, String visibility) {
        String objectKey = ossService.upload("mindmaps", file);
        String title = file.getOriginalFilename();
        if (title == null) title = "思维导图";
        
        // 解析思维导图文件，提取节点数量
        int nodeCount = 0;
        String description = title;
        
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            // 简单的节点计数，实际项目中应该使用专业的思维导图解析库
            nodeCount = countNodes(content);
            
            // 提取描述信息
            if (content.length() > 0) {
                String[] lines = content.split("\n");
                if (lines.length > 0) {
                    description = lines[0].trim();
                    if (description.length() > 100) {
                        description = description.substring(0, 100) + "...";
                    }
                }
            }
        } catch (IOException e) {
            // 如果无法读取内容，使用文件名作为描述
        }
        
        Mindmap m = new Mindmap();
        m.setUserId(userId);
        m.setCategoryId(categoryId);
        m.setTitle(title);
        m.setDescription(description);
        m.setOssKey(objectKey); // 正确设置ossKey
        String format = "xmind";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) {
            format = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
        }
        // 支持mmap和xmind格式
        if ("mmap".equals(format)) {
            m.setFormat("mmap");
        } else {
            m.setFormat("xmind");
        }
        m.setVisibility(visibility);
        m.setNodeCount(nodeCount);
        m.setLikes(0);
        m.setViews(0);
        m.setCreatedAt(LocalDateTime.now());
        m.setUpdatedAt(LocalDateTime.now());
        mindmapMapper.insert(m);
        return m;
    }
    
    private int countNodes(String content) {
        // 简单的节点计数逻辑，实际项目中应该使用专业的思维导图解析库
        int count = 0;
        String[] lines = content.split("\n");
        for (String line : lines) {
            // 计算以特定字符开头的行数作为节点数
            if (line.trim().startsWith("-") || line.trim().startsWith("*") || line.contains(":")) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    @Override
    public void delete(Long id) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m != null) {
            ossService.delete(m.getOssKey());
            mindmapMapper.deleteById(id);
        }
    }

    @Override
    public List<Mindmap> search(Long userId, String keyword, Long categoryId) {
        LambdaQueryWrapper<Mindmap> w = new LambdaQueryWrapper<Mindmap>()
                .eq(Mindmap::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Mindmap::getTitle, keyword).or().like(Mindmap::getDescription, keyword);
        }
        if (categoryId != null) w.eq(Mindmap::getCategoryId, categoryId);
        return mindmapMapper.selectList(w);
    }

    @Override
    public Mindmap update(Long id, String title, String description, String coverKey, String visibility) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m == null) throw new RuntimeException("思维导图不存在");
        if (title != null) m.setTitle(title);
        if (description != null) m.setDescription(description);
        if (coverKey != null) m.setCoverKey(coverKey);
        if (visibility != null) m.setVisibility(visibility);
        m.setUpdatedAt(java.time.LocalDateTime.now());
        mindmapMapper.updateById(m);
        return m;
    }

    @Override
    public void like(Long id) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m == null) throw new RuntimeException("思维导图不存在");
        m.setLikes(m.getLikes() == null ? 1 : m.getLikes() + 1);
        mindmapMapper.updateById(m);
    }

    @Override
    public java.io.InputStream download(Long id) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m == null) throw new RuntimeException("思维导图不存在");
        m.setViews(m.getViews() == null ? 1 : m.getViews() + 1);
        mindmapMapper.updateById(m);
        if (m.getOssKey() == null || m.getOssKey().isEmpty()) {
            throw new RuntimeException("该思维导图暂无文件可下载");
        }
        return ossService.get(m.getOssKey());
    }

    @Override
    public Mindmap getById(Long id) {
        return mindmapMapper.selectById(id);
    }

    @Override
    public void saveContent(Long id, String content, Integer nodeCount) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m == null) throw new RuntimeException("思维导图不存在");
        m.setContent(content);
        m.setNodeCount(nodeCount);
        m.setUpdatedAt(LocalDateTime.now());
        mindmapMapper.updateById(m);
    }

    @Override
    public String getContent(Long id) {
        Mindmap m = mindmapMapper.selectById(id);
        if (m == null) throw new RuntimeException("思维导图不存在");
        return m.getContent();
    }

    @Override
    public String uploadImage(Long userId, Long mindmapId, String nodeId, MultipartFile file) {
        // 验证思维导图是否存在
        Mindmap mindmap = mindmapMapper.selectById(mindmapId);
        if (mindmap == null) throw new RuntimeException("思维导图不存在");

        // 上传文件到OSS
        String objectKey = ossService.upload("mindmaps/" + mindmapId + "/resources", file);
        
        // 保存资源信息到数据库
        MindmapResource resource = new MindmapResource();
        resource.setUserId(userId);
        resource.setMindmapId(mindmapId);
        resource.setNodeId(nodeId);
        resource.setOssKey(objectKey);
        resource.setFileName(file.getOriginalFilename());
        resource.setFileSize(file.getSize());
        resource.setMimeType(file.getContentType());
        resource.setSortOrder(0);
        resource.setCreatedAt(LocalDateTime.now());
        
        mindmapResourceMapper.insert(resource);
        
        return objectKey;
    }

    @Override
    public List<MindmapResource> getImagesByNodeId(Long mindmapId, String nodeId) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getMindmapId, mindmapId)
               .eq(MindmapResource::getNodeId, nodeId);
        return mindmapResourceMapper.selectList(wrapper);
    }

    @Override
    public void deleteImage(Long resourceId) {
        MindmapResource resource = mindmapResourceMapper.selectById(resourceId);
        if (resource == null) throw new RuntimeException("资源不存在");
        
        // 从OSS删除文件
        ossService.delete(resource.getOssKey());
        
        // 从数据库删除记录
        mindmapResourceMapper.deleteById(resourceId);
    }

    @Override
    public MindmapTag createTag(Long userId, String name, String color) {
        MindmapTag tag = new MindmapTag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#1890ff");
        tag.setCreatedAt(LocalDateTime.now());
        mindmapTagMapper.insert(tag);
        return tag;
    }

    @Override
    public List<MindmapTag> getUserTags(Long userId) {
        LambdaQueryWrapper<MindmapTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTag::getUserId, userId);
        return mindmapTagMapper.selectList(wrapper);
    }

    @Override
    public void addTagToMindmap(Long mindmapId, Long tagId) {
        // 检查关联是否已存在
        LambdaQueryWrapper<MindmapTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTagRelation::getMindmapId, mindmapId)
               .eq(MindmapTagRelation::getTagId, tagId);
        
        if (mindmapTagRelationMapper.selectCount(wrapper) > 0) {
            // 关联已存在，无需重复添加
            return;
        }
        
        MindmapTagRelation relation = new MindmapTagRelation();
        relation.setMindmapId(mindmapId);
        relation.setTagId(tagId);
        relation.setCreatedAt(LocalDateTime.now());
        mindmapTagRelationMapper.insert(relation);
    }

    @Override
    public void removeTagFromMindmap(Long mindmapId, Long tagId) {
        LambdaQueryWrapper<MindmapTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTagRelation::getMindmapId, mindmapId)
               .eq(MindmapTagRelation::getTagId, tagId);
        mindmapTagRelationMapper.delete(wrapper);
    }

    @Override
    public List<MindmapTag> getTagsByMindmapId(Long mindmapId) {
        return mindmapTagMapper.selectTagsByMindmapId(mindmapId);
    }

    @Override
    public List<MindmapResource> getResourcesByMindmapId(Long mindmapId) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getMindmapId, mindmapId);
        return mindmapResourceMapper.selectList(wrapper);
    }

    @Override
    public MindmapResource getResourceByOssKey(String ossKey) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getOssKey, ossKey);
        return mindmapResourceMapper.selectOne(wrapper);
    }
    
    @Override
    public void updateNodeNote(Long id, String nodeId, String note) {
        // 获取思维导图内容
        Mindmap mindmap = mindmapMapper.selectById(id);
        if (mindmap == null) {
            throw new RuntimeException("思维导图不存在");
        }
        
        // 获取当前内容
        String contentStr = mindmap.getContent();
        if (contentStr == null || contentStr.isEmpty()) {
            throw new RuntimeException("思维导图内容为空");
        }
        
        try {
            // 解析JSON内容
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> content = mapper.readValue(contentStr, java.util.Map.class);
            
            // 更新指定节点的备注
            updateNodeNoteInTree((java.util.Map<String, Object>) content.get("nodeData"), nodeId, note);
            
            // 保存更新后的内容
            String updatedContent = mapper.writeValueAsString(content);
            mindmap.setContent(updatedContent);
            mindmapMapper.updateById(mindmap);
        } catch (Exception e) {
            throw new RuntimeException("更新节点备注失败: " + e.getMessage());
        }
    }
    
    @Override
    public void updateMindmapData(Long id, String content, Integer nodeCount) {
        Mindmap mindmap = mindmapMapper.selectById(id);
        if (mindmap == null) {
            throw new RuntimeException("思维导图不存在");
        }
        
        mindmap.setContent(content);
        mindmap.setNodeCount(nodeCount);
        mindmap.setUpdatedAt(LocalDateTime.now());
        mindmapMapper.updateById(mindmap);
    }
    
    @Override
    public void addNodeImage(Long id, String nodeId, java.util.Map<String, Object> image) {
        // 获取思维导图内容
        Mindmap mindmap = mindmapMapper.selectById(id);
        if (mindmap == null) {
            throw new RuntimeException("思维导图不存在");
        }
        
        // 获取当前内容
        String contentStr = mindmap.getContent();
        if (contentStr == null || contentStr.isEmpty()) {
            throw new RuntimeException("思维导图内容为空");
        }
        
        try {
            // 解析JSON内容
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> content = mapper.readValue(contentStr, java.util.Map.class);
            
            // 查找节点并添加图片
            addNodeImageInTree((java.util.Map<String, Object>) content.get("nodeData"), nodeId, image);
            
            // 保存更新后的内容
            String updatedContent = mapper.writeValueAsString(content);
            mindmap.setContent(updatedContent);
            mindmapMapper.updateById(mindmap);
        } catch (Exception e) {
            throw new RuntimeException("添加节点图片失败: " + e.getMessage());
        }
    }
    
    @Override
    public java.util.List<java.util.Map<String, Object>> getNodeImages(Long id, String nodeId) {
        // 获取思维导图内容
        Mindmap mindmap = mindmapMapper.selectById(id);
        if (mindmap == null) {
            throw new RuntimeException("思维导图不存在");
        }
        
        // 获取当前内容
        String contentStr = mindmap.getContent();
        if (contentStr == null || contentStr.isEmpty()) {
            throw new RuntimeException("思维导图内容为空");
        }
        
        try {
            // 解析JSON内容
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> content = mapper.readValue(contentStr, java.util.Map.class);
            
            // 查找节点并获取图片
            return getNodeImagesInTree((java.util.Map<String, Object>) content.get("nodeData"), nodeId);
        } catch (Exception e) {
            throw new RuntimeException("获取节点图片失败: " + e.getMessage());
        }
    }
    
    /**
     * 在节点树中递归查找并更新指定节点的备注
     */
    private void updateNodeNoteInTree(java.util.Map<String, Object> node, String targetNodeId, String note) {
        if (node == null) return;
        
        String nodeId = (String) node.get("id");
        if (targetNodeId.equals(nodeId)) {
            node.put("note", note != null ? note : "");
            return;
        }
        
        java.util.List<java.util.Map<String, Object>> children = (java.util.List<java.util.Map<String, Object>>) node.get("children");
        if (children != null) {
            for (java.util.Map<String, Object> child : children) {
                updateNodeNoteInTree(child, targetNodeId, note);
            }
        }
    }
    
    /**
     * 在节点树中递归查找并添加图片到指定节点
     */
    private void addNodeImageInTree(java.util.Map<String, Object> node, String targetNodeId, java.util.Map<String, Object> image) {
        if (node == null) return;
        
        String nodeId = (String) node.get("id");
        if (targetNodeId.equals(nodeId)) {
            // 获取或创建images数组
            java.util.List<java.util.Map<String, Object>> images = (java.util.List<java.util.Map<String, Object>>) node.get("images");
            if (images == null) {
                images = new java.util.ArrayList<>();
                node.put("images", images);
            }
            images.add(image);
            return;
        }
        
        java.util.List<java.util.Map<String, Object>> children = (java.util.List<java.util.Map<String, Object>>) node.get("children");
        if (children != null) {
            for (java.util.Map<String, Object> child : children) {
                addNodeImageInTree(child, targetNodeId, image);
            }
        }
    }
    
    /**
     * 在节点树中递归查找并更新指定节点的图片
     */
    private void updateNodeImagesInTree(java.util.Map<String, Object> node, String targetNodeId, java.util.List<java.util.Map<String, Object>> images) {
        if (node == null) return;
        
        String nodeId = (String) node.get("id");
        if (targetNodeId.equals(nodeId)) {
            node.put("images", images != null ? images : new java.util.ArrayList<>());
            return;
        }
        
        java.util.List<java.util.Map<String, Object>> children = (java.util.List<java.util.Map<String, Object>>) node.get("children");
        if (children != null) {
            for (java.util.Map<String, Object> child : children) {
                updateNodeImagesInTree(child, targetNodeId, images);
            }
        }
    }
    
    /**
     * 在节点树中递归查找并获取指定节点的图片
     */
    private java.util.List<java.util.Map<String, Object>> getNodeImagesInTree(java.util.Map<String, Object> node, String targetNodeId) {
        if (node == null) return new java.util.ArrayList<>();
        
        String nodeId = (String) node.get("id");
        if (targetNodeId.equals(nodeId)) {
            java.util.List<java.util.Map<String, Object>> images = (java.util.List<java.util.Map<String, Object>>) node.get("images");
            return images != null ? images : new java.util.ArrayList<>();
        }
        
        java.util.List<java.util.Map<String, Object>> children = (java.util.List<java.util.Map<String, Object>>) node.get("children");
        if (children != null) {
            for (java.util.Map<String, Object> child : children) {
                java.util.List<java.util.Map<String, Object>> images = getNodeImagesInTree(child, targetNodeId);
                if (images != null && !images.isEmpty()) {
                    return images;
                }
            }
        }
        
        return new java.util.ArrayList<>();
    }
    
    @Override
    public String getPublicUrl(String ossKey) {
        return ossService.getPublicUrl(ossKey);
    }
    
    /**
     * 创建默认的思维导图内容（一个父节点和两个子节点）
     * @param title 思维导图标题，将作为根节点的主题
     */
    private String createDefaultMindmapContent(String title) {
        // 转义标题中的特殊字符
        String escapedTitle = title.replace("\"", "\\\"").replace("\n", "\\n");
        
        return "{\n" +
                "  \"nodeData\": {\n" +
                "    \"id\": \"root\",\n" +
                "    \"topic\": \"" + escapedTitle + "\",\n" +
                "    \"root\": true,\n" +
                "    \"children\": [\n" +
                "      {\n" +
                "        \"id\": \"child1\",\n" +
                "        \"topic\": \"子主题1\",\n" +
                "        \"children\": []\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"child2\",\n" +
                "        \"topic\": \"子主题2\",\n" +
                "        \"children\": []\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"linkData\": {}\n" +
                "}";
    }
}