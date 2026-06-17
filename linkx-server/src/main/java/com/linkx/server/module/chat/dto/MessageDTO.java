package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageDTO {
    private Long id;
    private Long sessionId;
    private String clientMessageId;
    private Long fromUserId;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private Integer sessionType;
    private String content;
    private Integer msgType;
    private Boolean mentionAll;
    private List<Long> mentionUserIds;
    private List<String> mentionDisplayNames;
    private String fileName;
    private Long fileSize;
    private String fileType;
    private Integer status;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
