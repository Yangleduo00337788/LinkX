package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.entity.SysAdmin;
import com.linkx.server.mapper.SysAdminMapper;
import com.linkx.server.module.admin.dto.AdminAuthResponse;
import com.linkx.server.module.admin.dto.AdminLoginRequest;
import com.linkx.server.module.admin.dto.AdminProfileDTO;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final SysAdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminAuditService adminAuditService;

    @Transactional
    public AdminAuthResponse login(AdminLoginRequest request, String clientIp) {
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 64, "用户名");
        SysAdmin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<SysAdmin>().eq(SysAdmin::getUsername, username));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp(clientIp);
        adminMapper.updateById(admin);

        String token = jwtTokenProvider.generateAdminToken(admin.getId(), admin.getUsername(), admin.getRole());
        adminAuditService.record(admin.getId(), admin.getUsername(), "ADMIN_LOGIN", "admin", String.valueOf(admin.getId()),
                null, clientIp);

        return AdminAuthResponse.builder()
                .accessToken(token)
                .adminId(admin.getId())
                .username(admin.getUsername())
                .displayName(admin.getDisplayName())
                .role(admin.getRole())
                .build();
    }

    public AdminProfileDTO getProfile(Long adminId) {
        SysAdmin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return AdminProfileDTO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .displayName(admin.getDisplayName())
                .role(admin.getRole())
                .build();
    }
}