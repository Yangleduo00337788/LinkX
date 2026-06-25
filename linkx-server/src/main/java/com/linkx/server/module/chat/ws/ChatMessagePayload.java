package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型

/** {@link ChatEventType#MESSAGE} 下行：新消息或撤回后的消息快照。 */
// 行注：定义 ChatMessagePayload 记录类型
public record ChatMessagePayload(MessageDTO message) {
}  // 行注：结束当前代码块
