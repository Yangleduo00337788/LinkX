package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysLoginLog;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysLoginLogMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.admin.dto.AdminAuditLogListItemDTO;
import com.linkx.server.module.admin.dto.AdminMessageListItemDTO;
import com.linkx.server.module.admin.service.AdminAuditLogService;
import com.linkx.server.module.admin.service.AdminMessageService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class AdminExportController {

    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final AdminAuditLogService auditLogService;
    private final AdminMessageService messageService;

    @GetMapping("/users")
    public void exportUsers(@RequestParam(required = false) String keyword, HttpServletResponse response)
            throws IOException {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(SysUser::getUsername, keyword.trim());
        }
        w.orderByDesc(SysUser::getCreateTime).last("LIMIT 5000");
        List<SysUser> rows = userMapper.selectList(w);
        writeCsv(response, "users.csv",
                List.of("id", "username", "nickname", "status", "createTime"),
                rows.stream()
                        .map(u -> List.of(
                                str(u.getId()),
                                csv(u.getUsername()),
                                csv(u.getNickname()),
                                str(u.getStatus()),
                                str(u.getCreateTime())))
                        .toList());
    }

    @GetMapping("/login-logs")
    public void exportLoginLogs(@RequestParam(required = false) String username, HttpServletResponse response)
            throws IOException {
        LambdaQueryWrapper<SysLoginLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            w.like(SysLoginLog::getUsername, username.trim());
        }
        w.orderByDesc(SysLoginLog::getLoginTime).last("LIMIT 5000");
        List<SysLoginLog> rows = loginLogMapper.selectList(w);
        writeCsv(response, "login-logs.csv",
                List.of("id", "userId", "username", "loginStatus", "loginIp", "createTime"),
                rows.stream()
                        .map(l -> List.of(
                                str(l.getId()),
                                str(l.getUserId()),
                                csv(l.getUsername()),
                                str(l.getLoginStatus()),
                                csv(l.getLoginIp()),
                                str(l.getCreateTime())))
                        .toList());
    }

    @GetMapping("/audit-logs")
    public void exportAuditLogs(@RequestParam(required = false) String action, HttpServletResponse response)
            throws IOException {
        Page<AdminAuditLogListItemDTO> page = auditLogService.list(1, 5000, action);
        writeCsv(response, "audit-logs.csv",
                List.of("id", "adminUsername", "action", "targetType", "targetId", "detail", "ip", "createTime"),
                page.getRecords().stream()
                        .map(a -> List.of(
                                str(a.getId()),
                                csv(a.getAdminUsername()),
                                csv(a.getAction()),
                                csv(a.getTargetType()),
                                csv(a.getTargetId()),
                                csv(a.getDetail()),
                                csv(a.getClientIp()),
                                str(a.getCreateTime())))
                        .toList());
    }

    @GetMapping("/messages")
    public void exportMessages(@RequestParam(required = false) Long sessionId, HttpServletResponse response)
            throws IOException {
        Page<AdminMessageListItemDTO> page = messageService.list(1, 5000, sessionId, null);
        writeCsv(response, "messages.csv",
                List.of("id", "sessionId", "fromUserId", "messageType", "content", "createTime"),
                page.getRecords().stream()
                        .map(m -> List.of(
                                str(m.getId()),
                                str(m.getSessionId()),
                                str(m.getFromUserId()),
                                str(m.getMessageType()),
                                csv(m.getContent()),
                                str(m.getCreateTime())))
                        .toList());
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\n") || t.contains("\r")) {
            return "\"" + t + "\"";
        }
        return t;
    }

    private void writeCsv(HttpServletResponse response, String filename, List<String> headers, List<List<String>> rows)
            throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        try (PrintWriter w = new PrintWriter(response.getOutputStream(), true, StandardCharsets.UTF_8)) {
            w.println(String.join(",", headers));
            for (List<String> row : rows) {
                w.println(String.join(",", row));
            }
        }
    }
}