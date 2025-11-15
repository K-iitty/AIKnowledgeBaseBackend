package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.MindmapResource;

import java.util.List;

public interface MindmapResourceService {
    boolean save(MindmapResource resource);
    void deleteByNodeId(String nodeId);
    List<MindmapResource> findByMindmapId(Long mindmapId);
    List<MindmapResource> findByNodeId(String nodeId);
}