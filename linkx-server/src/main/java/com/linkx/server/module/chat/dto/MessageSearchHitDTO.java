package com.linkx.server.module.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageSearchHitDTO {
    private Long messageId;
    private Long targetId;
    private Integer sessionType;
    private String sessionTitle;
    private String contentPreview;
    private Long fromUserId;
    private String fromNickname;
    private LocalDateTime createTime;
}