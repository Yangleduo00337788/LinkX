package com.linkx.server.module.chat.ws;

/** {@link ChatEventType#COMMAND_RESULT}：客户端 WS 命令的异步执行结果。 */
public record ChatCommandResultPayload(
        String requestId,
        String action,
        boolean success,
        int code,
        String message,
        Object data
) {
}
