package com.linkx.server.module.session.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatDraftDTO {
    private Long targetId;
    private Integer sessionType;
    private String draftContent;
}