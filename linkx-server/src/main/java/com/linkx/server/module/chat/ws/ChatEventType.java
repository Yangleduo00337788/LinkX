package com.linkx.server.module.chat.ws;

public final class ChatEventType {

    private ChatEventType() {
    }

    public static final String CONNECTED = "CONNECTED";
    public static final String COMMAND_RESULT = "COMMAND_RESULT";
    public static final String MESSAGE = "MESSAGE";
    public static final String SESSION = "SESSION";
    public static final String GROUP_DETAIL = "GROUP_DETAIL";
    public static final String GROUP_REMOVED = "GROUP_REMOVED";
    public static final String READ_RECEIPT = "READ_RECEIPT";
    public static final String ONLINE_STATUS = "ONLINE_STATUS";
    public static final String MESSAGE_RECALLED = "MESSAGE_RECALLED";
}
