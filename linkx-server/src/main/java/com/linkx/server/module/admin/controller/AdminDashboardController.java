package com.linkx.server.module.admin.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.admin.dto.AdminDashboardStatsDTO;
import com.linkx.server.module.admin.service.AdminDashboardService;
import com.linkx.server.module.chat.ws.ChatSocketSessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final ChatSocketSessionRegistry sessionRegistry;

    @GetMapping("/online-count")
    public Result<java.util.Map<String, Integer>> onlineCount() {
        return Result.success(java.util.Map.of("onlineUsers", sessionRegistry.getOnlineUserCount()));
    }

    @GetMapping("/stats")
    public Result<AdminDashboardStatsDTO> stats(
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(adminDashboardService.stats(days));
    }
}