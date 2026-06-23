package com.linkx.server.module.auth.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService(redisTemplate);
    }

    @Test
    void should_issue_captcha_and_store_in_redis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        CaptchaIssueDTO issue = captchaService.issue("login");

        assertNotNull(issue.getCaptchaId());
        assertNotNull(issue.getImageDataUrl());
        assertEquals(300L, issue.getExpiresInSeconds());
        verify(valueOperations).set(anyString(), any(String.class), eq(Duration.ofMinutes(5)));
    }

    @Test
    void should_consume_valid_captcha_once() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("auth:captcha:login:abc")).thenReturn("ABCD");

        captchaService.consume("login", "abc", "abcd");

        verify(redisTemplate).delete("auth:captcha:login:abc");
    }

    @Test
    void should_reject_wrong_captcha_code() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn("WXYZ");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> captchaService.consume("login", "id1", "ABCD"));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
    }
}