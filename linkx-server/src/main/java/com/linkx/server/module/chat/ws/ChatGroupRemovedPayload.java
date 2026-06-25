package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/** {@link ChatEventType#GROUP_REMOVED}：被踢出群、群解散等导致会话失效。 */
// 行注：定义 ChatGroupRemovedPayload 记录类型
public record ChatGroupRemovedPayload(Long groupId, String reason) {
}  // 行注：结束当前代码块
