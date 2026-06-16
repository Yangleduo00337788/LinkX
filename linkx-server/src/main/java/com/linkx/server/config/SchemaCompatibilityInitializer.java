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
}
