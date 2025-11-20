package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapResource;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapResourceMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapResourceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MindmapResourceServiceImpl extends ServiceImpl<MindmapResourceMapper, MindmapResource> implements MindmapResourceService {
    
    private final MindmapResourceMapper mindmapResourceMapper;

    public MindmapResourceServiceImpl(MindmapResourceMapper mindmapResourceMapper) {
        this.mindmapResourceMapper = mindmapResourceMapper;
    }

    @Override
    public boolean save(MindmapResource resource) {
        return super.save(resource);
    }

    @Override
    public void deleteByNodeId(String nodeId) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getNodeId, nodeId);
        mindmapResourceMapper.delete(wrapper);
    }

    @Override
    public List<MindmapResource> findByMindmapId(Long mindmapId) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getMindmapId, mindmapId);
        return mindmapResourceMapper.selectList(wrapper);
    }

    @Override
    public List<MindmapResource> findByNodeId(String nodeId) {
        LambdaQueryWrapper<MindmapResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapResource::getNodeId, nodeId);
        return mindmapResourceMapper.selectList(wrapper);
    }
}