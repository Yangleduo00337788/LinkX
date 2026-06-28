package com.linkx.server.security;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * JWT 认证后 {@link UserDetails#getUsername()} 为 userId 字符串（见 {@link JwtAuthenticationFilter}）。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long resolveUserId(UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }
}