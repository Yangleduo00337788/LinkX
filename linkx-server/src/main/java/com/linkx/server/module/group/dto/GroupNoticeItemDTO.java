package com.linkx.server.module.group.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupNoticeItemDTO {
    private Long id;
    private String content;
    private Boolean pinned;
    private Long publisherId;
    private String publisherNickname;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}