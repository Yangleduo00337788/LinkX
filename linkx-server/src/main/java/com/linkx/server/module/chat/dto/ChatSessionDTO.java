package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionDTO {
    private Long id;
    private Long userId;
    private Long targetId;
    private Integer sessionType;
    private String targetNickname;
    private String targetUsername;
    private String targetAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer unreadCount;
    private Integer memberCount;
    private Integer myRole;
    private String notice;
    private Boolean noticeUnread;
    private Boolean muted;
    private LocalDateTime muteTime;
    private Boolean targetOnline;
}
