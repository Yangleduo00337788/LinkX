package com.linkx.server.module.chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDTO {
    private Long id;
    private Long sessionId;
    private Long fromUserId;
    private String fromNickname;
    private Long toUserId;
    private String content;
    private Integer msgType;
    private Integer status;
    private LocalDateTime createTime;
}
