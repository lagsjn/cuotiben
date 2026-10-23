package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.entity.Mistake;
import com.cuotiben.entity.ReviewRecord;
import com.cuotiben.enums.MasteryStatus;
import com.cuotiben.mapper.MistakeMapper;
import com.cuotiben.mapper.ReviewRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计服务：总体概览、近 7 天趋势、学科/错误原因分布。
 * 说明：返回 Map 结构便于骨架期快速联调，接口文档已固定字段名；
 * 后续如需强类型，可替换为 record VO，JSON 结构不变。
 */
@Service
public class StatsService {

    private final MistakeMapper mistakeMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final HomeService homeService;

    public StatsService(MistakeMapper mistakeMapper,
                        ReviewRecordMapper reviewRecordMapper,
                        HomeService homeService) {
        this.mistakeMapper = mistakeMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.homeService = homeService;
    }

    /** 统计概览：总题数、各掌握状态数量、掌握率、今日待复习 */
    public Map<String, Object> summary(Long userId) {
        LocalDate today = LocalDate.now();
        long total = count(userId, w -> {
        });
        long mastered = count(userId, w -> w.eq(Mistake::getMasteryStatus, MasteryStatus.MASTERED.name()));
        long reviewing = count(userId, w -> w.eq(Mistake::getMasteryStatus, MasteryStatus.REVIEWING.name()));
        long notReviewed = count(userId, w -> w.eq(Mistake::getMasteryStatus, MasteryStatus.NOT_REVIEWED.name()));
        long dueToday = count(userId, w -> w.eq(Mistake::getInReviewQueue, true)
                .le(Mistake::getNextReviewDate, today));

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCount", total);
        data.put("masteredCount", mastered);
        data.put("reviewingCount", reviewing);
        data.put("notReviewedCount", notReviewed);
        data.put("dueTodayCount", dueToday);
        data.put("masteredRate", total == 0 ? 0 : Math.round(mastered * 100.0 / total));
        data.put("byMastery", List.of(
                masterySlice(MasteryStatus.NOT_REVIEWED, notReviewed),
                masterySlice(MasteryStatus.REVIEWING, reviewing),
                masterySlice(MasteryStatus.MASTERED, mastered)));
        data.put("bySubject", homeService.subjectDistribution(userId, total));
        data.put("byReason", homeService.reasonDistribution(userId, total));
        return data;
    }

    /** 近 7 天趋势：每天新增错题数 + 复习次数 */
    public List<Map<String, Object>> trend(Long userId, int days) {
        LocalDate from = LocalDate.now().minusDays(days - 1L);
        LocalDateTime fromTime = from.atStartOfDay();

        List<Mistake> mistakes = mistakeMapper.selectList(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .ge(Mistake::getCreatedAt, fromTime));
        List<ReviewRecord> records = reviewRecordMapper.selectList(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getUserId, userId)
                .ge(ReviewRecord::getReviewedAt, fromTime));

        List<Map<String, Object>> result = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = from.plusDays(i);
            long added = mistakes.stream()
                    .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().toLocalDate().isEqual(date))
                    .count();
            long reviewed = records.stream()
                    .filter(r -> r.getReviewedAt() != null && r.getReviewedAt().toLocalDate().isEqual(date))
                    .count();
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date", date.toString());
            day.put("added", added);
            day.put("reviewed", reviewed);
            result.add(day);
        }
        return result;
    }

    /** 错误原因分布（百分比合计 100） */
    public List<HomeOverviewVO.ReasonSlice> reasonDistribution(Long userId) {
        return homeService.reasonDistribution(userId, count(userId, w -> {
        }));
    }

    /** 学科分布 */
    public List<HomeOverviewVO.SubjectSlice> subjectDistribution(Long userId) {
        return homeService.subjectDistribution(userId, count(userId, w -> {
        }));
    }

    private Map<String, Object> masterySlice(MasteryStatus status, long count) {
        Map<String, Object> slice = new LinkedHashMap<>();
        slice.put("status", status.name());
        slice.put("label", status.getLabel());
        slice.put("count", count);
        return slice;
    }

    private long count(Long userId, java.util.function.Consumer<LambdaQueryWrapper<Mistake>> customizer) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>().eq(Mistake::getUserId, userId);
        customizer.accept(wrapper);
        Long value = mistakeMapper.selectCount(wrapper);
        return value == null ? 0L : value;
    }
}
