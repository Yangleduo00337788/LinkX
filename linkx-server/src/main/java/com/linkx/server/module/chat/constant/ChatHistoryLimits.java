package com.linkx.server.module.chat.constant;

/** 聊天历史分页上限，防止单次拉取过大。 */
public final class ChatHistoryLimits {

    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final int MAX_PAGE_SIZE = 200;
    public static final int MAX_PAGE_NUMBER = 100;

    private ChatHistoryLimits() {
    }

    public static int clampPage(int page) {
        return Math.min(Math.max(page, 1), MAX_PAGE_NUMBER);
    }

    public static int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }
}