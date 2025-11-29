package com.fanfan.aiknowledgebasebackend.domain.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 管理后台统计数据VO
 */
@Data
public class DashboardStatsVO {
    
    // 总览数据
    private OverviewStats overview;
    
    // 趋势数据
    private TrendData trend;
    
    // 分类统计
    private CategoryStats category;
    
    /**
     * 总览统计
     */
    @Data
    public static class OverviewStats {
        private Long totalUsers;           // 用户总数
        private Long newUsersToday;        // 今日新增用户
        private Double userGrowthRate;     // 用户增长率
        
        private Long totalNotes;           // 笔记总数
        private Long newNotesToday;        // 今日新增笔记
        private Double noteGrowthRate;     // 笔记增长率
        
        private Long totalMindmaps;        // 思维导图总数
        private Long newMindmapsToday;     // 今日新增思维导图
        private Double mindmapGrowthRate;  // 思维导图增长率
        
        private Long totalAiQuestions;     // AI提问总数
        private Long newAiQuestionsToday;  // 今日AI提问
        private Double aiQuestionGrowthRate; // AI提问增长率
        
        private Long totalLinks;           // 链接总数
        private Long newLinksToday;        // 今日新增链接
        private Double linkGrowthRate;     // 链接增长率
        
        private Long totalViews;           // 总浏览量
        private Long totalLikes;           // 总点赞数
    }
    
    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        // 最近7天用户新增趋势
        private List<String> userDates;
        private List<Long> userCounts;
        
        // 最近7天笔记新增趋势
        private List<String> noteDates;
        private List<Long> noteCounts;
        
        // 最近7天思维导图新增趋势
        private List<String> mindmapDates;
        private List<Long> mindmapCounts;
        
        // 最近7天AI提问趋势
        private List<String> aiQuestionDates;
        private List<Long> aiQuestionCounts;
        
        // 最近7天链接新增趋势
        private List<String> linkDates;
        private List<Long> linkCounts;
    }
    
    /**
     * 分类统计
     */
    @Data
    public static class CategoryStats {
        // 笔记分类统计
        private List<CategoryItem> noteCategories;
        
        // 思维导图分类统计
        private List<CategoryItem> mindmapCategories;
        
        // 链接分类统计
        private List<CategoryItem> linkCategories;
    }
    
    /**
     * 分类项
     */
    @Data
    public static class CategoryItem {
        private String name;    // 分类名称
        private Long count;     // 数量
        private Double percentage; // 占比
    }
}
