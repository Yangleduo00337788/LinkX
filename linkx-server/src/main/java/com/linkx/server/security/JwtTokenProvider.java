package com.linkx.server.security;  // 行注：声明当前文件所在包 com.linkx.server.security

import io.jsonwebtoken.Claims;  // 行注：引入 Claims 类型
import io.jsonwebtoken.ExpiredJwtException;  // 行注：引入 ExpiredJwtException 类型
import io.jsonwebtoken.JwtException;  // 行注：引入 JwtException 类型
import io.jsonwebtoken.Jwts;  // 行注：引入 Jwts 类型
import io.jsonwebtoken.security.Keys;  // 行注：引入 Keys 类型
import jakarta.annotation.PostConstruct;  // 行注：引入 PostConstruct 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.beans.factory.annotation.Value;  // 行注：引入 Value 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import javax.crypto.SecretKey;  // 行注：引入 SecretKey 类型
import java.nio.charset.StandardCharsets;  // 行注：引入 StandardCharsets 类型
import java.util.Date;  // 行注：引入 Date 类型

/**
 * JWT 签发与解析（HS256）。
 * <p>
 * access 与 refresh 通过 claim {@code type} 区分；subject 为 userId 字符串。
 * 启动时校验密钥非空、非占位、长度 ≥32。
 * </p>
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Component  // 行注：应用 @Component 注解
// 行注：定义 JwtTokenProvider 类
public class JwtTokenProvider {

    /** access 令牌类型，用于 API 鉴权 */
    public static final String TOKEN_TYPE_ACCESS = "access";  // 行注：定义令牌类型访问常量
    /** refresh 令牌类型，仅用于刷新 access */
    public static final String TOKEN_TYPE_REFRESH = "refresh";  // 行注：定义令牌类型刷新常量
    /** 管理后台 access 令牌 */
    public static final String TOKEN_TYPE_ADMIN_ACCESS = "admin_access";
    // 行注：定义默认JWT密钥PLACEHOLDER常量
    private static final String DEFAULT_JWT_SECRET_PLACEHOLDER = "PLEASE_CHANGE_ME_LINKX_JWT_SECRET_2026_32CHARS_MIN";
    private static final int MIN_SECRET_LENGTH = 32;  // 行注：定义最小密钥长度常量

    @Value("${linkx.jwt.secret}")  // 行注：应用 @Value 注解
    private String jwtSecret;  // 行注：声明JWT密钥字段

    @Value("${linkx.jwt.expiration}")  // 行注：应用 @Value 注解
    private long jwtExpiration;  // 行注：声明JWT过期时间字段

    @Value("${linkx.jwt.refresh-expiration}")  // 行注：应用 @Value 注解
    private long refreshExpiration;  // 行注：声明刷新过期时间字段

    /** 防止误用默认密钥上线 */
    @PostConstruct  // 行注：应用 @PostConstruct 注解
    // 行注：定义validate配置方法
    public void validateConfiguration() {
        // 空密钥或占位密钥都会直接阻断启动，避免生产环境在弱配置下运行。
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("linkx.jwt.secret 未配置");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String normalizedSecret = jwtSecret.trim();  // 行注：初始化规范化后的密钥
        // 行注：判断是否满足当前条件
        if (DEFAULT_JWT_SECRET_PLACEHOLDER.equals(normalizedSecret)) {
            throw new IllegalStateException("linkx.jwt.secret 仍在使用默认占位值，请通过环境变量配置生产密钥");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (normalizedSecret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("linkx.jwt.secret 长度不能少于 " + MIN_SECRET_LENGTH + " 个字符");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 最终统一回写去空白后的密钥，保证后续签发与验签使用的是同一份标准值。
        jwtSecret = normalizedSecret;  // 行注：初始化JWT密钥
    }  // 行注：结束当前代码块

    // 行注：定义获取Signing键方法
    private SecretKey getSigningKey() {
        // 每次按 UTF-8 派生 HS256 密钥，避免不同运行环境字符集差异导致验签失败。
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 签发 access token。
     *
     * @param userId   用户主键
     * @param username 登录名（写入 claim，便于日志与展示）
     */
    // 行注：定义generate令牌方法
    public String generateToken(Long userId, String username) {
        Date now = new Date();  // 行注：初始化now
        Date expiryDate = new Date(now.getTime() + jwtExpiration);  // 行注：初始化expiry日期

        // access token 额外写入 username，便于审计、日志和必要时的轻量展示。
        return Jwts.builder()  // 行注：返回处理结果
                // 行注：继续调用subject
                .subject(String.valueOf(userId))
                // 行注：继续调用claim
                .claim("type", TOKEN_TYPE_ACCESS)
                // 行注：继续调用claim
                .claim("username", username)
                // 行注：继续调用issuedAt
                .issuedAt(now)
                // 行注：继续调用过期时间
                .expiration(expiryDate)
                // 行注：继续调用sign
                .signWith(getSigningKey())
                .compact();  // 行注：继续调用compact
    }  // 行注：结束当前代码块

    /**
     * 签发管理后台 access token（subject 为 adminId）。
     */
    public String generateAdminToken(Long adminId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        return Jwts.builder()
                .subject(String.valueOf(adminId))
                .claim("type", TOKEN_TYPE_ADMIN_ACCESS)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /** 签发 refresh token（不含 username claim） */
    // 行注：定义generate刷新令牌方法
    public String generateRefreshToken(Long userId) {
        Date now = new Date();  // 行注：初始化now
        Date expiryDate = new Date(now.getTime() + refreshExpiration);  // 行注：初始化expiry日期

        // refresh token 只保留最小必要信息，职责单一，只用于换取新的 access。
        return Jwts.builder()  // 行注：返回处理结果
                // 行注：继续调用subject
                .subject(String.valueOf(userId))
                // 行注：继续调用claim
                .claim("type", TOKEN_TYPE_REFRESH)
                // 行注：继续调用issuedAt
                .issuedAt(now)
                // 行注：继续调用过期时间
                .expiration(expiryDate)
                // 行注：继续调用sign
                .signWith(getSigningKey())
                .compact();  // 行注：继续调用compact
    }  // 行注：结束当前代码块

    /**
     * 解析令牌；过期时仍返回 claims 并标记 expired=true，便于刷新流程判断。
     *
     * @return 无效令牌返回 null
     */
    // 行注：定义parse令牌方法
    public ParsedToken parseToken(String token) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(token)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：尝试执行可能失败的逻辑
        try {
            // 正常情况下直接解析并返回 claims，供鉴权或刷新流程继续使用。
            // 行注：调用parser
            Claims claims = Jwts.parser()
                    // 行注：继续调用verify
                    .verifyWith(getSigningKey())
                    // 行注：继续调用构建
                    .build()
                    // 行注：继续调用parseSignedClaims
                    .parseSignedClaims(token.trim())
                    .getPayload();  // 行注：继续调用获取载荷
            return new ParsedToken(claims);  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (ExpiredJwtException exception) {
            // 过期 token 仍然保留 claims，方便 refresh 流程读取 userId 与 type 做进一步判断。
            return new ParsedToken(exception.getClaims(), true);  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT parse failed: {}", exception.getMessage());  // 行注：调用debug
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 从 subject 解析用户 ID；令牌无效时抛异常。
     *
     * @param token JWT 字符串
     * @return 用户主键
     */
    // 行注：定义获取用户ID令牌方法
    public Long getUserIdFromToken(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        // 行注：判断是否满足当前条件
        if (parsed == null || parsed.claims() == null) {
            throw new JwtException("Invalid token");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return Long.parseLong(parsed.claims().getSubject());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 读取 access token 中的 username claim（refresh 可能无此字段） */
    // 行注：定义获取Username令牌方法
    public String getUsernameFromToken(String token) {
        ParsedToken parsed = requireParsed(token);  // 行注：初始化parsed
        return parsed.claims().get("username", String.class);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 返回 claim {@code type}：{@link #TOKEN_TYPE_ACCESS} 或 {@link #TOKEN_TYPE_REFRESH} */
    // 行注：定义获取令牌类型方法
    public String getTokenType(String token) {
        ParsedToken parsed = requireParsed(token);  // 行注：初始化parsed
        return parsed.claims().get("type", String.class);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 签名有效且 type 为 access（过期令牌仍可能解析出 type） */
    // 行注：定义是否访问令牌方法
    public boolean isAccessToken(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        // 行注：判断是否满足当前条件
        if (parsed == null || parsed.claims() == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String tokenType = parsed.claims().get("type", String.class);  // 行注：初始化令牌类型
        return TOKEN_TYPE_ACCESS.equals(tokenType);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 签名有效且 type 为 admin_access */
    public boolean isAdminAccessToken(String token) {
        ParsedToken parsed = parseToken(token);
        if (parsed == null || parsed.claims() == null) {
            return false;
        }
        return TOKEN_TYPE_ADMIN_ACCESS.equals(parsed.claims().get("type", String.class));
    }

    /** 签名有效且 type 为 refresh */
    // 行注：定义是否刷新令牌方法
    public boolean isRefreshToken(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        // 行注：判断是否满足当前条件
        if (parsed == null || parsed.claims() == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return TOKEN_TYPE_REFRESH.equals(parsed.claims().get("type", String.class));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 签名有效且未过期 */
    // 行注：定义validate令牌方法
    public boolean validateToken(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        return parsed != null && parsed.claims() != null && !parsed.expired();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 无法解析或已过期返回 true */
    // 行注：定义是否令牌Expired方法
    public boolean isTokenExpired(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        return parsed == null || parsed.expired();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 用于 access 黑名单 TTL：与令牌剩余有效期对齐 */
    // 行注：定义获取剩余时长ValidityMillis方法
    public long getRemainingValidityMillis(String token) {
        ParsedToken parsed = requireParsed(token);  // 行注：初始化parsed
        // denylist TTL 只需要覆盖“剩余生命周期”，过期后 Redis 记录即可自然淘汰。
        long remaining = parsed.claims().getExpiration().getTime() - System.currentTimeMillis();  // 行注：初始化剩余时长
        return Math.max(remaining, 0L);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义requireParsed方法
    private ParsedToken requireParsed(String token) {
        ParsedToken parsed = parseToken(token);  // 行注：初始化parsed
        // 行注：判断是否满足当前条件
        if (parsed == null || parsed.claims() == null) {
            throw new JwtException("Invalid token");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return parsed;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * @param claims  JWT 载荷
     * @param expired 是否已过期（解析时捕获 ExpiredJwtException 则为 true）
     */
    // 行注：定义 ParsedToken 记录类型
    public record ParsedToken(Claims claims, boolean expired) {
        // 行注：调用Parsed令牌
        ParsedToken(Claims claims) {
            this(claims, claims.getExpiration().before(new Date()));  // 行注：调用获取过期时间
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
