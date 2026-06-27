package com.linkx.server.module.session.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveChatDraftRequest {
    @NotNull
    private Long targetId;
    @NotNull
    private Integer sessionType;
    private String draftContent;
}