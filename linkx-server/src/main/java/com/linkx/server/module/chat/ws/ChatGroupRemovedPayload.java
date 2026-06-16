package com.linkx.server.module.chat.ws;

public record ChatGroupRemovedPayload(Long groupId, String reason) {
}
