package com.fanfan.aiknowledgebasebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanfan.aiknowledgebasebackend.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {
}
