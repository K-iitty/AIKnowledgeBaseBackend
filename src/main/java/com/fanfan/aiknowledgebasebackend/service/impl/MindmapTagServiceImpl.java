package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fanfan.aiknowledgebasebackend.entity.MindmapTag;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapTagMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapTagService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MindmapTagServiceImpl extends ServiceImpl<MindmapTagMapper, MindmapTag> implements MindmapTagService {
    private final MindmapTagMapper mindmapTagMapper;

    public MindmapTagServiceImpl(MindmapTagMapper mindmapTagMapper) {
        this.mindmapTagMapper = mindmapTagMapper;
    }

    @Override
    public List<MindmapTag> listByUserId(Long userId) {
        return mindmapTagMapper.selectList(new LambdaQueryWrapper<MindmapTag>()
                .eq(MindmapTag::getUserId, userId));
    }

    @Override
    public MindmapTag create(Long userId, String name, String color) {
        MindmapTag tag = new MindmapTag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#1890ff");
        tag.setCreatedAt(LocalDateTime.now());
        mindmapTagMapper.insert(tag);
        return tag;
    }

    @Override
    public MindmapTag getByNameAndUserId(String name, Long userId) {
        LambdaQueryWrapper<MindmapTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindmapTag::getName, name)
               .eq(MindmapTag::getUserId, userId);
        return mindmapTagMapper.selectOne(wrapper);
    }

    @Override
    public void deleteById(Long id) {
        mindmapTagMapper.deleteById(id);
    }

    @Override
    public void create(MindmapTag tag) {
        mindmapTagMapper.insert(tag);
    }
}