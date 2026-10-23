package com.cuotiben.enums;

/**
 * 错误原因，对应前端 constants/enums.js 的 ERROR_REASONS。
 */
public enum ErrorReasonCode {

    KNOWLEDGE("知识点不会"),
    THINKING("思路错误"),
    FORMULA("公式记错"),
    CALCULATION("计算错误"),
    READING("审题错误"),
    CARELESS("粗心"),
    TIME_SHORTAGE("时间不足"),
    OTHER("其他");

    private final String label;

    ErrorReasonCode(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
