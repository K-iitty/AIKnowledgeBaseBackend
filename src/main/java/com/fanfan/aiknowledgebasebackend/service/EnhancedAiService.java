package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.entity.ChatMessage;
import com.fanfan.aiknowledgebasebackend.domain.entity.ChatSession;
import com.fanfan.aiknowledgebasebackend.mapper.ChatMessageMapper;
import com.fanfan.aiknowledgebasebackend.mapper.ChatSessionMapper;
import com.fanfan.aiknowledgebasebackend.common.config.AiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnhancedAiService {

    @Autowired
    private RagService ragService;

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private AiConfig aiConfig;

    // In-memory conversation memory for better performance
    private final Map<Long, List<Map<String, String>>> conversationMemory = new ConcurrentHashMap<>();
    private static final int MAX_MEMORY_MESSAGES = 20;

    /**
     * Enhanced chat with conversation memory and RAG support
     */
    public String chatWithMemory(Long userId, Long sessionId, String question, String mode, Long categoryId) {
        try {
            // Get session and validate
            ChatSession session = chatSessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                throw new RuntimeException("会话不存在或无权限");
            }

            // Build conversation context
            List<Map<String, String>> messages = getOrCreateConversationMemory(sessionId);
            
            // Remove old system prompts to avoid mode conflicts
            messages.removeIf(msg -> "system".equals(msg.get("role")));
            
            // Add RAG context for local mode
            if ("local".equalsIgnoreCase(mode)) {
                String ragContext = ragService.getCombinedRelevantContext(userId, question, 5);
                if (!ragContext.isEmpty()) {
                    String systemPrompt = "你是知识库助手。请严格基于以下本地知识库内容回答用户问题：\n\n" + ragContext + 
                        "\n\n【重要规则】\n" +
                        "1. 只使用上述笔记和思维导图中的信息回答\n" +
                        "2. 如果上述内容中没有相关信息，请明确回复：\"抱歉，在您的本地知识库中暂未找到相关信息。\"\n" +
                        "3. 不要使用你的通用知识，只使用提供的本地内容\n" +
                        "4. 回答时可以引用具体的笔记或思维导图标题";
                    messages.add(createMessage("system", systemPrompt));
                } else {
                    // No relevant content found
                    messages.add(createMessage("system", "在本地知识库中未找到与用户问题相关的内容。请明确告知用户：\"抱歉，在您的本地知识库中暂未找到相关信息。\""));
                }
            } else {
                // Default mode: General assistant with Markdown formatting
                messages.add(createMessage("system", "你是一个全能的助手，请始终使用Markdown格式进行回复，以便于阅读。例如，使用标题、列表、代码块等来组织内容。"));
            }

            // Add user message
            messages.add(createMessage("user", question));

            // Keep only recent messages to avoid context overflow
            if (messages.size() > MAX_MEMORY_MESSAGES) {
                messages = messages.subList(messages.size() - MAX_MEMORY_MESSAGES, messages.size());
            }

            // Call AI with conversation context
            String answer = callAiWithMessages(messages);

            // Store conversation in memory
            messages.add(createMessage("assistant", answer));
            conversationMemory.put(sessionId, messages);

            // Persist to database
            persistConversation(sessionId, question, answer);

            // Update session timestamp
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.updateById(session);

            return answer;

        } catch (Exception e) {
            throw new RuntimeException("AI对话失败: " + e.getMessage(), e);
        }
    }

    /**
     * Stream chat with conversation memory and RAG support
     */
    public String streamChatWithMemory(Long userId, Long sessionId, String question, String mode, Long categoryId) {
        try {
            // Get session and validate
            ChatSession session = chatSessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                throw new RuntimeException("会话不存在或无权限");
            }

            // Build conversation context
            List<Map<String, String>> messages = getOrCreateConversationMemory(sessionId);
            
            // Remove old system prompts to avoid mode conflicts
            messages.removeIf(msg -> "system".equals(msg.get("role")));
            
            // Add RAG context for local mode
            if ("local".equalsIgnoreCase(mode)) {
                String ragContext = ragService.getCombinedRelevantContext(userId, question, 5);
                if (!ragContext.isEmpty()) {
                    String systemPrompt = "你是知识库助手。请严格基于以下本地知识库内容回答用户问题：\n\n" + ragContext + 
                        "\n\n【重要规则】\n" +
                        "1. 只使用上述笔记和思维导图中的信息回答\n" +
                        "2. 如果上述内容中没有相关信息，请明确回复：\"抱歉，在您的本地知识库中暂未找到相关信息。\"\n" +
                        "3. 不要使用你的通用知识，只使用提供的本地内容\n" +
                        "4. 回答时可以引用具体的笔记或思维导图标题";
                    messages.add(createMessage("system", systemPrompt));
                } else {
                    // No relevant content found
                    messages.add(createMessage("system", "在本地知识库中未找到与用户问题相关的内容。请明确告知用户：\"抱歉，在您的本地知识库中暂未找到相关信息。\""));
                }
            } else {
                // Default mode: General assistant with Markdown formatting
                messages.add(createMessage("system", "你是一个全能的助手，请始终使用Markdown格式进行回复，以便于阅读。例如，使用标题、列表、代码块等来组织内容。"));
            }

            // Add user message
            messages.add(createMessage("user", question));

            // Keep only recent messages to avoid context overflow
            if (messages.size() > MAX_MEMORY_MESSAGES) {
                messages = messages.subList(messages.size() - MAX_MEMORY_MESSAGES, messages.size());
            }

            // Call AI with conversation context and return the answer
            String answer = callAiWithMessages(messages);

            // Store conversation in memory
            messages.add(createMessage("assistant", answer));
            conversationMemory.put(sessionId, messages);

            // Persist to database
            persistConversation(sessionId, question, answer);

            // Update session timestamp
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.updateById(session);

            return answer;

        } catch (Exception e) {
            throw new RuntimeException("AI对话失败: " + e.getMessage(), e);
        }
    }

    /**
     * Call AI with conversation messages
     */
    private String callAiWithMessages(List<Map<String, String>> messages) {
        try {
            // Validate API key first
            String apiKey = aiConfig.getApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("DashScope API Key 未配置");
            }
            
            String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiConfig.getModel());
            body.put("messages", messages);
            if (aiConfig.getTemperature() != null) {
                body.put("temperature", aiConfig.getTemperature());
            }
            
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            System.out.println("AI Service Debug - URL: " + url);
            System.out.println("AI Service Debug - Model: " + aiConfig.getModel());
            System.out.println("AI Service Debug - API Key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
            System.out.println("AI Service Debug - Request Body: " + json);
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build();
                    
            java.net.http.HttpResponse<String> resp = httpClient.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            
            System.out.println("AI Service Debug - Response Status: " + resp.statusCode());
            System.out.println("AI Service Debug - Response Body: " + resp.body());
            
            if (resp.statusCode() / 100 != 2) {
                String errorBody = resp.body();
                String errorMsg = "AI服务返回错误: " + resp.statusCode();
                if (errorBody != null && !errorBody.trim().isEmpty()) {
                    try {
                        Map<?,?> errorMap = new com.fasterxml.jackson.databind.ObjectMapper().readValue(errorBody, Map.class);
                        if (errorMap.containsKey("error")) {
                            Object error = errorMap.get("error");
                            if (error instanceof Map) {
                                Map<?,?> errorDetails = (Map<?,?>) error;
                                if (errorDetails.containsKey("message")) {
                                    errorMsg += " - " + errorDetails.get("message");
                                }
                            } else {
                                errorMsg += " - " + error.toString();
                            }
                        }
                    } catch (Exception parseError) {
                        errorMsg += " - " + errorBody.substring(0, Math.min(200, errorBody.length()));
                    }
                }
                throw new RuntimeException(errorMsg);
            }
            
            Map<?,?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), Map.class);
            if (map == null || !map.containsKey("choices")) {
                throw new RuntimeException("AI服务返回无效响应格式");
            }
            
            List choices = (List) map.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("AI服务返回空响应");
            }
            
            Map choice = (Map) choices.get(0);
            if (choice == null || !choice.containsKey("message")) {
                throw new RuntimeException("AI服务响应格式错误");
            }
            
            Map msg = (Map) choice.get("message");
            if (msg == null || !msg.containsKey("content")) {
                throw new RuntimeException("AI服务响应内容格式错误");
            }
            
            String content = (String) msg.get("content");
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("AI服务返回空内容");
            }
            
            return content;
            
        } catch (Exception e) {
            if (e.getMessage().contains("未配置")) {
                throw new RuntimeException("AI服务配置错误: " + e.getMessage());
            }
            throw new RuntimeException("AI调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * Create a message map
     */
    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    /**
     * Get or create conversation memory for a session
     */
    private List<Map<String, String>> getOrCreateConversationMemory(Long sessionId) {
        return conversationMemory.computeIfAbsent(sessionId, k -> {
            // Load from database if not in memory
            List<ChatMessage> history = chatMessageMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatMessage>()
                    .eq(ChatMessage::getSessionId, sessionId)
                    .orderByAsc(ChatMessage::getCreatedAt)
            );

            List<Map<String, String>> messages = new ArrayList<>();
            for (ChatMessage msg : history) {
                messages.add(createMessage(msg.getRole(), msg.getContent()));
            }
            return messages;
        });
    }

    /**
     * Persist conversation to database
     */
    private void persistConversation(Long sessionId, String question, String answer) {
        // Save user message
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(question);
        userMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(userMsg);

        // Save assistant message
        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(answer);
        assistantMsg.setCreatedAt(LocalDateTime.now());
        chatMessageMapper.insert(assistantMsg);
    }

    /**
     * Clear conversation memory for a session
     */
    public void clearConversationMemory(Long sessionId) {
        conversationMemory.remove(sessionId);
    }

    /**
     * Rebuild RAG index for user
     */
    public void rebuildUserRagIndex(Long userId) {
        ragService.buildUserIndex(userId);
        ragService.buildUserMindmapIndex(userId);
    }

    /**
     * Functional interface for streaming callback
     */
    @FunctionalInterface
    public interface StreamCallback {
        void onChunk(String chunk);
    }

    /**
     * Real-time streaming chat with conversation memory and RAG support
     */
    public void streamChatWithMemoryRealtime(Long userId, Long sessionId, String question, 
                                             String mode, Long categoryId, StreamCallback callback) {
        try {
            // Get session and validate
            ChatSession session = chatSessionMapper.selectById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                throw new RuntimeException("会话不存在或无权限");
            }

            // Build conversation context
            List<Map<String, String>> messages = getOrCreateConversationMemory(sessionId);
            
            // Remove old system prompts to avoid mode conflicts
            messages.removeIf(msg -> "system".equals(msg.get("role")));
            
            // Add RAG context for local mode
            if ("local".equalsIgnoreCase(mode)) {
                String ragContext = ragService.getCombinedRelevantContext(userId, question, 5);
                if (!ragContext.isEmpty()) {
                    String systemPrompt = "你是知识库助手。请严格基于以下本地知识库内容回答用户问题：\n\n" + ragContext + 
                        "\n\n【重要规则】\n" +
                        "1. 只使用上述笔记和思维导图中的信息回答\n" +
                        "2. 如果上述内容中没有相关信息，请明确回复：\"抱歉，在您的本地知识库中暂未找到相关信息。\"\n" +
                        "3. 不要使用你的通用知识，只使用提供的本地内容\n" +
                        "4. 回答时可以引用具体的笔记或思维导图标题\n" +
                        "5. 你的回复必须使用Markdown格式进行排版。";
                    messages.add(createMessage("system", systemPrompt));
                } else {
                    // No relevant content found
                    messages.add(createMessage("system", "在本地知识库中未找到与用户问题相关的内容。请明确告知用户：\"抱歉，在您的本地知识库中暂未找到相关信息。\""));
                }
            } else {
                // Default mode: General assistant with Markdown formatting
                messages.add(createMessage("system", "你是一个全能的助手，请始终使用Markdown格式进行回复，以便于阅读。例如，使用标题、列表、代码块等来组织内容。"));
            }

            // Add user message
            messages.add(createMessage("user", question));

            // Keep only recent messages to avoid context overflow
            if (messages.size() > MAX_MEMORY_MESSAGES) {
                messages = messages.subList(messages.size() - MAX_MEMORY_MESSAGES, messages.size());
            }

            // Call AI with streaming
            StringBuilder fullAnswer = new StringBuilder();
            callAiWithMessagesStreaming(messages, chunk -> {
                fullAnswer.append(chunk);
                callback.onChunk(chunk);
            });

            // Store conversation in memory
            messages.add(createMessage("assistant", fullAnswer.toString()));
            conversationMemory.put(sessionId, messages);

            // Persist to database
            persistConversation(sessionId, question, fullAnswer.toString());

            // Update session timestamp
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionMapper.updateById(session);

        } catch (Exception e) {
            throw new RuntimeException("AI对话失败: " + e.getMessage(), e);
        }
    }

    /**
     * Call AI with streaming support
     */
    private void callAiWithMessagesStreaming(List<Map<String, String>> messages, StreamCallback callback) {
        try {
            // Validate API key first
            String apiKey = aiConfig.getApiKey();
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new RuntimeException("DashScope API Key 未配置");
            }
            
            String url = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiConfig.getModel());
            body.put("messages", messages);
            body.put("stream", true);  // Enable streaming
            if (aiConfig.getTemperature() != null) {
                body.put("temperature", aiConfig.getTemperature());
            }
            
            String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(body);
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(json))
                    .build();
            
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                    .version(java.net.http.HttpClient.Version.HTTP_1_1)
                    .build();
            
            // Use streaming response handler
            java.net.http.HttpResponse<java.io.InputStream> resp = httpClient.send(
                request, 
                java.net.http.HttpResponse.BodyHandlers.ofInputStream()
            );
            
            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("AI服务返回错误: " + resp.statusCode());
            }
            
            // Read stream line by line
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(resp.body(), java.nio.charset.StandardCharsets.UTF_8))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        
                        if (data.equals("[DONE]")) {
                            break;
                        }
                        
                        if (data.isEmpty()) {
                            continue;
                        }
                        
                        try {
                            Map<?,?> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(data, Map.class);
                            List<?> choices = (List<?>) map.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                Map<?,?> choice = (Map<?,?>) choices.get(0);
                                Map<?,?> delta = (Map<?,?>) choice.get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String content = (String) delta.get("content");
                                    if (content != null && !content.isEmpty()) {
                                        callback.onChunk(content);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("解析流式数据失败: " + e.getMessage());
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("调用AI流式接口失败: " + e.getMessage(), e);
        }
    }
}