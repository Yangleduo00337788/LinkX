package com.linkx.server.security;

import com.linkx.server.module.auth.service.AccessTokenDenylistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从 {@code Authorization: Bearer} 解析 JWT，写入 Spring Security 上下文。
 * <p>
 * 仅接受 type=access 且未在登出黑名单中的令牌；refresh 不能访问普通 API。
 * {@link UserDetails#getUsername()} 在此项目中为 userId 字符串。
 * </p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final AccessTokenDenylistService accessTokenDenylistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);

        if (StringUtils.hasText(token)) {
            // 登出后的 access 一律视为未登录
            if (accessTokenDenylistService.isDenied(token)) {
                SecurityContextHolder.clearContext();
            } else {
                JwtTokenProvider.ParsedToken parsed = jwtTokenProvider.parseToken(token);
                String tokenType = parsed != null && parsed.claims() != null
                        ? parsed.claims().get("type", String.class) : null;
                if (parsed != null && parsed.claims() != null && !parsed.expired()
                        && JwtTokenProvider.TOKEN_TYPE_ACCESS.equals(tokenType)) {
                    Long userId = Long.parseLong(parsed.claims().getSubject());
                    UserDetails userDetails = userDetailsService.loadUserByUsername(String.valueOf(userId));
                    if (!userDetails.isEnabled()) {
                        SecurityContextHolder.clearContext();
                    } else if (!userDetails.isAccountNonExpired()
                            || !userDetails.isAccountNonLocked()
                            || !userDetails.isCredentialsNonExpired()) {
                        SecurityContextHolder.clearContext();
                    } else {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}