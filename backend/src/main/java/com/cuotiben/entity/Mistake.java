package com.cuotiben.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mistake")
public class Mistake {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long subjectId;

    private Long knowledgePointId;

    /** 题干 */
    private String title;

    /** 正确答案 / 解析 */
    private String answer;

    /** 自己当时的错误答案 */
    private String myAnswer;

    /** 错因分析 */
    private String analysis;

    /** 题目来源，如「2024 期末卷 第 12 题」 */
    private String source;

    /** 图片路径列表，JSON 数组字符串，如 ["/uploads/a.png"] */
    private String imageUrls;

    /** 错误原因，取值见 ErrorReasonCode */
    private String reasonCode;

    /** 错误原因补充说明 */
    private String reasonNote;

    /** 掌握状态，取值见 MasteryStatus */
    private String masteryStatus;

    /** 下次复习日期 */
    private LocalDate nextReviewDate;

    /** 是否在复习队列里（录入后默认 false，需要复习时置为 true） */
    private Boolean inReviewQueue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
