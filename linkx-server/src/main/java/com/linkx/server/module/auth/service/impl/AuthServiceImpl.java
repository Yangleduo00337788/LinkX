package com.linkx.server.module.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.auth.dto.AuthResponse;
import com.linkx.server.module.auth.dto.LoginRequest;
import com.linkx.server.module.auth.dto.RegisterRequest;
import com.linkx.server.module.auth.service.AccessTokenDenylistService;
import com.linkx.server.module.auth.service.AuthService;
import com.linkx.server.module.auth.service.PasswordPolicy;
import com.linkx.server.module.auth.service.RefreshTokenSessionService;
import com.linkx.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 认证服务实现：BCrypt 存密、JWT 双令牌、Redis 管理 refresh 与 access 黑名单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final AccessTokenDenylistService accessTokenDenylistService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");
        String nickname = TextNormalizer.normalizeRequiredSingleLine(request.getNickname(), 50, "昵称");
        String phone = TextNormalizer.normalizeOptionalSingleLine(request.getPhone(), 32, "手机号");
        String email = TextNormalizer.normalizeOptionalSingleLine(request.getEmail(), 128, "邮箱");
        PasswordPolicy.validate(request.getPassword());

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        if (userMapper.selectCount(wrapper) > 0) {
            log.warn("Auth register rejected, reason=username_exists, username={}", username);
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }

        if (phone != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, phone);
            if (userMapper.selectCount(wrapper) > 0) {
                log.warn("Auth register rejected, reason=phone_exists, username={}, phone={}", username, phone);
                throw new BusinessException(ErrorCode.PHONE_EXISTS);
            }
        }

        if (email != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getEmail, email);
            if (userMapper.selectCount(wrapper) > 0) {
                log.warn("Auth register rejected, reason=email_exists, username={}, email={}", username, email);
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);
            }
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(phone);
        user.setEmail(email);
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
        log.info("Auth register issued tokens, userId={}, username={}", user.getId(), user.getUsername());

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
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = userMapper.selectOne(wrapper);

        if (user == null) {
            log.warn("Auth login rejected, reason=user_not_found, username={}", username);
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Auth login rejected, reason=password_error, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);
        }

        if (user.getStatus() == 0) {
            log.warn("Auth login rejected, reason=user_disabled, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenSessionService.issueToken(user.getId(), refreshToken);
        log.info("Auth login issued tokens, userId={}, username={}", user.getId(), user.getUsername());

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
            log.warn("Auth refresh rejected, reason=token_invalid");
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("Auth refresh rejected, reason=not_refresh_token");
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "刷新令牌无效");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (!refreshTokenSessionService.matchesActiveToken(userId, refreshToken)) {
            log.warn("Auth refresh rejected, reason=token_revoked, userId={}", userId);
            throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED, "刷新令牌已失效，请重新登录");
        }
        SysUser user = userMapper.selectById(userId);

        if (user == null) {
            refreshTokenSessionService.revokeToken(userId);
            log.warn("Auth refresh rejected, reason=user_not_found, userId={}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            refreshTokenSessionService.revokeToken(userId);
            log.warn("Auth refresh rejected, reason=user_disabled, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        refreshTokenSessionService.issueToken(user.getId(), newRefreshToken);
        log.info("Auth refresh issued tokens, userId={}, username={}", user.getId(), user.getUsername());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public void logout(Long userId, String refreshToken, String accessToken) {
        if (userId != null) {
            refreshTokenSessionService.revokeToken(userId);
        }
        if (StringUtils.hasText(refreshToken) && jwtTokenProvider.validateToken(refreshToken)
                && jwtTokenProvider.isRefreshToken(refreshToken)) {
            Long tokenUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            if (userId == null || userId.equals(tokenUserId)) {
                refreshTokenSessionService.revokeToken(tokenUserId);
            }
        }
        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)
                && jwtTokenProvider.isAccessToken(accessToken)) {
            accessTokenDenylistService.denyToken(accessToken);
        }
        log.info("Auth logout completed, userId={}", userId);
    }

    private BusinessException resolveRegisterConflict(RegisterRequest request) {
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");
        String phone = TextNormalizer.normalizeOptionalSingleLine(request.getPhone(), 32, "手机号");
        String email = TextNormalizer.normalizeOptionalSingleLine(request.getEmail(), 128, "邮箱");
        if (existsByUsername(username)) {
            return new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (phone != null && existsByPhone(phone)) {
            return new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        if (email != null && existsByEmail(email)) {
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
