package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddFileHashBlacklistRequest {

    @NotBlank
    @Size(max = 128)
    private String contentHash;

    @Size(max = 500)
    private String reason;
}