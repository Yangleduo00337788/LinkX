package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private Long id;
    private Long userId;
    private Long targetId;
    private String targetNickname;
    private String targetUsername;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
}
