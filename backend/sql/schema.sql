-- ============================================================
-- 错题系统数据库结构（MySQL 8.0）
-- 执行方式：
--   mysql -u root -p < backend/sql/schema.sql
-- 或在本机用 MySQL 客户端打开本文件执行一次即可。
-- 说明：所有表使用 InnoDB + utf8mb4，支持中文与 emoji。
-- ============================================================

CREATE DATABASE IF NOT EXISTS `cuotiben`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `cuotiben`;

-- ------------------------------------------------------------
-- 1. 用户表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(20)     NOT NULL COMMENT '用户名',
    `email`         VARCHAR(120)    NOT NULL COMMENT '邮箱',
    `password_hash` VARCHAR(100)    NOT NULL COMMENT 'BCrypt 加密后的密码',
    `nickname`      VARCHAR(20)              DEFAULT NULL COMMENT '昵称',
    `identity`      VARCHAR(20)              DEFAULT NULL COMMENT '身份，如 大学生/考研/四六级',
    `goal`          VARCHAR(50)              DEFAULT NULL COMMENT '当前目标，如 期末复习',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_email` (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- ------------------------------------------------------------
-- 2. 学科表（功能 4：按学科分类）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `subject`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT UNSIGNED NOT NULL COMMENT '归属用户，数据按用户隔离',
    `name`       VARCHAR(30)     NOT NULL COMMENT '学科名，如 高等数学',
    `color`      CHAR(7)                  DEFAULT '#2c4bd6' COMMENT '前端展示色值',
    `sort_no`    INT             NOT NULL DEFAULT 0 COMMENT '排序号',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_subject_user_name` (`user_id`, `name`),
    KEY `idx_subject_user` (`user_id`),
    CONSTRAINT `fk_subject_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='学科表';

-- ------------------------------------------------------------
-- 3. 知识点表（功能 4：学科 → 知识点，可两级）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `knowledge_point`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `subject_id` BIGINT UNSIGNED NOT NULL,
    `parent_id`  BIGINT UNSIGNED          DEFAULT NULL COMMENT '父知识点，NULL 表示一级',
    `name`       VARCHAR(50)     NOT NULL COMMENT '知识点名，如 洛必达法则',
    `sort_no`    INT             NOT NULL DEFAULT 0,
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_kp_user_subject` (`user_id`, `subject_id`),
    KEY `idx_kp_parent` (`parent_id`),
    CONSTRAINT `fk_kp_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_kp_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='知识点表';

-- ------------------------------------------------------------
-- 4. 标签表（功能 4：自定义多标签）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tag`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT UNSIGNED NOT NULL,
    `name`       VARCHAR(20)     NOT NULL COMMENT '标签名，如 重点题/易错题',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_user_name` (`user_id`, `name`),
    CONSTRAINT `fk_tag_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='标签表';

-- ------------------------------------------------------------
-- 5. 错题表（功能 2/3/5/7：录入、编辑、错误原因、掌握状态）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mistake`
(
    `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT UNSIGNED NOT NULL,
    `subject_id`         BIGINT UNSIGNED NOT NULL,
    `knowledge_point_id` BIGINT UNSIGNED          DEFAULT NULL,
    `title`              TEXT            NOT NULL COMMENT '题干（文字录入）',
    `answer`             TEXT                     DEFAULT NULL COMMENT '正确答案',
    `my_answer`          TEXT                     DEFAULT NULL COMMENT '当时的错误答案',
    `analysis`           TEXT                     DEFAULT NULL COMMENT '解析 / 错因分析',
    `source`             VARCHAR(200)             DEFAULT NULL COMMENT '题目来源，如 2024 期末卷第 12 题',
    `image_urls`         VARCHAR(1000)            DEFAULT NULL COMMENT '图片路径 JSON 数组，如 ["/uploads/a.png"]',
    `reason_code`        VARCHAR(30)              DEFAULT NULL COMMENT '错误原因编码，见 ErrorReasonCode',
    `reason_note`        VARCHAR(500)             DEFAULT NULL COMMENT '错因补充说明',
    `mastery_status`     VARCHAR(20)     NOT NULL DEFAULT 'NOT_REVIEWED' COMMENT 'NOT_REVIEWED/REVIEWING/MASTERED',
    `next_review_date`   DATE                     DEFAULT NULL COMMENT '下次复习日期',
    `in_review_queue`    TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '是否在待复习队列',
    `created_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_mistake_user_created` (`user_id`, `created_at`),
    KEY `idx_mistake_user_subject` (`user_id`, `subject_id`),
    KEY `idx_mistake_user_mastery` (`user_id`, `mastery_status`),
    KEY `idx_mistake_review` (`user_id`, `in_review_queue`, `next_review_date`),
    KEY `idx_mistake_kp` (`knowledge_point_id`),
    CONSTRAINT `fk_mistake_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_mistake_subject` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
    CONSTRAINT `fk_mistake_kp` FOREIGN KEY (`knowledge_point_id`) REFERENCES `knowledge_point` (`id`) ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='错题表';

-- ------------------------------------------------------------
-- 6. 错题-标签关联表（多对多）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `mistake_tag`
(
    `mistake_id` BIGINT UNSIGNED NOT NULL,
    `tag_id`     BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`mistake_id`, `tag_id`),
    KEY `idx_mt_tag` (`tag_id`),
    CONSTRAINT `fk_mt_mistake` FOREIGN KEY (`mistake_id`) REFERENCES `mistake` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_mt_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='错题-标签关联表';

-- ------------------------------------------------------------
-- 7. 复习记录表（功能 8/9：复习历史与连续打卡统计）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `review_record`
(
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `mistake_id`       BIGINT UNSIGNED NOT NULL,
    `user_id`          BIGINT UNSIGNED NOT NULL,
    `result`           VARCHAR(20)     NOT NULL COMMENT 'CORRECT/WRONG/UNSURE',
    `mastery_status`   VARCHAR(20)              DEFAULT NULL COMMENT '本次复习后的掌握状态',
    `reason_code`      VARCHAR(30)              DEFAULT NULL COMMENT '本次仍错的错误原因',
    `reason_note`      VARCHAR(500)             DEFAULT NULL,
    `next_review_date` DATE                     DEFAULT NULL COMMENT '本次设置的下次复习日期',
    `reviewed_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_review_user_time` (`user_id`, `reviewed_at`),
    KEY `idx_review_mistake` (`mistake_id`),
    CONSTRAINT `fk_review_mistake` FOREIGN KEY (`mistake_id`) REFERENCES `mistake` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='复习记录表';

-- ============================================================
-- 演示数据说明
-- 注册接口会自动为新用户初始化 5 个常用学科，
-- 因此这里不需要预置用户与错题数据；
-- 启动后端后在注册页注册一个账号即可开始录入错题。
-- ============================================================
