package com.linkx.server.module.blacklist.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.blacklist.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;  // 行注：引入 BlacklistUserDTO 类型
import com.linkx.server.module.blacklist.service.BlacklistService;  // 行注：引入 BlacklistService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 黑名单：拉黑/取消、列表；拉黑后限制私聊与好友操作。
 */
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/blacklist")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 BlacklistController 类
public class BlacklistController {

    private final BlacklistService blacklistService;  // 行注：注入黑名单服务依赖

    /** 将目标用户加入黑名单 */
    @PostMapping("/{targetUserId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义添加黑名单方法
    public Result<Void> addBlacklist(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable("targetUserId") Long targetUserId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        blacklistService.addBlacklist(userId, targetUserId);  // 行注：调用添加黑名单
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 取消对目标用户的拉黑 */
    @DeleteMapping("/{targetUserId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义移除黑名单方法
    public Result<Void> removeBlacklist(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable("targetUserId") Long targetUserId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        blacklistService.removeBlacklist(userId, targetUserId);  // 行注：调用移除黑名单
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前用户的黑名单列表 */
    @GetMapping("/list")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取黑名单方法
    public Result<List<BlacklistUserDTO>> getBlacklist(
            @AuthenticationPrincipal UserDetails userDetails) {  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(blacklistService.getBlacklist(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 查询当前用户是否已拉黑目标用户 */
    @GetMapping("/check/{targetUserId}")  // 行注：应用 @GetMapping 注解
    // 行注：定义是否Blacklisted方法
    public Result<Boolean> isBlacklisted(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable("targetUserId") Long targetUserId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(blacklistService.isBlacklisted(userId, targetUserId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
