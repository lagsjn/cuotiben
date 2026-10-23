package com.cuotiben.dto;

import java.util.List;

/**
 * 首页概览数据：GET /api/home/overview
 * 字段与前端 src/views/HomeView.vue 使用的 mock 结构保持一致。
 */
public record HomeOverviewVO(
        Profile profile,
        Stats stats,
        List<QueueItem> reviewQueue,
        List<SubjectSlice> subjectDistribution,
        List<ReasonSlice> reasonDistribution,
        List<MistakeSummaryVO> recentMistakes
) {

    /** 顶部称呼信息 */
    public record Profile(String nickname, String identity, String goal) {
    }

    /** 四张统计卡片 */
    public record Stats(
            int totalCount,
            int dueTodayCount,
            int overdueCount,
            int masteredCount,
            int masteredRate,
            int newThisWeekCount,
            int newGrowth,
            int streakDays
    ) {
    }

    /** 今日待复习条目 */
    public record QueueItem(
            Long id,
            String title,
            String subjectName,
            String knowledgePointName,
            String masteryStatus,
            String dueLabel,
            boolean overdue
    ) {
    }

    /** 学科分布 */
    public record SubjectSlice(String name, int count, String color) {
    }

    /** 错误原因分布 */
    public record ReasonSlice(String code, String name, int count, int percent) {
    }
}
