package com.cuotiben.dto;

import java.util.List;

/**
 * 错题列表/最近录入使用的精简结构。
 */
public record MistakeSummaryVO(
        Long id,
        String title,
        String subjectName,
        String knowledgePointName,
        List<String> tags,
        String masteryStatus,
        String reasonCode,
        String reasonLabel,
        String displayDate
) {
}
