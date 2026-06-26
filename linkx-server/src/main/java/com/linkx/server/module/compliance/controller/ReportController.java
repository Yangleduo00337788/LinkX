package com.linkx.server.module.compliance.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.compliance.dto.CreateReportRequest;
import com.linkx.server.module.compliance.service.ReportService;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.entity.SysUser;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SysUserMapper userMapper;

    @PostMapping
    public Result<Long> submit(@Valid @RequestBody CreateReportRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return Result.success(reportService.submitReport(userId, request));
    }

    private Long resolveUserId(UserDetails userDetails) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.eq(SysUser::getUsername, userDetails.getUsername());
        SysUser user = userMapper.selectOne(w);
        if (user == null) {
            throw new com.linkx.server.common.BusinessException(com.linkx.server.common.ErrorCode.USER_NOT_FOUND);
        }
        return user.getId();
    }
}