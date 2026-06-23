package com.linkx.server.module.abuse.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.config.LinkxSecurityProperties;
import com.linkx.server.module.auth.service.AuthSecurityGuard;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbuseProtectionGuardTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private AuthSecurityGuard authSecurityGuard;

    private LinkxSecurityProperties securityProperties;
    private AbuseProtectionGuard guard;

    @BeforeEach
    void setUp() {
        securityProperties = new LinkxSecurityProperties();
        securityProperties.getAbuseProtection().setEnabled(true);
        securityProperties.getAbuseProtection().getUserSearch().setMaxRequestsPerUser(2);
        securityProperties.getAbuseProtection().getUserSearch().setMaxRequestsPerIp(0);
        securityProperties.getAbuseProtection().getFriendRequest().setMaxPerUserPerDay(1);
        securityProperties.getAbuseProtection().getFriendRequest().setMaxPerIpPerDay(0);
        guard = new AbuseProtectionGuard(redisTemplate, securityProperties, authSecurityGuard);
    }

    @Test
    void should_block_user_search_when_user_limit_exceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);

        guard.checkUserSearch(9001L, request);
        guard.checkUserSearch(9001L, request);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> guard.checkUserSearch(9001L, request));
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, exception.getErrorCode());
    }

    @Test
    void should_block_friend_request_when_daily_user_limit_exceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L);

        guard.checkFriendRequestSend(9002L, request);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> guard.checkFriendRequestSend(9002L, request));
        assertEquals(ErrorCode.TOO_MANY_REQUESTS, exception.getErrorCode());
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    void should_skip_checks_when_abuse_protection_disabled() {
        securityProperties.getAbuseProtection().setEnabled(false);
        guard.checkUserSearch(1L, request);
        guard.checkFriendRequestSend(1L, request);
    }
}