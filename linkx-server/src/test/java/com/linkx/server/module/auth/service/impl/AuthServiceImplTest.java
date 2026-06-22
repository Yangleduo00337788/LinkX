package com.linkx.server.module.auth.service.impl;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.service.RefreshTokenSessionService;
import com.linkx.server.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenSessionService refreshTokenSessionService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userMapper, passwordEncoder, jwtTokenProvider, refreshTokenSessionService);
    }

    @Test
    void should_refresh_token_successfully_when_active_refresh_token_matches() {
        String refreshToken = "refresh-token";
        SysUser user = new SysUser();
        user.setId(1001L);
        user.setUsername("tester");
        user.setNickname("Tester");
        user.setAvatar("avatar.png");
        user.setStatus(1);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(1001L);
        when(refreshTokenSessionService.matchesActiveToken(1001L, refreshToken)).thenReturn(true);
        when(userMapper.selectById(1001L)).thenReturn(user);
        when(jwtTokenProvider.generateToken(1001L, "tester")).thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken(1001L)).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals(1001L, response.getUserId());
        assertEquals("tester", response.getUsername());
        verify(refreshTokenSessionService).issueToken(1001L, "new-refresh-token");
        verify(refreshTokenSessionService, never()).revokeToken(1001L);
    }

    @Test
    void should_reject_refresh_when_token_is_not_active() {
        String refreshToken = "refresh-token";

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(1002L);
        when(refreshTokenSessionService.matchesActiveToken(1002L, refreshToken)).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));

        assertEquals(ErrorCode.TOKEN_BLACKLISTED, exception.getErrorCode());
        verify(userMapper, never()).selectById(1002L);
        verify(refreshTokenSessionService, never()).issueToken(1002L, refreshToken);
    }

    @Test
    void should_revoke_refresh_token_when_user_is_disabled() {
        String refreshToken = "refresh-token";
        SysUser user = new SysUser();
        user.setId(1003L);
        user.setUsername("disabled-user");
        user.setStatus(0);

        when(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtTokenProvider.getUserIdFromToken(refreshToken)).thenReturn(1003L);
        when(refreshTokenSessionService.matchesActiveToken(1003L, refreshToken)).thenReturn(true);
        when(userMapper.selectById(1003L)).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.refreshToken(refreshToken));

        assertEquals(ErrorCode.USER_DISABLED, exception.getErrorCode());
        verify(refreshTokenSessionService).revokeToken(1003L);
        verify(refreshTokenSessionService, never()).issueToken(1003L, refreshToken);
    }
}
