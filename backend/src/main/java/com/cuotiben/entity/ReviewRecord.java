package com.cuotiben.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("review_record")
public class ReviewRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long mistakeId;

    private Long userId;

    /** 复习结果，取值见 ReviewResult */
    private String result;

    /** 本次复习后的掌握状态 */
    private String masteryStatus;

    /** 本次记录下的错误原因（可选） */
    private String reasonCode;

    private String reasonNote;

    /** 下次复习日期 */
    private LocalDate nextReviewDate;

    private LocalDateTime reviewedAt;
}
