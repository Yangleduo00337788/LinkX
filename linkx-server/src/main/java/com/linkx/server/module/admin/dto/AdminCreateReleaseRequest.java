package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateReleaseRequest {
    @NotBlank
    private String platform;
    @NotBlank
    private String version;
    private String downloadUrl;
    private String releaseNotes;
    private Boolean forceUpdate;
    private Boolean published;
}