CREATE DATABASE IF NOT EXISTS linkx_im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE linkx_im;

-- ============================================
-- 用户模块
-- ============================================

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    nickname VARCHAR(64) NOT NULL COMMENT '昵称',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    mobile VARCHAR(32) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(64) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_mobile (mobile)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT COMMENT '用户ID',
    login_ip VARCHAR(100) COMMENT '登录IP',
    device VARCHAR(255) COMMENT '设备',
    os VARCHAR(100) COMMENT '操作系统',
    browser VARCHAR(100) COMMENT '浏览器',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- Token黑名单表
CREATE TABLE IF NOT EXISTS sys_token_blacklist (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    token VARCHAR(1000) COMMENT 'Token',
    expire_time DATETIME COMMENT '过期时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_token (token(100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token黑名单表';

-- ============================================
-- 好友模块
-- ============================================

-- 好友关系表
CREATE TABLE IF NOT EXISTS sys_friend (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    friend_id BIGINT NOT NULL COMMENT '好友ID',
    remark VARCHAR(100) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_friend_id (friend_id),
    UNIQUE INDEX uk_user_friend (user_id, friend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 好友申请表
CREATE TABLE IF NOT EXISTS sys_friend_request (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    from_user_id BIGINT NOT NULL COMMENT '申请人ID',
    to_user_id BIGINT NOT NULL COMMENT '被申请人ID',
    message VARCHAR(255) COMMENT '验证消息',
    status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已同意 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_to_user (to_user_id),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- 黑名单表
CREATE TABLE IF NOT EXISTS sys_blacklist (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    blacklist_user_id BIGINT NOT NULL COMMENT '被拉黑用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_user_blacklist (user_id, blacklist_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单表';

-- ============================================
-- 即时通讯模块
-- ============================================

-- 聊天会话表
CREATE TABLE IF NOT EXISTS im_session (
    id BIGINT PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    target_id BIGINT NOT NULL COMMENT '目标用户ID',
    session_type TINYINT DEFAULT 1 COMMENT '会话类型 1单聊 2群聊',
    last_message VARCHAR(500) COMMENT '最后一条消息',
    last_message_time DATETIME COMMENT '最后消息时间',
    unread_count INT DEFAULT 0 COMMENT '未读消息数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_user_target (user_id, target_id, session_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS im_message (
    id BIGINT PRIMARY KEY COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    from_user_id BIGINT NOT NULL COMMENT '发送者ID',
    to_user_id BIGINT NOT NULL COMMENT '接收者ID',
    content TEXT COMMENT '消息内容',
    msg_type TINYINT DEFAULT 0 COMMENT '消息类型 0文本 1图片 2文件 3系统消息',
    status TINYINT DEFAULT 0 COMMENT '状态 0正常 1已撤回',
    read_time DATETIME COMMENT '已读时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_session_id (session_id),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

-- ============================================
-- 文件模块
-- ============================================

-- 文件记录表
CREATE TABLE IF NOT EXISTS sys_file (
    id BIGINT PRIMARY KEY COMMENT '文件ID',
    user_id BIGINT NOT NULL COMMENT '上传用户ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_url VARCHAR(500) NOT NULL COMMENT '文件访问URL',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(64) COMMENT '文件类型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_original_name (original_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- ============================================
-- 群组模块（Sprint 5）
-- ============================================

-- 群组信息表
CREATE TABLE IF NOT EXISTS im_group_info (
    id BIGINT PRIMARY KEY COMMENT '群组ID',
    group_name VARCHAR(100) NOT NULL COMMENT '群名称',
    group_avatar VARCHAR(255) COMMENT '群头像',
    owner_id BIGINT NOT NULL COMMENT '群主ID',
    max_members INT DEFAULT 500 COMMENT '最大成员数',
    notice VARCHAR(1000) COMMENT '群公告',
    notice_update_time DATETIME COMMENT '群公告更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组信息表';

-- 群组成员表
CREATE TABLE IF NOT EXISTS im_group_member (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    group_id BIGINT NOT NULL COMMENT '群组ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role TINYINT DEFAULT 0 COMMENT '角色 0普通成员 1管理员 2群主',
    mute_time DATETIME COMMENT '禁言截止时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_group_user (group_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组成员表';

CREATE TABLE IF NOT EXISTS im_group_request (
    id BIGINT PRIMARY KEY COMMENT 'ID',
    group_id BIGINT NOT NULL COMMENT '群组ID',
    from_user_id BIGINT NOT NULL COMMENT '发起人ID',
    to_user_id BIGINT NOT NULL COMMENT '审批人/被邀请人ID',
    request_type TINYINT DEFAULT 0 COMMENT '请求类型 0申请入群 1邀请入群',
    message VARCHAR(255) COMMENT '申请/邀请说明',
    status TINYINT DEFAULT 0 COMMENT '状态 0待处理 1已同意 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    handle_time DATETIME COMMENT '处理时间',
    INDEX idx_group_id (group_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_from_user (from_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群组申请表';
