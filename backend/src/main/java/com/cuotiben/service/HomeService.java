package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.dto.MistakeSummaryVO;
import com.cuotiben.entity.Mistake;
import com.cuotiben.entity.ReviewRecord;
import com.cuotiben.entity.Subject;
import com.cuotiben.entity.User;
import com.cuotiben.enums.ErrorReasonCode;
import com.cuotiben.enums.MasteryStatus;
import com.cuotiben.mapper.MistakeMapper;
import com.cuotiben.mapper.ReviewRecordMapper;
import com.cuotiben.mapper.SubjectMapper;
import com.cuotiben.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 首页概览：GET /api/home/overview
 * 说明：骨架阶段用多次简单查询拼装（数据量小、逻辑清晰），
 * 后续可改为 GROUP BY 聚合 SQL 或缓存，接口结构不用变。
 */
@Service
public class HomeService {

    private final MistakeMapper mistakeMapper;
    private final SubjectMapper subjectMapper;
    private final UserMapper userMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final MistakeService mistakeService;

    public HomeService(MistakeMapper mistakeMapper,
                       SubjectMapper subjectMapper,
                       UserMapper userMapper,
                       ReviewRecordMapper reviewRecordMapper,
                       MistakeService mistakeService) {
        this.mistakeMapper = mistakeMapper;
        this.subjectMapper = subjectMapper;
        this.userMapper = userMapper;
        this.reviewRecordMapper = reviewRecordMapper;
        this.mistakeService = mistakeService;
    }

    public HomeOverviewVO overview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        long total = count(userId, w -> {
        });
        long overdue = count(userId, w -> w.eq(Mistake::getInReviewQueue, true)
                .lt(Mistake::getNextReviewDate, today));
        long dueToday = count(userId, w -> w.eq(Mistake::getInReviewQueue, true)
                .le(Mistake::getNextReviewDate, today));
        long mastered = count(userId, w -> w.eq(Mistake::getMasteryStatus, MasteryStatus.MASTERED.name()));
        long newThisWeek = count(userId, w -> w.ge(Mistake::getCreatedAt, weekStart.atStartOfDay()));
        long lastWeek = count(userId, w -> w.ge(Mistake::getCreatedAt, weekStart.minusWeeks(1).atStartOfDay())
                .lt(Mistake::getCreatedAt, weekStart.atStartOfDay()));

        int masteredRate = total == 0 ? 0 : (int) Math.round(mastered * 100.0 / total);

        HomeOverviewVO.Profile profile = buildProfile(userId);
        HomeOverviewVO.Stats stats = new HomeOverviewVO.Stats(
                (int) total,
                (int) dueToday,
                (int) overdue,
                (int) mastered,
                masteredRate,
                (int) newThisWeek,
                (int) (newThisWeek - lastWeek),
                streakDays(userId)
        );

        return new HomeOverviewVO(
                profile,
                stats,
                mistakeService.queueItems(userId, today, 6),
                subjectDistribution(userId, total),
                reasonDistribution(userId, total),
                mistakeService.recent(userId, 4)
        );
    }

    /** 未登录访客：返回空概览，让首页作为落地页也能正常渲染 */
    public HomeOverviewVO guestOverview() {
        return new HomeOverviewVO(
                new HomeOverviewVO.Profile("同学", "大学生", "期末复习"),
                new HomeOverviewVO.Stats(0, 0, 0, 0, 0, 0, 0, 0),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    /** 供统计页复用的分布数据 */
    public List<HomeOverviewVO.SubjectSlice> subjectDistribution(Long userId, long total) {
        List<Subject> subjects = subjectMapper.selectList(new LambdaQueryWrapper<Subject>()
                .eq(Subject::getUserId, userId)
                .orderByAsc(Subject::getSortNo)
                .orderByAsc(Subject::getId));
        List<HomeOverviewVO.SubjectSlice> slices = new ArrayList<>();
        for (Subject subject : subjects) {
            long count = count(userId, w -> w.eq(Mistake::getSubjectId, subject.getId()));
            if (count > 0) {
                slices.add(new HomeOverviewVO.SubjectSlice(subject.getName(), (int) count, subject.getColor()));
            }
        }
        slices.sort(Comparator.comparingInt(HomeOverviewVO.SubjectSlice::count).reversed());
        return slices;
    }

    /** 错误原因分布，百分比按「有错误原因的错题」计算，合计 100 */
    public List<HomeOverviewVO.ReasonSlice> reasonDistribution(Long userId, long total) {
        Map<ErrorReasonCode, Long> counter = new LinkedHashMap<>();
        long sum = 0;
        for (ErrorReasonCode code : ErrorReasonCode.values()) {
            long count = count(userId, w -> w.eq(Mistake::getReasonCode, code.name()));
            if (count > 0) {
                counter.put(code, count);
                sum += count;
            }
        }
        List<HomeOverviewVO.ReasonSlice> slices = new ArrayList<>();
        for (Map.Entry<ErrorReasonCode, Long> entry : counter.entrySet()) {
            int percent = sum == 0 ? 0 : (int) Math.round(entry.getValue() * 100.0 / sum);
            slices.add(new HomeOverviewVO.ReasonSlice(
                    entry.getKey().name(),
                    entry.getKey().getLabel(),
                    entry.getValue().intValue(),
                    percent));
        }
        slices.sort(Comparator.comparingInt(HomeOverviewVO.ReasonSlice::count).reversed());
        return slices;
    }

    public List<MistakeSummaryVO> recent(Long userId, int limit) {
        return mistakeService.recent(userId, limit);
    }

    private HomeOverviewVO.Profile buildProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return new HomeOverviewVO.Profile("同学", "大学生", "期末复习");
        }
        String nickname = isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername();
        String identity = isNotBlank(user.getIdentity()) ? user.getIdentity() : "大学生";
        String goal = isNotBlank(user.getGoal()) ? user.getGoal() : "期末复习";
        return new HomeOverviewVO.Profile(nickname, identity, goal);
    }

    /**
     * 连续复习天数：从今天（今天还没复习则从昨天）往前数连续的复习日期。
     */
    private int streakDays(Long userId) {
        List<ReviewRecord> records = reviewRecordMapper.selectList(
                new LambdaQueryWrapper<ReviewRecord>()
                        .eq(ReviewRecord::getUserId, userId)
                        .ge(ReviewRecord::getReviewedAt, LocalDateTime.now().minusDays(90)));
        Set<LocalDate> days = records.stream()
                .map(ReviewRecord::getReviewedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());
        if (days.isEmpty()) {
            return 0;
        }
        LocalDate cursor = LocalDate.now();
        if (!days.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        int streak = 0;
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private long count(Long userId, java.util.function.Consumer<LambdaQueryWrapper<Mistake>> customizer) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId);
        customizer.accept(wrapper);
        Long value = mistakeMapper.selectCount(wrapper);
        return value == null ? 0L : value;
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
