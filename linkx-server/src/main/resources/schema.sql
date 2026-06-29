-- LinkX IM 数据库结构（仅 DDL，无测试数据）
-- 与当前线上一致表结构；新环境执行本文件初始化 linkx_im

CREATE DATABASE IF NOT EXISTS linkx_im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE linkx_im;

-- ============================================
-- 用户模块
-- ============================================

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL COMMENT '用户主键 ID',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值(BCrypt)',
    nickname VARCHAR(64) NOT NULL COMMENT '用户昵称',
    avatar_url VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    gender TINYINT DEFAULT 0 COMMENT '性别，0-未知，1-男，2-女',
    mobile VARCHAR(32) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态，0-禁用，1-启用',
    ban_until DATETIME DEFAULT NULL COMMENT '封禁截止时间',
    restrict_add_friend TINYINT NOT NULL DEFAULT 0 COMMENT '禁止加好友',
    restrict_create_group TINYINT NOT NULL DEFAULT 0 COMMENT '禁止建群',
    restrict_send_message TINYINT NOT NULL DEFAULT 0 COMMENT '禁止发消息',
    force_password_change TINYINT NOT NULL DEFAULT 0 COMMENT '强制改密',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录 IP',
    create_by BIGINT DEFAULT 0 COMMENT '创建人',
    update_by BIGINT DEFAULT 0 COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_sys_user_username (username),
    UNIQUE INDEX uk_sys_user_mobile (mobile),
    INDEX idx_sys_user_status (status),
    INDEX idx_sys_user_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL COMMENT '登录日志主键 ID',
    user_id BIGINT DEFAULT NULL COMMENT '登录用户 ID',
    username VARCHAR(64) NOT NULL COMMENT '登录账号',
    login_type VARCHAR(32) NOT NULL COMMENT '登录方式',
    login_ip VARCHAR(64) DEFAULT NULL COMMENT '登录 IP',
    login_location VARCHAR(128) DEFAULT NULL COMMENT '登录地点',
    user_agent VARCHAR(255) DEFAULT NULL COMMENT '客户端标识',
    login_status TINYINT NOT NULL DEFAULT 1 COMMENT '登录结果，0-失败，1-成功',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    create_by BIGINT DEFAULT 0 COMMENT '创建人',
    update_by BIGINT DEFAULT 0 COMMENT '更新人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0-未删除，1-已删除',
    PRIMARY KEY (id),
    INDEX idx_sys_login_log_user_id (user_id),
    INDEX idx_sys_login_log_username (username),
    INDEX idx_sys_login_log_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT NOT NULL COMMENT 'ID',
    token VARCHAR(1000) DEFAULT NULL COMMENT 'Token',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_token (token(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Token黑名单表';

-- ============================================
-- 好友模块
-- ============================================

CREATE TABLE IF NOT EXISTS sys_friend (
    id BIGINT NOT NULL COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    remark VARCHAR(100) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_friend (user_id, friend_id),
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

CREATE TABLE IF NOT EXISTS sys_friend_request (
    id BIGINT NOT NULL COMMENT 'ID',
    from_user_id BIGINT NOT NULL COMMENT '申请人ID',
    to_user_id BIGINT NOT NULL COMMENT '被申请人ID',
    message VARCHAR(255) DEFAULT NULL COMMENT '验证消息',
    status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已同意 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

CREATE TABLE IF NOT EXISTS sys_blacklist (
    id BIGINT NOT NULL COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    blacklist_user_id BIGINT NOT NULL COMMENT '被拉黑用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_blacklist (user_id, blacklist_user_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='黑名单表';

-- ============================================
-- 即时通讯模块
-- ============================================

CREATE TABLE IF NOT EXISTS im_session (
    id BIGINT NOT NULL COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '目标ID（单聊对端用户或群ID）',
    session_type TINYINT DEFAULT 1 COMMENT '会话类型 1单聊 2群聊',
    last_message VARCHAR(500) DEFAULT NULL COMMENT '最后一条消息',
    last_message_time DATETIME DEFAULT NULL COMMENT '最后消息时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_target (user_id, target_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天会话表';

CREATE TABLE IF NOT EXISTS im_message (
    id BIGINT NOT NULL COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    from_user_id BIGINT NOT NULL COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT COMMENT '消息内容',
    msg_type TINYINT DEFAULT 0 COMMENT '消息类型 0文本 1图片 2文件 3系统消息',
    status TINYINT DEFAULT 0 COMMENT '状态 0正常 1已撤回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    read_time DATETIME DEFAULT NULL COMMENT '已读时间',
    mention_all TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否@所有人 0否 1是',
    mention_user_ids VARCHAR(1024) DEFAULT NULL COMMENT '@成员ID列表，逗号分隔',
    client_message_id VARCHAR(64) DEFAULT NULL COMMENT '客户端幂等消息ID',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_im_message_from_client (from_user_id, client_message_id),
    INDEX idx_session_id (session_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- ============================================
-- 文件模块
-- ============================================

CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT NOT NULL COMMENT '文件ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(64) DEFAULT NULL COMMENT '文件类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_original_name (original_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ============================================
-- 群组模块
-- ============================================

CREATE TABLE IF NOT EXISTS im_group_info (
    id BIGINT NOT NULL COMMENT '群组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_avatar VARCHAR(255) DEFAULT NULL COMMENT '群头像',
    owner_id BIGINT NOT NULL COMMENT '群主ID',
    max_members INT DEFAULT 500 COMMENT '最大成员数',
    notice VARCHAR(1000) DEFAULT NULL COMMENT '群公告',
    notice_update_time DATETIME DEFAULT NULL COMMENT '群公告更新时间',
    group_status TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1封群只读 2全员禁言',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组信息表';

CREATE TABLE IF NOT EXISTS im_group_member (
    id BIGINT NOT NULL COMMENT 'ID',
    group_id BIGINT NOT NULL COMMENT '群组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT DEFAULT 0 COMMENT '角色 0普通成员 1管理员 2群主',
    mute_time DATETIME DEFAULT NULL COMMENT '禁言截止时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    notice_read_time DATETIME DEFAULT NULL COMMENT '群公告已读时间',
    group_remark VARCHAR(100) DEFAULT NULL COMMENT '群备注/别名',
    notification_muted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '消息免打扰 0否 1是',
    last_message_read_time DATETIME DEFAULT NULL COMMENT '群聊消息已读游标时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_group_user (group_id, user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

CREATE TABLE IF NOT EXISTS im_group_request (
    id BIGINT NOT NULL COMMENT 'ID',
    group_id BIGINT NOT NULL COMMENT '群组ID',
    from_user_id BIGINT NOT NULL COMMENT '发起人ID',
    to_user_id BIGINT NOT NULL COMMENT '审批人/被邀请人ID',
    request_type TINYINT DEFAULT 0 COMMENT '请求类型 0申请入群 1邀请入群',
    message VARCHAR(255) DEFAULT NULL COMMENT '申请/邀请说明',
    status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已同意 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    PRIMARY KEY (id),
    INDEX idx_group_id (group_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组申请表';

-- ============================================
-- 管理后台模块
-- ============================================

CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT NOT NULL COMMENT '管理员ID',
    username VARCHAR(64) NOT NULL COMMENT '登录名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    display_name VARCHAR(64) NOT NULL COMMENT '显示名称',
    role VARCHAR(32) NOT NULL DEFAULT 'OPERATOR' COMMENT '角色 SUPER_ADMIN/OPERATOR',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='后台管理员表';

CREATE TABLE IF NOT EXISTS sys_audit_log (
    id BIGINT NOT NULL COMMENT 'ID',
    admin_id BIGINT DEFAULT NULL COMMENT '操作管理员ID',
    admin_username VARCHAR(64) DEFAULT NULL COMMENT '操作管理员用户名',
    action VARCHAR(64) NOT NULL COMMENT '动作',
    target_type VARCHAR(64) DEFAULT NULL COMMENT '目标类型',
    target_id VARCHAR(64) DEFAULT NULL COMMENT '目标ID',
    detail VARCHAR(1000) DEFAULT NULL COMMENT '详情',
    client_ip VARCHAR(64) DEFAULT NULL COMMENT '客户端IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理操作审计日志表';

CREATE TABLE IF NOT EXISTS sys_app_release (
    id BIGINT NOT NULL COMMENT 'ID',
    platform VARCHAR(32) NOT NULL COMMENT '平台 win/mac/linux',
    version VARCHAR(32) NOT NULL COMMENT '版本号',
    download_url VARCHAR(500) DEFAULT NULL COMMENT '下载地址',
    release_notes VARCHAR(2000) DEFAULT NULL COMMENT '更新说明',
    force_update TINYINT DEFAULT 0 COMMENT '是否强制更新 0否 1是',
    published TINYINT DEFAULT 0 COMMENT '是否发布 0否 1是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_platform_version (platform, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户端版本发布表';

-- ============================================
-- P0 合规与安全
-- ============================================

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

-- ============================================
-- P1 群与用户治理扩展
-- ============================================

-- 已有库缺列时：对 sys_user / im_group_info 执行与上文 CREATE 中一致的 ALTER（列已存在则跳过）

-- ============================================
-- P2 申诉与配置中心
-- ============================================

CREATE TABLE IF NOT EXISTS sys_user_appeal (
    id BIGINT NOT NULL COMMENT '申诉ID',
    user_id BIGINT NOT NULL COMMENT '申诉用户ID',
    appeal_type VARCHAR(32) NOT NULL COMMENT 'BAN/RESTRICT/OTHER',
    reason VARCHAR(1000) NOT NULL COMMENT '申诉理由',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
    handler_admin_id BIGINT DEFAULT NULL COMMENT '处理管理员',
    handle_note VARCHAR(1000) DEFAULT NULL COMMENT '处理说明',
    handled_time DATETIME DEFAULT NULL COMMENT '处理时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_appeal_user (user_id),
    INDEX idx_appeal_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户申诉';

CREATE TABLE IF NOT EXISTS sys_runtime_config (
    id BIGINT NOT NULL COMMENT 'ID',
    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
    config_value VARCHAR(2000) NOT NULL COMMENT '配置值',
    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING' COMMENT 'STRING/INT/BOOL/JSON',
    description VARCHAR(255) DEFAULT NULL COMMENT '说明',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_runtime_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营运行时配置（系统开关等）';
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运行时配置';