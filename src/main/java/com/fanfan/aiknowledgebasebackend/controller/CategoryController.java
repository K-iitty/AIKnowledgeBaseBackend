package com.fanfan.aiknowledgebasebackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.dto.BatchDeleteRequest;
import com.fanfan.aiknowledgebasebackend.domain.dto.CategoryRequest;
import com.fanfan.aiknowledgebasebackend.domain.dto.CatReq;
import com.fanfan.aiknowledgebasebackend.domain.entity.LinkCategory;
import com.fanfan.aiknowledgebasebackend.domain.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapCategory;
import com.fanfan.aiknowledgebasebackend.domain.entity.Note;
import com.fanfan.aiknowledgebasebackend.domain.entity.NoteCategory;
import com.fanfan.aiknowledgebasebackend.domain.entity.User;
import com.fanfan.aiknowledgebasebackend.mapper.LinkCategoryMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapCategoryMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapMapper;
import com.fanfan.aiknowledgebasebackend.mapper.NoteCategoryMapper;
import com.fanfan.aiknowledgebasebackend.mapper.NoteMapper;
import com.fanfan.aiknowledgebasebackend.service.MindmapService;
import com.fanfan.aiknowledgebasebackend.service.NoteService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final NoteCategoryMapper noteCategoryMapper;
    private final MindmapCategoryMapper mindmapCategoryMapper;
    private final LinkCategoryMapper linkCategoryMapper;
    private final UserService userService;
    private final NoteService noteService;
    private final MindmapService mindmapService;
    private final NoteMapper noteMapper;
    private final MindmapMapper mindmapMapper;

    public CategoryController(NoteCategoryMapper noteCategoryMapper, MindmapCategoryMapper mindmapCategoryMapper, LinkCategoryMapper linkCategoryMapper, UserService userService, NoteService noteService, MindmapService mindmapService, NoteMapper noteMapper, MindmapMapper mindmapMapper) {
        this.noteCategoryMapper = noteCategoryMapper;
        this.mindmapCategoryMapper = mindmapCategoryMapper;
        this.linkCategoryMapper = linkCategoryMapper;
        this.userService = userService;
        this.noteService = noteService;
        this.mindmapService = mindmapService;
        this.noteMapper = noteMapper;
        this.mindmapMapper = mindmapMapper;
    }

    @GetMapping("/notes")
    public List<NoteCategory> listNotes(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return noteCategoryMapper.selectList(new LambdaQueryWrapper<NoteCategory>()
                .eq(NoteCategory::getUserId, u.getId())
                .orderByAsc(NoteCategory::getSortOrder));
    }
    
    @GetMapping("/notes/{id}")
    public NoteCategory getNoteCategory(@PathVariable Long id, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        NoteCategory category = noteCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        return category;
    }
    
    @PostMapping("/notes")
    public NoteCategory createNoteCategory(@RequestBody CategoryRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        NoteCategory cat = new NoteCategory();
        cat.setUserId(u.getId());
        cat.setName(req.getName());
        cat.setParentId(req.getParentId());
        cat.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        cat.setIcon(req.getIcon());
        cat.setDescription(req.getDescription());
        cat.setVisibility(req.getVisibility() != null ? req.getVisibility() : "private");
        cat.setBadgeText(req.getBadgeText());
        cat.setBackgroundStyle(req.getBackgroundStyle());
        cat.setCreatedAt(java.time.LocalDateTime.now());
        cat.setUpdatedAt(java.time.LocalDateTime.now());
        noteCategoryMapper.insert(cat);
        return cat;
    }
    
    @PutMapping("/notes/{id}")
    public NoteCategory updateNoteCategory(@PathVariable Long id, @RequestBody CategoryRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        NoteCategory existing = noteCategoryMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getParentId() != null) existing.setParentId(req.getParentId());
        if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
        if (req.getIcon() != null) existing.setIcon(req.getIcon());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getVisibility() != null) existing.setVisibility(req.getVisibility());
        if (req.getBadgeText() != null) existing.setBadgeText(req.getBadgeText());
        if (req.getBackgroundStyle() != null) existing.setBackgroundStyle(req.getBackgroundStyle());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        noteCategoryMapper.updateById(existing);
        return existing;
    }
    
    @GetMapping("/notes/tree")
    public List<NoteCategory> listNoteTree(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        List<NoteCategory> allCategories = noteCategoryMapper.selectList(new LambdaQueryWrapper<NoteCategory>()
                .eq(NoteCategory::getUserId, u.getId())
                .orderByAsc(NoteCategory::getSortOrder));
        
        // 构建树形结构
        return buildTree(allCategories);
    }
    
    private List<NoteCategory> buildTree(List<NoteCategory> categories) {
        Map<Long, NoteCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(NoteCategory::getId, c -> c));
        
        // 设置子分类
        for (NoteCategory category : categories) {
            if (category.getParentId() != null && categoryMap.containsKey(category.getParentId())) {
                NoteCategory parent = categoryMap.get(category.getParentId());
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(category);
            }
        }
        
        // 返回根分类（没有父分类的分类）
        return categories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/notes/{id}")
    public void delNote(@PathVariable Long id, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        NoteCategory c = noteCategoryMapper.selectById(id);
        if (c == null || !c.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        
        // 删除分类下的所有笔记
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>().eq(Note::getCategoryId, id));
        for (Note note : notes) {
            noteService.deleteNote(note.getId());
        }
        
        // 删除分类
        noteCategoryMapper.deleteById(id);
    }
    
    // 批量删除笔记分类
    @DeleteMapping("/notes/batch")
    public void batchDeleteNotes(@RequestBody BatchDeleteRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        for (Long id : req.getIds()) {
            NoteCategory c = noteCategoryMapper.selectById(id);
            if (c != null && c.getUserId().equals(u.getId())) {
                // 删除分类下的所有笔记
                List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>().eq(Note::getCategoryId, id));
                for (Note note : notes) {
                    noteService.deleteNote(note.getId());
                }
                
                // 删除分类
                noteCategoryMapper.deleteById(id);
            }
        }
    }

    @GetMapping("/mindmaps")
    public List<MindmapCategory> listMindmaps(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return mindmapCategoryMapper.selectList(new LambdaQueryWrapper<MindmapCategory>()
                .eq(MindmapCategory::getUserId, u.getId())
                .orderByAsc(MindmapCategory::getSortOrder));
    }
    
    @GetMapping("/mindmaps/{id}")
    public MindmapCategory getMindmapCategory(@PathVariable Long id, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        MindmapCategory category = mindmapCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        return category;
    }
    
    @PostMapping("/mindmaps")
    public MindmapCategory createMindmapCategory(@RequestBody CategoryRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        MindmapCategory cat = new MindmapCategory();
        cat.setUserId(u.getId());
        cat.setName(req.getName());
        cat.setParentId(req.getParentId());
        cat.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        cat.setIcon(req.getIcon());
        cat.setDescription(req.getDescription());
        cat.setVisibility(req.getVisibility() != null ? req.getVisibility() : "private");
        cat.setBadgeText(req.getBadgeText());
        cat.setBackgroundStyle(req.getBackgroundStyle());
        cat.setCreatedAt(java.time.LocalDateTime.now());
        cat.setUpdatedAt(java.time.LocalDateTime.now());
        mindmapCategoryMapper.insert(cat);
        return cat;
    }
    
    @PutMapping("/mindmaps/{id}")
    public MindmapCategory updateMindmapCategory(@PathVariable Long id, @RequestBody CategoryRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        MindmapCategory existing = mindmapCategoryMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getParentId() != null) existing.setParentId(req.getParentId());
        if (req.getSortOrder() != null) existing.setSortOrder(req.getSortOrder());
        if (req.getIcon() != null) existing.setIcon(req.getIcon());
        if (req.getDescription() != null) existing.setDescription(req.getDescription());
        if (req.getVisibility() != null) existing.setVisibility(req.getVisibility());
        if (req.getBadgeText() != null) existing.setBadgeText(req.getBadgeText());
        if (req.getBackgroundStyle() != null) existing.setBackgroundStyle(req.getBackgroundStyle());
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        
        mindmapCategoryMapper.updateById(existing);
        return existing;
    }

    @DeleteMapping("/mindmaps/{id}")
    public void delMindmap(@PathVariable Long id, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        MindmapCategory c = mindmapCategoryMapper.selectById(id);
        if (c == null || !c.getUserId().equals(u.getId())) {
            throw new RuntimeException("分类不存在或无权限访问");
        }
        
        // 删除分类下的所有思维导图
        List<Mindmap> mindmaps = mindmapMapper.selectList(new LambdaQueryWrapper<Mindmap>().eq(Mindmap::getCategoryId, id));
        for (Mindmap mindmap : mindmaps) {
            mindmapService.delete(mindmap.getId());
        }
        
        // 删除分类
        mindmapCategoryMapper.deleteById(id);
    }
    
    // 批量删除思维导图分类
    @DeleteMapping("/mindmaps/batch")
    public void batchDeleteMindmaps(@RequestBody BatchDeleteRequest req, @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        for (Long id : req.getIds()) {
            MindmapCategory c = mindmapCategoryMapper.selectById(id);
            if (c != null && c.getUserId().equals(u.getId())) {
                // 删除分类下的所有思维导图
                List<Mindmap> mindmaps = mindmapMapper.selectList(new LambdaQueryWrapper<Mindmap>().eq(Mindmap::getCategoryId, id));
                for (Mindmap mindmap : mindmaps) {
                    mindmapService.delete(mindmap.getId());
                }
                
                // 删除分类
                mindmapCategoryMapper.deleteById(id);
            }
        }
    }

    @GetMapping("/links")
    public List<LinkCategory> listLinks(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return linkCategoryMapper.selectList(new LambdaQueryWrapper<LinkCategory>().eq(LinkCategory::getUserId, u.getId()));
    }

    @PostMapping("/links")
    public LinkCategory addLink(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestBody CatReq req) {
        User u = userService.findByUsername(principal.getUsername());
        LinkCategory c = new LinkCategory();
        c.setUserId(u.getId());
        c.setName(req.getName());
        c.setParentId(req.getParentId());
        linkCategoryMapper.insert(c);
        return c;
    }

    @DeleteMapping("/links/{id}")
    public void delLink(@PathVariable Long id) {
        linkCategoryMapper.deleteById(id);
    }

    @PutMapping("/links/{id}")
    public LinkCategory updateLink(@PathVariable Long id, @RequestBody CatReq req) {
        LinkCategory c = linkCategoryMapper.selectById(id);
        if (c == null) throw new RuntimeException("分类不存在");
        c.setName(req.getName());
        linkCategoryMapper.updateById(c);
        return c;
    }
}