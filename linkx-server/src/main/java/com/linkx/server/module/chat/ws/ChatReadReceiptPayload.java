package com.linkx.server.module.chat.ws;

import java.time.LocalDateTime;
import java.util.List;

public record ChatReadReceiptPayload(
        Long targetId,
        Integer sessionType,
        Long readerUserId,
        LocalDateTime readTime,
        List<Long> messageIds
) {
}
