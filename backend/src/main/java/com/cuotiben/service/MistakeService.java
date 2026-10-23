package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cuotiben.common.BusinessException;
import com.cuotiben.common.PageResult;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.dto.MistakeDetailVO;
import com.cuotiben.dto.MistakeRequest;
import com.cuotiben.dto.MistakeSummaryVO;
import com.cuotiben.entity.KnowledgePoint;
import com.cuotiben.entity.Mistake;
import com.cuotiben.entity.Subject;
import com.cuotiben.entity.Tag;
import com.cuotiben.enums.ErrorReasonCode;
import com.cuotiben.enums.MasteryStatus;
import com.cuotiben.mapper.KnowledgePointMapper;
import com.cuotiben.mapper.MistakeMapper;
import com.cuotiben.mapper.MistakeTagMapper;
import com.cuotiben.mapper.SubjectMapper;
import com.cuotiben.mapper.TagMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 错题核心服务：增删改查、掌握状态、错误原因、复习计划。
 */
@Service
public class MistakeService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MistakeMapper mistakeMapper;
    private final SubjectMapper subjectMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final TagMapper tagMapper;
    private final MistakeTagMapper mistakeTagMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MistakeService(MistakeMapper mistakeMapper,
                          SubjectMapper subjectMapper,
                          KnowledgePointMapper knowledgePointMapper,
                          TagMapper tagMapper,
                          MistakeTagMapper mistakeTagMapper) {
        this.mistakeMapper = mistakeMapper;
        this.subjectMapper = subjectMapper;
        this.knowledgePointMapper = knowledgePointMapper;
        this.tagMapper = tagMapper;
        this.mistakeTagMapper = mistakeTagMapper;
    }

    // ------------------------------------------------------------------ 查询

    /**
     * 错题列表（分页 + 检索筛选）。
     * 支持的筛选：关键词、学科、知识点、错误原因、掌握状态、标签、仅看今日待复习。
     */
    public PageResult<MistakeSummaryVO> page(Long userId, String keyword, Long subjectId, Long knowledgePointId,
                                             String reasonCode, String masteryStatus, List<Long> tagIds,
                                             Boolean dueOnly, int pageNo, int pageSize) {
        LambdaQueryWrapper<Mistake> wrapper = new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .eq(subjectId != null, Mistake::getSubjectId, subjectId)
                .eq(knowledgePointId != null, Mistake::getKnowledgePointId, knowledgePointId)
                .eq(isNotBlank(reasonCode), Mistake::getReasonCode, reasonCode)
                .eq(isNotBlank(masteryStatus), Mistake::getMasteryStatus, masteryStatus)
                .and(isNotBlank(keyword), w -> w.like(Mistake::getTitle, keyword)
                        .or().like(Mistake::getAnalysis, keyword)
                        .or().like(Mistake::getSource, keyword));

        if (Boolean.TRUE.equals(dueOnly)) {
            wrapper.eq(Mistake::getInReviewQueue, true)
                    .le(Mistake::getNextReviewDate, LocalDate.now());
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> mistakeIds = tagIds.stream()
                    .map(mistakeTagMapper::selectMistakeIds)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();
            if (mistakeIds.isEmpty()) {
                return PageResult.of(List.of(), 0, pageNo, pageSize);
            }
            wrapper.in(Mistake::getId, mistakeIds);
        }

        wrapper.orderByDesc(Mistake::getCreatedAt);
        Page<Mistake> result = mistakeMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return PageResult.of(toSummaries(result.getRecords()), result.getTotal(), pageNo, pageSize);
    }

    public MistakeDetailVO detail(Long userId, Long id) {
        Mistake mistake = requireOwned(userId, id);
        String subjectName = subjectName(mistake.getSubjectId());
        String pointName = knowledgePointName(mistake.getKnowledgePointId());
        return new MistakeDetailVO(
                mistake.getId(),
                mistake.getSubjectId(),
                subjectName,
                mistake.getKnowledgePointId(),
                pointName,
                mistake.getTitle(),
                mistake.getAnswer(),
                mistake.getMyAnswer(),
                mistake.getAnalysis(),
                mistake.getSource(),
                parseImages(mistake.getImageUrls()),
                mistake.getReasonCode(),
                reasonLabel(mistake.getReasonCode()),
                mistake.getReasonNote(),
                mistake.getMasteryStatus(),
                mistake.getNextReviewDate(),
                tagNames(id),
                mistake.getCreatedAt(),
                mistake.getUpdatedAt()
        );
    }

    // ------------------------------------------------------------------ 写入

    @Transactional
    public Long create(Long userId, MistakeRequest request) {
        requireSubject(userId, request.subjectId());
        Mistake mistake = new Mistake();
        mistake.setUserId(userId);
        applyRequest(mistake, request);
        if (!isNotBlank(mistake.getMasteryStatus())) {
            mistake.setMasteryStatus(MasteryStatus.NOT_REVIEWED.name());
        }
        if (mistake.getNextReviewDate() == null) {
            // 默认明天进入复习队列，避免刚录入就堆积成「逾期」
            mistake.setNextReviewDate(LocalDate.now().plusDays(1));
        }
        mistake.setInReviewQueue(true);
        mistake.setCreatedAt(LocalDateTime.now());
        mistake.setUpdatedAt(LocalDateTime.now());
        mistakeMapper.insert(mistake);
        syncTags(userId, mistake.getId(), request.tags());
        return mistake.getId();
    }

    /**
     * 全量更新。注意：MyBatis-Plus 的 updateById 默认忽略 null 字段，
     * 若要支持「清空某个字段」，需改用 LambdaUpdateWrapper 显式 set(null)。
     */
    @Transactional
    public void update(Long userId, Long id, MistakeRequest request) {
        Mistake mistake = requireOwned(userId, id);
        requireSubject(userId, request.subjectId());
        applyRequest(mistake, request);
        mistake.setUpdatedAt(LocalDateTime.now());
        mistakeMapper.updateById(mistake);
        syncTags(userId, id, request.tags());
    }

    @Transactional
    public void delete(Long userId, Long id) {
        requireOwned(userId, id);
        mistakeTagMapper.deleteByMistakeId(id);
        mistakeMapper.deleteById(id);
    }

    /** 修改掌握状态：标记已掌握时自动移出复习队列 */
    public void updateMastery(Long userId, Long id, String masteryStatus) {
        requireOwned(userId, id);
        if (!isMasteryStatus(masteryStatus)) {
            throw new BusinessException(40002, "掌握状态不合法");
        }
        LambdaUpdateWrapper<Mistake> update = new LambdaUpdateWrapper<Mistake>()
                .eq(Mistake::getId, id)
                .eq(Mistake::getUserId, userId)
                .set(Mistake::getMasteryStatus, masteryStatus)
                .set(Mistake::getUpdatedAt, LocalDateTime.now());
        if (MasteryStatus.MASTERED.name().equals(masteryStatus)) {
            update.set(Mistake::getInReviewQueue, false).set(Mistake::getNextReviewDate, null);
        } else {
            update.set(Mistake::getInReviewQueue, true);
        }
        mistakeMapper.update(null, update);
    }

    /** 修改错误原因 */
    public void updateErrorReason(Long userId, Long id, String reasonCode, String reasonNote) {
        requireOwned(userId, id);
        mistakeMapper.update(null, new LambdaUpdateWrapper<Mistake>()
                .eq(Mistake::getId, id)
                .eq(Mistake::getUserId, userId)
                .set(Mistake::getReasonCode, isNotBlank(reasonCode) ? reasonCode : null)
                .set(Mistake::getReasonNote, isNotBlank(reasonNote) ? reasonNote : null)
                .set(Mistake::getUpdatedAt, LocalDateTime.now()));
    }

    /** 设置下次复习日期 */
    public void schedule(Long userId, Long id, LocalDate nextReviewDate) {
        requireOwned(userId, id);
        mistakeMapper.update(null, new LambdaUpdateWrapper<Mistake>()
                .eq(Mistake::getId, id)
                .eq(Mistake::getUserId, userId)
                .set(Mistake::getNextReviewDate, nextReviewDate)
                .set(Mistake::getInReviewQueue, nextReviewDate != null)
                .set(Mistake::getUpdatedAt, LocalDateTime.now()));
    }

    // ------------------------------------------------------------------ 供首页/复习使用

    public List<MistakeSummaryVO> recent(Long userId, int limit) {
        List<Mistake> list = mistakeMapper.selectList(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .orderByDesc(Mistake::getCreatedAt)
                .last("LIMIT " + limit));
        return toSummaries(list);
    }

    public List<Mistake> dueList(Long userId, LocalDate today, int limit) {
        return mistakeMapper.selectList(new LambdaQueryWrapper<Mistake>()
                .eq(Mistake::getUserId, userId)
                .eq(Mistake::getInReviewQueue, true)
                .le(Mistake::getNextReviewDate, today)
                .orderByAsc(Mistake::getNextReviewDate)
                .orderByAsc(Mistake::getMasteryStatus)
                .last("LIMIT " + limit));
    }

    /** 今日待复习队列（含学科名、知识点名、到期文案），首页与复习页共用 */
    public List<HomeOverviewVO.QueueItem> queueItems(Long userId, LocalDate today, int limit) {
        List<Mistake> due = dueList(userId, today, limit);
        if (due.isEmpty()) {
            return List.of();
        }
        Map<Long, String> subjectNames = loadSubjectNames(due);
        Map<Long, String> pointNames = loadPointNames(due);
        List<HomeOverviewVO.QueueItem> items = new ArrayList<>(due.size());
        for (Mistake mistake : due) {
            LocalDate nextReviewDate = mistake.getNextReviewDate();
            items.add(new HomeOverviewVO.QueueItem(
                    mistake.getId(),
                    mistake.getTitle(),
                    subjectNames.get(mistake.getSubjectId()),
                    pointNames.get(mistake.getKnowledgePointId()),
                    mistake.getMasteryStatus(),
                    dueLabel(nextReviewDate),
                    nextReviewDate != null && nextReviewDate.isBefore(today)
            ));
        }
        return items;
    }

    /** 把实体批量转换成列表用的精简结构（含学科名、知识点名、标签） */
    public List<MistakeSummaryVO> toSummaries(Collection<Mistake> mistakes) {
        if (mistakes == null || mistakes.isEmpty()) {
            return List.of();
        }
        Map<Long, String> subjectNames = loadSubjectNames(mistakes);
        Map<Long, String> pointNames = loadPointNames(mistakes);
        List<MistakeSummaryVO> result = new ArrayList<>(mistakes.size());
        for (Mistake mistake : mistakes) {
            result.add(new MistakeSummaryVO(
                    mistake.getId(),
                    mistake.getTitle(),
                    subjectNames.get(mistake.getSubjectId()),
                    pointNames.get(mistake.getKnowledgePointId()),
                    tagNames(mistake.getId()),
                    mistake.getMasteryStatus(),
                    mistake.getReasonCode(),
                    reasonLabel(mistake.getReasonCode()),
                    displayDate(mistake.getCreatedAt())
            ));
        }
        return result;
    }

    /** 逾期/到期文案，首页与复习页共用 */
    public String dueLabel(LocalDate nextReviewDate) {
        if (nextReviewDate == null) {
            return "未排期";
        }
        LocalDate today = LocalDate.now();
        if (nextReviewDate.isBefore(today)) {
            return "逾期 " + (today.toEpochDay() - nextReviewDate.toEpochDay()) + " 天";
        }
        if (nextReviewDate.isEqual(today)) {
            return "今天到期";
        }
        return "还有 " + (nextReviewDate.toEpochDay() - today.toEpochDay()) + " 天";
    }

    public String reasonLabel(String reasonCode) {
        if (!isNotBlank(reasonCode)) {
            return null;
        }
        try {
            return ErrorReasonCode.valueOf(reasonCode).getLabel();
        } catch (IllegalArgumentException ex) {
            return reasonCode;
        }
    }

    // ------------------------------------------------------------------ 内部工具

    private void applyRequest(Mistake mistake, MistakeRequest request) {
        mistake.setSubjectId(request.subjectId());
        mistake.setKnowledgePointId(request.knowledgePointId());
        mistake.setTitle(request.title());
        mistake.setAnswer(request.answer());
        mistake.setMyAnswer(request.myAnswer());
        mistake.setAnalysis(request.analysis());
        mistake.setSource(request.source());
        mistake.setImageUrls(writeImages(request.imageUrls()));
        mistake.setReasonCode(request.reasonCode());
        mistake.setReasonNote(request.reasonNote());
        if (isNotBlank(request.masteryStatus())) {
            mistake.setMasteryStatus(request.masteryStatus());
        }
        if (request.nextReviewDate() != null) {
            mistake.setNextReviewDate(request.nextReviewDate());
        }
    }

    private List<String> tagNames(Long mistakeId) {
        List<Long> tagIds = mistakeTagMapper.selectTagIds(mistakeId);
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectBatchIds(tagIds).stream().map(Tag::getName).toList();
    }

    private void syncTags(Long userId, Long mistakeId, List<String> tags) {
        mistakeTagMapper.deleteByMistakeId(mistakeId);
        if (tags == null || tags.isEmpty()) {
            return;
        }
        for (String rawName : tags) {
            String name = rawName == null ? "" : rawName.trim();
            if (name.isEmpty()) {
                continue;
            }
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getUserId, userId)
                    .eq(Tag::getName, name));
            if (tag == null) {
                tag = new Tag();
                tag.setUserId(userId);
                tag.setName(name);
                tag.setCreatedAt(LocalDateTime.now());
                tagMapper.insert(tag);
            }
            mistakeTagMapper.insertOne(mistakeId, tag.getId());
        }
    }

    private Mistake requireOwned(Long userId, Long id) {
        Mistake mistake = mistakeMapper.selectById(id);
        if (mistake == null || !Objects.equals(mistake.getUserId(), userId)) {
            throw new BusinessException(40400, "错题不存在或无权访问");
        }
        return mistake;
    }

    private void requireSubject(Long userId, Long subjectId) {
        if (subjectId == null) {
            throw new BusinessException(40003, "请选择学科");
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null || !Objects.equals(subject.getUserId(), userId)) {
            throw new BusinessException(40003, "学科不存在或无权访问");
        }
    }

    private Map<Long, String> loadSubjectNames(Collection<Mistake> mistakes) {
        Set<Long> ids = mistakes.stream().map(Mistake::getSubjectId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new LinkedHashMap<>();
        for (Subject subject : subjectMapper.selectBatchIds(ids)) {
            map.put(subject.getId(), subject.getName());
        }
        return map;
    }

    private Map<Long, String> loadPointNames(Collection<Mistake> mistakes) {
        Set<Long> ids = mistakes.stream().map(Mistake::getKnowledgePointId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new LinkedHashMap<>();
        for (KnowledgePoint point : knowledgePointMapper.selectBatchIds(ids)) {
            map.put(point.getId(), point.getName());
        }
        return map;
    }

    private String subjectName(Long subjectId) {
        if (subjectId == null) {
            return null;
        }
        Subject subject = subjectMapper.selectById(subjectId);
        return subject == null ? null : subject.getName();
    }

    private String knowledgePointName(Long pointId) {
        if (pointId == null) {
            return null;
        }
        KnowledgePoint point = knowledgePointMapper.selectById(pointId);
        return point == null ? null : point.getName();
    }

    private String displayDate(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }
        LocalDate date = createdAt.toLocalDate();
        LocalDate today = LocalDate.now();
        if (date.isEqual(today)) {
            return "今天";
        }
        if (date.isEqual(today.minusDays(1))) {
            return "昨天";
        }
        if (date.isAfter(today.minusDays(7))) {
            return (today.toEpochDay() - date.toEpochDay()) + " 天前";
        }
        return date.format(DATE_FORMAT);
    }

    private List<String> parseImages(String raw) {
        if (!isNotBlank(raw)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<List<String>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String writeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception ex) {
            throw new BusinessException(40004, "图片地址格式不正确");
        }
    }

    private boolean isMasteryStatus(String value) {
        if (!isNotBlank(value)) {
            return false;
        }
        try {
            MasteryStatus.valueOf(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
