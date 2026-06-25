package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/** 客户端经 WebSocket 上行的 action 字段取值（与 REST 能力互补）。 */
// 行注：定义 ChatSocketAction 类
public final class ChatSocketAction {

    // 行注：定义聊天连接操作方法
    private ChatSocketAction() {
    }  // 行注：结束当前代码块

    /** 拉取会话列表 */
    public static final String GET_SESSIONS = "GET_SESSIONS";  // 行注：定义获取会话列表常量
    /** 分页历史消息 */
    public static final String GET_HISTORY = "GET_HISTORY";  // 行注：定义获取历史常量
    /** 发送文本消息 */
    public static final String SEND_MESSAGE = "SEND_MESSAGE";  // 行注：定义发送消息常量
    /** 发送文件/图片消息 */
    public static final String SEND_FILE_MESSAGE = "SEND_FILE_MESSAGE";  // 行注：定义发送文件消息常量
    /** 标记已读 */
    public static final String MARK_READ = "MARK_READ";  // 行注：定义标记已读常量
    /** 撤回消息 */
    public static final String RECALL_MESSAGE = "RECALL_MESSAGE";  // 行注：定义RECALL消息常量
}  // 行注：结束当前代码块
