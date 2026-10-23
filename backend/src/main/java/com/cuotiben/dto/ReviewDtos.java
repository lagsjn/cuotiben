package com.cuotiben.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 复习提交/复习计划相关请求体。
 */
public final class ReviewDtos {

    private ReviewDtos() {
    }

    /** 前端提交复习结果的完整请求体：POST /api/reviews */
    public record CreateRequest(
            @NotNull(message = "缺少错题 id") Long mistakeId,
            @NotBlank(message = "缺少复习结果") String result,
            String reasonCode,
            String reasonNote,
            LocalDate nextReviewDate,
            String masteryStatus
    ) {
    }

    /** 提交一次复习结果 */
    public record SubmitRequest(
            @NotBlank(message = "缺少复习结果") String result,
            String reasonCode,
            String reasonNote,
            LocalDate nextReviewDate,
            String masteryStatus
    ) {
    }

    /** 只修改下次复习日期 */
    public record ScheduleRequest(LocalDate nextReviewDate) {
    }

    /** 只修改掌握状态 */
    public record MasteryRequest(@NotBlank(message = "缺少掌握状态") String masteryStatus) {
    }

    /** 只修改错误原因 */
    public record ErrorReasonRequest(String reasonCode, String reasonNote) {
    }
}
