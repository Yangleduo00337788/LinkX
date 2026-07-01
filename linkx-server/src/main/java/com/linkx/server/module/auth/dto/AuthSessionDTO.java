package com.linkx.server.module.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthSessionDTO {
    private String sessionId;
    private String deviceLabel;
    private String clientIp;
    private String userAgent;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private boolean current;
}