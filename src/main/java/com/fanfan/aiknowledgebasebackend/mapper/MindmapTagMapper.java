package com.fanfan.aiknowledgebasebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.MindmapTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MindmapTagMapper extends BaseMapper<MindmapTag> {
    @Select("SELECT t.* FROM mindmap_tags t " +
            "INNER JOIN mindmap_tag_relations r ON t.id = r.tag_id " +
            "WHERE r.mindmap_id = #{mindmapId}")
    List<MindmapTag> selectTagsByMindmapId(Long mindmapId);
}