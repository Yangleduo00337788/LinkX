package com.linkx.server.module.auth.service;

import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
}
