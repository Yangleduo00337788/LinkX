package com.linkx.server.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 使用 refresh 轮换一对新 access + refresh。 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;
}