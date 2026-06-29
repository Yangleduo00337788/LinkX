package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.config.LinkxSecurityProperties;
import com.linkx.server.module.config.service.RuntimeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/system-settings")
@RequiredArgsConstructor
public class AdminSystemSettingsController {

    private final RuntimeConfigService runtimeConfigService;
    private final LinkxSecurityProperties securityProperties;

    @GetMapping
    public Result<Map<String, Object>> get() {
        boolean yamlCaptcha = securityProperties.getCaptcha() != null
                && securityProperties.getCaptcha().isEnabled();
        return Result.success(runtimeConfigService.getAdminSnapshot(true, yamlCaptcha));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Map<String, Object> body) {
        if (body.containsKey("registerEnabled")) {
            runtimeConfigService.upsertBoolean(
                    RuntimeConfigService.KEY_REGISTER_ENABLED,
                    Boolean.TRUE.equals(body.get("registerEnabled")),
                    "是否开放用户注册");
        }
        if (body.containsKey("userCaptchaEnabled")) {
            runtimeConfigService.upsertBoolean(
                    RuntimeConfigService.KEY_USER_CAPTCHA_ENABLED,
                    Boolean.TRUE.equals(body.get("userCaptchaEnabled")),
                    "用户端登录/注册验证码");
        }
        return Result.success();
    }
}