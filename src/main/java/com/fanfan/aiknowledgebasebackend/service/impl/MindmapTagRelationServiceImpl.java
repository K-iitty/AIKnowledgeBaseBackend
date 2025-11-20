package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapTagRelation;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapTagRelationMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapTagRelationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MindmapTagRelationServiceImpl extends ServiceImpl<MindmapTagRelationMapper, MindmapTagRelation> implements MindmapTagRelationService {
    private final MindmapTagRelationMapper mindmapTagRelationMapper;

    public MindmapTagRelationServiceImpl(MindmapTagRelationMapper mindmapTagRelationMapper) {
        this.mindmapTagRelationMapper = mindmapTagRelationMapper;
    }

    @Override
    public void addRelation(Long mindmapId, Long tagId) {
        // 检查关联是否已存在
        LambdaQueryWrapper<MindmapTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTagRelation::getMindmapId, mindmapId)
               .eq(MindmapTagRelation::getTagId, tagId);
        
        if (mindmapTagRelationMapper.selectCount(wrapper) == 0) {
            // 只有当关联不存在时才添加
            MindmapTagRelation relation = new MindmapTagRelation();
            relation.setMindmapId(mindmapId);
            relation.setTagId(tagId);
            relation.setCreatedAt(LocalDateTime.now());
            mindmapTagRelationMapper.insert(relation);
        }
    }

    @Override
    public void deleteRelation(Long mindmapId, Long tagId) {
        LambdaQueryWrapper<MindmapTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTagRelation::getMindmapId, mindmapId)
               .eq(MindmapTagRelation::getTagId, tagId);
        mindmapTagRelationMapper.delete(wrapper);
    }

    @Override
    public List<MindmapTagRelation> getRelationsByMindmapId(Long mindmapId) {
        return mindmapTagRelationMapper.selectList(
                new LambdaQueryWrapper<MindmapTagRelation>()
                        .eq(MindmapTagRelation::getMindmapId, mindmapId)
        );
    }

    @Override
    public List<MindmapTagRelation> getRelationsByTagId(Long tagId) {
        return mindmapTagRelationMapper.selectList(
                new LambdaQueryWrapper<MindmapTagRelation>()
                        .eq(MindmapTagRelation::getTagId, tagId)
        );
    }
}