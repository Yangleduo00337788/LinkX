-- 会话偏好列（若已存在请跳过对应语句）
ALTER TABLE im_session ADD COLUMN session_remark VARCHAR(128) NULL COMMENT '单聊会话备注';
ALTER TABLE im_session ADD COLUMN pinned TINYINT NOT NULL DEFAULT 0 COMMENT '置顶';
ALTER TABLE im_session ADD COLUMN notification_muted TINYINT NOT NULL DEFAULT 0 COMMENT '免打扰';

CREATE TABLE IF NOT EXISTS im_chat_draft (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    session_type INT NOT NULL,
    draft_content TEXT NULL,
    update_time DATETIME NULL,
    UNIQUE KEY uk_user_target (user_id, target_id, session_type)
) COMMENT '聊天草稿';