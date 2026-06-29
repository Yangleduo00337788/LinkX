package com.linkx.server.module.group.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateGroupNoticeRequest {
    @NotBlank
    private String content;
    private Boolean pinned;
}