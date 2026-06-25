package com.linkx.server.security;  // 行注：声明当前文件所在包 com.linkx.server.security

import com.linkx.server.module.auth.service.AccessTokenDenylistService;  // 行注：引入 AccessTokenDenylistService 类型
import jakarta.servlet.FilterChain;  // 行注：引入 FilterChain 类型
import jakarta.servlet.ServletException;  // 行注：引入 ServletException 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import jakarta.servlet.http.HttpServletResponse;  // 行注：引入 HttpServletResponse 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // 行注：引入 UsernamePasswordAuthenticationToken 类型
import org.springframework.security.core.context.SecurityContextHolder;  // 行注：引入 SecurityContextHolder 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.security.core.userdetails.UserDetailsService;  // 行注：引入 UserDetailsService 类型
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;  // 行注：引入 WebAuthenticationDetailsSource 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型
import org.springframework.web.filter.OncePerRequestFilter;  // 行注：引入 OncePerRequestFilter 类型

import java.io.IOException;  // 行注：引入 IOException 类型

/**
 * 从 {@code Authorization: Bearer} 解析 JWT，写入 Spring Security 上下文。
 * <p>
 * 仅接受 type=access 且未在登出黑名单中的令牌；refresh 不能访问普通 API。
 * {@link UserDetails#getUsername()} 在此项目中为 userId 字符串。
 * </p>
 */
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 JwtAuthenticationFilter 类
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;  // 行注：注入JWT令牌提供器依赖
    private final UserDetailsService userDetailsService;  // 行注：注入用户Details服务依赖
    private final AccessTokenDenylistService accessTokenDenylistService;  // 行注：注入访问令牌拒绝列表服务依赖

    /**
     * 每个请求最多执行一次：解析 Bearer → 校验黑名单与 access 类型 → 加载用户并设置认证。
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义do过滤Internal方法
    protected void doFilterInternal(HttpServletRequest request,
                                    // 行注：补充当前表达式片段
                                    HttpServletResponse response,
                                    // 行注：开始当前语句对应的代码块
                                    FilterChain filterChain) throws ServletException, IOException {
        // 先从标准 Authorization 头中提取 Bearer token；没有 token 时直接放行到后续链路。
        String token = getTokenFromRequest(request);  // 行注：初始化令牌

        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(token)) {
            // 登出后的 access 一律视为未登录
            // 行注：判断是否满足当前条件
            if (accessTokenDenylistService.isDenied(token)) {
                SecurityContextHolder.clearContext();  // 行注：调用clearContext
            // 行注：开始当前语句对应的代码块
            } else {
                // 解析 token 时同时获得 claims 与过期状态，避免重复验签。
                JwtTokenProvider.ParsedToken parsed = jwtTokenProvider.parseToken(token);  // 行注：执行初始化操作
                // 行注：调用claims
                String tokenType = parsed != null && parsed.claims() != null
                        ? parsed.claims().get("type", String.class) : null;  // 行注：调用claims
                // 只有“未过期的 access token”才允许建立 API 访问身份。
                // 行注：判断是否满足当前条件
                if (parsed != null && parsed.claims() != null && !parsed.expired()
                        // 行注：调用equals
                        && JwtTokenProvider.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                    Long userId = Long.parseLong(parsed.claims().getSubject());  // 行注：初始化用户ID
                    // 当前项目里 UserDetailsService 的入参并不是用户名，而是 userId 字符串。
                    UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));  // 行注：初始化用户Details
                    // 行注：判断是否满足当前条件
                    if (!userDetails.isEnabled()) {
                        // 账号被禁用时，即使 token 仍然有效，也不能继续访问受保护资源。
                        SecurityContextHolder.clearContext();  // 行注：调用clearContext
                    // 行注：调用是否AccountNonExpired
                    } else if (!userDetails.isAccountNonExpired()
                            // 行注：调用是否AccountNonLocked
                            || !userDetails.isAccountNonLocked()
                            // 行注：调用是否CredentialsNonExpired
                            || !userDetails.isCredentialsNonExpired()) {
                        // 预留标准账号状态开关，未来扩展锁定、过期策略时无需改过滤器主流程。
                        SecurityContextHolder.clearContext();  // 行注：调用clearContext
                    // 行注：开始当前语句对应的代码块
                    } else {
                        // 构造认证对象并写入上下文，后续控制器即可通过 @AuthenticationPrincipal 取到当前用户。
                        // 行注：补充当前表达式片段
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());  // 行注：调用获取Authorities
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));  // 行注：调用设置Details
                        SecurityContextHolder.getContext().setAuthentication(authentication);  // 行注：调用获取Context
                    }  // 行注：结束当前代码块
                }  // 行注：结束当前代码块
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        // 无论是否识别出登录态，都必须继续执行过滤器链，让公开接口或异常处理器正常工作。
        filterChain.doFilter(request, response);  // 行注：调用do过滤
    }  // 行注：结束当前代码块

    /** 从 Authorization 头截取 Bearer 后的 JWT 原文 */
    // 行注：定义获取令牌请求方法
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");  // 行注：初始化bearer令牌
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return null;  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
