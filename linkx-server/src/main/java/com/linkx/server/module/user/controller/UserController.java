package com.linkx.server.module.user.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.abuse.service.AbuseProtectionGuard;
import com.linkx.server.module.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户资料 API：本人信息、好友可见资料、搜索用户。
 * <p>
 * {@code GET /{userId}} 非好友返回 403；搜索走 {@link AbuseProtectionGuard} 限流。
 * </p>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AbuseProtectionGuard abuseProtectionGuard;

    @GetMapping("/me")
    public Result<UserProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.getMyProfile(userId));
    }

    @GetMapping("/{userId}")
    public Result<UserProfileDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "userId") Long userId) {
        Long requesterId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.getProfile(requesterId, userId));
    }

    @PutMapping("/me")
    public Result<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.updateProfile(userId, request));
    }

    @GetMapping("/search")
    public Result<List<UserProfileDTO>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "keyword") String keyword,
            HttpServletRequest request) {
        Long requesterId = Long.parseLong(userDetails.getUsername());
        abuseProtectionGuard.checkUserSearch(requesterId, request);
        return Result.success(userService.searchUsers(requesterId, keyword));
    }
}
