package com.linkx.server.module.compliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {

    @NotNull
    private Integer status;

    private String resolution;

    private String resolutionNote;

    private Boolean notifyReporter;
}