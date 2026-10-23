package com.cuotiben.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cuotiben.common.BusinessException;
import com.cuotiben.dto.HomeOverviewVO;
import com.cuotiben.dto.ReviewDtos;
import com.cuotiben.entity.Mistake;
import com.cuotiben.entity.ReviewRecord;
import com.cuotiben.mapper.MistakeMapper;
import com.cuotiben.mapper.ReviewRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 复习服务：今日待复习队列、提交复习结果、复习记录。
 */
@Service
public class ReviewService {

    private final MistakeMapper mistakeMapper;
    private final MistakeService mistakeService;
    private final ReviewRecordMapper reviewRecordMapper;

    public ReviewService(MistakeMapper mistakeMapper,
                         MistakeService mistakeService,
                         ReviewRecordMapper reviewRecordMapper) {
        this.mistakeMapper = mistakeMapper;
        this.mistakeService = mistakeService;
        this.reviewRecordMapper = reviewRecordMapper;
    }

    /** 今日待复习列表（按到期时间升序） */
    public List<HomeOverviewVO.QueueItem> queue(Long userId) {
        return mistakeService.queueItems(userId, LocalDate.now(), 50);
    }

    /**
     * 提交一次复习结果：
     * 1. 写入 review_record 历史；
     * 2. 更新错题的掌握状态、错误原因、下次复习日期。
     */
    @Transactional
    public void submit(Long userId, Long mistakeId, ReviewDtos.SubmitRequest request) {
        Mistake mistake = mistakeMapper.selectById(mistakeId);
        if (mistake == null || !Objects.equals(mistake.getUserId(), userId)) {
            throw new BusinessException(40400, "错题不存在或无权访问");
        }

        String masteryStatus = isNotBlank(request.masteryStatus())
                ? request.masteryStatus()
                : suggestMastery(request.result());
        LocalDate nextReviewDate = request.nextReviewDate();

        ReviewRecord record = new ReviewRecord();
        record.setMistakeId(mistakeId);
        record.setUserId(userId);
        record.setResult(request.result());
        record.setMasteryStatus(masteryStatus);
        record.setReasonCode(isNotBlank(request.reasonCode()) ? request.reasonCode() : mistake.getReasonCode());
        record.setReasonNote(isNotBlank(request.reasonNote()) ? request.reasonNote() : mistake.getReasonNote());
        record.setNextReviewDate(nextReviewDate);
        record.setReviewedAt(LocalDateTime.now());
        reviewRecordMapper.insert(record);

        mistakeService.updateMastery(userId, mistakeId, masteryStatus);
        if (nextReviewDate != null) {
            mistakeService.schedule(userId, mistakeId, nextReviewDate);
        }
        if (isNotBlank(request.reasonCode())) {
            mistakeService.updateErrorReason(userId, mistakeId, request.reasonCode(), request.reasonNote());
        }
    }

    /** 复习历史（最近的在前） */
    public List<ReviewRecord> history(Long userId, Long mistakeId) {
        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getUserId, userId)
                .eq(mistakeId != null, ReviewRecord::getMistakeId, mistakeId)
                .orderByDesc(ReviewRecord::getReviewedAt)
                .last("LIMIT 100");
        return reviewRecordMapper.selectList(wrapper);
    }

    /** 根据复习结果推荐掌握状态 */
    private String suggestMastery(String result) {
        if (result == null) {
            return "REVIEWING";
        }
        return switch (result) {
            case "CORRECT" -> "MASTERED";
            case "WRONG" -> "NOT_REVIEWED";
            default -> "REVIEWING";
        };
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }
}
