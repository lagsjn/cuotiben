package com.cuotiben.enums;

/**
 * 掌握状态，对应前端 constants/enums.js 的 MASTERY_STATUS。
 */
public enum MasteryStatus {

    NOT_REVIEWED("未复习"),
    REVIEWING("复习中"),
    MASTERED("已掌握");

    private final String label;

    MasteryStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
