/**
 * 用户缓存服务
 * 使用Redis缓存用户会话信息，减少数据库查询
 */
package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.common.constant.RedisConstants;
import com.fanfan.aiknowledgebasebackend.common.util.RedisUtil;
import com.fanfan.aiknowledgebasebackend.domain.entity.User;
import com.fanfan.aiknowledgebasebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 用户缓存服务
 * 提供用户信息的缓存读写操作
 */
@Service
public class UserCacheService {

    private final RedisUtil redisUtil;
    private final UserMapper userMapper;

    public UserCacheService(RedisUtil redisUtil, UserMapper userMapper) {
        this.redisUtil = redisUtil;
        this.userMapper = userMapper;
    }

    /**
     * 获取用户信息（优先从缓存读取）
     * @param userId 用户ID
     * @return 用户信息
     */
    public User getUserById(Long userId) {
        String key = RedisConstants.USER_SESSION_KEY + userId;
        
        // 1. 先从缓存获取
        Object cachedUser = redisUtil.get(key);
        if (cachedUser != null) {
            System.out.println("[缓存命中] 用户ID: " + userId);
            return (User) cachedUser;
        }
        
        // 2. 缓存未命中，从数据库查询
        System.out.println("[缓存未命中] 用户ID: " + userId + "，从数据库查询");
        User user = userMapper.selectById(userId);
        
        // 3. 查询结果存入缓存
        if (user != null) {
            redisUtil.set(key, user, RedisConstants.USER_SESSION_TTL, TimeUnit.SECONDS);
            System.out.println("[缓存写入] 用户ID: " + userId + "，过期时间: " + RedisConstants.USER_SESSION_TTL + "秒");
        }
        
        return user;
    }

    /**
     * 更新用户缓存
     * @param user 用户信息
     */
    public void updateUserCache(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        String key = RedisConstants.USER_SESSION_KEY + user.getId();
        redisUtil.set(key, user, RedisConstants.USER_SESSION_TTL, TimeUnit.SECONDS);
        System.out.println("[缓存更新] 用户ID: " + user.getId());
    }

    /**
     * 删除用户缓存
     * @param userId 用户ID
     */
    public void deleteUserCache(Long userId) {
        String key = RedisConstants.USER_SESSION_KEY + userId;
        redisUtil.delete(key);
        System.out.println("[缓存删除] 用户ID: " + userId);
    }

    /**
     * 刷新用户缓存过期时间（用于滑动过期策略）
     * @param userId 用户ID
     */
    public void refreshUserCache(Long userId) {
        String key = RedisConstants.USER_SESSION_KEY + userId;
        if (Boolean.TRUE.equals(redisUtil.hasKey(key))) {
            redisUtil.expire(key, RedisConstants.USER_SESSION_TTL, TimeUnit.SECONDS);
            System.out.println("[缓存刷新] 用户ID: " + userId + "，延长过期时间");
        }
    }
}
