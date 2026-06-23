package com.linkx.server.module.chat.ws;

import java.time.LocalDateTime;
import java.util.List;

/** {@link ChatEventType#READ_RECEIPT}：对方已读游标或已读消息 ID 列表。 */
public record ChatReadReceiptPayload(
        Long targetId,
        Integer sessionType,
        Long readerUserId,
        LocalDateTime readTime,
        List<Long> messageIds
) {
}
