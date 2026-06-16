package com.linkx.server.module.chat.ws;

import com.linkx.server.module.chat.dto.MessageDTO;

public record ChatMessagePayload(MessageDTO message) {
}
