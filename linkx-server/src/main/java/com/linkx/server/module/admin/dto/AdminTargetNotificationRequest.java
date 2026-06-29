package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AdminTargetNotificationRequest {
    @NotEmpty
    private List<Long> userIds;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}