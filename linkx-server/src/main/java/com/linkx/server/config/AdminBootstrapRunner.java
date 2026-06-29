package com.linkx.server.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.SysAdmin;
import com.linkx.server.mapper.SysAdminMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 确保管理表存在，并在无管理员时创建默认超级管理员。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final LinkxAdminBootstrapProperties bootstrapProperties;

    @Override
    public void run(ApplicationArguments args) {
        ensureAdminTables();
        seedDefaultAdminIfEmpty();
    }

    private void ensureAdminTables() {
        if (!tableExists("sys_admin")) {
            jdbcTemplate.execute("""
                CREATE TABLE sys_admin (
                    id BIGINT PRIMARY KEY COMMENT '管理员ID',
                    username VARCHAR(64) NOT NULL COMMENT '登录名',
                    password_hash VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
                    display_name VARCHAR(64) NOT NULL COMMENT '显示名称',
                    role VARCHAR(32) NOT NULL DEFAULT 'OPERATOR' COMMENT '角色',
                    status TINYINT DEFAULT 1 COMMENT '状态',
                    last_login_time DATETIME NULL,
                    last_login_ip VARCHAR(64) NULL,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    deleted TINYINT DEFAULT 0,
                    UNIQUE INDEX uk_admin_username (username)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表'
                """);
            log.info("Created table sys_admin");
        }
        if (!tableExists("sys_audit_log")) {
            jdbcTemplate.execute("""
                CREATE TABLE sys_audit_log (
                    id BIGINT PRIMARY KEY,
                    admin_id BIGINT NULL,
                    admin_username VARCHAR(64) NULL,
                    action VARCHAR(64) NOT NULL,
                    target_type VARCHAR(64) NULL,
                    target_id VARCHAR(64) NULL,
                    detail VARCHAR(1000) NULL,
                    client_ip VARCHAR(64) NULL,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_admin_id (admin_id),
                    INDEX idx_create_time (create_time)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            log.info("Created table sys_audit_log");
        }
        if (!tableExists("sys_app_release")) {
            jdbcTemplate.execute("""
                CREATE TABLE sys_app_release (
                    id BIGINT PRIMARY KEY,
                    platform VARCHAR(32) NOT NULL,
                    version VARCHAR(32) NOT NULL,
                    download_url VARCHAR(500) NULL,
                    release_notes VARCHAR(2000) NULL,
                    force_update TINYINT DEFAULT 0,
                    published TINYINT DEFAULT 0,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    UNIQUE INDEX uk_platform_version (platform, version)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
                """);
            log.info("Created table sys_app_release");
        }
        if (!tableExists("sys_runtime_config")) {
            jdbcTemplate.execute("""
                CREATE TABLE sys_runtime_config (
                    id BIGINT NOT NULL COMMENT 'ID',
                    config_key VARCHAR(128) NOT NULL COMMENT '配置键',
                    config_value VARCHAR(2000) NOT NULL COMMENT '配置值',
                    value_type VARCHAR(16) NOT NULL DEFAULT 'STRING' COMMENT 'STRING/INT/BOOL/JSON',
                    description VARCHAR(255) DEFAULT NULL COMMENT '说明',
                    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE INDEX uk_runtime_config_key (config_key)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='运营运行时配置（系统开关等）'
                """);
            log.info("Created table sys_runtime_config");
        }
    }

    private void seedDefaultAdminIfEmpty() {
        if (!bootstrapProperties.isEnabled()) {
            return;
        }
        Long count = adminMapper.selectCount(new LambdaQueryWrapper<>());
        if (count != null && count > 0) {
            return;
        }
        SysAdmin admin = new SysAdmin();
        admin.setUsername(bootstrapProperties.getDefaultUsername());
        admin.setPassword(passwordEncoder.encode(bootstrapProperties.getDefaultPassword()));
        admin.setDisplayName(bootstrapProperties.getDefaultDisplayName());
        admin.setRole("SUPER_ADMIN");
        admin.setStatus(1);
        admin.setDeleted(0);
        adminMapper.insert(admin);
        log.warn("已创建默认管理员账号 [{}]，请尽快登录后台修改密码（可通过环境变量 LINKX_ADMIN_BOOTSTRAP_DEFAULT_PASSWORD 指定初始密码）",
                admin.getUsername());
    }

    private boolean tableExists(String tableName) {
        Integer n = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName);
        return n != null && n > 0;
    }
}