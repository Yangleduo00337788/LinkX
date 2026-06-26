package com.linkx.server.module.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateAdminRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String displayName;
    @NotBlank
    private String role;
}