-- 已有 linkx_im 库未执行完整 schema 时，单独执行本文件创建「系统开关」表
USE linkx_im;

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