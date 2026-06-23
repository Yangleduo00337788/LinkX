package com.linkx.server.module.chat.ws;

import com.linkx.server.module.chat.dto.MessageDTO;

/** {@link ChatEventType#MESSAGE} 下行：新消息或撤回后的消息快照。 */
public record ChatMessagePayload(MessageDTO message) {
}
