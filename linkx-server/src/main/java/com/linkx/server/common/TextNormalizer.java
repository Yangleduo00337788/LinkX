package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

/**
 * 用户输入文本规范化：去首尾空白、折叠空白、长度校验。
 * <p>注册/登录用户名等使用单行模式；群公告等可使用多行模式。</p>
 */
// 行注：定义 TextNormalizer 类
public final class TextNormalizer {

    // 行注：定义文本Normalizer方法
    private TextNormalizer() {
    }  // 行注：结束当前代码块

    /**
     * 规范化RequiredSingleLine。
     *
     * @param value 待处理值
     * @param maxLength 最大长度
     * @param fieldLabel 字段中文名
     * @return 字符串结果
     */
    // 行注：定义规范化必填单聊Line方法
    public static String normalizeRequiredSingleLine(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeSingleLine(value, maxLength, fieldLabel);  // 行注：初始化规范化后的
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 规范化OptionalSingleLine。
     *
     * @param value 待处理值
     * @param maxLength 最大长度
     * @param fieldLabel 字段中文名
     * @return 字符串结果
     */
    // 行注：定义规范化可选单聊Line方法
    public static String normalizeOptionalSingleLine(String value, int maxLength, String fieldLabel) {
        return normalizeSingleLine(value, maxLength, fieldLabel);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 规范化RequiredMultiline。
     *
     * @param value 待处理值
     * @param maxLength 最大长度
     * @param fieldLabel 字段中文名
     * @return 字符串结果
     */
    // 行注：定义规范化必填多行方法
    public static String normalizeRequiredMultiline(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeMultiline(value, maxLength, fieldLabel);  // 行注：初始化规范化后的
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 规范化OptionalMultiline。
     *
     * @param value 待处理值
     * @param maxLength 最大长度
     * @param fieldLabel 字段中文名
     * @return 字符串结果
     */
    // 行注：定义规范化可选多行方法
    public static String normalizeOptionalMultiline(String value, int maxLength, String fieldLabel) {
        return normalizeMultiline(value, maxLength, fieldLabel);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化单聊Line方法
    private static String normalizeSingleLine(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeWhitespace(value, false);  // 行注：初始化规范化后的
        validateLength(normalized, maxLength, fieldLabel);  // 行注：调用validate长度
        return normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化多行方法
    private static String normalizeMultiline(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeWhitespace(value, true);  // 行注：初始化规范化后的
        validateLength(normalized, maxLength, fieldLabel);  // 行注：调用validate长度
        return normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化Whitespace方法
    private static String normalizeWhitespace(String value, boolean multiline) {
        // 行注：判断是否满足当前条件
        if (value == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        StringBuilder builder = new StringBuilder(value.length());  // 行注：初始化构建器
        boolean previousWhitespace = false;  // 行注：初始化previousWhitespace
        // 行注：遍历当前集合或范围
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);  // 行注：初始化当前
            // 行注：判断是否满足当前条件
            if (current == '\u0000') {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (current == '\r') {
                // 行注：判断是否满足当前条件
                if (multiline) {
                    // 行注：判断是否满足当前条件
                    if (!endsWithNewline(builder)) {
                        builder.append('\n');  // 行注：调用append
                    }  // 行注：结束当前代码块
                    previousWhitespace = false;  // 行注：初始化previousWhitespace
                // 行注：执行当前方法调用
                } else if (!previousWhitespace) {
                    builder.append(' ');  // 行注：调用append
                    previousWhitespace = true;  // 行注：初始化previousWhitespace
                }  // 行注：结束当前代码块
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (current == '\n') {
                // 行注：判断是否满足当前条件
                if (multiline) {
                    // 行注：判断是否满足当前条件
                    if (!endsWithNewline(builder)) {
                        builder.append('\n');  // 行注：调用append
                    }  // 行注：结束当前代码块
                    previousWhitespace = false;  // 行注：初始化previousWhitespace
                // 行注：执行当前方法调用
                } else if (!previousWhitespace) {
                    builder.append(' ');  // 行注：调用append
                    previousWhitespace = true;  // 行注：初始化previousWhitespace
                }  // 行注：结束当前代码块
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (Character.isISOControl(current) && current != '\t') {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (Character.isWhitespace(current)) {
                // 行注：判断是否满足当前条件
                if (!previousWhitespace) {
                    builder.append(' ');  // 行注：调用append
                    previousWhitespace = true;  // 行注：初始化previousWhitespace
                }  // 行注：结束当前代码块
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            builder.append(current);  // 行注：调用append
            previousWhitespace = false;  // 行注：初始化previousWhitespace
        }  // 行注：结束当前代码块

        String normalized = builder.toString().trim();  // 行注：初始化规范化后的
        return normalized.isEmpty() ? null : normalized;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义endsNewline方法
    private static boolean endsWithNewline(StringBuilder builder) {
        return builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n';  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义validate长度方法
    private static void validateLength(String normalized, int maxLength, String fieldLabel) {
        // 行注：判断是否满足当前条件
        if (normalized == null || maxLength <= 0) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "长度不能超过" + maxLength + "个字符");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
