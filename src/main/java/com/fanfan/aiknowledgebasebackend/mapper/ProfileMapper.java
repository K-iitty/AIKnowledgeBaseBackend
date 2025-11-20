package com.fanfan.aiknowledgebasebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.Profile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProfileMapper extends BaseMapper<Profile> {
}
