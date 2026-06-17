ALTER TABLE im_group_member
    ADD COLUMN IF NOT EXISTS group_remark VARCHAR(100) NULL COMMENT '群备注/别名',
    ADD COLUMN IF NOT EXISTS notification_muted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '消息免打扰 0否 1是';
