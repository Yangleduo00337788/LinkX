package com.linkx.server.module.auth.service;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service

import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.util.regex.Pattern;  // 行注：引入 Pattern 类型

/**
 * 注册密码强度策略（与前端应对齐）。
 * <p>8～50 位，且至少包含一个字母与一个数字。</p>
 */
// 行注：定义 PasswordPolicy 类
public final class PasswordPolicy {

    private static final Pattern COMPLEXITY = Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d).+$");  // 行注：定义COMPLEXITY常量

    // 行注：定义密码策略方法
    private PasswordPolicy() {
    }  // 行注：结束当前代码块

    /**
     * @param password 明文密码
     * @throws BusinessException 不符合规则时 {@link ErrorCode#BAD_REQUEST}
     */
    // 行注：定义validate方法
    public static void validate(String password) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (password.length() < 8 || password.length() > 50) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码长度需为8-50位，且包含字母和数字");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!COMPLEXITY.matcher(password).matches()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "密码需同时包含字母和数字");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
