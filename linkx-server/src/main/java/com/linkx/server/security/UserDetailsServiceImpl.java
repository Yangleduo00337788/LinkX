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
