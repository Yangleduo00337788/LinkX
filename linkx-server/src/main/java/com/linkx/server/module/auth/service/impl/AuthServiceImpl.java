package com.linkx.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.module.auth.service.RefreshTokenSessionService;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        if (request.getPhone() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, request.getPhone());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }

        if (request.getEmail() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, request.getEmail());
            if (userMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setDeleted(0);
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw resolveRegisterConflict(request);
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenSessionService.issueToken(user.getId(), refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenSessionService.issueToken(user.getId(), refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "刷新令牌无效");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (!refreshTokenSessionService.matchesActiveToken(userId, refreshToken)) {
            throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED, "刷新令牌已失效，请重新登录");
        }
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            refreshTokenSessionService.revokeToken(userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            refreshTokenSessionService.revokeToken(userId);
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenSessionService.issueToken(user.getId(), newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    private BusinessException resolveRegisterConflict(RegisterRequest request) {
        if (existsByUsername(request.getUsername())) {
            return new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (request.getPhone() != null && existsByPhone(request.getPhone())) {
            return new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        if (request.getEmail() != null && existsByEmail(request.getEmail())) {
            return new BusinessException(ErrorCode.EMAIL_EXISTS);
        }
        return new BusinessException(ErrorCode.BAD_REQUEST, "注册信息已存在，请检查后重试");
    }

    private boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }

    private boolean existsByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, phone);
        return userMapper.selectCount(wrapper) > 0;
    }

    private boolean existsByEmail(String email) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getEmail, email);
        return userMapper.selectCount(wrapper) > 0;
    }
}
