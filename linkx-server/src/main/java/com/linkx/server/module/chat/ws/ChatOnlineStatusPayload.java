package com.linkx.server.module.chat.ws;

/** {@link ChatEventType#ONLINE_STATUS}：好友/相关用户上下线通知。 */
public record ChatOnlineStatusPayload(Long userId, boolean online) {
}
