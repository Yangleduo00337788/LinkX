package com.linkx.server.module.group.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupHighlightRequest {
    private Long messageId;

    @Size(max = 200, message = "标题不能超过200个字符")
    private String title;
}