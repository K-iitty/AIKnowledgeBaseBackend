package com.fanfan.aiknowledgebasebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fanfan.aiknowledgebasebackend.domain.entity.*;
import com.fanfan.aiknowledgebasebackend.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台 - 内容管理控制器
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理后台-内容管理", description = "用户、笔记、思维导图、链接管理")
public class AdminManagementController {
    
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final MindmapMapper mindmapMapper;
    private final LinkMapper linkMapper;
    private final NoteCategoryMapper noteCategoryMapper;
    private final MindmapCategoryMapper mindmapCategoryMapper;
    private final LinkCategoryMapper linkCategoryMapper;
    
    public AdminManagementController(UserMapper userMapper, NoteMapper noteMapper,
                                    MindmapMapper mindmapMapper, LinkMapper linkMapper,
                                    NoteCategoryMapper noteCategoryMapper,
                                    MindmapCategoryMapper mindmapCategoryMapper,
                                    LinkCategoryMapper linkCategoryMapper) {
        this.userMapper = userMapper;
        this.noteMapper = noteMapper;
        this.mindmapMapper = mindmapMapper;
        this.linkMapper = linkMapper;
        this.noteCategoryMapper = noteCategoryMapper;
        this.mindmapCategoryMapper = mindmapCategoryMapper;
        this.linkCategoryMapper = linkCategoryMapper;
    }
    
    // ==================== 用户管理 ====================
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持搜索")
    public IPage<User> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {
        
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(email)) {
            wrapper.like(User::getEmail, email);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(pageParam, wrapper);
    }
    
    /**
     * 更新用户状态
     */
    @PutMapping("/users/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户")
    public Map<String, Object> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(body.get("status"));
        userMapper.updateById(user);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "状态更新成功");
        return result;
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @Operation(summary = "删除用户", description = "删除指定用户及其所有数据")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        
        // 可以选择级联删除用户的笔记、思维导图等
        // noteMapper.delete(new LambdaQueryWrapper<Note>().eq(Note::getUserId, id));
        // mindmapMapper.delete(new LambdaQueryWrapper<Mindmap>().eq(Mindmap::getUserId, id));
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
    
    // ==================== 笔记管理 ====================
    
    /**
     * 获取笔记列表
     */
    @GetMapping("/notes")
    @Operation(summary = "获取笔记列表", description = "分页查询笔记列表，支持搜索")
    public Map<String, Object> getNoteList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String visibility) {
        
        Page<Note> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        
        // 排除content字段，避免查询大字段导致内存问题
        wrapper.select(Note.class, info -> !info.getColumn().equals("content"));
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Note::getTitle, title);
        }
        if (categoryId != null) {
            wrapper.eq(Note::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(visibility)) {
            wrapper.eq(Note::getVisibility, visibility);
        }
        
        wrapper.orderByDesc(Note::getCreatedAt);
        IPage<Note> result = noteMapper.selectPage(pageParam, wrapper);
        
        // 填充分类名称和用户名
        result.getRecords().forEach(note -> {
            if (note.getCategoryId() != null) {
                NoteCategory category = noteCategoryMapper.selectById(note.getCategoryId());
                if (category != null) {
                    note.setCategoryName(category.getName());
                }
            }
            if (note.getUserId() != null) {
                User user = userMapper.selectById(note.getUserId());
                if (user != null) {
                    note.setUsername(user.getUsername());
                }
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        return response;
    }
    
    /**
     * 删除笔记
     */
    @DeleteMapping("/notes/{id}")
    @Operation(summary = "删除笔记", description = "删除指定笔记")
    public Map<String, Object> deleteNote(@PathVariable Long id) {
        noteMapper.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
    
    // ==================== 思维导图管理 ====================
    
    /**
     * 获取思维导图列表
     */
    @GetMapping("/mindmaps")
    @Operation(summary = "获取思维导图列表", description = "分页查询思维导图列表，支持搜索")
    public Map<String, Object> getMindmapList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String visibility) {
        
        Page<Mindmap> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Mindmap> wrapper = new LambdaQueryWrapper<>();
        
        // 排除content字段，避免查询大字段导致内存问题
        wrapper.select(Mindmap.class, info -> !info.getColumn().equals("content"));
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Mindmap::getTitle, title);
        }
        if (categoryId != null) {
            wrapper.eq(Mindmap::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(visibility)) {
            wrapper.eq(Mindmap::getVisibility, visibility);
        }
        
        wrapper.orderByDesc(Mindmap::getCreatedAt);
        IPage<Mindmap> result = mindmapMapper.selectPage(pageParam, wrapper);
        
        // 填充分类名称和用户名
        result.getRecords().forEach(mindmap -> {
            if (mindmap.getCategoryId() != null) {
                MindmapCategory category = mindmapCategoryMapper.selectById(mindmap.getCategoryId());
                if (category != null) {
                    mindmap.setCategoryName(category.getName());
                }
            }
            if (mindmap.getUserId() != null) {
                User user = userMapper.selectById(mindmap.getUserId());
                if (user != null) {
                    mindmap.setUsername(user.getUsername());
                }
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        return response;
    }
    
    /**
     * 删除思维导图
     */
    @DeleteMapping("/mindmaps/{id}")
    @Operation(summary = "删除思维导图", description = "删除指定思维导图")
    public Map<String, Object> deleteMindmap(@PathVariable Long id) {
        mindmapMapper.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
    
    // ==================== 链接管理 ====================
    
    /**
     * 获取链接列表
     */
    @GetMapping("/links")
    @Operation(summary = "获取链接列表", description = "分页查询链接列表，支持搜索")
    public Map<String, Object> getLinkList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Long categoryId) {
        
        Page<Link> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Link> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Link::getTitle, title);
        }
        if (StringUtils.hasText(url)) {
            wrapper.like(Link::getUrl, url);
        }
        if (categoryId != null) {
            wrapper.eq(Link::getCategoryId, categoryId);
        }
        
        wrapper.orderByDesc(Link::getCreatedAt);
        IPage<Link> result = linkMapper.selectPage(pageParam, wrapper);
        
        // 填充分类名称和用户名
        result.getRecords().forEach(link -> {
            if (link.getCategoryId() != null) {
                LinkCategory category = linkCategoryMapper.selectById(link.getCategoryId());
                if (category != null) {
                    link.setCategoryName(category.getName());
                }
            }
            if (link.getUserId() != null) {
                User user = userMapper.selectById(link.getUserId());
                if (user != null) {
                    link.setUsername(user.getUsername());
                }
            }
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        return response;
    }
    
    /**
     * 删除链接
     */
    @DeleteMapping("/links/{id}")
    @Operation(summary = "删除链接", description = "删除指定链接")
    public Map<String, Object> deleteLink(@PathVariable Long id) {
        linkMapper.deleteById(id);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
}
