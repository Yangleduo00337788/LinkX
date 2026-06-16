package com.linkx.server.module.auth.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RefreshTokenRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(authService.register(request));
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }
}
