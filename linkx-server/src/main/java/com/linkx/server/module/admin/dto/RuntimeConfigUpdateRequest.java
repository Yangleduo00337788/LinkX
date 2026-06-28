package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RuntimeConfigUpdateRequest {

    @NotBlank
    @Size(max = 128)
    private String configKey;

    @NotBlank
    @Size(max = 2000)
    private String configValue;

    @Size(max = 255)
    private String description;
}