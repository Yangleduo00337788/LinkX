package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.SysRuntimeConfig;
import com.linkx.server.mapper.SysRuntimeConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RuntimeConfigService {

    public static final String KEY_REGISTER_ENABLED = "auth.register.enabled";
    public static final String KEY_USER_CAPTCHA_ENABLED = "auth.user.captcha.enabled";

    private final SysRuntimeConfigMapper configMapper;

    public String getString(String key, String defaultValue) {
        SysRuntimeConfig row = getRow(key);
        if (row == null || !StringUtils.hasText(row.getConfigValue())) {
            return defaultValue;
        }
        return row.getConfigValue().trim();
    }

    public boolean getBool(String key, boolean defaultValue) {
        String v = getString(key, defaultValue ? "true" : "false");
        return "1".equals(v) || "true".equalsIgnoreCase(v) || "yes".equalsIgnoreCase(v);
    }

    public Map<String, String> listPublicSettings() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put(KEY_REGISTER_ENABLED, String.valueOf(getBool(KEY_REGISTER_ENABLED, true)));
        m.put(KEY_USER_CAPTCHA_ENABLED, String.valueOf(getBool(KEY_USER_CAPTCHA_ENABLED, false)));
        return m;
    }

    public List<SysRuntimeConfig> listAll() {
        return configMapper.selectList(new LambdaQueryWrapper<SysRuntimeConfig>().orderByAsc(SysRuntimeConfig::getConfigKey));
    }

    public void upsert(String key, String value, String description) {
        SysRuntimeConfig row = getRow(key);
        if (row == null) {
            row = new SysRuntimeConfig();
            row.setConfigKey(key);
            row.setConfigValue(value);
            row.setValueType("STRING");
            row.setDescription(description);
            configMapper.insert(row);
        } else {
            row.setConfigValue(value);
            if (description != null) {
                row.setDescription(description);
            }
            configMapper.updateById(row);
        }
    }

    private SysRuntimeConfig getRow(String key) {
        return configMapper.selectOne(new LambdaQueryWrapper<SysRuntimeConfig>().eq(SysRuntimeConfig::getConfigKey, key).last("LIMIT 1"));
    }
}