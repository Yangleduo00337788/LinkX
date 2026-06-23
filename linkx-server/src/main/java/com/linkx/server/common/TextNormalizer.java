package com.linkx.server.common;

import org.springframework.util.StringUtils;

/**
 * 用户输入文本规范化：去首尾空白、折叠空白、长度校验。
 * <p>注册/登录用户名等使用单行模式；群公告等可使用多行模式。</p>
 */
public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String normalizeRequiredSingleLine(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeSingleLine(value, maxLength, fieldLabel);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "不能为空");
        }
        return normalized;
    }

    public static String normalizeOptionalSingleLine(String value, int maxLength, String fieldLabel) {
        return normalizeSingleLine(value, maxLength, fieldLabel);
    }

    public static String normalizeRequiredMultiline(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeMultiline(value, maxLength, fieldLabel);
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "不能为空");
        }
        return normalized;
    }

    public static String normalizeOptionalMultiline(String value, int maxLength, String fieldLabel) {
        return normalizeMultiline(value, maxLength, fieldLabel);
    }

    private static String normalizeSingleLine(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeWhitespace(value, false);
        validateLength(normalized, maxLength, fieldLabel);
        return normalized;
    }

    private static String normalizeMultiline(String value, int maxLength, String fieldLabel) {
        String normalized = normalizeWhitespace(value, true);
        validateLength(normalized, maxLength, fieldLabel);
        return normalized;
    }

    private static String normalizeWhitespace(String value, boolean multiline) {
        if (value == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(value.length());
        boolean previousWhitespace = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current == '\u0000') {
                continue;
            }
            if (current == '\r') {
                if (multiline) {
                    if (!endsWithNewline(builder)) {
                        builder.append('\n');
                    }
                    previousWhitespace = false;
                } else if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
                continue;
            }
            if (current == '\n') {
                if (multiline) {
                    if (!endsWithNewline(builder)) {
                        builder.append('\n');
                    }
                    previousWhitespace = false;
                } else if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
                continue;
            }
            if (Character.isISOControl(current) && current != '\t') {
                continue;
            }
            if (Character.isWhitespace(current)) {
                if (!previousWhitespace) {
                    builder.append(' ');
                    previousWhitespace = true;
                }
                continue;
            }
            builder.append(current);
            previousWhitespace = false;
        }

        String normalized = builder.toString().trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static boolean endsWithNewline(StringBuilder builder) {
        return builder.length() > 0 && builder.charAt(builder.length() - 1) == '\n';
    }

    private static void validateLength(String normalized, int maxLength, String fieldLabel) {
        if (normalized == null || maxLength <= 0) {
            return;
        }
        if (normalized.length() > maxLength) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, fieldLabel + "长度不能超过" + maxLength + "个字符");
        }
    }
}
