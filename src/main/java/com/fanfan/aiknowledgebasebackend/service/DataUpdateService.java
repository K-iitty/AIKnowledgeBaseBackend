package com.fanfan.aiknowledgebasebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据更新服务
 * 监听 Redis 消息并通过 WebSocket 推送给前端
 */
@Slf4j
@Service
public class DataUpdateService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // Redis 频道名称
    private static final String CHANNEL_NOTE_UPDATE = "data:update:note";
    private static final String CHANNEL_MINDMAP_UPDATE = "data:update:mindmap";
    private static final String CHANNEL_LINK_UPDATE = "data:update:link";
    private static final String CHANNEL_AI_CHAT_UPDATE = "data:update:ai-chat";
    private static final String CHANNEL_USER_UPDATE = "data:update:user";

    public DataUpdateService(RedisTemplate<String, String> redisTemplate,
                            RedisMessageListenerContainer redisMessageListenerContainer,
                            SimpMessagingTemplate messagingTemplate,
                            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 初始化 Redis 订阅
     */
    @PostConstruct
    public void init() {
        log.info("初始化 Redis 消息订阅...");
        
        // 订阅笔记更新
        subscribeChannel(CHANNEL_NOTE_UPDATE, "note");
        // 订阅思维导图更新
        subscribeChannel(CHANNEL_MINDMAP_UPDATE, "mindmap");
        // 订阅链接更新
        subscribeChannel(CHANNEL_LINK_UPDATE, "link");
        // 订阅AI对话更新
        subscribeChannel(CHANNEL_AI_CHAT_UPDATE, "ai-chat");
        // 订阅用户更新
        subscribeChannel(CHANNEL_USER_UPDATE, "user");
        
        log.info("Redis 消息订阅初始化完成");
    }

    /**
     * 订阅 Redis 频道
     */
    private void subscribeChannel(String channel, String type) {
        redisMessageListenerContainer.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                try {
                    String msg = new String(message.getBody());
                    log.info("收到 Redis 消息 [{}]: {}", type, msg);
                    
                    // 通过 WebSocket 推送给前端
                    pushToWebSocket(type, msg);
                } catch (Exception e) {
                    log.error("处理 Redis 消息失败", e);
                }
            }
        }, new ChannelTopic(channel));
        
        log.info("已订阅 Redis 频道: {} -> {}", channel, type);
    }

    /**
     * 通过 WebSocket 推送消息给前端
     */
    private void pushToWebSocket(String type, String action) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", type);
            payload.put("action", action);
            payload.put("timestamp", System.currentTimeMillis());
            
            // 发送到对应的 topic
            String destination = "/topic/data-update/" + type;
            messagingTemplate.convertAndSend(destination, payload);
            
            log.info("已推送 WebSocket 消息到 {}: {}", destination, payload);
        } catch (Exception e) {
            log.error("推送 WebSocket 消息失败", e);
        }
    }

    /**
     * 发布数据更新通知
     * @param type 数据类型: note, mindmap, link, ai-chat, user
     * @param action 操作类型: create, update, delete
     */
    public void publishUpdate(String type, String action) {
        try {
            String channel = switch (type) {
                case "note" -> CHANNEL_NOTE_UPDATE;
                case "mindmap" -> CHANNEL_MINDMAP_UPDATE;
                case "link" -> CHANNEL_LINK_UPDATE;
                case "ai-chat" -> CHANNEL_AI_CHAT_UPDATE;
                case "user" -> CHANNEL_USER_UPDATE;
                default -> null;
            };
            
            if (channel != null) {
                redisTemplate.convertAndSend(channel, action);
                log.info("已发布 Redis 消息: {} -> {}", channel, action);
            }
        } catch (Exception e) {
            log.error("发布 Redis 消息失败", e);
        }
    }
}
