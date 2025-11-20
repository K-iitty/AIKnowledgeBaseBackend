package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.Link;
import com.fanfan.aiknowledgebasebackend.mapper.LinkMapper;
import com.fanfan.aiknowledgebasebackend.service.LinkService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LinkServiceImpl implements LinkService {

    private final LinkMapper linkMapper;

    public LinkServiceImpl(LinkMapper linkMapper) {
        this.linkMapper = linkMapper;
    }

    @Override
    public Link create(Long userId, Long categoryId, String title, String url, String remark, String icon, Integer orderIndex) {
        // 验证URL格式
        if (url == null || !url.matches("^(https?://).+")) {
            throw new RuntimeException("URL格式不正确，必须以http://或https://开头");
        }
        
        Link l = new Link();
        l.setUserId(userId);
        l.setCategoryId(categoryId);
        l.setTitle(title);
        l.setUrl(url);
        l.setRemark(remark);
        l.setIcon(icon);
        l.setOrderIndex(orderIndex != null ? orderIndex : 0);
        l.setCreatedAt(java.time.LocalDateTime.now());
        linkMapper.insert(l);
        return l;
    }
    
    @Override
    public Link update(Long id, Long categoryId, String title, String url, String remark, String icon, Integer orderIndex) {
        Link l = linkMapper.selectById(id);
        if (l == null) {
            throw new RuntimeException("链接不存在");
        }
        
        // 验证URL格式
        if (url != null && !url.matches("^(https?://).+")) {
            throw new RuntimeException("URL格式不正确，必须以http://或https://开头");
        }
        
        if (categoryId != null) l.setCategoryId(categoryId);
        if (title != null) l.setTitle(title);
        if (url != null) l.setUrl(url);
        if (remark != null) l.setRemark(remark);
        if (icon != null) l.setIcon(icon);
        if (orderIndex != null) l.setOrderIndex(orderIndex);
        
        linkMapper.updateById(l);
        return l;
    }

    @Override
    public void delete(Long id) {
        linkMapper.deleteById(id);
    }

    @Override
    public List<Link> list(Long userId, Long categoryId) {
        LambdaQueryWrapper<Link> w = new LambdaQueryWrapper<Link>()
                .eq(Link::getUserId, userId)
                .orderByAsc(Link::getOrderIndex);
        if (categoryId != null) w.eq(Link::getCategoryId, categoryId);
        return linkMapper.selectList(w);
    }
}
