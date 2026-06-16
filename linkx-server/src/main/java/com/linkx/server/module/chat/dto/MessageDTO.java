package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long sessionId;
    private Long fromUserId;
    private String fromNickname;
    private String fromAvatar;
    private Long toUserId;
    private Integer sessionType;
    private String content;
    private Integer msgType;
    private String fileName;
    private Long fileSize;
    private Integer status;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
}
