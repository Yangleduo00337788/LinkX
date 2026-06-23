package com.linkx.server.module.chat.ws;

/** {@link ChatEventType#GROUP_REMOVED}：被踢出群、群解散等导致会话失效。 */
public record ChatGroupRemovedPayload(Long groupId, String reason) {
}
