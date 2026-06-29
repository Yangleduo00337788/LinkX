-- 会话置顶/备注/免打扰、群内名片、群公告多条、群精华
-- 可重复执行：ADD COLUMN 前会检查列是否已存在
USE linkx_im;

-- im_session
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_session' AND COLUMN_NAME = 'session_remark') = 0,
    'ALTER TABLE im_session ADD COLUMN session_remark VARCHAR(100) NULL COMMENT ''会话备注'' AFTER unread_count',
    'SELECT ''skip session_remark''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_session' AND COLUMN_NAME = 'pinned') = 0,
    'ALTER TABLE im_session ADD COLUMN pinned TINYINT NOT NULL DEFAULT 0 COMMENT ''置顶 0否 1是'' AFTER session_remark',
    'SELECT ''skip pinned''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_session' AND COLUMN_NAME = 'notification_muted') = 0,
    'ALTER TABLE im_session ADD COLUMN notification_muted TINYINT NOT NULL DEFAULT 0 COMMENT ''免打扰 0否 1是'' AFTER pinned',
    'SELECT ''skip notification_muted''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_session' AND COLUMN_NAME = 'history_clear_time') = 0,
    'ALTER TABLE im_session ADD COLUMN history_clear_time DATETIME NULL COMMENT ''清空聊天记录时间点'' AFTER notification_muted',
    'SELECT ''skip history_clear_time''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- im_group_member
SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'im_group_member' AND COLUMN_NAME = 'member_card_name') = 0,
    'ALTER TABLE im_group_member ADD COLUMN member_card_name VARCHAR(64) NULL COMMENT ''我在本群的群名片'' AFTER group_remark',
    'SELECT ''skip member_card_name''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS im_group_notice (
    id BIGINT NOT NULL COMMENT '公告ID',
    group_id BIGINT NOT NULL COMMENT '群ID',
    content VARCHAR(2000) NOT NULL COMMENT '公告内容',
    pinned TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶展示 0否 1是',
    publisher_id BIGINT NOT NULL COMMENT '发布者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_group_notice_group (group_id, pinned, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群公告（多条）';

CREATE TABLE IF NOT EXISTS im_group_highlight (
    id BIGINT NOT NULL COMMENT '精华ID',
    group_id BIGINT NOT NULL COMMENT '群ID',
    message_id BIGINT NOT NULL COMMENT '消息ID',
    title VARCHAR(200) NULL COMMENT '标题摘要',
    created_by BIGINT NOT NULL COMMENT '设置者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_group_highlight_msg (group_id, message_id),
    INDEX idx_group_highlight_group (group_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群精华';

-- 将 im_group_info 现有单条公告迁入 im_group_notice（仅当该群尚无公告记录时）
INSERT INTO im_group_notice (id, group_id, content, pinned, publisher_id, create_time, update_time, deleted)
SELECT
    (UNIX_TIMESTAMP(COALESCE(g.notice_update_time, g.create_time, NOW())) * 1000000 + g.id) AS id,
    g.id AS group_id,
    g.notice AS content,
    1 AS pinned,
    g.owner_id AS publisher_id,
    COALESCE(g.notice_update_time, g.create_time, NOW()) AS create_time,
    COALESCE(g.notice_update_time, NOW()) AS update_time,
    0 AS deleted
FROM im_group_info g
WHERE g.deleted = 0
  AND g.notice IS NOT NULL
  AND TRIM(g.notice) <> ''
  AND NOT EXISTS (SELECT 1 FROM im_group_notice n WHERE n.group_id = g.id AND n.deleted = 0);