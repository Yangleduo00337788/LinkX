package com.linkx.server.common;  // 行注：声明当前文件所在包 com.linkx.server.common

import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

/**
 * 上传资源 URL 校验：防止用户资料中写入任意外链或路径穿越。
 */
// 行注：定义 UploadAssetUrlUtils 类
public final class UploadAssetUrlUtils {

    private static final String DEFAULT_AVATAR_PATH_PREFIX = "/uploads/avatar/";  // 行注：定义默认头像路径PREFIX常量

    // 行注：定义上传资源URL工具方法
    private UploadAssetUrlUtils() {
    }  // 行注：结束当前代码块

    /**
     * 规范化NullableText。
     *
     * @param value 待处理值
     * @return 字符串结果
     */
    // 行注：定义规范化Nullable文本方法
    public static String normalizeNullableText(String value) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(value)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return value.trim();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 头像仅允许本系统 {@code /uploads/avatar/} 或配置的 uploadUrl 前缀下路径。
     */
    // 行注：定义规范化头像URL方法
    public static String normalizeAvatarUrl(String rawAvatarUrl, String uploadUrl, String assetLabel) {
        String normalizedAvatarUrl = normalizeNullableText(rawAvatarUrl);  // 行注：初始化规范化后的头像URL
        // 行注：判断是否满足当前条件
        if (normalizedAvatarUrl == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (normalizedAvatarUrl.contains("..") || normalizedAvatarUrl.contains("\\")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, assetLabel + "地址不合法");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        String avatarUrlPrefix = buildAvatarUrlPrefix(uploadUrl);  // 行注：初始化头像URLPrefix
        // 行注：判断是否满足当前条件
        if (normalizedAvatarUrl.startsWith(DEFAULT_AVATAR_PATH_PREFIX)
                // 行注：调用是否包含文本
                || (StringUtils.hasText(avatarUrlPrefix) && normalizedAvatarUrl.startsWith(avatarUrlPrefix))) {
            return normalizedAvatarUrl;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        throw new BusinessException(ErrorCode.BAD_REQUEST, assetLabel + "仅支持使用系统上传图片");  // 行注：抛出异常并中断当前流程
    }  // 行注：结束当前代码块

    // 行注：定义构建头像URLPrefix方法
    private static String buildAvatarUrlPrefix(String uploadUrl) {
        String normalizedUploadUrl = normalizeNullableText(uploadUrl);  // 行注：初始化规范化后的上传URL
        // 行注：判断是否满足当前条件
        if (normalizedUploadUrl == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：返回处理结果
        return normalizedUploadUrl.endsWith("/") ? normalizedUploadUrl + "avatar/" : normalizedUploadUrl + "/avatar/";
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
