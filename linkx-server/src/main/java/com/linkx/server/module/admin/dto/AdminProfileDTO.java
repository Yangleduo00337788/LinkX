package com.linkx.server.module.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminProfileDTO {
    private Long id;
    private String username;
    private String displayName;
    private String role;
}