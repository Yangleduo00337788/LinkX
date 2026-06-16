package com.linkx.server.module.chat.ws;

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
