package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/** 服务端经 WebSocket 下行推送的事件 type 字段取值。 */
// 行注：定义 ChatEventType 类
public final class ChatEventType {

    // 行注：定义聊天事件类型方法
    private ChatEventType() {
    }  // 行注：结束当前代码块

    /** 本端 WebSocket 连接成功 */
    public static final String CONNECTED = "CONNECTED";  // 行注：定义已连接常量
    /** 客户端命令（发消息等）的同步响应 */
    public static final String COMMAND_RESULT = "COMMAND_RESULT";  // 行注：定义COMMAND结果常量
    /** 新消息或消息更新 */
    public static final String MESSAGE = "MESSAGE";  // 行注：定义消息常量
    /** 会话列表项变更（未读、预览等） */
    public static final String SESSION = "SESSION";  // 行注：定义会话常量
    /** 群资料变更 */
    public static final String GROUP_DETAIL = "GROUP_DETAIL";  // 行注：定义群详情常量
    /** 被移出群或群解散 */
    public static final String GROUP_REMOVED = "GROUP_REMOVED";  // 行注：定义群REMOVED常量
    /** 已读回执 */
    public static final String READ_RECEIPT = "READ_RECEIPT";  // 行注：定义已读RECEIPT常量
    /** 好友上/下线 */
    public static final String ONLINE_STATUS = "ONLINE_STATUS";  // 行注：定义在线状态常量
    /** 消息撤回 */
    public static final String MESSAGE_RECALLED = "MESSAGE_RECALLED";  // 行注：定义消息RECALLED常量
    /** 管理员强制下线 */
    public static final String FORCE_LOGOUT = "FORCE_LOGOUT";
}
