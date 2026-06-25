package com.linkx.server.security;  // 行注：声明当前文件所在包 com.linkx.server.security

import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.userdetails.User;  // 行注：引入 User 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.security.core.userdetails.UserDetailsService;  // 行注：引入 UserDetailsService 类型
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // 行注：引入 UsernameNotFoundException 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型

import java.util.Collections;  // 行注：引入 Collections 类型

/**
 * 为 JWT 过滤器加载用户：{@code loadUserByUsername} 的参数实为 userId 字符串。
 * <p>
 * {@link User#getUsername()} 返回 userId；密码哈希仅登录流程使用，API 鉴权不校验密码。
 * status=1 为启用，否则 {@code isEnabled()=false}。
 * </p>
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 UserDetailsServiceImpl 类
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖

    /**
     * 按用户标识加载 Spring Security 用户详情。
     *
     * @param userId 用户 ID
     * @return 用户详情
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义加载用户Username方法
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // 当前项目约定把 userId 当作“用户名”传入，因此这里直接按主键查询用户。
        SysUser user = userMapper.selectById(Long.parseLong(userId));  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + userId);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 这里返回的是 Spring Security 标准 User，实现里只关心账号状态与授权信息。
        return new User(  // 行注：返回处理结果
                // 行注：调用值Of
                String.valueOf(user.getId()),
                // 行注：调用获取密码
                user.getPassword(),
                // status=1 表示启用；禁用账号即使 token 合法也会在过滤器中被拦下。
                // 行注：调用获取状态
                user.getStatus() == 1,
                // 行注：补充当前表达式片段
                true, true, true,
                // 行注：调用空列表
                Collections.emptyList()
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
