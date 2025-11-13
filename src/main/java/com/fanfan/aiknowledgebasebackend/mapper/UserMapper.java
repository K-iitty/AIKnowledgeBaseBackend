package com.fanfan.aiknowledgebasebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanfan.aiknowledgebasebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
