package com.linkx.server.module.config.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.SysRuntimeConfig;
import com.linkx.server.mapper.SysRuntimeConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuntimeConfigService {

    public static final String KEY_REGISTER_ENABLED = "auth.register.enabled";
    public static final String KEY_USER_CAPTCHA_ENABLED = "auth.user.captcha.enabled";
    /** 找回密码是否在 API 响应中返回 resetToken（无邮件通道时联调） */
    public static final String KEY_PASSWORD_RESET_EXPOSE_TOKEN = "auth.password_reset.expose_token";

    private final SysRuntimeConfigMapper mapper;

    public boolean getBoolean(String key, boolean defaultValue) {
        SysRuntimeConfig row = getRow(key);
        if (row == null || row.getConfigValue() == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(row.getConfigValue().trim())
                || "1".equals(row.getConfigValue().trim());
    }

    public Map<String, Object> getAdminSnapshot(boolean yamlRegisterOpen, boolean yamlCaptchaEnabled) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("registerEnabled", getBoolean(KEY_REGISTER_ENABLED, yamlRegisterOpen));
        m.put("userCaptchaEnabled", getBoolean(KEY_USER_CAPTCHA_ENABLED, yamlCaptchaEnabled));
        m.put("registerEnabledOverridden", getRow(KEY_REGISTER_ENABLED) != null);
        m.put("userCaptchaEnabledOverridden", getRow(KEY_USER_CAPTCHA_ENABLED) != null);
        return m;
    }

    @Transactional
    public void upsertBoolean(String key, boolean value, String description) {
        SysRuntimeConfig row = getRow(key);
        if (row == null) {
            row = new SysRuntimeConfig();
            row.setConfigKey(key);
            row.setValueType("BOOL");
            row.setDescription(description);
        }
        row.setConfigValue(value ? "true" : "false");
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
    }

    private SysRuntimeConfig getRow(String key) {
        return mapper.selectOne(new LambdaQueryWrapper<SysRuntimeConfig>()
                .eq(SysRuntimeConfig::getConfigKey, key)
                .last("LIMIT 1"));
    }
}