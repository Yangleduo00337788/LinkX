package com.linkx.server.module.auth.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 图形验证码：Redis 存答案（5 分钟 TTL），SVG Data URL 返回前端；校验后一次性删除。
 * <p>场景仅支持 {@code login}、{@code register}。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String KEY_PREFIX = "auth:captcha:";
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final Set<String> ALLOWED_SCENES = Set.of("login", "register");
    private static final String CODE_ALPHABET = "23456789ACEFGHJKLMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    public CaptchaIssueDTO issue(String scene) {
        String normalizedScene = normalizeScene(scene);
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode();
        redisTemplate.opsForValue().set(buildKey(normalizedScene, captchaId), code, CAPTCHA_TTL);
        String imageDataUrl = toSvgDataUrl(code);
        log.debug("Captcha issued, scene={}, captchaId={}", normalizedScene, captchaId);
        return new CaptchaIssueDTO(captchaId, imageDataUrl, CAPTCHA_TTL.toSeconds());
    }

    public void consume(String scene, String captchaId, String captchaCode) {
        String normalizedScene = normalizeScene(scene);
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            log.warn("Captcha validation rejected, scene={}, reason=missing_payload", normalizedScene);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先完成验证码校验");
        }
        String redisKey = buildKey(normalizedScene, captchaId.trim());
        Object stored = redisTemplate.opsForValue().get(redisKey);
        redisTemplate.delete(redisKey);
        if (!(stored instanceof String expectedCode) || !StringUtils.hasText(expectedCode)) {
            log.warn("Captcha validation rejected, scene={}, captchaId={}, reason=expired_or_unknown", normalizedScene, captchaId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已失效，请重新获取");
        }
        String provided = captchaCode.trim().toUpperCase(Locale.ROOT);
        String expected = expectedCode.trim().toUpperCase(Locale.ROOT);
        if (!expected.equals(provided)) {
            log.warn("Captcha validation rejected, scene={}, captchaId={}, reason=mismatch", normalizedScene, captchaId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");
        }
    }

    private String normalizeScene(String scene) {
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码场景无效");
        }
        String normalized = scene.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_SCENES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码场景无效");
        }
        return normalized;
    }

    private String buildKey(String scene, String captchaId) {
        return KEY_PREFIX + scene + ":" + captchaId;
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));
        }
        return builder.toString();
    }

    private String toSvgDataUrl(String code) {
        int noiseLineCount = 4 + secureRandom.nextInt(3);
        StringBuilder noiseLines = new StringBuilder();
        for (int i = 0; i < noiseLineCount; i++) {
            int x1 = secureRandom.nextInt(120);
            int y1 = secureRandom.nextInt(40);
            int x2 = secureRandom.nextInt(120);
            int y2 = secureRandom.nextInt(40);
            noiseLines.append(String.format(
                    "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#a1a1aa\" stroke-width=\"1\" opacity=\"0.6\"/>",
                    x1, y1, x2, y2));
        }
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="160" height="48" viewBox="0 0 160 48">
                  <rect width="160" height="48" fill="#f4f4f5"/>
                  %s
                  <text x="80" y="30" font-family="Arial,sans-serif" font-size="20" font-weight="bold"
                        text-anchor="middle" fill="#18181b" letter-spacing="3" transform="rotate(-3 80 24)">%s</text>
                </svg>
                """.formatted(noiseLines, escapeXml(code));
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));
        return "data:image/svg+xml;base64," + base64;
    }

    private String escapeXml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}