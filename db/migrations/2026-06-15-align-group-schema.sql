-- LinkX IM
-- Purpose: rebuild empty group tables to match current Java entities.
-- Target DB: linkx_im
-- Preconditions already verified:
--   im_group_info row count = 0
--   im_group_member row count = 0

USE `linkx_im`;

SELECT DATABASE() AS current_db;
SELECT COUNT(*) AS group_info_count FROM im_group_info;
SELECT COUNT(*) AS group_member_count FROM im_group_member;

-- Drop old empty tables in dependency order.
DROP TABLE IF EXISTS `im_group_member`;
DROP TABLE IF EXISTS `im_group_info`;

-- Recreate tables expected by current code.
CREATE TABLE `im_group_info` (
    `id` BIGINT PRIMARY KEY COMMENT 'group id',
    `group_name` VARCHAR(100) NOT NULL COMMENT 'group name',
    `group_avatar` VARCHAR(255) NULL COMMENT 'group avatar',
    `owner_id` BIGINT NOT NULL COMMENT 'owner user id',
    `max_members` INT DEFAULT 500 COMMENT 'max members',
    `notice` VARCHAR(1000) NULL COMMENT 'group notice',
    `notice_update_time` DATETIME NULL COMMENT 'notice update time',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
    `deleted` TINYINT DEFAULT 0 COMMENT '0 active 1 deleted',
    INDEX `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='group info';

CREATE TABLE `im_group_member` (
    `id` BIGINT PRIMARY KEY COMMENT 'id',
    `group_id` BIGINT NOT NULL COMMENT 'group id',
    `user_id` BIGINT NOT NULL COMMENT 'user id',
    `role` TINYINT DEFAULT 0 COMMENT '0 member 1 admin 2 owner',
    `mute_time` DATETIME NULL COMMENT 'mute until',
    `notice_read_time` DATETIME NULL COMMENT 'group notice read time',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
    INDEX `idx_group_id` (`group_id`),
    INDEX `idx_user_id` (`user_id`),
    UNIQUE INDEX `uk_group_user` (`group_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='group member';

SHOW CREATE TABLE im_group_info;
SHOW CREATE TABLE im_group_member;
