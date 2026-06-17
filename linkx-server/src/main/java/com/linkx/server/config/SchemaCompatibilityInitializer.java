package com.linkx.server.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
    }

    private void ensureImMessageReadTimeColumn() {
        Integer columnCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'im_message'
                  AND COLUMN_NAME = 'read_time'
                """,
                Integer.class
        );
        if (columnCount != null && columnCount > 0) {
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
