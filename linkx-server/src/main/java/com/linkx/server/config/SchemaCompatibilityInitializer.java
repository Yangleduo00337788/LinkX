package com.linkx.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 应用启动后检查并补齐旧库缺失的列/索引（幂等）。
 * <p>
 * 新环境由 {@code schema.sql} 初始化；从旧版本升级时无需手工跑迁移 SQL。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureImMessageReadTimeColumn();
        ensureImMessageMentionAllColumn();
        ensureImMessageMentionUserIdsColumn();
        ensureImGroupMemberNoticeReadTimeColumn();
        ensureImGroupMemberGroupRemarkColumn();
        ensureImGroupMemberNotificationMutedColumn();
        ensureImMessageClientMessageIdColumn();
        ensureImMessageClientMessageIdUniqueIndex();
        ensureImGroupMemberLastMessageReadTimeColumn();
    }

    private void ensureImMessageReadTimeColumn() {
        if (hasColumn("im_message", "read_time")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_message ADD COLUMN read_time DATETIME NULL COMMENT '已读时间'");
        log.info("Schema compatibility repaired: added im_message.read_time column");
    }

    private void ensureImMessageMentionAllColumn() {
        if (hasColumn("im_message", "mention_all")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_message ADD COLUMN mention_all TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否@所有人 0否 1是'");
        log.info("Schema compatibility repaired: added im_message.mention_all column");
    }

    private void ensureImMessageMentionUserIdsColumn() {
        if (hasColumn("im_message", "mention_user_ids")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_message ADD COLUMN mention_user_ids VARCHAR(1024) NULL COMMENT '@成员ID列表，逗号分隔'");
        log.info("Schema compatibility repaired: added im_message.mention_user_ids column");
    }

    private void ensureImGroupMemberNoticeReadTimeColumn() {
        if (hasColumn("im_group_member", "notice_read_time")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_group_member ADD COLUMN notice_read_time DATETIME NULL COMMENT '群公告已读时间'");
        log.info("Schema compatibility repaired: added im_group_member.notice_read_time column");
    }

    private void ensureImGroupMemberGroupRemarkColumn() {
        if (hasColumn("im_group_member", "group_remark")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_group_member ADD COLUMN group_remark VARCHAR(100) NULL COMMENT '群备注/别名'");
        log.info("Schema compatibility repaired: added im_group_member.group_remark column");
    }

    private void ensureImGroupMemberNotificationMutedColumn() {
        if (hasColumn("im_group_member", "notification_muted")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_group_member ADD COLUMN notification_muted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '消息免打扰 0否 1是'");
        log.info("Schema compatibility repaired: added im_group_member.notification_muted column");
    }

    private void ensureImMessageClientMessageIdColumn() {
        if (hasColumn("im_message", "client_message_id")) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE im_message ADD COLUMN client_message_id VARCHAR(64) NULL COMMENT '客户端幂等消息ID'");
        log.info("Schema compatibility repaired: added im_message.client_message_id column");
    }

    private void ensureImMessageClientMessageIdUniqueIndex() {
        if (hasIndex("im_message", "uk_im_message_from_client")) {
            return;
        }
        jdbcTemplate.execute(
                "CREATE UNIQUE INDEX uk_im_message_from_client ON im_message (from_user_id, client_message_id)"
        );
        log.info("Schema compatibility repaired: added im_message.uk_im_message_from_client index");
    }

    private void ensureImGroupMemberLastMessageReadTimeColumn() {
        if (hasColumn("im_group_member", "last_message_read_time")) {
            return;
        }
        jdbcTemplate.execute(
                "ALTER TABLE im_group_member ADD COLUMN last_message_read_time DATETIME NULL COMMENT '群聊消息已读游标时间'"
        );
        log.info("Schema compatibility repaired: added im_group_member.last_message_read_time column");
    }

    private boolean hasIndex(String tableName, String indexName) {
        Integer indexCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = ?
                """,
                Integer.class,
                tableName,
                indexName
        );
        return indexCount != null && indexCount > 0;
    }

    private boolean hasColumn(String tableName, String columnName) {
        Integer columnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                Integer.class,
                tableName,
                columnName
        );
        return columnCount != null && columnCount > 0;
    }
}