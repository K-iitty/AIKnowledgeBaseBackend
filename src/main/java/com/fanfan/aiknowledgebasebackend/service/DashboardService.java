package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.vo.DashboardStatsVO;

/**
 * 仪表板服务接口
 */
public interface DashboardService {
    
    /**
     * 获取统计数据
     */
    DashboardStatsVO getStatistics();
}
