package com.cuotiben.dto;

import java.util.List;

/**
 * 导出相关请求体。
 */
public final class ExportDtos {

    private ExportDtos() {
    }

    /**
     * 导出请求：
     * format 取 WORD / PDF；mistakeIds 为空表示导出当前筛选条件下的全部错题。
     */
    public record ExportRequest(String format, List<Long> mistakeIds, Long subjectId, String masteryStatus) {
    }
}
