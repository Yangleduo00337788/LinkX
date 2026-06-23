package com.linkx.server.module.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** GET /api/chat/ws-ticket 返回，用于连接 /ws/chat?ticket= */
@Data
@AllArgsConstructor
public class ChatWsTicketDTO {

    private String ticket;
}
