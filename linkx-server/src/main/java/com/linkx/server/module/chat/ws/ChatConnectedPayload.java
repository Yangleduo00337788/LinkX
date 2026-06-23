package com.linkx.server.module.chat.ws;

import java.time.LocalDateTime;

/** {@link ChatEventType#CONNECTED}：握手成功后的欢迎包。 */
public record ChatConnectedPayload(Long userId, LocalDateTime serverTime) {
}
