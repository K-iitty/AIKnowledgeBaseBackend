/**
 * 笔记缓存服务
 * 使用Redis缓存热点笔记，提升AI搜索速度和查询性能
 */
package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.common.constant.RedisConstants;
import com.fanfan.aiknowledgebasebackend.common.util.RedisUtil;
import com.fanfan.aiknowledgebasebackend.domain.entity.Note;
import com.fanfan.aiknowledgebasebackend.mapper.NoteMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 笔记缓存服务
 * 提供笔记的缓存读写操作
 */
@Service
public class NoteCacheService {

    private final RedisUtil redisUtil;
    private final NoteMapper noteMapper;

    public NoteCacheService(RedisUtil redisUtil, NoteMapper noteMapper) {
        this.redisUtil = redisUtil;
        this.noteMapper = noteMapper;
    }

    /**
     * 获取笔记详情（优先从缓存读取）
     * @param noteId 笔记ID
     * @return 笔记信息
     */
    public Note getNoteById(Long noteId) {
        String key = RedisConstants.NOTE_DETAIL_KEY + noteId;
        
        // 1. 先从缓存获取
        Object cachedNote = redisUtil.get(key);
        if (cachedNote != null) {
            System.out.println("[缓存命中] 笔记ID: " + noteId);
            return (Note) cachedNote;
        }
        
        // 2. 缓存未命中，从数据库查询
        System.out.println("[缓存未命中] 笔记ID: " + noteId + "，从数据库查询");
        Note note = noteMapper.selectById(noteId);
        
        // 3. 查询结果存入缓存
        if (note != null) {
            redisUtil.set(key, note, RedisConstants.NOTE_CACHE_TTL, TimeUnit.SECONDS);
            System.out.println("[缓存写入] 笔记ID: " + noteId);
        }
        
        return note;
    }

    /**
     * 获取笔记内容（优先从缓存读取）
     * @param noteId 笔记ID
     * @param contentSupplier 内容获取函数
     * @return 笔记内容
     */
    public String getNoteContent(Long noteId, java.util.function.Supplier<String> contentSupplier) {
        String key = RedisConstants.NOTE_CONTENT_KEY + noteId;
        
        // 1. 先从缓存获取
        Object cachedContent = redisUtil.get(key);
        if (cachedContent != null) {
            System.out.println("[缓存命中] 笔记内容ID: " + noteId);
            return (String) cachedContent;
        }
        
        // 2. 缓存未命中，调用原有逻辑获取内容
        System.out.println("[缓存未命中] 笔记内容ID: " + noteId + "，从原有逻辑获取");
        String content = contentSupplier.get();
        
        // 3. 内容存入缓存
        if (content != null && !content.isEmpty()) {
            redisUtil.set(key, content, RedisConstants.NOTE_CACHE_TTL, TimeUnit.SECONDS);
            System.out.println("[缓存写入] 笔记内容ID: " + noteId + "，长度: " + content.length());
        }
        
        return content;
    }

    /**
     * 获取用户笔记列表（优先从缓存读取）
     * @param userId 用户ID
     * @param listSupplier 列表获取函数
     * @return 笔记列表
     */
    @SuppressWarnings("unchecked")
    public List<Note> getUserNoteList(Long userId, java.util.function.Supplier<List<Note>> listSupplier) {
        String key = RedisConstants.NOTE_LIST_KEY + userId;
        
        // 1. 先从缓存获取
        Object cachedList = redisUtil.get(key);
        if (cachedList != null) {
            System.out.println("[缓存命中] 用户笔记列表，用户ID: " + userId);
            return (List<Note>) cachedList;
        }
        
        // 2. 缓存未命中，调用原有逻辑获取列表
        System.out.println("[缓存未命中] 用户笔记列表，用户ID: " + userId);
        List<Note> noteList = listSupplier.get();
        
        // 3. 列表存入缓存
        if (noteList != null && !noteList.isEmpty()) {
            redisUtil.set(key, noteList, RedisConstants.NOTE_CACHE_TTL, TimeUnit.SECONDS);
            System.out.println("[缓存写入] 用户笔记列表，用户ID: " + userId + "，数量: " + noteList.size());
        }
        
        return noteList;
    }

    /**
     * 更新笔记缓存
     * @param note 笔记信息
     */
    public void updateNoteCache(Note note) {
        if (note == null || note.getId() == null) {
            return;
        }
        String key = RedisConstants.NOTE_DETAIL_KEY + note.getId();
        redisUtil.set(key, note, RedisConstants.NOTE_CACHE_TTL, TimeUnit.SECONDS);
        System.out.println("[缓存更新] 笔记ID: " + note.getId());
    }

    /**
     * 删除笔记缓存（笔记被修改或删除时调用）
     * @param noteId 笔记ID
     * @param userId 用户ID
     */
    public void deleteNoteCache(Long noteId, Long userId) {
        // 删除笔记详情缓存
        String detailKey = RedisConstants.NOTE_DETAIL_KEY + noteId;
        redisUtil.delete(detailKey);
        
        // 删除笔记内容缓存
        String contentKey = RedisConstants.NOTE_CONTENT_KEY + noteId;
        redisUtil.delete(contentKey);
        
        // 删除用户笔记列表缓存（因为列表可能变化）
        String listKey = RedisConstants.NOTE_LIST_KEY + userId;
        redisUtil.delete(listKey);
        
        System.out.println("[缓存删除] 笔记ID: " + noteId + "，用户ID: " + userId);
    }

    /**
     * 删除用户所有笔记缓存
     * @param userId 用户ID
     */
    public void deleteUserNoteListCache(Long userId) {
        String listKey = RedisConstants.NOTE_LIST_KEY + userId;
        redisUtil.delete(listKey);
        System.out.println("[缓存删除] 用户笔记列表，用户ID: " + userId);
    }
}
