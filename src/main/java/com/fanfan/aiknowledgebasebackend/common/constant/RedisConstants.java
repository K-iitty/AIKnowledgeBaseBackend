/**
 * Redis缓存Key常量类
 * 统一管理所有Redis Key的前缀和过期时间
 */
package com.fanfan.aiknowledgebasebackend.common.constant;

/**
 * Redis缓存常量
 * 定义缓存Key前缀和过期时间
 */
public class RedisConstants {

    // ==================== 用户会话缓存 ====================
    /**
     * 用户会话缓存Key前缀
     * 格式: user:session:userId:{userId}
     */
    public static final String USER_SESSION_KEY = "user:session:userId:";
    
    /**
     * 用户会话缓存过期时间：30分钟
     */
    public static final long USER_SESSION_TTL = 30 * 60;

    // ==================== 热点笔记缓存 ====================
    /**
     * 笔记详情缓存Key前缀
     * 格式: note:detail:id:{noteId}
     */
    public static final String NOTE_DETAIL_KEY = "note:detail:id:";
    
    /**
     * 笔记内容缓存Key前缀
     * 格式: note:content:id:{noteId}
     */
    public static final String NOTE_CONTENT_KEY = "note:content:id:";
    
    /**
     * 用户笔记列表缓存Key前缀
     * 格式: note:list:userId:{userId}
     */
    public static final String NOTE_LIST_KEY = "note:list:userId:";
    
    /**
     * 笔记缓存过期时间：10分钟
     */
    public static final long NOTE_CACHE_TTL = 10 * 60;

    // ==================== 思维导图缓存 ====================
    /**
     * 思维导图详情缓存Key前缀
     * 格式: mindmap:detail:id:{mindmapId}
     */
    public static final String MINDMAP_DETAIL_KEY = "mindmap:detail:id:";
    
    /**
     * 思维导图缓存过期时间：10分钟
     */
    public static final long MINDMAP_CACHE_TTL = 10 * 60;

    // ==================== 接口限流 ====================
    /**
     * AI接口限流Key前缀
     * 格式: ratelimit:ai:userId:{userId}
     */
    public static final String RATELIMIT_AI_KEY = "ratelimit:ai:userId:";
    
    /**
     * AI接口限流时间窗口：1分钟
     */
    public static final long RATELIMIT_AI_WINDOW = 60;
    
    /**
     * AI接口限流次数：每分钟最多10次
     */
    public static final long RATELIMIT_AI_MAX_COUNT = 10;

    /**
     * 通用接口限流Key前缀
     * 格式: ratelimit:api:userId:{userId}:{apiPath}
     */
    public static final String RATELIMIT_API_KEY = "ratelimit:api:userId:";
    
    /**
     * 通用接口限流时间窗口：1分钟
     */
    public static final long RATELIMIT_API_WINDOW = 60;
    
    /**
     * 通用接口限流次数：每分钟最多100次
     */
    public static final long RATELIMIT_API_MAX_COUNT = 100;

    // ==================== 验证码缓存（已有，保留） ====================
    /**
     * 验证码缓存Key前缀（AJ-Captcha自动管理）
     * 格式: captcha:verification:{uuid}
     */
    public static final String CAPTCHA_KEY = "captcha:verification:";
    
    /**
     * 验证码过期时间：120秒
     */
    public static final long CAPTCHA_TTL = 120;
}
