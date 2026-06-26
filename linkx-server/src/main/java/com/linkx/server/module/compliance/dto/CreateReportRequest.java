package com.linkx.server.module.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {

    @NotBlank
    private String targetType;

    @NotBlank
    private String targetId;

    @NotBlank
    private String reasonCategory;

    private String reasonDetail;
}