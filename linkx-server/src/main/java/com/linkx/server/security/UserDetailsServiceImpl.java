package com.linkx.server.security;

import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 为 JWT 过滤器加载用户：{@code loadUserByUsername} 的参数实为 userId 字符串。
 * <p>
 * {@link User#getUsername()} 返回 userId；密码哈希仅登录流程使用，API 鉴权不校验密码。
 * status=1 为启用，否则 {@code isEnabled()=false}。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        SysUser user = userMapper.selectById(Long.parseLong(userId));
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }

        return new User(
                String.valueOf(user.getId()),
                user.getPassword(),
                user.getStatus() == 1,
                true, true, true,
                Collections.emptyList()
        );
    }
}