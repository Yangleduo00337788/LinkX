package com.linkx.server.module.chat.constant;

/** 会话类型、消息类型与消息状态枚举值（与库表、前端约定一致）。 */
public final class ChatConstants {

    private ChatConstants() {
    }

    public static final int SESSION_TYPE_SINGLE = 1;
    public static final int SESSION_TYPE_GROUP = 2;

    public static final int MESSAGE_TYPE_TEXT = 0;
    public static final int MESSAGE_TYPE_IMAGE = 1;
    public static final int MESSAGE_TYPE_FILE = 2;
    public static final int MESSAGE_TYPE_SYSTEM = 3;

    public static final int MESSAGE_STATUS_NORMAL = 0;
    public static final int MESSAGE_STATUS_RECALLED = 1;
}
