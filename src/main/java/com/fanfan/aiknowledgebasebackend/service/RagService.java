package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.Note;
import com.fanfan.aiknowledgebasebackend.domain.entity.NoteCategory;
import com.fanfan.aiknowledgebasebackend.domain.entity.Mindmap;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapCategory;
import com.fanfan.aiknowledgebasebackend.mapper.NoteCategoryMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapMapper;
import com.fanfan.aiknowledgebasebackend.mapper.MindmapCategoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RagService {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteCategoryMapper noteCategoryMapper;
    
    @Autowired
    private MindmapMapper mindmapMapper;
    
    @Autowired
    private MindmapCategoryMapper mindmapCategoryMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Simple keyword-based search for now
    private final Map<Long, List<NoteIndex>> userNoteIndexes = new ConcurrentHashMap<>();
    private final Map<Long, List<MindmapIndex>> userMindmapIndexes = new ConcurrentHashMap<>();

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
            // Split by whitespace and common punctuation
            String[] words = text.toLowerCase().split("[\\s,，。.、;；:：!！?？()（）\\[\\]【】]+");
            for (String word : words) {
                word = word.replaceAll("[^\\w\\u4e00-\\u9fff]", ""); // Remove remaining punctuation
                if (word.length() >= 2 && !isStopWord(word)) {
                    keywords.add(word);
                    // Also add single Chinese characters if they're meaningful
                    if (word.length() > 2) {
                        for (int i = 0; i < word.length(); i++) {
                            char c = word.charAt(i);
                            if (c >= '\u4e00' && c <= '\u9fff') {
                                // Don't add single character stopwords
                                String singleChar = String.valueOf(c);
                                if (!isStopWord(singleChar)) {
                                    keywords.add(singleChar);
                                }
                            }
                        }
                    }
                }
            }
            return keywords;
        }
        
        private boolean isStopWord(String word) {
            String[] stopWords = {
                "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这",
                "个", "中", "大", "为", "来", "以", "时", "地", "们", "得", "可", "下", "而", "子", "后", "她", "多", "么", "并", "两", "那", "与", "于", "但", "从", "把", "被", "让", "用", "给",
                "an", "a", "the", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "should", "could", "may", "might", "can",
                "of", "in", "on", "at", "to", "for", "with", "by", "from", "as", "or", "and", "but", "if", "then", "so", "than", "such", "no", "not", "only", "own", "same", "than", "too", "very"
            };
            return Arrays.asList(stopWords).contains(word.toLowerCase());
        }
        
        double calculateRelevance(String query) {
            String queryLower = query.toLowerCase();
            double score = 0.0;
            
            // Extract query keywords for flexible matching
            Set<String> queryKeywords = extractKeywords(queryLower);
            
            // Title match (highest weight) - check both full match and keyword match
            if (title.contains(queryLower)) {
                score += 10.0;
            } else {
                // Check if any query keyword appears in title
                for (String keyword : queryKeywords) {
                    if (title.contains(keyword)) {
                        score += 8.0; // Slightly lower than full match
                        break;
                    }
                }
            }
            
            // Category match
            if (categoryName.contains(queryLower)) {
                score += 5.0;
            } else {
                for (String keyword : queryKeywords) {
                    if (categoryName.contains(keyword)) {
                        score += 3.0;
                        break;
                    }
                }
            }
            
            // Content match - check for any keyword presence
            int contentMatches = 0;
            for (String keyword : queryKeywords) {
                if (content.contains(keyword)) {
                    contentMatches++;
                }
            }
            if (contentMatches > 0) {
                score += 3.0 + (contentMatches - 1) * 0.5; // Bonus for multiple keyword matches
            }
            
            // Keyword overlap in extracted keywords
            int overlap = 0;
            for (String keyword : queryKeywords) {
                if (keywords.contains(keyword)) {
                    overlap++;
                }
            }
            if (queryKeywords.size() > 0) {
                score += (double) overlap / queryKeywords.size() * 2.0;
            }
            
            return score;
        }
    }
    
    private static class MindmapIndex {
        Long mindmapId;
        String title;
        String nodesText;
        String categoryName;
        Set<String> keywords;
        
        MindmapIndex(Mindmap mindmap, String nodesText, String categoryName) {
            this.mindmapId = mindmap.getId();
            this.title = mindmap.getTitle().toLowerCase();
            this.nodesText = nodesText.toLowerCase();
            this.categoryName = categoryName.toLowerCase();
            this.keywords = extractKeywords(title + " " + nodesText + " " + categoryName);
        }
        
        private Set<String> extractKeywords(String text) {
            Set<String> keywords = new HashSet<>();
            // Split by whitespace and common punctuation
            String[] words = text.toLowerCase().split("[\\s,，。.、;；:：!！?？()（）\\[\\]【】]+");
            for (String word : words) {
                word = word.replaceAll("[^\\w\\u4e00-\\u9fff]", ""); // Remove remaining punctuation
                if (word.length() >= 2 && !isStopWord(word)) {
                    keywords.add(word);
                    // Also add single Chinese characters if they're meaningful
                    if (word.length() > 2) {
                        for (int i = 0; i < word.length(); i++) {
                            char c = word.charAt(i);
                            if (c >= '\u4e00' && c <= '\u9fff') {
                                // Don't add single character stopwords
                                String singleChar = String.valueOf(c);
                                if (!isStopWord(singleChar)) {
                                    keywords.add(singleChar);
                                }
                            }
                        }
                    }
                }
            }
            return keywords;
        }
        
        private boolean isStopWord(String word) {
            String[] stopWords = {
                "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这",
                "个", "中", "大", "为", "来", "以", "时", "地", "们", "得", "可", "下", "而", "子", "后", "她", "多", "么", "并", "两", "那", "与", "于", "但", "从", "把", "被", "让", "用", "给",
                "an", "a", "the", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "should", "could", "may", "might", "can",
                "of", "in", "on", "at", "to", "for", "with", "by", "from", "as", "or", "and", "but", "if", "then", "so", "than", "such", "no", "not", "only", "own", "same", "than", "too", "very"
            };
            return Arrays.asList(stopWords).contains(word.toLowerCase());
        }
        
        double calculateRelevance(String query) {
            String queryLower = query.toLowerCase();
            double score = 0.0;
            
            // Extract query keywords for flexible matching
            Set<String> queryKeywords = extractKeywords(queryLower);
            
            // Title match (highest weight) - check both full match and keyword match
            if (title.contains(queryLower)) {
                score += 10.0;
            } else {
                // Check if any query keyword appears in title
                for (String keyword : queryKeywords) {
                    if (title.contains(keyword)) {
                        score += 8.0; // Slightly lower than full match
                        break;
                    }
                }
            }
            
            // Category match
            if (categoryName.contains(queryLower)) {
                score += 5.0;
            } else {
                for (String keyword : queryKeywords) {
                    if (categoryName.contains(keyword)) {
                        score += 3.0;
                        break;
                    }
                }
            }
            
            // Nodes text match - check for any keyword presence
            int contentMatches = 0;
            for (String keyword : queryKeywords) {
                if (nodesText.contains(keyword)) {
                    contentMatches++;
                }
            }
            if (contentMatches > 0) {
                score += 3.0 + (contentMatches - 1) * 0.5; // Bonus for multiple keyword matches
            }
            
            // Keyword overlap in extracted keywords
            int overlap = 0;
            for (String keyword : queryKeywords) {
                if (keywords.contains(keyword)) {
                    overlap++;
                }
            }
            if (queryKeywords.size() > 0) {
                score += (double) overlap / queryKeywords.size() * 2.0;
            }
            
            return score;
        }
    }

    /**
     * Build or update RAG index for user's notes
     */
    public void buildUserIndex(Long userId) {
        try {
            System.out.println("开始构建笔记索引 - 用户ID: " + userId);
            List<Note> notes = noteService.listByUserId(userId);
            System.out.println("找到笔记数量: " + notes.size());
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
                        System.out.println("索引笔记: " + note.getTitle() + " (内容长度: " + content.length() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("索引笔记失败: " + note.getTitle() + " - " + e.getMessage());
                }
            }
            
            userNoteIndexes.put(userId, noteIndexes);
            System.out.println("笔记索引构建完成，共 " + noteIndexes.size() + " 条");
        } catch (Exception e) {
            System.err.println("构建笔记索引失败: " + e.getMessage());
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
            System.out.println("笔记索引为空，无法搜索");
            return new ArrayList<>();
        }
        
        try {
            System.out.println("开始搜索笔记 - 查询: " + query + ", 索引数: " + noteIndexes.size());
            // Calculate relevance scores
            List<Map.Entry<NoteIndex, Double>> scoredNotes = new ArrayList<>();
            for (NoteIndex index : noteIndexes) {
                double score = index.calculateRelevance(query);
                if (score > 0) {
                    scoredNotes.add(new AbstractMap.SimpleEntry<>(index, score));
                    System.out.println("  笔记匹配: " + index.title + " (分数: " + score + ")");
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
                    System.out.println("  选中笔记: " + note.getTitle() + " (分数: " + scoredNotes.get(i).getValue() + ")");
                }
            }
            
            return relevantNotes;
        } catch (Exception e) {
            System.err.println("搜索笔记失败: " + e.getMessage());
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
     * Build mindmap index for user
     */
    public void buildUserMindmapIndex(Long userId) {
        try {
            System.out.println("开始构建思维导图索引 - 用户ID: " + userId);
            List<Mindmap> mindmaps = mindmapMapper.selectList(
                new LambdaQueryWrapper<Mindmap>()
                    .eq(Mindmap::getUserId, userId)
                    .eq(Mindmap::getStatus, "active")
            );
            System.out.println("找到思维导图数量: " + mindmaps.size());
            
            List<MindmapIndex> mindmapIndexes = new ArrayList<>();
            
            for (Mindmap mindmap : mindmaps) {
                try {
                    String nodesText = extractTextFromMindmap(mindmap.getContent());
                    if (nodesText != null && !nodesText.trim().isEmpty()) {
                        String categoryName = "";
                        if (mindmap.getCategoryId() != null) {
                            MindmapCategory category = mindmapCategoryMapper.selectById(mindmap.getCategoryId());
                            if (category != null) {
                                categoryName = category.getName();
                            }
                        }
                        
                        MindmapIndex index = new MindmapIndex(mindmap, nodesText, categoryName);
                        mindmapIndexes.add(index);
                        System.out.println("索引思维导图: " + mindmap.getTitle() + " (节点文本长度: " + nodesText.length() + ")");
                    }
                } catch (Exception e) {
                    System.err.println("索引思维导图失败: " + mindmap.getTitle() + " - " + e.getMessage());
                }
            }
            
            userMindmapIndexes.put(userId, mindmapIndexes);
            System.out.println("思维导图索引构建完成，共 " + mindmapIndexes.size() + " 条");
        } catch (Exception e) {
            System.err.println("构建思维导图索引失败: " + e.getMessage());
            throw new RuntimeException("Failed to build mindmap index for user: " + userId, e);
        }
    }
    
    /**
     * Extract text from mindmap JSON content
     */
    private String extractTextFromMindmap(String contentJson) {
        if (contentJson == null || contentJson.trim().isEmpty()) {
            return "";
        }
        
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            StringBuilder text = new StringBuilder();
            extractNodeText(root.get("nodeData"), text);
            return text.toString();
        } catch (Exception e) {
            return "";
        }
    }
    
    private void extractNodeText(JsonNode node, StringBuilder text) {
        if (node == null) return;
        
        // Extract topic
        if (node.has("topic")) {
            text.append(node.get("topic").asText()).append(" ");
        }
        
        // Extract note if exists
        if (node.has("note")) {
            text.append(node.get("note").asText()).append(" ");
        }
        
        // Recursively extract from children
        if (node.has("children") && node.get("children").isArray()) {
            for (JsonNode child : node.get("children")) {
                extractNodeText(child, text);
            }
        }
    }
    
    /**
     * Search relevant mindmaps
     */
    public List<Mindmap> searchRelevantMindmaps(Long userId, String query, int maxResults) {
        List<MindmapIndex> mindmapIndexes = userMindmapIndexes.get(userId);
        if (mindmapIndexes == null) {
            buildUserMindmapIndex(userId);
            mindmapIndexes = userMindmapIndexes.get(userId);
        }
        
        if (mindmapIndexes == null || mindmapIndexes.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<Map.Entry<MindmapIndex, Double>> scoredMindmaps = new ArrayList<>();
            for (MindmapIndex index : mindmapIndexes) {
                double score = index.calculateRelevance(query);
                if (score > 0) {
                    scoredMindmaps.add(new AbstractMap.SimpleEntry<>(index, score));
                }
            }
            
            scoredMindmaps.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            List<Mindmap> relevantMindmaps = new ArrayList<>();
            for (int i = 0; i < Math.min(maxResults, scoredMindmaps.size()); i++) {
                Long mindmapId = scoredMindmaps.get(i).getKey().mindmapId;
                Mindmap mindmap = mindmapMapper.selectById(mindmapId);
                if (mindmap != null && !relevantMindmaps.contains(mindmap)) {
                    relevantMindmaps.add(mindmap);
                }
            }
            
            return relevantMindmaps;
        } catch (Exception e) {
            throw new RuntimeException("Failed to search relevant mindmaps", e);
        }
    }
    
    /**
     * Get combined context from notes and mindmaps
     */
    public String getCombinedRelevantContext(Long userId, String query, int maxItems) {
        System.out.println("RAG搜索 - 用户ID: " + userId + ", 查询: " + query);
        
        // Ensure indexes are built
        if (userNoteIndexes.get(userId) == null) {
            System.out.println("笔记索引不存在，正在构建...");
            buildUserIndex(userId);
        }
        if (userMindmapIndexes.get(userId) == null) {
            System.out.println("思维导图索引不存在，正在构建...");
            buildUserMindmapIndex(userId);
        }
        
        List<Note> relevantNotes = searchRelevantNotes(userId, query, maxItems);
        List<Mindmap> relevantMindmaps = searchRelevantMindmaps(userId, query, maxItems);
        
        System.out.println("找到相关笔记数: " + relevantNotes.size() + ", 思维导图数: " + relevantMindmaps.size());
        
        if (relevantNotes.isEmpty() && relevantMindmaps.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        
        // Add notes
        if (!relevantNotes.isEmpty()) {
            context.append("【相关笔记】\n\n");
            for (int i = 0; i < relevantNotes.size(); i++) {
                Note note = relevantNotes.get(i);
                try {
                    String content = noteService.getNoteContent(note.getId());
                    if (content != null && !content.trim().isEmpty()) {
                        String truncatedContent = content.length() > 1500 ? 
                            content.substring(0, 1500) + "..." : content;
                        context.append(String.format("%d. 笔记《%s》：\n%s\n\n", 
                            i + 1, note.getTitle(), truncatedContent));
                    }
                } catch (Exception e) {
                    // Skip
                }
            }
        }
        
        // Add mindmaps
        if (!relevantMindmaps.isEmpty()) {
            context.append("【相关思维导图】\n\n");
            for (int i = 0; i < relevantMindmaps.size(); i++) {
                Mindmap mindmap = relevantMindmaps.get(i);
                try {
                    String nodesText = extractTextFromMindmap(mindmap.getContent());
                    if (nodesText != null && !nodesText.trim().isEmpty()) {
                        String truncatedContent = nodesText.length() > 1000 ? 
                            nodesText.substring(0, 1000) + "..." : nodesText;
                        context.append(String.format("%d. 思维导图《%s》：\n%s\n\n", 
                            i + 1, mindmap.getTitle(), truncatedContent));
                    }
                } catch (Exception e) {
                    // Skip
                }
            }
        }
        
        return context.toString();
    }

    /**
     * Clear user's RAG index
     */
    public void clearUserIndex(Long userId) {
        userNoteIndexes.remove(userId);
        userMindmapIndexes.remove(userId);
    }
}