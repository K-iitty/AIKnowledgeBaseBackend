package com.fanfan.aiknowledgebasebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fanfan.aiknowledgebasebackend.domain.entity.*;
import com.fanfan.aiknowledgebasebackend.domain.vo.DashboardStatsVO;
import com.fanfan.aiknowledgebasebackend.mapper.*;
import com.fanfan.aiknowledgebasebackend.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 仪表板服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    
    private final UserMapper userMapper;
    private final NoteMapper noteMapper;
    private final MindmapMapper mindmapMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final NoteCategoryMapper noteCategoryMapper;
    private final MindmapCategoryMapper mindmapCategoryMapper;
    
    public DashboardServiceImpl(UserMapper userMapper, NoteMapper noteMapper, 
                               MindmapMapper mindmapMapper, ChatMessageMapper chatMessageMapper,
                               NoteCategoryMapper noteCategoryMapper, MindmapCategoryMapper mindmapCategoryMapper) {
        this.userMapper = userMapper;
        this.noteMapper = noteMapper;
        this.mindmapMapper = mindmapMapper;
        this.chatMessageMapper = chatMessageMapper;
        this.noteCategoryMapper = noteCategoryMapper;
        this.mindmapCategoryMapper = mindmapCategoryMapper;
    }
    
    @Override
    public DashboardStatsVO getStatistics(Integer days) {
        // 默认7天，支持7天或30天
        if (days == null || (days != 7 && days != 30)) {
            days = 7;
        }
        
        DashboardStatsVO stats = new DashboardStatsVO();
        
        // 总览数据
        stats.setOverview(getOverviewStats());
        
        // 趋势数据（根据天数）
        stats.setTrend(getTrendData(days));
        
        // 分类统计
        stats.setCategory(getCategoryStats());
        
        return stats;
    }
    
    /**
     * 获取总览统计数据
     */
    private DashboardStatsVO.OverviewStats getOverviewStats() {
        DashboardStatsVO.OverviewStats overview = new DashboardStatsVO.OverviewStats();
        
        LocalDateTime today = LocalDate.now().atStartOfDay();
        LocalDateTime yesterday = today.minusDays(1);
        
        // 用户统计
        long totalUsers = userMapper.selectCount(null);
        long newUsersToday = userMapper.selectCount(new QueryWrapper<User>()
                .ge("created_at", today));
        long usersYesterday = userMapper.selectCount(new QueryWrapper<User>()
                .ge("created_at", yesterday).lt("created_at", today));
        
        overview.setTotalUsers(totalUsers);
        overview.setNewUsersToday(newUsersToday);
        overview.setUserGrowthRate(calculateGrowthRate(newUsersToday, usersYesterday));
        
        // 笔记统计
        long totalNotes = noteMapper.selectCount(null);
        long newNotesToday = noteMapper.selectCount(new QueryWrapper<Note>()
                .ge("created_at", today));
        long notesYesterday = noteMapper.selectCount(new QueryWrapper<Note>()
                .ge("created_at", yesterday).lt("created_at", today));
        
        overview.setTotalNotes(totalNotes);
        overview.setNewNotesToday(newNotesToday);
        overview.setNoteGrowthRate(calculateGrowthRate(newNotesToday, notesYesterday));
        
        // 思维导图统计
        long totalMindmaps = mindmapMapper.selectCount(null);
        long newMindmapsToday = mindmapMapper.selectCount(new QueryWrapper<Mindmap>()
                .ge("created_at", today));
        long mindmapsYesterday = mindmapMapper.selectCount(new QueryWrapper<Mindmap>()
                .ge("created_at", yesterday).lt("created_at", today));
        
        overview.setTotalMindmaps(totalMindmaps);
        overview.setNewMindmapsToday(newMindmapsToday);
        overview.setMindmapGrowthRate(calculateGrowthRate(newMindmapsToday, mindmapsYesterday));
        
        // AI提问统计
        long totalAiQuestions = chatMessageMapper.selectCount(new QueryWrapper<ChatMessage>()
                .eq("role", "user"));
        long newAiQuestionsToday = chatMessageMapper.selectCount(new QueryWrapper<ChatMessage>()
                .eq("role", "user")
                .ge("created_at", today));
        long aiQuestionsYesterday = chatMessageMapper.selectCount(new QueryWrapper<ChatMessage>()
                .eq("role", "user")
                .ge("created_at", yesterday).lt("created_at", today));
        
        overview.setTotalAiQuestions(totalAiQuestions);
        overview.setNewAiQuestionsToday(newAiQuestionsToday);
        overview.setAiQuestionGrowthRate(calculateGrowthRate(newAiQuestionsToday, aiQuestionsYesterday));
        
        // 浏览量和点赞数
        List<Note> notes = noteMapper.selectList(null);
        long totalViews = notes.stream().mapToLong(n -> n.getViews() == null ? 0 : n.getViews()).sum();
        long totalLikes = notes.stream().mapToLong(n -> n.getLikes() == null ? 0 : n.getLikes()).sum();
        
        overview.setTotalViews(totalViews);
        overview.setTotalLikes(totalLikes);
        
        return overview;
    }
    
    /**
     * 获取趋势数据
     * @param days 统计天数（7或30）
     */
    private DashboardStatsVO.TrendData getTrendData(Integer days) {
        DashboardStatsVO.TrendData trend = new DashboardStatsVO.TrendData();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<String> dates = new ArrayList<>();
        
        // 根据天数生成日期列表
        for (int i = days - 1; i >= 0; i--) {
            dates.add(LocalDate.now().minusDays(i).format(formatter));
        }
        
        // 用户趋势
        List<Long> userCounts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime start = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            long count = userMapper.selectCount(new QueryWrapper<User>()
                    .ge("created_at", start).lt("created_at", end));
            userCounts.add(count);
        }
        trend.setUserDates(dates);
        trend.setUserCounts(userCounts);
        
        // 笔记趋势
        List<Long> noteCounts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime start = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            long count = noteMapper.selectCount(new QueryWrapper<Note>()
                    .ge("created_at", start).lt("created_at", end));
            noteCounts.add(count);
        }
        trend.setNoteDates(dates);
        trend.setNoteCounts(noteCounts);
        
        // 思维导图趋势
        List<Long> mindmapCounts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime start = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            long count = mindmapMapper.selectCount(new QueryWrapper<Mindmap>()
                    .ge("created_at", start).lt("created_at", end));
            mindmapCounts.add(count);
        }
        trend.setMindmapDates(dates);
        trend.setMindmapCounts(mindmapCounts);
        
        // AI提问趋势
        List<Long> aiQuestionCounts = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime start = LocalDate.now().minusDays(i).atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            long count = chatMessageMapper.selectCount(new QueryWrapper<ChatMessage>()
                    .eq("role", "user")
                    .ge("created_at", start).lt("created_at", end));
            aiQuestionCounts.add(count);
        }
        trend.setAiQuestionDates(dates);
        trend.setAiQuestionCounts(aiQuestionCounts);
        
        return trend;
    }
    
    /**
     * 获取分类统计数据
     */
    private DashboardStatsVO.CategoryStats getCategoryStats() {
        DashboardStatsVO.CategoryStats category = new DashboardStatsVO.CategoryStats();
        
        // 笔记分类统计
        List<NoteCategory> noteCategories = noteCategoryMapper.selectList(null);
        long totalNotes = noteMapper.selectCount(null);
        List<DashboardStatsVO.CategoryItem> noteCategoryItems = noteCategories.stream().map(cat -> {
            long count = noteMapper.selectCount(new QueryWrapper<Note>()
                    .eq("category_id", cat.getId()));
            DashboardStatsVO.CategoryItem item = new DashboardStatsVO.CategoryItem();
            item.setName(cat.getName());
            item.setCount(count);
            item.setPercentage(totalNotes > 0 ? (count * 100.0 / totalNotes) : 0);
            return item;
        }).collect(Collectors.toList());
        category.setNoteCategories(noteCategoryItems);
        
        // 思维导图分类统计
        List<MindmapCategory> mindmapCategories = mindmapCategoryMapper.selectList(null);
        long totalMindmaps = mindmapMapper.selectCount(null);
        List<DashboardStatsVO.CategoryItem> mindmapCategoryItems = mindmapCategories.stream().map(cat -> {
            long count = mindmapMapper.selectCount(new QueryWrapper<Mindmap>()
                    .eq("category_id", cat.getId()));
            DashboardStatsVO.CategoryItem item = new DashboardStatsVO.CategoryItem();
            item.setName(cat.getName());
            item.setCount(count);
            item.setPercentage(totalMindmaps > 0 ? (count * 100.0 / totalMindmaps) : 0);
            return item;
        }).collect(Collectors.toList());
        category.setMindmapCategories(mindmapCategoryItems);
        
        return category;
    }
    
    /**
     * 计算增长率
     */
    private Double calculateGrowthRate(long today, long yesterday) {
        if (yesterday == 0) {
            return today > 0 ? 100.0 : 0.0;
        }
        return ((today - yesterday) * 100.0 / yesterday);
    }
}
