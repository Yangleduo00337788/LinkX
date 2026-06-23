package com.linkx.server.module.chat.ws;

import com.linkx.server.module.chat.dto.ChatSessionDTO;

/** {@link ChatEventType#SESSION}：会话列表项变更（新消息预览、未读数等）。 */
public record ChatSessionPayload(ChatSessionDTO session) {
}
