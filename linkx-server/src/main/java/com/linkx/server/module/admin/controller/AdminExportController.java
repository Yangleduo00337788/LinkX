package com.linkx.server.module.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysAuditLog;
import com.linkx.server.entity.SysLoginLog;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysAuditLogMapper;
import com.linkx.server.mapper.SysLoginLogMapper;
import com.linkx.server.mapper.SysUserMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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

    private static final int EXPORT_LIMIT = 5000;

    private final SysUserMapper userMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final SysAuditLogMapper auditLogMapper;

    @GetMapping("/users.csv")
    public void usersCsv(@RequestParam(required = false) String keyword, HttpServletResponse response)
            throws IOException {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            w.and(q -> q.like(SysUser::getUsername, k).or().like(SysUser::getNickname, k));
        }
        w.orderByDesc(SysUser::getCreateTime).last("LIMIT " + EXPORT_LIMIT);
        List<SysUser> rows = userMapper.selectList(w);
        writeCsv(response, "users.csv", new String[]{"id", "username", "nickname", "status", "createTime"},
                rows.stream()
                        .map(u -> new String[]{
                                str(u.getId()),
                                csv(u.getUsername()),
                                csv(u.getNickname()),
                                str(u.getStatus()),
                                csv(String.valueOf(u.getCreateTime()))
                        })
                        .toList());
    }

    @GetMapping("/login-logs.csv")
    public void loginLogsCsv(@RequestParam(required = false) String username, HttpServletResponse response)
            throws IOException {
        LambdaQueryWrapper<SysLoginLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            w.like(SysLoginLog::getUsername, username.trim());
        }
        w.orderByDesc(SysLoginLog::getCreateTime).last("LIMIT " + EXPORT_LIMIT);
        List<SysLoginLog> rows = loginLogMapper.selectList(w);
        writeCsv(response, "login-logs.csv",
                new String[]{"id", "userId", "username", "loginStatus", "clientIp", "createTime"},
                rows.stream()
                        .map(r -> new String[]{
                                str(r.getId()),
                                str(r.getUserId()),
                                csv(r.getUsername()),
                                str(r.getLoginStatus()),
                                csv(r.getLoginIp()),
                                csv(String.valueOf(r.getCreateTime()))
                        })
                        .toList());
    }

    @GetMapping("/audit-logs.csv")
    public void auditLogsCsv(@RequestParam(required = false) String action, HttpServletResponse response)
            throws IOException {
        LambdaQueryWrapper<SysAuditLog> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(action)) {
            w.eq(SysAuditLog::getAction, action.trim());
        }
        w.orderByDesc(SysAuditLog::getCreateTime).last("LIMIT " + EXPORT_LIMIT);
        List<SysAuditLog> rows = auditLogMapper.selectList(w);
        writeCsv(response, "audit-logs.csv",
                new String[]{"id", "adminId", "adminUsername", "action", "targetType", "targetId", "detail", "clientIp", "createTime"},
                rows.stream()
                        .map(r -> new String[]{
                                str(r.getId()),
                                str(r.getAdminId()),
                                csv(r.getAdminUsername()),
                                csv(r.getAction()),
                                csv(r.getTargetType()),
                                csv(r.getTargetId()),
                                csv(r.getDetail()),
                                csv(r.getClientIp()),
                                csv(String.valueOf(r.getCreateTime()))
                        })
                        .toList());
    }

    private static void writeCsv(HttpServletResponse response, String filename, String[] headers,
                                 List<String[]> dataRows) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        PrintWriter out = response.getWriter();
        out.write('\ufeff');
        out.println(String.join(",", headers));
        for (String[] row : dataRows) {
            out.println(String.join(",", row));
        }
        out.flush();
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String csv(String s) {
        if (s == null) {
            return "";
        }
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v + "\"";
        }
        return v;
    }
}