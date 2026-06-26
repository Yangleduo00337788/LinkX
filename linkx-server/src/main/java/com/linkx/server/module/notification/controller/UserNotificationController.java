package com.linkx.server.module.notification.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.Result;
import com.linkx.server.entity.SysUser;
import com.linkx.server.entity.SysUserNotification;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.notification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService notificationService;
    private final SysUserMapper userMapper;

    @GetMapping
    public Result<Page<SysUserNotification>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(notificationService.pageForUser(resolveUserId(userDetails), page, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markRead(resolveUserId(userDetails), id);
        return Result.success();
    }

    private Long resolveUserId(UserDetails userDetails) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, userDetails.getUsername()));
        if (user == null) {
            throw new com.linkx.server.common.BusinessException(com.linkx.server.common.ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}