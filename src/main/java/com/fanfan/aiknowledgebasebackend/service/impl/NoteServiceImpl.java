package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.Note;
import com.fanfan.aiknowledgebasebackend.mapper.NoteMapper;
import com.fanfan.aiknowledgebasebackend.service.NoteCacheService;
import com.fanfan.aiknowledgebasebackend.service.NoteService;
import com.fanfan.aiknowledgebasebackend.service.OssService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final OssService ossService;
    private final NoteCacheService noteCacheService;

    public NoteServiceImpl(NoteMapper noteMapper, OssService ossService, NoteCacheService noteCacheService) {
        this.noteMapper = noteMapper;
        this.ossService = ossService;
        this.noteCacheService = noteCacheService;
    }

    @Override
    public Note createFromContent(Long userId, Long categoryId, String title, String description, String content, String visibility, String tags, String coverKey) {
        String objectKey = ossService.uploadBytes("notes", title + ".md", content.getBytes(StandardCharsets.UTF_8));
        Note n = new Note();
        n.setUserId(userId);
        n.setCategoryId(categoryId);
        n.setTitle(title);
        n.setDescription(description);
        n.setOssKey(objectKey);
        n.setFormat("md");
        n.setVisibility(visibility);
        n.setTags(tags);
        n.setCoverKey(coverKey);
        n.setLikes(0);
        n.setViews(0);
        n.setWordCount(content == null ? 0 : content.length());
        n.setContent(content); // 保存原始内容
        n.setCreatedAt(LocalDateTime.now());
        n.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(n);
        // 清除用户笔记列表缓存
        noteCacheService.deleteUserNoteListCache(userId);
        return n;
    }

    @Override
    public Note importFile(Long userId, Long categoryId, MultipartFile file, String visibility) {
        String objectKey = ossService.upload("notes", file);
        String title = file.getOriginalFilename();
        if (title == null) title = "笔记";
        
        // 解析文件内容
        String description = title;
        String content = "";
        int wordCount = 0;
        
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
            wordCount = content.length();
            // 提取第一行作为描述
            String[] lines = content.split("\n");
            if (lines.length > 0 && !lines[0].trim().isEmpty()) {
                description = lines[0].trim();
                if (description.length() > 100) {
                    description = description.substring(0, 100) + "...";
                }
            }
        } catch (IOException e) {
            // 如果无法读取内容，使用文件名作为描述
        }
        
        Note n = new Note();
        n.setUserId(userId);
        n.setCategoryId(categoryId);
        n.setTitle(title);
        n.setDescription(description);
        n.setOssKey(objectKey);
        n.setContent(content); // 保存原始内容
        String format = "md";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) format = name.substring(name.lastIndexOf('.') + 1);
        n.setFormat(format);
        n.setVisibility(visibility);
        n.setWordCount(wordCount);
        n.setLikes(0);
        n.setViews(0);
        n.setCreatedAt(LocalDateTime.now());
        n.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(n);
        // 清除用户笔记列表缓存
        noteCacheService.deleteUserNoteListCache(userId);
        return n;
    }

    @Override
    public void deleteNote(Long id) {
        Note n = noteMapper.selectById(id);
        if (n != null) {
            ossService.delete(n.getOssKey());
            noteMapper.deleteById(id);
            // 删除笔记相关缓存
            noteCacheService.deleteNoteCache(id, n.getUserId());
        }
    }

    @Override
    public List<Note> search(Long userId, String keyword, Long categoryId) {
        LambdaQueryWrapper<Note> w = new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) {
            w.like(Note::getTitle, keyword).or().like(Note::getDescription, keyword);
        }
        if (categoryId != null) w.eq(Note::getCategoryId, categoryId);
        return noteMapper.selectList(w);
    }

    @Override
    public java.io.InputStream download(Long id) {
        Note n = noteMapper.selectById(id);
        if (n == null) throw new RuntimeException("笔记不存在");
        
        // 增加浏览量
        n.setViews(n.getViews() == null ? 1 : n.getViews() + 1);
        noteMapper.updateById(n);
        
        return ossService.get(n.getOssKey());
    }
    
    @Override
    public byte[] convertToPdf(Long id) throws IOException {
        Note n = noteMapper.selectById(id);
        if (n == null) throw new RuntimeException("笔记不存在");
        
        InputStream is = ossService.get(n.getOssKey());
        String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        
        // 创建PDF文档
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 700);
        
        // 添加标题
        contentStream.showText("标题: " + n.getTitle());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("描述: " + n.getDescription());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("创建时间: " + n.getCreatedAt());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("");
        contentStream.newLineAtOffset(0, -20);
        
        // 添加内容
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.length() > 80) {
                // 长行换行处理
                for (int i = 0; i < line.length(); i += 80) {
                    int end = Math.min(i + 80, line.length());
                    contentStream.showText(line.substring(i, end));
                    contentStream.newLineAtOffset(0, -16);
                }
            } else {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -16);
            }
        }
        
        contentStream.endText();
        contentStream.close();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();
        
        return baos.toByteArray();
    }

    @Override
    public Note getById(Long id) {
        // 优先从缓存获取
        Note note = noteCacheService.getNoteById(id);
        // 确保内容字段被填充
        if (note != null && note.getContent() == null) {
            try {
                note.setContent(getNoteContent(id));
            } catch (Exception e) {
                // 如果获取内容失败，保持content为null
                note.setContent("");
            }
        }
        return note;
    }

    @Override
    public Note update(Long id, String title, String description, String content, String tags, String coverKey, String visibility) {
        Note n = noteMapper.selectById(id);
        if (n == null) throw new RuntimeException("笔记不存在");
        if (content != null) {
            String objectKey = ossService.uploadBytes("notes", (title != null ? title : n.getTitle()) + ".md", content.getBytes(StandardCharsets.UTF_8));
            n.setOssKey(objectKey);
            n.setFormat("md");
            n.setWordCount(content.length());
            n.setContent(content); // 更新内容
        }
        if (title != null) n.setTitle(title);
        if (description != null) n.setDescription(description);
        if (tags != null) n.setTags(tags);
        if (coverKey != null) n.setCoverKey(coverKey);
        if (visibility != null) n.setVisibility(visibility);
        n.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(n);
        // 删除笔记相关缓存
        noteCacheService.deleteNoteCache(id, n.getUserId());
        return n;
    }

    @Override
    public void like(Long id) {
        Note n = noteMapper.selectById(id);
        if (n == null) throw new RuntimeException("笔记不存在");
        n.setLikes(n.getLikes() == null ? 1 : n.getLikes() + 1);
        noteMapper.updateById(n);
    }

    @Override
    public List<Note> listByUserId(Long userId) {
        // 优先从缓存获取用户笔记列表
        return noteCacheService.getUserNoteList(userId, () -> 
            noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .orderByDesc(Note::getCreatedAt))
        );
    }

    @Override
    public String getNoteContent(Long id) {
        // 优先从缓存获取笔记内容
        return noteCacheService.getNoteContent(id, () -> {
            try {
                // 首先尝试从content字段获取内容
                Note note = noteMapper.selectById(id);
                if (note != null && note.getContent() != null) {
                    return note.getContent();
                }
                
                // 如果content字段为空，则从OSS获取
                InputStream is = download(id);
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get note content for id: " + id, e);
            }
        });
    }
}