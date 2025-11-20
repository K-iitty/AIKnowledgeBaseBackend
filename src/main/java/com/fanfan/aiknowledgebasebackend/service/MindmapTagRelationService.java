package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapTagRelation;

import java.util.List;

public interface MindmapTagRelationService {
    void addRelation(Long mindmapId, Long tagId);
    void deleteRelation(Long mindmapId, Long tagId);
    List<MindmapTagRelation> getRelationsByMindmapId(Long mindmapId);
    List<MindmapTagRelation> getRelationsByTagId(Long tagId);
}