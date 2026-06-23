package com.linkx.server.module.auth.service;

import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 注册密码强度策略（与前端应对齐）。
 * <p>8～50 位，且至少包含一个字母与一个数字。</p>
 */
public final class PasswordPolicy {

    private static final Pattern COMPLEXITY = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).+$");

    private PasswordPolicy() {
    }

    /**
     * @param password 明文密码
     * @throws BusinessException 不符合规则时 {@link ErrorCode#BAD_REQUEST}
     */
    public static void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");
        }
        if (password.length() < 8 || password.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度需为8-50位，且包含字母和数字");
        }
        if (!COMPLEXITY.matcher(password).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需同时包含字母和数字");
        }
    }
}