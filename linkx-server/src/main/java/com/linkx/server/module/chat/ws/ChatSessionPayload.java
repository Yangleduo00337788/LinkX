package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.linkx.server.module.chat.dto.ChatSessionDTO;  // 行注：引入 ChatSessionDTO 类型

/** {@link ChatEventType#SESSION}：会话列表项变更（新消息预览、未读数等）。 */
// 行注：定义 ChatSessionPayload 记录类型
public record ChatSessionPayload(ChatSessionDTO session) {
}  // 行注：结束当前代码块
