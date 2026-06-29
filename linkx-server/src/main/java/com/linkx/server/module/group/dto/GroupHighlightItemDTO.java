package com.linkx.server.module.group.dto;

import com.linkx.server.module.chat.dto.MessageDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupHighlightItemDTO {
    private Long id;
    private Long groupId;
    private Long messageId;
    private String title;
    private Long createdBy;
    private String createdByNickname;
    private LocalDateTime createTime;
    private MessageDTO message;
}