package com.cuotiben.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 错题详情：GET /api/mistakes/{id}
 */
public record MistakeDetailVO(
        Long id,
        Long subjectId,
        String subjectName,
        Long knowledgePointId,
        String knowledgePointName,
        String title,
        String answer,
        String myAnswer,
        String analysis,
        String source,
        List<String> imageUrls,
        String reasonCode,
        String reasonLabel,
        String reasonNote,
        String masteryStatus,
        LocalDate nextReviewDate,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
