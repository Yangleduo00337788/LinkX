package com.linkx.server.module.chat.ws;

public record ChatCommandResultPayload(
        String requestId,
        String action,
        boolean success,
        int code,
        String message,
        Object data
) {
}
