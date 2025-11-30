package com.fanfan.aiknowledgebasebackend.service;

import com.fanfan.aiknowledgebasebackend.domain.vo.DashboardStatsVO;

/**
 * 仪表板服务接口
 */
public interface DashboardService {
    
    /**
     * 获取统计数据
     * @param days 统计天数（7或30）
     */
    DashboardStatsVO getStatistics(Integer days);
}
