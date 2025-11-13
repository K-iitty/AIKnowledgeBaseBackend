package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapService;
import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MindmapServiceImpl implements MindmapService {

    private final MindmapMapper mindmapMapper;
    private final OssService ossService;

    public MindmapServiceImpl(MindmapMapper mindmapMapper, OssService ossService) {
        this.mindmapMapper = mindmapMapper;
        this.ossService = ossService;
    }

    @Override
    public Mindmap create(Long userId, Long categoryId, String title, String description, String coverKey, String visibility) {
        Mindmap m = new Mindmap();
        m.setUserId(userId);
        m.setCategoryId(categoryId);
        m.setTitle(title != null ? title : "思维导图");
        m.setDescription(description);
        m.setCoverKey(coverKey);
        m.setVisibility(visibility != null ? visibility : "private");
        m.setFormat("xmind");
        m.setLikes(0);
        m.setViews(0);
        m.setNodeCount(0);
        m.setCreatedAt(java.time.LocalDateTime.now());
        m.setUpdatedAt(java.time.LocalDateTime.now());
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
        m.setOssKey(objectKey);
        String format = "xmind";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) format = name.substring(name.lastIndexOf('.') + 1);
        m.setFormat(format);
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
}
