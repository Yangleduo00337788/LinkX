package com.linkx.server.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 前端登录/注册页用于判断是否展示验证码及可用 scene。 */
@Data
@AllArgsConstructor
public class CaptchaMetaDTO {
    private boolean enabled;
    /** 通常为 login、register */
    private List<String> scenes;
}