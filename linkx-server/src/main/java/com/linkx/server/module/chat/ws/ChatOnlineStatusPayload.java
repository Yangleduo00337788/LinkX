package com.linkx.server.module.chat.ws;

public record ChatOnlineStatusPayload(Long userId, boolean online) {
}
