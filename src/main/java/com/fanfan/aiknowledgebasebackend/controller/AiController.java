package com.fanfan.aiknowledgebasebackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fanfan.aiknowledgebasebackend.entity.ChatSession;
import com.fanfan.aiknowledgebasebackend.entity.ChatMessage;
import com.fanfan.aiknowledgebasebackend.entity.Note;
import com.fanfan.aiknowledgebasebackend.entity.User;
import com.fanfan.aiknowledgebasebackend.mapper.ChatSessionMapper;
import com.fanfan.aiknowledgebasebackend.mapper.ChatMessageMapper;
import com.fanfan.aiknowledgebasebackend.service.NoteService;
import com.fanfan.aiknowledgebasebackend.service.UserService;
import com.fanfan.aiknowledgebasebackend.service.EnhancedAiService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
            .version(java.net.http.HttpClient.Version.HTTP_1_1)
            .build();

    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final NoteService noteService;
    private final UserService userService;
    private final EnhancedAiService enhancedAiService;

    public AiController(ChatSessionMapper chatSessionMapper, ChatMessageMapper chatMessageMapper, 
                         NoteService noteService, UserService userService, EnhancedAiService enhancedAiService) {
        this.chatSessionMapper = chatSessionMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.noteService = noteService;
        this.userService = userService;
        this.enhancedAiService = enhancedAiService;
    }

    @Value("${spring.ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${spring.ai.dashscope.chat.options.model:qwen3-max}")
    private String model;

    @Value("${spring.ai.dashscope.chat.options.temperature:0.8}")
    private Double temperature;

    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatReq req) {
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> msgs = req.getMessages();
        if (msgs == null || msgs.isEmpty()) {
            msgs = List.of(Map.of("role", "user", "content", "你好"));
        }
        body.put("messages", msgs);
        if (temperature != null) body.put("temperature", temperature);
        Map<String, Object> out = new HashMap<>();
        try {
            if (apiKey == null || apiKey.trim().isEmpty() || apiKey.contains("${")) {
                out.put("error", "DashScope API Key 未配置或无效");
                return out;
            }
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> resp = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code / 100 != 2) {
                out.put("error", "DashScope服务返回错误：" + code);
                out.put("details", resp.body());
                return out;
            }
            Map<?,?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), Map.class);
            List choices = (List) map.get("choices");
            Map choice = (Map) choices.get(0);
            Map msg = (Map) choice.get("message");
            String content = (String) msg.get("content");
            out.put("answer", content);
            return out;
        } catch (Exception e) {
            out.put("error", "抱歉，当前AI服务不可用，请稍后重试或检查API配置。");
            out.put("details", e.getMessage());
            return out;
        }
    }

    @GetMapping("/chat/stream")
    public SseEmitter stream(@RequestParam("q") String question) {
        SseEmitter emitter = new SseEmitter(0L);
        new Thread(() -> {
            try {
                String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
                Map<String, Object> body = new HashMap<>();
                body.put("model", model);
                body.put("stream", true);
                List<Map<String, String>> msgs = List.of(
                        Map.of("role", "user", "content", question)
                );
                body.put("messages", msgs);
                if (temperature != null) body.put("temperature", temperature);
                String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
                java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(url))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/event-stream")
                        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                        .build();
                java.net.http.HttpResponse<java.io.InputStream> resp = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofInputStream());
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(resp.body()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("data:")) {
                            String jsonLine = line.substring(5).trim();
                            if ("[DONE]".equals(jsonLine)) break;
                            emitter.send(SseEmitter.event().data(jsonLine));
                        }
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                try { emitter.send(SseEmitter.event().data("")); } catch (Exception ignored) {}
                emitter.complete();
            }
        }).start();
        return emitter;
    }

    // Sessions APIs
    @GetMapping("/sessions")
    public List<ChatSession> sessions(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User u = userService.findByUsername(principal.getUsername());
        return chatSessionMapper.selectList(new LambdaQueryWrapper<ChatSession>().eq(ChatSession::getUserId, u.getId()).orderByDesc(ChatSession::getUpdatedAt));
    }

    @PostMapping("/sessions")
    public ChatSession createSession(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestBody Map<String, String> req) {
        User u = userService.findByUsername(principal.getUsername());
        ChatSession s = new ChatSession();
        s.setUserId(u.getId());
        s.setTitle(req.getOrDefault("title", "新的会话"));
        s.setMode(req.getOrDefault("mode", "default"));
        s.setCreatedAt(java.time.LocalDateTime.now());
        s.setUpdatedAt(java.time.LocalDateTime.now());
        chatSessionMapper.insert(s);
        return s;
    }

    @DeleteMapping("/sessions/{id}")
    public void deleteSession(@PathVariable Long id) {
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, id));
        chatSessionMapper.deleteById(id);
    }

    @PutMapping("/sessions/{id}")
    public ChatSession updateSession(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                   @PathVariable Long id,
                                   @RequestBody Map<String, String> req) {
        User u = userService.findByUsername(principal.getUsername());
        ChatSession s = chatSessionMapper.selectById(id);
        if (s == null || !s.getUserId().equals(u.getId())) {
            throw new RuntimeException("会话不存在或无权限");
        }
        
        if (req.containsKey("title")) {
            s.setTitle(req.get("title"));
        }
        if (req.containsKey("mode")) {
            s.setMode(req.get("mode"));
        }
        s.setUpdatedAt(java.time.LocalDateTime.now());
        chatSessionMapper.updateById(s);
        return s;
    }

    @GetMapping("/sessions/{id}/messages")
    public List<ChatMessage> listMessages(@PathVariable Long id) {
        return chatMessageMapper.selectList(new LambdaQueryWrapper<ChatMessage>().eq(ChatMessage::getSessionId, id).orderByAsc(ChatMessage::getCreatedAt));
    }

    @PostMapping("/chat/session")
    public Map<String, Object> chatWithSession(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal, @RequestBody ChatSessionReq req) {
        User u = userService.findByUsername(principal.getUsername());
        ChatSession s = chatSessionMapper.selectById(req.getSessionId());
        if (s == null || !s.getUserId().equals(u.getId())) throw new RuntimeException("会话不存在或无权限");

        try {
            // Use enhanced AI service with conversation memory and RAG
            String answer = enhancedAiService.chatWithMemory(
                u.getId(), 
                req.getSessionId(), 
                req.getQuestion(), 
                req.getMode(), 
                req.getCategoryId()
            );

            Map<String, Object> out = new HashMap<>();
            out.put("answer", answer);
            out.put("sessionId", s.getId());
            return out;
            
        } catch (Exception e) {
            Map<String, Object> out = new HashMap<>();
            out.put("answer", "抱歉，当前AI服务不可用，请稍后重试或检查API配置。");
            out.put("error", e.getMessage());
            return out;
        }
    }

    @GetMapping("/chat/stream/session")
    public SseEmitter streamSession(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
                                    @RequestParam Long sessionId,
                                    @RequestParam String q,
                                    @RequestParam(defaultValue = "default") String mode,
                                    @RequestParam(required = false) Long categoryId) {
        SseEmitter emitter = new SseEmitter(0L);
        if (principal == null) {
            try { emitter.send(SseEmitter.event().data("{"+"\"error\":\"未登录或令牌无效\"}")); } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }
        User u = userService.findByUsername(principal.getUsername());
        ChatSession s = chatSessionMapper.selectById(sessionId);
        if (s == null || !s.getUserId().equals(u.getId())) {
            try { emitter.send(SseEmitter.event().data("{"+"\"error\":\"会话不存在或无权限\"}")); } catch (Exception ignored) {}
            emitter.complete();
            return emitter;
        }
        new Thread(() -> {
            try {
                // Use enhanced AI service for streaming
                String answer = enhancedAiService.streamChatWithMemory(
                    u.getId(), sessionId, q, mode, categoryId
                );
                
                // Simulate streaming by sending the response in chunks
                int chunkSize = 50; // Send ~50 characters at a time
                for (int i = 0; i < answer.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, answer.length());
                    String chunk = answer.substring(i, end);
                    emitter.send(SseEmitter.event().data(chunk));
                    Thread.sleep(50); // Small delay to simulate streaming
                }
                
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) { 
                try { 
                    String errorMsg = "{"+"\"error\":\"AI服务错误: " + e.getMessage().replace("\"", "\\\"") + "\"}";
                    emitter.send(SseEmitter.event().data(errorMsg)); 
                } catch (Exception ignored) {} 
                emitter.complete(); 
            }
        }).start();
        return emitter;
    }

    @PostMapping("/rebuild-index")
    public Map<String, Object> rebuildIndex(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        Map<String, Object> result = new HashMap<>();
        try {
            User u = userService.findByUsername(principal.getUsername());
            enhancedAiService.rebuildUserRagIndex(u.getId());
            result.put("success", true);
            result.put("message", "RAG索引重建成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "索引重建失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/polish")
    public Map<String, Object> polishNote(@RequestBody PolishReq req) {
        String content = req.getContent();
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "内容不能为空");
            return error;
        }
        
        String prompt = "请帮我润色以下笔记内容，使其更加通顺、专业，保持原意不变：\n\n" + content;
        
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> msgs = List.of(
                Map.of("role", "user", "content", prompt)
        );
        body.put("messages", msgs);
        body.put("temperature", 0.7); // 降低温度以获得更稳定的输出
        
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> resp = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            Map<?,?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), Map.class);
            List choices = (List) map.get("choices");
            Map choice = (Map) choices.get(0);
            Map msg = (Map) choice.get("message");
            String polishedContent = (String) msg.get("content");
            
            Map<String, Object> out = new HashMap<>();
            out.put("original", content);
            out.put("polished", polishedContent);
            out.put("success", true);
            return out;
        } catch (Exception e) {
            Map<String, Object> out = new HashMap<>();
            out.put("error", "AI润色服务暂时不可用");
            out.put("success", false);
            return out;
        }
    }
    
    @PostMapping("/generate-summary")
    public Map<String, Object> generateSummary(@RequestBody SummaryReq req) {
        String content = req.getContent();
        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "内容不能为空");
            return error;
        }
        
        String prompt = "请为以下内容生成一个简洁的摘要（50-100字）：\n\n" + content;
        
        String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        List<Map<String, String>> msgs = List.of(
                Map.of("role", "user", "content", prompt)
        );
        body.put("messages", msgs);
        body.put("temperature", 0.5);
        
        try {
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            java.net.http.HttpResponse<String> resp = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            Map<?,?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), Map.class);
            List choices = (List) map.get("choices");
            Map choice = (Map) choices.get(0);
            Map msg = (Map) choice.get("message");
            String summary = (String) msg.get("content");
            
            Map<String, Object> out = new HashMap<>();
            out.put("summary", summary);
            out.put("success", true);
            return out;
        } catch (Exception e) {
            Map<String, Object> out = new HashMap<>();
            out.put("error", "AI摘要生成服务暂时不可用");
            out.put("success", false);
            return out;
        }
    }

    public static class ChatReq {
        private List<Map<String, String>> messages;
        public List<Map<String, String>> getMessages() { return messages; }
        public void setMessages(List<Map<String, String>> messages) { this.messages = messages; }
    }
    @lombok.Data
    public static class ChatSessionReq {
        private Long sessionId;
        private String question;
        private String mode; // default | local
        private Long categoryId;
    }
    
    public static class PolishReq {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class SummaryReq {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
