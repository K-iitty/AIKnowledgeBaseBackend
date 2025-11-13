package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.entity.Note;
import com.fanfan.aiknowledgebasebackend.entity.NoteCategory;
import com.fanfan.aiknowledgebasebackend.mapper.NoteCategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class RagService {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteCategoryMapper noteCategoryMapper;

    // Simple keyword-based search for now
    private final Map<Long, List<NoteIndex>> userNoteIndexes = new ConcurrentHashMap<>();

    private static class NoteIndex {
        Long noteId;
        String title;
        String content;
        String categoryName;
        Set<String> keywords;
        
        NoteIndex(Note note, String content, String categoryName) {
            this.noteId = note.getId();
            this.title = note.getTitle().toLowerCase();
            this.content = content.toLowerCase();
            this.categoryName = categoryName.toLowerCase();
            this.keywords = extractKeywords(title + " " + content + " " + categoryName);
        }
        
        private Set<String> extractKeywords(String text) {
            Set<String> keywords = new HashSet<>();
            // Simple keyword extraction
            String[] words = text.toLowerCase().split("\\s+");
            for (String word : words) {
                word = word.replaceAll("[^\\w\\u4e00-\\u9fff]", ""); // Remove punctuation
                if (word.length() > 1 && !isStopWord(word)) {
                    keywords.add(word);
                }
            }
            return keywords;
        }
        
        private boolean isStopWord(String word) {
            String[] stopWords = {"的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这"};
            return Arrays.asList(stopWords).contains(word);
        }
        
        double calculateRelevance(String query) {
            String queryLower = query.toLowerCase();
            double score = 0.0;
            
            // Title match (highest weight)
            if (title.contains(queryLower)) {
                score += 10.0;
            }
            
            // Category match
            if (categoryName.contains(queryLower)) {
                score += 5.0;
            }
            
            // Content match
            if (content.contains(queryLower)) {
                score += 3.0;
            }
            
            // Keyword overlap
            Set<String> queryKeywords = extractKeywords(queryLower);
            int overlap = 0;
            for (String keyword : queryKeywords) {
                if (keywords.contains(keyword)) {
                    overlap++;
                }
            }
            score += (double) overlap / queryKeywords.size() * 2.0;
            
            return score;
        }
    }

    /**
     * Build or update RAG index for user's notes
     */
    public void buildUserIndex(Long userId) {
        try {
            List<Note> notes = noteService.listByUserId(userId);
            List<NoteIndex> noteIndexes = new ArrayList<>();
            
            for (Note note : notes) {
                try {
                    String content = noteService.getNoteContent(note.getId());
                    if (content != null && !content.trim().isEmpty()) {
                        // Get category name
                        String categoryName = "";
                        if (note.getCategoryId() != null) {
                            NoteCategory category = noteCategoryMapper.selectById(note.getCategoryId());
                            if (category != null) {
                                categoryName = category.getName();
                            }
                        }
                        
                        NoteIndex index = new NoteIndex(note, content, categoryName);
                        noteIndexes.add(index);
                    }
                } catch (Exception e) {
                    // Skip problematic notes
                }
            }
            
            userNoteIndexes.put(userId, noteIndexes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RAG index for user: " + userId, e);
        }
    }

    /**
     * Search relevant notes using RAG
     */
    public List<Note> searchRelevantNotes(Long userId, String query, int maxResults) {
        List<NoteIndex> noteIndexes = userNoteIndexes.get(userId);
        if (noteIndexes == null) {
            buildUserIndex(userId);
            noteIndexes = userNoteIndexes.get(userId);
        }
        
        if (noteIndexes == null || noteIndexes.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // Calculate relevance scores
            List<Map.Entry<NoteIndex, Double>> scoredNotes = new ArrayList<>();
            for (NoteIndex index : noteIndexes) {
                double score = index.calculateRelevance(query);
                if (score > 0) {
                    scoredNotes.add(new AbstractMap.SimpleEntry<>(index, score));
                }
            }
            
            // Sort by relevance score
            scoredNotes.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            // Get top results
            List<Note> relevantNotes = new ArrayList<>();
            for (int i = 0; i < Math.min(maxResults, scoredNotes.size()); i++) {
                Long noteId = scoredNotes.get(i).getKey().noteId;
                Note note = noteService.getById(noteId);
                if (note != null && !relevantNotes.contains(note)) {
                    relevantNotes.add(note);
                }
            }
            
            return relevantNotes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search relevant notes", e);
        }
    }

    /**
     * Get context string from relevant notes for AI prompt
     */
    public String getRelevantContext(Long userId, String query, int maxNotes) {
        List<Note> relevantNotes = searchRelevantNotes(userId, query, maxNotes);
        
        if (relevantNotes.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        context.append("基于以下本地笔记内容：\n\n");
        
        for (int i = 0; i < relevantNotes.size() && i < maxNotes; i++) {
            Note note = relevantNotes.get(i);
            try {
                String content = noteService.getNoteContent(note.getId());
                if (content != null && !content.trim().isEmpty()) {
                    // Truncate content if too long
                    String truncatedContent = content.length() > 2000 ? 
                        content.substring(0, 2000) + "..." : content;
                    context.append(String.format("【笔记：%s】\n%s\n\n", note.getTitle(), truncatedContent));
                }
            } catch (Exception e) {
                // Skip problematic notes
            }
        }
        
        return context.toString();
    }

    /**
     * Clear user's RAG index
     */
    public void clearUserIndex(Long userId) {
        userNoteIndexes.remove(userId);
    }
}