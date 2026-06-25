package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.util.List;  // 行注：引入 List 类型

/** {@link ChatEventType#READ_RECEIPT}：对方已读游标或已读消息 ID 列表。 */
// 行注：定义 ChatReadReceiptPayload 记录类型
public record ChatReadReceiptPayload(
        // 行注：补充当前表达式片段
        Long targetId,
        // 行注：补充当前表达式片段
        Integer sessionType,
        // 行注：补充当前表达式片段
        Long readerUserId,
        // 行注：补充当前表达式片段
        LocalDateTime readTime,
        // 行注：补充当前表达式片段
        List<Long> messageIds
// 行注：开始当前语句对应的代码块
) {
}  // 行注：结束当前代码块
