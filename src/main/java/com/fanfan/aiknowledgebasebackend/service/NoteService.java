package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.Note;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface NoteService {
    Note createFromContent(Long userId, Long categoryId, String title, String description, String content, String visibility, String tags, String coverKey);
    Note importFile(Long userId, Long categoryId, MultipartFile file, String visibility);
    void deleteNote(Long id);
    List<Note> search(Long userId, String keyword, Long categoryId);
    java.io.InputStream download(Long id);
    byte[] convertToPdf(Long id) throws IOException;
    Note getById(Long id);
    Note update(Long id, String title, String description, String content, String tags, String coverKey, String visibility);
    void like(Long id);
    List<Note> listByUserId(Long userId);
    String getNoteContent(Long id);
}
