package com.fanfan.aiknowledgebasebackend.controller;

import com.fanfan.aiknowledgebasebackend.domain.dto.NoteRequest;
import com.fanfan.aiknowledgebasebackend.domain.entity.Note;
import com.fanfan.aiknowledgebasebackend.domain.entity.User;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import com.fanfan.aiknowledgebasebackend.service.NoteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// 删除冲突的导入，已在方法参数中使用全限定名 org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    public NoteController(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @PostMapping
    public Note create(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestBody NoteRequest req) {
        User u = userService.findByUsername(principal.getUsername());
        return noteService.createFromContent(u.getId(), req.getCategoryId(), req.getTitle(), req.getDescription(), req.getContent(), req.getVisibility(), req.getTags(), req.getCoverKey());
    }

    @PostMapping("/import")
    public Note importFile(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestParam Long categoryId, @RequestParam String visibility, @RequestPart("file") MultipartFile file) {
        User u = userService.findByUsername(principal.getUsername());
        return noteService.importFile(u.getId(), categoryId, file, visibility);
    }

    @GetMapping("/list")
    public List<Note> list(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId) {
        User u = userService.findByUsername(principal.getUsername());
        return noteService.search(u.getId(), keyword, categoryId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        noteService.deleteNote(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id, @RequestParam(defaultValue = "md") String format) throws IOException {
        if ("pdf".equalsIgnoreCase(format)) {
            byte[] pdfData = noteService.convertToPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=note.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfData);
        } else {
            InputStream is = noteService.download(id);
            byte[] data = is.readAllBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=note.md")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(data);
        }
    }

    @GetMapping("/{id}")
    public Note detail(@PathVariable Long id) {
        Note note = noteService.getById(id);
        // 确保内容字段被填充
        if (note != null && note.getContent() == null) {
            try {
                note.setContent(noteService.getNoteContent(id));
            } catch (Exception e) {
                // 如果获取内容失败，保持content为null
                note.setContent("");
            }
        }
        return note;
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<String> content(@PathVariable Long id) throws IOException {
        InputStream is = noteService.download(id);
        String text = new String(is.readAllBytes());
        return ResponseEntity.ok(text);
    }

    @PutMapping("/{id}")
    public Note update(@PathVariable Long id, @RequestBody NoteRequest req) {
        return noteService.update(id, req.getTitle(), req.getDescription(), req.getContent(), req.getTags(), req.getCoverKey(), req.getVisibility());
    }

    @PostMapping("/{id}/like")
    public void like(@PathVariable Long id) {
        noteService.like(id);
    }

}
