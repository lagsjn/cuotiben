package com.cuotiben.enums;

/**
 * 单次复习结果，对应前端 constants/enums.js 的 REVIEW_RESULTS。
 */
public enum ReviewResult {

    CORRECT("答对了"),
    WRONG("还是错"),
    UNSURE("不太确定");

    private final String label;

    ReviewResult(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
