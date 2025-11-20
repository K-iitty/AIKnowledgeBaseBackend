package com.fanfan.aiknowledgebasebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}

