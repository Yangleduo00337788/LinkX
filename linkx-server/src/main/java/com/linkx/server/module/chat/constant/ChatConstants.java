package com.linkx.server.module.chat.constant;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.constant

/** 会话类型、消息类型与消息状态枚举值（与库表、前端约定一致）。 */
// 行注：定义 ChatConstants 类
public final class ChatConstants {

    // 行注：定义聊天Constants方法
    private ChatConstants() {
    }  // 行注：结束当前代码块

    /** 单聊会话 */
    public static final int SESSION_TYPE_SINGLE = 1;  // 行注：定义会话类型单聊常量
    /** 群聊会话 */
    public static final int SESSION_TYPE_GROUP = 2;  // 行注：定义会话类型群常量

    /** 纯文本 */
    public static final int MESSAGE_TYPE_TEXT = 0;  // 行注：定义消息类型文本常量
    /** 图片（内容为文件 URL） */
    public static final int MESSAGE_TYPE_IMAGE = 1;  // 行注：定义消息类型IMAGE常量
    /** 普通文件 */
    public static final int MESSAGE_TYPE_FILE = 2;  // 行注：定义消息类型文件常量
    /** 系统提示类消息 */
    public static final int MESSAGE_TYPE_SYSTEM = 3;  // 行注：定义消息类型SYSTEM常量

    /** 正常展示 */
    public static final int MESSAGE_STATUS_NORMAL = 0;  // 行注：定义消息状态NORMAL常量
    /** 已撤回 */
    public static final int MESSAGE_STATUS_RECALLED = 1;  // 行注：定义消息状态RECALLED常量
}  // 行注：结束当前代码块
