package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/**
 * WebSocket 下行消息信封：{@code type} 见 {@link ChatEventType}，{@code data} 为对应 Payload。
 */
// 行注：定义 ChatRealtimeEvent 记录类型
public record ChatRealtimeEvent(String type, Object data) {
}  // 行注：结束当前代码块
