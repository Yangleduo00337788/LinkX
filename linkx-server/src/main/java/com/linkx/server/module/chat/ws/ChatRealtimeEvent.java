package com.linkx.server.module.chat.ws;

/**
 * WebSocket 下行消息信封：{@code type} 见 {@link ChatEventType}，{@code data} 为对应 Payload。
 */
public record ChatRealtimeEvent(String type, Object data) {
}
