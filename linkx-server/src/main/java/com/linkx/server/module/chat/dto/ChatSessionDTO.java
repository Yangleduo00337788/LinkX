package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 会话列表项：对端用户或群、最后消息预览、未读数、置顶等。 */
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
    private String groupRemark;
    private String notice;
    private Boolean noticeUnread;
    private Boolean muted;
    private LocalDateTime muteTime;
    private Boolean notificationMuted;
    private Boolean targetOnline;
}
