package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** {@link ChatEventType#CONNECTED}：握手成功后的欢迎包。 */
// 行注：定义 ChatConnectedPayload 记录类型
public record ChatConnectedPayload(Long userId, LocalDateTime serverTime) {
}  // 行注：结束当前代码块
