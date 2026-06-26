package com.linkx.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysLoginLog;
import com.linkx.server.mapper.SysLoginLogMapper;
import com.linkx.server.module.auth.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private static final int USER_AGENT_MAX = 255;

    private final SysLoginLogMapper loginLogMapper;

    @Override
    public void recordSuccess(Long userId, String username, String loginType, String ip, String userAgent) {
        insert(userId, username, loginType, ip, userAgent, 1, null);
    }

    @Override
    public void recordFailure(String username, String loginType, String ip, String userAgent, String failReason) {
        insert(null, username, loginType, ip, userAgent, 0, truncate(failReason, 255));
    }

    @Override
    public Page<SysLoginLog> pageForAdmin(int page, int size, String username, Long userId, Integer loginStatus) {
        LambdaQueryWrapper<SysLoginLog> w = new LambdaQueryWrapper<>();
        if (userId != null) {
            w.eq(SysLoginLog::getUserId, userId);
        }
        if (StringUtils.hasText(username)) {
            w.like(SysLoginLog::getUsername, username.trim());
        }
        if (loginStatus != null) {
            w.eq(SysLoginLog::getLoginStatus, loginStatus);
        }
        w.orderByDesc(SysLoginLog::getCreateTime);
        return loginLogMapper.selectPage(new Page<>(page, size), w);
    }

    private void insert(Long userId, String username, String loginType, String ip, String userAgent,
                        int status, String failReason) {
        SysLoginLog log = new SysLoginLog();
        log.setUserId(userId);
        log.setUsername(username != null ? username : "");
        log.setLoginType(loginType != null ? loginType : "password");
        log.setLoginIp(ip);
        log.setUserAgent(truncate(userAgent, USER_AGENT_MAX));
        log.setLoginStatus(status);
        log.setFailReason(failReason);
        log.setDeleted(0);
        loginLogMapper.insert(log);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}