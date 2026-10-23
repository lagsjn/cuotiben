package com.cuotiben.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    /** BCrypt 加密后的密码 */
    private String passwordHash;

    private String nickname;

    /** 身份标签，如「大三 · 计算机科学与技术」，用于首页展示 */
    @TableField("`identity`")
    private String identity;

    /** 当前目标，如「考研 + 期末复习」，用于首页展示 */
    private String goal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
