package com.linkx.server.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UploadAssetUrlUtilsTest {

    @Test
    void should_normalize_avatar_url_for_default_upload_path() {
        String avatarUrl = UploadAssetUrlUtils.normalizeAvatarUrl(" /uploads/avatar/test.png ", "http://localhost:8080/uploads/", "头像");

        assertEquals("/uploads/avatar/test.png", avatarUrl);
    }

    @Test
    void should_normalize_avatar_url_for_configured_upload_prefix() {
        String avatarUrl = UploadAssetUrlUtils.normalizeAvatarUrl("https://cdn.example.com/uploads/avatar/test.png", "https://cdn.example.com/uploads", "群头像");

        assertEquals("https://cdn.example.com/uploads/avatar/test.png", avatarUrl);
    }

    @Test
    void should_return_null_for_blank_avatar_url() {
        assertNull(UploadAssetUrlUtils.normalizeAvatarUrl("   ", "http://localhost:8080/uploads/", "头像"));
    }

    @Test
    void should_reject_illegal_avatar_url() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> UploadAssetUrlUtils.normalizeAvatarUrl("../avatar/test.png", "http://localhost:8080/uploads/", "头像")
        );

        assertEquals("头像地址不合法", exception.getMessage());
    }

    @Test
    void should_reject_non_uploaded_avatar_url() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> UploadAssetUrlUtils.normalizeAvatarUrl("https://example.com/avatar/test.png", "http://localhost:8080/uploads/", "群头像")
        );

        assertEquals("群头像仅支持使用系统上传图片", exception.getMessage());
    }
}
