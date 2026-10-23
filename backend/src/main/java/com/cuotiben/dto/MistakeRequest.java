package com.cuotiben.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * 新增/编辑错题的请求体。
 */
public record MistakeRequest(
        @NotNull(message = "请选择学科") Long subjectId,
        Long knowledgePointId,
        @NotBlank(message = "请填写题目内容") @Size(max = 5000, message = "题目内容过长") String title,
        String answer,
        String myAnswer,
        String analysis,
        @Size(max = 200, message = "来源过长") String source,
        List<String> imageUrls,
        String reasonCode,
        @Size(max = 500, message = "错因说明过长") String reasonNote,
        List<String> tags,
        String masteryStatus,
        LocalDate nextReviewDate
) {
}
