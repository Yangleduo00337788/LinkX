package com.linkx.server.module.chat.ws;

/** 客户端经 WebSocket 上行的 action 字段取值（与 REST 能力互补）。 */
public final class ChatSocketAction {

    private ChatSocketAction() {
    }

    public static final String GET_SESSIONS = "GET_SESSIONS";
    public static final String GET_HISTORY = "GET_HISTORY";
    public static final String SEND_MESSAGE = "SEND_MESSAGE";
    public static final String SEND_FILE_MESSAGE = "SEND_FILE_MESSAGE";
    public static final String MARK_READ = "MARK_READ";
    public static final String RECALL_MESSAGE = "RECALL_MESSAGE";
}
