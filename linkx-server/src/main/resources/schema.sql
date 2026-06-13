CREATE DATABASE IF NOT EXISTS linkx_im DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE linkx_im;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像',
    signature VARCHAR(255) COMMENT '个性签名',
    gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
    region VARCHAR(100) COMMENT '地区',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0未删除 1已删除',
    UNIQUE INDEX uk_username (username),
    UNIQUE INDEX uk_phone (phone),
    UNIQUE INDEX uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

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
