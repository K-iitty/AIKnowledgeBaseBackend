package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.MindmapTag;

import java.util.List;

public interface MindmapTagService {
    void create(MindmapTag tag);
    List<MindmapTag> listByUserId(Long userId);
    MindmapTag getByNameAndUserId(String name, Long userId);
    void deleteById(Long id);
    MindmapTag create(Long userId, String name, String color);
}