package com.linkx.server.module.auth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysLoginLog;

/**
 * 用户登录日志写入与查询。
 */
public interface LoginLogService {

    void recordSuccess(Long userId, String username, String loginType, String ip, String userAgent);

    void recordFailure(String username, String loginType, String ip, String userAgent, String failReason);

    Page<SysLoginLog> pageForAdmin(int page, int size, String username, Long userId, Integer loginStatus);
}