package com.linkx.server.module.blacklist.controller;

import com.linkx.server.common.Result;
import com.linkx.server.entity.SysUser;
import com.linkx.server.module.blacklist.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/{targetUserId}")
    public Result<Void> addBlacklist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("targetUserId") Long targetUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        blacklistService.addBlacklist(userId, targetUserId);
        return Result.success();
    }

    @DeleteMapping("/{targetUserId}")
    public Result<Void> removeBlacklist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("targetUserId") Long targetUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        blacklistService.removeBlacklist(userId, targetUserId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<SysUser>> getBlacklist(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(blacklistService.getBlacklist(userId));
    }

    @GetMapping("/check/{targetUserId}")
    public Result<Boolean> isBlacklisted(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("targetUserId") Long targetUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(blacklistService.isBlacklisted(userId, targetUserId));
    }
}
