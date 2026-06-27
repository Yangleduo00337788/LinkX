package com.linkx.server.module.auth.service;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service

import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.module.auth.dto.CaptchaIssueDTO;  // 行注：引入 CaptchaIssueDTO 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.data.redis.core.RedisTemplate;  // 行注：引入 RedisTemplate 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.nio.charset.StandardCharsets;  // 行注：引入 StandardCharsets 类型
import java.security.SecureRandom;  // 行注：引入 SecureRandom 类型
import java.time.Duration;  // 行注：引入 Duration 类型
import java.util.Base64;  // 行注：引入 Base64 类型
import java.util.Locale;  // 行注：引入 Locale 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.UUID;  // 行注：引入 UUID 类型

/**
 * 图形验证码：Redis 存答案（5 分钟 TTL），SVG Data URL 返回前端；校验后一次性删除。
 * <p>场景仅支持 {@code login}、{@code register}。</p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 CaptchaService 类
public class CaptchaService {

    private static final String KEY_PREFIX = "auth:captcha:";  // 行注：定义键PREFIX常量
    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);  // 行注：定义验证码TTL常量
    private static final Set<String> ALLOWED_SCENES = Set.of("login", "register", "admin-login");
    private static final String CODE_ALPHABET = "23456789ACEFGHJKLMNPQRSTUVWXYZ";  // 行注：定义验证码ALPHABET常量
    private static final int CODE_LENGTH = 6;  // 行注：定义验证码长度常量

    private final RedisTemplate<String, Object> redisTemplate;  // 行注：注入RedisTemplate依赖
    private final SecureRandom secureRandom = new SecureRandom();  // 行注：注入secureRandom依赖

    /**
     * 生成验证码并写入 Redis，返回 captchaId 与 SVG 图片 Data URL。
     *
     * @param scene 业务场景：login / register
     */
    // 行注：定义申请方法
    public CaptchaIssueDTO issue(String scene) {
        String normalizedScene = normalizeScene(scene);  // 行注：初始化规范化后的场景
        String captchaId = UUID.randomUUID().toString().replace("-", "");  // 行注：初始化验证码ID
        String code = randomCode();  // 行注：初始化验证码
        redisTemplate.opsForValue().set(buildKey(normalizedScene, captchaId), code, CAPTCHA_TTL);  // 行注：调用ops值
        String imageDataUrl = toSvgDataUrl(code);  // 行注：初始化imageDataURL
        log.debug("Captcha issued, scene={}, captchaId={}", normalizedScene, captchaId);  // 行注：执行初始化操作
        return new CaptchaIssueDTO(captchaId, imageDataUrl, CAPTCHA_TTL.toSeconds());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 校验用户输入的验证码；无论成败均删除 Redis 中的答案（防重放）。
     */
    // 行注：定义consume方法
    public void consume(String scene, String captchaId, String captchaCode) {
        String normalizedScene = normalizeScene(scene);  // 行注：初始化规范化后的场景
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(captchaId) || !StringUtils.hasText(captchaCode)) {
            log.warn("Captcha validation rejected, scene={}, reason=missing_payload", normalizedScene);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先完成验证码校验");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String redisKey = buildKey(normalizedScene, captchaId.trim());  // 行注：初始化Redis键
        Object stored = redisTemplate.opsForValue().get(redisKey);  // 行注：初始化已存储值
        redisTemplate.delete(redisKey);  // 行注：调用删除
        // 行注：判断是否满足当前条件
        if (!(stored instanceof String expectedCode) || !StringUtils.hasText(expectedCode)) {
            // 行注：执行初始化操作
            log.warn("Captcha validation rejected, scene={}, captchaId={}, reason=expired_or_unknown", normalizedScene, captchaId);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码已失效，请重新获取");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String provided = captchaCode.trim().toUpperCase(Locale.ROOT);  // 行注：初始化传入值
        String expected = expectedCode.trim().toUpperCase(Locale.ROOT);  // 行注：初始化期望值
        // 行注：判断是否满足当前条件
        if (!expected.equals(provided)) {
            log.warn("Captcha validation rejected, scene={}, captchaId={}, reason=mismatch", normalizedScene, captchaId);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码错误");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义规范化场景方法
    private String normalizeScene(String scene) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(scene)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码场景无效");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String normalized = scene.trim().toLowerCase(Locale.ROOT);  // 行注：初始化规范化后的
        // 行注：判断是否满足当前条件
        if (!ALLOWED_SCENES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "验证码场景无效");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建键方法
    private String buildKey(String scene, String captchaId) {
        return KEY_PREFIX + scene + ":" + captchaId;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义random验证码方法
    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);  // 行注：初始化构建器
        // 行注：遍历当前集合或范围
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(CODE_ALPHABET.charAt(secureRandom.nextInt(CODE_ALPHABET.length())));  // 行注：调用append
        }  // 行注：结束当前代码块
        return builder.toString();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为SvgDataURL方法
    private String toSvgDataUrl(String code) {
        int noiseLineCount = 4 + secureRandom.nextInt(3);  // 行注：初始化噪点Line数量
        StringBuilder noiseLines = new StringBuilder();  // 行注：初始化噪点线条
        // 行注：遍历当前集合或范围
        for (int i = 0; i < noiseLineCount; i++) {
            int x1 = secureRandom.nextInt(120);  // 行注：初始化x1
            int y1 = secureRandom.nextInt(40);  // 行注：初始化y1
            int x2 = secureRandom.nextInt(120);  // 行注：初始化x2
            int y2 = secureRandom.nextInt(40);  // 行注：初始化y2
            // 行注：补充当前表达式片段
            noiseLines.append(String.format(
                    // 行注：补充当前表达式片段
                    "<line x1=\"%d\" y1=\"%d\" x2=\"%d\" y2=\"%d\" stroke=\"#a1a1aa\" stroke-width=\"1\" opacity=\"0.6\"/>",
                    x1, y1, x2, y2));  // 行注：完成当前语句
        }  // 行注：结束当前代码块
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="160" height="48" viewBox="0 0 160 48">
                  <rect width="160" height="48" fill="#f4f4f5"/>
                  %s
                  <text x="80" y="30" font-family="Arial,sans-serif" font-size="20" font-weight="bold"
                        text-anchor="middle" fill="#18181b" letter-spacing="3" transform="rotate(-3 80 24)">%s</text>
                </svg>
                """.formatted(noiseLines, escapeXml(code));
        String base64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));  // 行注：初始化基础64
        return "data:image/svg+xml;base64," + base64;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义escapeXml方法
    private String escapeXml(String value) {
        return value.replace("&", "&amp;")  // 行注：返回处理结果
                // 行注：继续调用replace
                .replace("<", "&lt;")
                // 行注：继续调用replace
                .replace(">", "&gt;")
                .replace("\"", "&quot;");  // 行注：继续调用replace
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
