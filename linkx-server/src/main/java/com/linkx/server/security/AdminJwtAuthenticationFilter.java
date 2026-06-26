package com.linkx.server.security;

import com.linkx.server.entity.SysAdmin;
import com.linkx.server.mapper.SysAdminMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析管理后台 Bearer JWT（type=admin_access），仅作用于 /api/admin/**。
 */
@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SysAdminMapper adminMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/admin")) {
            return true;
        }
        return path.startsWith("/api/admin/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearer(request);
        if (StringUtils.hasText(token)) {
            JwtTokenProvider.ParsedToken parsed = jwtTokenProvider.parseToken(token);
            if (parsed != null && parsed.claims() != null && !parsed.expired()
                    && jwtTokenProvider.isAdminAccessToken(token)) {
                Long adminId = Long.parseLong(parsed.claims().getSubject());
                SysAdmin admin = adminMapper.selectById(adminId);
                if (admin != null && admin.getStatus() != null && admin.getStatus() == 1) {
                    String role = parsed.claims().get("role", String.class);
                    AdminPrincipal principal = new AdminPrincipal(adminId, admin.getUsername(),
                            role != null ? role : admin.getRole());
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private static String resolveBearer(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        return null;
    }
}