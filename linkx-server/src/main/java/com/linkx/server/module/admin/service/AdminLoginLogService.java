package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysLoginLog;
import com.linkx.server.module.admin.dto.AdminLoginLogListItemDTO;
import com.linkx.server.module.auth.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminLoginLogService {

    private final LoginLogService loginLogService;

    public Page<AdminLoginLogListItemDTO> list(int page, int size, String username, Long userId, Integer loginStatus) {
        Page<SysLoginLog> raw = loginLogService.pageForAdmin(page, size, username, userId, loginStatus);
        Page<AdminLoginLogListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(l -> AdminLoginLogListItemDTO.builder()
                .id(l.getId())
                .userId(l.getUserId())
                .username(l.getUsername())
                .loginType(l.getLoginType())
                .loginIp(l.getLoginIp())
                .loginLocation(l.getLoginLocation())
                .userAgent(l.getUserAgent())
                .loginStatus(l.getLoginStatus())
                .failReason(l.getFailReason())
                .createTime(l.getCreateTime())
                .build()).toList());
        return result;
    }
}