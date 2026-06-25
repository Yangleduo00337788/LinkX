package com.linkx.server.module.auth.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.auth.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.TextNormalizer;  // 行注：引入 TextNormalizer 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.auth.dto.AuthResponse;  // 行注：引入 AuthResponse 类型
import com.linkx.server.module.auth.dto.LoginRequest;  // 行注：引入 LoginRequest 类型
import com.linkx.server.module.auth.dto.RegisterRequest;  // 行注：引入 RegisterRequest 类型
import com.linkx.server.module.auth.service.AccessTokenDenylistService;  // 行注：引入 AccessTokenDenylistService 类型
import com.linkx.server.module.auth.service.AuthService;  // 行注：引入 AuthService 类型
import com.linkx.server.module.auth.service.PasswordPolicy;  // 行注：引入 PasswordPolicy 类型
import com.linkx.server.module.auth.service.RefreshTokenSessionService;  // 行注：引入 RefreshTokenSessionService 类型
import com.linkx.server.security.JwtTokenProvider;  // 行注：引入 JwtTokenProvider 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.dao.DuplicateKeyException;  // 行注：引入 DuplicateKeyException 类型
import org.springframework.security.crypto.password.PasswordEncoder;  // 行注：引入 PasswordEncoder 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.transaction.annotation.Transactional;  // 行注：引入 Transactional 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

/**
 * 认证服务实现：BCrypt 存密、JWT 双令牌、Redis 管理 refresh 与 access 黑名单。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 AuthServiceImpl 类
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final PasswordEncoder passwordEncoder;  // 行注：注入密码编码器依赖
    private final JwtTokenProvider jwtTokenProvider;  // 行注：注入JWT令牌提供器依赖
    private final RefreshTokenSessionService refreshTokenSessionService;  // 行注：注入刷新令牌会话服务依赖
    private final AccessTokenDenylistService accessTokenDenylistService;  // 行注：注入访问令牌拒绝列表服务依赖

    /**
     * 注册相关逻辑。
     *
     * @param request 当前请求或请求对象
     * @return 认证响应
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义注册方法
    public AuthResponse register(RegisterRequest request) {
        // 先做统一文本清洗，避免把首尾空白、超长输入或非法单行文本写入数据库。
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");  // 行注：初始化username
        String nickname = TextNormalizer.normalizeRequiredSingleLine(request.getNickname(), 50, "昵称");  // 行注：初始化nickname
        String phone = TextNormalizer.normalizeOptionalSingleLine(request.getPhone(), 32, "手机号");  // 行注：初始化phone
        String email = TextNormalizer.normalizeOptionalSingleLine(request.getEmail(), 128, "邮箱");  // 行注：初始化email
        // 密码策略单独校验，保证数据库里只落满足复杂度要求的密文。
        PasswordPolicy.validate(request.getPassword());  // 行注：调用validate

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysUser::getUsername, username);  // 行注：调用等值条件
        // 行注：判断是否满足当前条件
        if (userMapper.selectCount(wrapper) > 0) {
            log.warn("Auth register rejected, reason=username_exists, username={}", username);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (phone != null) {
            wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
            wrapper.eq(SysUser::getPhone, phone);  // 行注：调用等值条件
            // 行注：判断是否满足当前条件
            if (userMapper.selectCount(wrapper) > 0) {
                log.warn("Auth register rejected, reason=phone_exists, username={}, phone={}", username, phone);  // 行注：执行初始化操作
                throw new BusinessException(ErrorCode.PHONE_EXISTS);  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (email != null) {
            wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
            wrapper.eq(SysUser::getEmail, email);  // 行注：调用等值条件
            // 行注：判断是否满足当前条件
            if (userMapper.selectCount(wrapper) > 0) {
                log.warn("Auth register rejected, reason=email_exists, username={}, email={}", username, email);  // 行注：执行初始化操作
                throw new BusinessException(ErrorCode.EMAIL_EXISTS);  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        SysUser user = new SysUser();  // 行注：初始化用户
        user.setUsername(username);  // 行注：调用设置Username
        user.setNickname(nickname);  // 行注：调用设置Nickname
        user.setPassword(passwordEncoder.encode(request.getPassword()));  // 行注：调用设置密码
        user.setPhone(phone);  // 行注：调用设置Phone
        user.setEmail(email);  // 行注：调用设置Email
        user.setStatus(1);  // 行注：调用设置状态
        user.setDeleted(0);  // 行注：调用设置Deleted
        // 行注：尝试执行可能失败的逻辑
        try {
            // 先做应用层预检查，再依赖数据库唯一索引兜底，避免并发注册穿透。
            userMapper.insert(user);  // 行注：调用insert
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException exception) {
            // 并发下可能两个请求都通过了前置查询，这里再次解析冲突字段并返回友好错误。
            throw resolveRegisterConflict(request);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 注册完成后立即签发 access + refresh，前端可直接进入登录态，无需再次手动登录。
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());  // 行注：初始化访问令牌
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());  // 行注：初始化刷新令牌
        // refresh token 只保留当前激活会话，后续刷新与登出都以 Redis 中的记录为准。
        refreshTokenSessionService.issueToken(user.getId(), refreshToken);  // 行注：调用申请令牌
        log.info("Auth register issued tokens, userId={}, username={}", user.getId(), user.getUsername());  // 行注：执行初始化操作

        return AuthResponse.builder()  // 行注：返回处理结果
                // 行注：继续调用访问令牌
                .accessToken(accessToken)
                // 行注：继续调用刷新令牌
                .refreshToken(refreshToken)
                // 行注：继续调用用户ID
                .userId(user.getId())
                // 行注：继续调用username
                .username(user.getUsername())
                // 行注：继续调用nickname
                .nickname(user.getNickname())
                // 行注：继续调用头像
                .avatar(user.getAvatar())
                .build();  // 行注：继续调用构建
    }  // 行注：结束当前代码块

    /**
     * 登录相关逻辑。
     *
     * @param request 当前请求或请求对象
     * @return 认证响应
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义登录方法
    public AuthResponse login(LoginRequest request) {
        // 登录也复用统一的文本清洗逻辑，避免同一个账号因为输入空白差异查不到用户。
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");  // 行注：初始化username
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysUser::getUsername, username);  // 行注：调用等值条件
        SysUser user = userMapper.selectOne(wrapper);  // 行注：初始化用户

        // 行注：判断是否满足当前条件
        if (user == null) {
            log.warn("Auth login rejected, reason=user_not_found, username={}", username);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // 行注：执行初始化操作
            log.warn("Auth login rejected, reason=password_error, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.AUTH_LOGIN_FAILED);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (user.getStatus() == 0) {
            // 行注：执行初始化操作
            log.warn("Auth login rejected, reason=user_disabled, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 每次登录都重新签发一对新令牌，并覆盖 Redis 中旧的 refresh 会话。
        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());  // 行注：初始化访问令牌
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());  // 行注：初始化刷新令牌
        refreshTokenSessionService.issueToken(user.getId(), refreshToken);  // 行注：调用申请令牌
        log.info("Auth login issued tokens, userId={}, username={}", user.getId(), user.getUsername());  // 行注：执行初始化操作

        return AuthResponse.builder()  // 行注：返回处理结果
                // 行注：继续调用访问令牌
                .accessToken(accessToken)
                // 行注：继续调用刷新令牌
                .refreshToken(refreshToken)
                // 行注：继续调用用户ID
                .userId(user.getId())
                // 行注：继续调用username
                .username(user.getUsername())
                // 行注：继续调用nickname
                .nickname(user.getNickname())
                // 行注：继续调用头像
                .avatar(user.getAvatar())
                .build();  // 行注：继续调用构建
    }  // 行注：结束当前代码块

    /**
     * 刷新令牌。
     *
     * @param refreshToken refresh token
     * @return 令牌相关结果
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义刷新令牌方法
    public AuthResponse refreshToken(String refreshToken) {
        // 第一步先校验签名与过期时间，失败直接拒绝，避免继续解析不可信 claims。
        // 行注：判断是否满足当前条件
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("Auth refresh rejected, reason=token_invalid");  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.TOKEN_INVALID);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 第二步强校验 token 类型，防止把 access token 当 refresh token 使用。
        // 行注：判断是否满足当前条件
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            log.warn("Auth refresh rejected, reason=not_refresh_token");  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "刷新令牌无效");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);  // 行注：初始化用户ID
        // 第三步到 Redis 检查该 refresh 是否仍是当前活跃会话，登出或异地登录后旧 token 应失效。
        // 行注：判断是否满足当前条件
        if (!refreshTokenSessionService.matchesActiveToken(userId, refreshToken)) {
            log.warn("Auth refresh rejected, reason=token_revoked, userId={}", userId);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.TOKEN_BLACKLISTED, "刷新令牌已失效，请重新登录");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        SysUser user = userMapper.selectById(userId);  // 行注：初始化用户

        // 行注：判断是否满足当前条件
        if (user == null) {
            // 账号已不存在时顺手吊销 Redis 中的 refresh 会话，避免脏数据残留。
            refreshTokenSessionService.revokeToken(userId);  // 行注：调用revoke令牌
            log.warn("Auth refresh rejected, reason=user_not_found, userId={}", userId);  // 行注：执行初始化操作
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (user.getStatus() == 0) {
            // 被禁用用户不允许继续续期，同时清掉现有 refresh 会话。
            refreshTokenSessionService.revokeToken(userId);  // 行注：调用revoke令牌
            // 行注：执行初始化操作
            log.warn("Auth refresh rejected, reason=user_disabled, userId={}, username={}", user.getId(), user.getUsername());
            throw new BusinessException(ErrorCode.USER_DISABLED);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 采用 refresh 轮换策略：签发新 access 的同时替换成新的 refresh，缩短旧 refresh 暴露窗口。
        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getUsername());  // 行注：初始化新建访问令牌
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());  // 行注：初始化新建刷新令牌
        refreshTokenSessionService.issueToken(user.getId(), newRefreshToken);  // 行注：调用申请令牌
        log.info("Auth refresh issued tokens, userId={}, username={}", user.getId(), user.getUsername());  // 行注：执行初始化操作

        return AuthResponse.builder()  // 行注：返回处理结果
                // 行注：继续调用访问令牌
                .accessToken(newAccessToken)
                // 行注：继续调用刷新令牌
                .refreshToken(newRefreshToken)
                // 行注：继续调用用户ID
                .userId(user.getId())
                // 行注：继续调用username
                .username(user.getUsername())
                // 行注：继续调用nickname
                .nickname(user.getNickname())
                // 行注：继续调用头像
                .avatar(user.getAvatar())
                .build();  // 行注：继续调用构建
    }  // 行注：结束当前代码块

    /**
     * 登出相关逻辑。
     *
     * @param userId 用户 ID
     * @param refreshToken refresh token
     * @param accessToken access token
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义登出方法
    public void logout(Long userId, String refreshToken, String accessToken) {
        // 行注：判断是否满足当前条件
        if (userId != null) {
            // 有用户上下文时优先按当前登录人撤销 refresh，会覆盖最常见的正常登出路径。
            refreshTokenSessionService.revokeToken(userId);  // 行注：调用revoke令牌
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(refreshToken) && jwtTokenProvider.validateToken(refreshToken)
                // 行注：调用是否刷新令牌
                && jwtTokenProvider.isRefreshToken(refreshToken)) {
            Long tokenUserId = jwtTokenProvider.getUserIdFromToken(refreshToken);  // 行注：初始化令牌用户ID
            // body 中显式传入 refreshToken 时，再按 token 归属做一次兜底撤销，兼容上下文缺失场景。
            // 行注：判断是否满足当前条件
            if (userId == null || userId.equals(tokenUserId)) {
                refreshTokenSessionService.revokeToken(tokenUserId);  // 行注：调用revoke令牌
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)
                // 行注：调用是否访问令牌
                && jwtTokenProvider.isAccessToken(accessToken)) {
            // access token 无法像 refresh 一样主动删除，只能加入 denylist 等待自然过期。
            accessTokenDenylistService.denyToken(accessToken);  // 行注：调用deny令牌
        }  // 行注：结束当前代码块
        log.info("Auth logout completed, userId={}", userId);  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    // 行注：定义解析注册Conflict方法
    private BusinessException resolveRegisterConflict(RegisterRequest request) {
        // 这里重新查询真实冲突字段，尽量把数据库唯一索引异常转成明确的业务错误码。
        String username = TextNormalizer.normalizeRequiredSingleLine(request.getUsername(), 50, "用户名");  // 行注：初始化username
        String phone = TextNormalizer.normalizeOptionalSingleLine(request.getPhone(), 32, "手机号");  // 行注：初始化phone
        String email = TextNormalizer.normalizeOptionalSingleLine(request.getEmail(), 128, "邮箱");  // 行注：初始化email
        // 行注：判断是否满足当前条件
        if (existsByUsername(username)) {
            return new BusinessException(ErrorCode.USERNAME_EXISTS);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (phone != null && existsByPhone(phone)) {
            return new BusinessException(ErrorCode.PHONE_EXISTS);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (email != null && existsByEmail(email)) {
            return new BusinessException(ErrorCode.EMAIL_EXISTS);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return new BusinessException(ErrorCode.BAD_REQUEST, "注册信息已存在，请检查后重试");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义判断是否存在Username方法
    private boolean existsByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysUser::getUsername, username);  // 行注：调用等值条件
        return userMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义判断是否存在Phone方法
    private boolean existsByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysUser::getPhone, phone);  // 行注：调用等值条件
        return userMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义判断是否存在Email方法
    private boolean existsByEmail(String email) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysUser::getEmail, email);  // 行注：调用等值条件
        return userMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

}  // 行注：结束当前代码块
