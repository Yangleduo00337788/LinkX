package com.linkx.server.module.chat.ws;

import java.time.LocalDateTime;

public record ChatConnectedPayload(Long userId, LocalDateTime serverTime) {
}
