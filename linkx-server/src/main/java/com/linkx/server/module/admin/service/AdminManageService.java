package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.entity.SysAdmin;
import com.linkx.server.mapper.SysAdminMapper;
import com.linkx.server.module.admin.dto.AdminManageListItemDTO;
import com.linkx.server.module.admin.dto.CreateAdminRequest;
import com.linkx.server.security.AdminRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminManageService {

    private static final Set<String> ROLES = Set.of(
            AdminRole.SUPER_ADMIN, AdminRole.OPERATOR, AdminRole.AUDITOR, AdminRole.VIEWER);

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService auditService;

    public Page<AdminManageListItemDTO> list(int page, int size, String keyword) {
        LambdaQueryWrapper<SysAdmin> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            w.and(q -> q.like(SysAdmin::getUsername, k).or().like(SysAdmin::getDisplayName, k));
        }
        w.orderByDesc(SysAdmin::getCreateTime);
        Page<SysAdmin> raw = adminMapper.selectPage(new Page<>(page, size), w);
        Page<AdminManageListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(a -> AdminManageListItemDTO.builder()
                .id(a.getId())
                .username(a.getUsername())
                .displayName(a.getDisplayName())
                .role(a.getRole())
                .status(a.getStatus())
                .lastLoginTime(a.getLastLoginTime())
                .createTime(a.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void create(CreateAdminRequest req, Long operatorId, String operatorName, String ip) {
        String role = req.getRole().trim().toUpperCase();
        if (!ROLES.contains(role)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效角色");
        }
        String username = TextNormalizer.normalizeRequiredSingleLine(req.getUsername(), 64, "用户名");
        long exists = adminMapper.selectCount(new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, username));
        if (exists > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        SysAdmin admin = new SysAdmin();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setDisplayName(TextNormalizer.normalizeRequiredSingleLine(req.getDisplayName(), 64, "显示名"));
        admin.setRole(role);
        admin.setStatus(1);
        adminMapper.insert(admin);
        auditService.record(operatorId, operatorName, "ADMIN_CREATE", "admin", String.valueOf(admin.getId()),
                "role=" + role, ip);
    }

    @Transactional
    public void setStatus(Long adminId, int status, Long operatorId, String operatorName, String ip) {
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        admin.setStatus(status);
        adminMapper.updateById(admin);
        auditService.record(operatorId, operatorName, status == 1 ? "ADMIN_ENABLE" : "ADMIN_DISABLE",
                "admin", String.valueOf(adminId), null, ip);
    }

    @Transactional
    public void resetPassword(Long adminId, String newPassword, Long operatorId, String operatorName, String ip) {
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        adminMapper.updateById(admin);
        auditService.record(operatorId, operatorName, "ADMIN_RESET_PASSWORD", "admin", String.valueOf(adminId), null, ip);
    }
}