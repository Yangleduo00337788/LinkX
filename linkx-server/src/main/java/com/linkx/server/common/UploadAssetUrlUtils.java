package com.linkx.server.common;

import org.springframework.util.StringUtils;

/**
 * 上传资源 URL 校验：防止用户资料中写入任意外链或路径穿越。
 */
public final class UploadAssetUrlUtils {

    private static final String DEFAULT_AVATAR_PATH_PREFIX = "/uploads/avatar/";

    private UploadAssetUrlUtils() {
    }

    public static String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 头像仅允许本系统 {@code /uploads/avatar/} 或配置的 uploadUrl 前缀下路径。
     */
    public static String normalizeAvatarUrl(String rawAvatarUrl, String uploadUrl, String assetLabel) {
        String normalizedAvatarUrl = normalizeNullableText(rawAvatarUrl);
        if (normalizedAvatarUrl == null) {
            return null;
        }
        if (normalizedAvatarUrl.contains("..") || normalizedAvatarUrl.contains("\\")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, assetLabel + "地址不合法");
        }
        String avatarUrlPrefix = buildAvatarUrlPrefix(uploadUrl);
        if (normalizedAvatarUrl.startsWith(DEFAULT_AVATAR_PATH_PREFIX)
                || (StringUtils.hasText(avatarUrlPrefix) && normalizedAvatarUrl.startsWith(avatarUrlPrefix))) {
            return normalizedAvatarUrl;
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, assetLabel + "仅支持使用系统上传图片");
    }

    private static String buildAvatarUrlPrefix(String uploadUrl) {
        String normalizedUploadUrl = normalizeNullableText(uploadUrl);
        if (normalizedUploadUrl == null) {
            return null;
        }
        return normalizedUploadUrl.endsWith("/") ? normalizedUploadUrl + "avatar/" : normalizedUploadUrl + "/avatar/";
    }
}