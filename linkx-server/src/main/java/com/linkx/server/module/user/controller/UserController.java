package com.linkx.server.module.user.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<UserProfileDTO> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.getMyProfile(userId));
    }

    @GetMapping("/{userId}")
    public Result<UserProfileDTO> getProfile(@PathVariable Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/me")
    public Result<UserProfileDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(userService.updateProfile(userId, request));
    }

    @GetMapping("/search")
    public Result<List<UserProfileDTO>> searchUsers(@RequestParam String keyword) {
        return Result.success(userService.searchUsers(keyword));
    }
}
