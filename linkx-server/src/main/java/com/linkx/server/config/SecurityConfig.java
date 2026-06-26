package com.linkx.server.config;  // 行注：声明当前文件所在包 com.linkx.server.config

import com.linkx.server.security.AdminJwtAuthenticationFilter;
import com.linkx.server.security.JwtAuthenticationEntryPoint;  // 行注：引入 JwtAuthenticationEntryPoint 类型
import com.linkx.server.security.JwtAuthenticationFilter;  // 行注：引入 JwtAuthenticationFilter 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.context.annotation.Bean;  // 行注：引入 Bean 类型
import org.springframework.context.annotation.Configuration;  // 行注：引入 Configuration 类型
import org.springframework.security.authentication.AuthenticationManager;  // 行注：引入 AuthenticationManager 类型
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;  // 行注：引入 AuthenticationConfiguration 类型
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // 行注：引入 HttpSecurity 类型
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 行注：引入 EnableWebSecurity 类型
import org.springframework.security.config.http.SessionCreationPolicy;  // 行注：引入 SessionCreationPolicy 类型
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // 行注：引入 BCryptPasswordEncoder 类型
import org.springframework.security.crypto.password.PasswordEncoder;  // 行注：引入 PasswordEncoder 类型
import org.springframework.security.web.SecurityFilterChain;  // 行注：引入 SecurityFilterChain 类型
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  // 行注：引入 UsernamePasswordAuthenticationFilter 类型
import org.springframework.web.cors.CorsConfiguration;  // 行注：引入 CorsConfiguration 类型
import org.springframework.web.cors.CorsConfigurationSource;  // 行注：引入 CorsConfigurationSource 类型
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;  // 行注：引入 UrlBasedCorsConfigurationSource 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * Spring Security 主配置：无状态 JWT、CORS、公开路径白名单。
 * <p>
 * {@code /api/auth/**} 与 WebSocket 握手路径 {@code /ws/**} 免登录；其余 API 需 access token。
 * 静态 {@code /uploads} 不对外开放，文件走 ticket 接口。
 * </p>
 */
@Configuration  // 行注：应用 @Configuration 注解
@EnableWebSecurity  // 行注：应用 @EnableWebSecurity 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 SecurityConfig 类
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;  // 行注：注入JWT 认证过滤依赖
    private final AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;  // 行注：注入JWT 认证入口点依赖
    private final LinkxSecurityProperties linkxSecurityProperties;  // 行注：注入LinkX 安全属性依赖

    /**
     * 配置 HTTP 安全链：无 Session、JWT 过滤器、公开路径白名单。
     *
     * @param http Spring Security 构建器
     * @return 生效的过滤器链
     */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义安全过滤链方法
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 行注：继续基于 HTTP 配置处理流程
        http
            // 跨域由下方 corsConfigurationSource 统一配置
            // 行注：继续调用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // REST + JWT 场景关闭 CSRF
            // 行注：继续调用CSRF
            .csrf(csrf -> csrf.disable())
            // 不创建 HttpSession，完全无状态
            // 行注：继续调用会话管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 未带有效 token 时返回 401 JSON，而非跳转登录页
            // 行注：继续调用异常处理
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            // 行注：继续调用授权 HTTP 请求
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/auth/login").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/api/admin/admins/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "OPERATOR", "AUDITOR", "VIEWER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(adminJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 全站 CORS：允许的方法、头与 Origin 模式来自 {@link LinkxSecurityProperties} */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义CORS 配置来源方法
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();  // 行注：初始化配置
        config.setAllowedOriginPatterns(linkxSecurityProperties.getAllowedOriginPatterns());  // 行注：调用设置允许的来源模式
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));  // 行注：调用设置允许的方法
        config.setAllowedHeaders(List.of("*"));  // 行注：调用设置允许的请求头
        config.setAllowCredentials(false);  // 行注：调用设置是否允许携带凭证

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();  // 行注：初始化来源
        source.registerCorsConfiguration("/**", config);  // 行注：调用注册CORS配置
        return source;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 用户密码存储与校验均使用 BCrypt */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义密码编码器方法
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 供 {@link org.springframework.security.authentication.AuthenticationManager} 注入登录流程使用。
     *
     * @param config Spring Security 认证配置
     */
    @Bean  // 行注：应用 @Bean 注解
    // 行注：定义认证管理器方法
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
