package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.Link;

import java.util.List;

public interface LinkService {
    Link create(Long userId, Long categoryId, String title, String url, String remark, String icon, Integer orderIndex);
    Link update(Long id, Long categoryId, String title, String url, String remark, String icon, Integer orderIndex);
    void delete(Long id);
    List<Link> list(Long userId, Long categoryId);
}
