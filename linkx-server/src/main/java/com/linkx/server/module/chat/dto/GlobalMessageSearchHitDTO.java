package com.linkx.server.module.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GlobalMessageSearchHitDTO {
    private Long messageId;
    private Integer sessionType;
    private Long targetId;
    private String targetLabel;
    private String contentPreview;
    private Integer msgType;
    private Long fromUserId;
    private String fromNickname;
    private LocalDateTime createTime;
}