package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

/** {@link ChatEventType#ONLINE_STATUS}：好友/相关用户上下线通知。 */
// 行注：定义 ChatOnlineStatusPayload 记录类型
public record ChatOnlineStatusPayload(Long userId, boolean online) {
}  // 行注：结束当前代码块
