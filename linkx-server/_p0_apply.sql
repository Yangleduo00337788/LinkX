USE linkx_im;

CREATE TABLE IF NOT EXISTS sys_report (
    id BIGINT NOT NULL COMMENT '举报单ID',
    reporter_user_id BIGINT NOT NULL COMMENT '举报人用户ID',
    target_type VARCHAR(32) NOT NULL COMMENT '目标类型 USER/MESSAGE/GROUP/FILE',
    target_id VARCHAR(64) NOT NULL COMMENT '目标ID',
    reason_category VARCHAR(32) NOT NULL COMMENT '原因分类 SPAM/ABUSE/FRAUD/PORNOGRAPHY/OTHER',
    reason_detail VARCHAR(1000) DEFAULT NULL COMMENT '补充说明',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已结案 3已驳回',
    handler_admin_id BIGINT DEFAULT NULL COMMENT '处理管理员ID',
    resolution VARCHAR(32) DEFAULT NULL COMMENT '处置结果 WARN/DELETE_CONTENT/BAN_USER/BAN_GROUP/DISMISS',
    resolution_note VARCHAR(1000) DEFAULT NULL COMMENT '处理说明',
    notify_reporter TINYINT NOT NULL DEFAULT 1 COMMENT '是否通知举报人 0否 1是',
    reporter_notified TINYINT NOT NULL DEFAULT 0 COMMENT '是否已通知举报人',
    handled_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_sys_report_status (status),
    INDEX idx_sys_report_target (target_type, target_id),
    INDEX idx_sys_report_reporter (reporter_user_id),
    INDEX idx_sys_report_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户举报工单表';

CREATE TABLE IF NOT EXISTS sys_sensitive_word (
    id BIGINT NOT NULL COMMENT 'ID',
    word VARCHAR(128) NOT NULL COMMENT '敏感词',
    match_mode TINYINT NOT NULL DEFAULT 1 COMMENT '1包含 2全词匹配',
    category VARCHAR(32) DEFAULT 'DEFAULT' COMMENT '分类',
    action TINYINT NOT NULL DEFAULT 1 COMMENT '1拦截 2仅记录',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sensitive_word (word),
    INDEX idx_sensitive_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词库';

CREATE TABLE IF NOT EXISTS sys_sensitive_hit_log (
    id BIGINT NOT NULL COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '触发用户ID',
    word_id BIGINT DEFAULT NULL COMMENT '命中词ID',
    matched_word VARCHAR(128) DEFAULT NULL COMMENT '命中词快照',
    content_snippet VARCHAR(500) DEFAULT NULL COMMENT '内容摘要',
    source VARCHAR(32) NOT NULL DEFAULT 'CHAT_SEND' COMMENT '来源',
    blocked TINYINT NOT NULL DEFAULT 0 COMMENT '是否已拦截',
    message_id BIGINT DEFAULT NULL COMMENT '关联消息ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_hit_user (user_id),
    INDEX idx_hit_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='敏感词命中记录';

CREATE TABLE IF NOT EXISTS sys_file_hash_blacklist (
    id BIGINT NOT NULL COMMENT 'ID',
    content_hash VARCHAR(128) NOT NULL COMMENT '文件内容哈希 SHA-256',
    reason VARCHAR(255) DEFAULT NULL COMMENT '拉黑原因',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_file_hash (content_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件哈希黑名单';

CREATE TABLE IF NOT EXISTS sys_user_notification (
    id BIGINT NOT NULL COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(128) NOT NULL COMMENT '标题',
    content VARCHAR(1000) NOT NULL COMMENT '内容',
    biz_type VARCHAR(32) DEFAULT NULL COMMENT '业务类型 REPORT_RESULT/APPEAL/SYSTEM',
    biz_id VARCHAR(64) DEFAULT NULL COMMENT '业务ID',
    read_flag TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_user_notification_user (user_id),
    INDEX idx_user_notification_read (user_id, read_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内通知';