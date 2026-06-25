package com.linkx.server.module.user.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.user.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.user.dto.UpdateProfileRequest;  // 行注：引入 UpdateProfileRequest 类型
import com.linkx.server.module.user.dto.UserProfileDTO;  // 行注：引入 UserProfileDTO 类型
import com.linkx.server.module.abuse.service.AbuseProtectionGuard;  // 行注：引入 AbuseProtectionGuard 类型
import com.linkx.server.module.user.service.UserService;  // 行注：引入 UserService 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import jakarta.validation.Valid;  // 行注：引入 Valid 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 用户资料 API：本人信息、好友可见资料、搜索用户。
 * <p>
 * {@code GET /{userId}} 非好友返回 403；搜索走 {@link AbuseProtectionGuard} 限流。
 * </p>
 */
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/user")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 UserController 类
public class UserController {

    private final UserService userService;  // 行注：注入用户服务依赖
    private final AbuseProtectionGuard abuseProtectionGuard;  // 行注：注入abuseProtectionGuard依赖

    /** 获取当前登录用户的完整资料 */
    @GetMapping("/me")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取我的Profile方法
    public Result<UserProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(userService.getMyProfile(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 查看他人资料；须为好友关系，否则业务层返回无权限 */
    @GetMapping("/{userId}")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取Profile方法
    public Result<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "userId") Long userId) {  // 行注：应用 @PathVariable 注解
        Long requesterId = Long.parseLong(userDetails.getUsername());  // 行注：初始化requesterID
        return Result.success(userService.getProfile(requesterId, userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 更新昵称、签名等可编辑字段 */
    @PutMapping("/me")  // 行注：应用 @PutMapping 注解
    // 行注：定义更新Profile方法
    public Result<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody UpdateProfileRequest request) {  // 行注：应用 @Valid @RequestBody UpdateProfileRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(userService.updateProfile(userId, request));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 按关键字搜索用户（用户名等），受按用户/IP 的滑动窗口限流保护 */
    @GetMapping("/search")  // 行注：应用 @GetMapping 注解
    // 行注：定义搜索Users方法
    public Result<List<UserProfileDTO>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam(value = "keyword") String keyword,  // 行注：应用 @RequestParam 注解
            // 行注：开始当前语句对应的代码块
            HttpServletRequest request) {
        Long requesterId = Long.parseLong(userDetails.getUsername());  // 行注：初始化requesterID
        abuseProtectionGuard.checkUserSearch(requesterId, request);  // 行注：调用检查用户搜索
        return Result.success(userService.searchUsers(requesterId, keyword));  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
