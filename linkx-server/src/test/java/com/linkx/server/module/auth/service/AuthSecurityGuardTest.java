package com.linkx.server.module.auth.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.config.LinkxSecurityProperties;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthSecurityGuardTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CaptchaService captchaService;

    private LinkxSecurityProperties securityProperties;
    private AuthSecurityGuard authSecurityGuard;

    @BeforeEach
    void setUp() {
        securityProperties = new LinkxSecurityProperties();
        securityProperties.getAuthRateLimit().setEnabled(true);
        securityProperties.getAuthRateLimit().setWindowSeconds(60);
        securityProperties.getAuthRateLimit().setLoginMaxRequests(1);
        authSecurityGuard = new AuthSecurityGuard(redisTemplate, securityProperties, captchaService);
    }

    @Test
    void should_block_login_when_rate_limit_is_exceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L);

        authSecurityGuard.checkLoginRateLimit(request, "tester");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authSecurityGuard.checkLoginRateLimit(request, "tester"));

        assertEquals(ErrorCode.TOO_MANY_REQUESTS, exception.getErrorCode());
        verify(redisTemplate).expire("auth:rate-limit:login:127.0.0.1:tester", Duration.ofSeconds(60));
    }

    @Test
    void should_require_captcha_payload_when_enabled() {
        securityProperties.getCaptcha().setEnabled(true);
        org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.BAD_REQUEST, "请先完成验证码校验"))
                .when(captchaService).consume(org.mockito.ArgumentMatchers.eq("login"), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authSecurityGuard.validateLoginCaptcha(null, null));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        org.mockito.Mockito.verify(captchaService).consume("login", null, null);
    }

    @Test
    void should_return_captcha_meta() {
        securityProperties.getCaptcha().setEnabled(true);

        var meta = authSecurityGuard.getCaptchaMeta();

        assertTrue(meta.isEnabled());
        assertEquals(2, meta.getScenes().size());
        assertTrue(meta.getScenes().contains("login"));
        assertTrue(meta.getScenes().contains("register"));
    }

    @Test
    void should_skip_captcha_validation_when_disabled() {
        securityProperties.getCaptcha().setEnabled(false);

        authSecurityGuard.validateRegisterCaptcha(null, null);

        assertFalse(securityProperties.getCaptcha().isEnabled());
        verify(redisTemplate, org.mockito.Mockito.never()).expire(anyString(), any(Duration.class));
    }
}
