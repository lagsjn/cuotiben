package com.cuotiben.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 错题与标签的多对多关联表。
 */
@Data
@TableName("mistake_tag")
public class MistakeTag {

    private Long mistakeId;

    private Long tagId;
}
