package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.AuthSessionDTO;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth/sessions")
@RequiredArgsConstructor
public class AuthSessionController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public Result<List<AuthSessionDTO>> list(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @RequestParam(value = "currentSessionId", required = false) String currentSessionIdParam) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String current = StringUtils.hasText(sessionIdHeader) ? sessionIdHeader : currentSessionIdParam;
        return Result.success(authService.listSessions(userId, current));
    }

    @DeleteMapping("/{sessionId}")
    public Result<Void> revoke(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String sessionId,
            @RequestHeader(value = "X-Session-Id", required = false) String currentSessionId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        authService.revokeSession(userId, sessionId, currentSessionId);
        return Result.success();
    }

    @PostMapping("/revoke-others")
    public Result<Void> revokeOthers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionIdHeader,
            @RequestParam(value = "currentSessionId", required = false) String currentSessionIdParam) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String keep = StringUtils.hasText(sessionIdHeader) ? sessionIdHeader : currentSessionIdParam;
        if (!StringUtils.hasText(keep)) {
            keep = null;
        }
        authService.revokeOtherSessions(userId, keep);
        return Result.success();
    }
}