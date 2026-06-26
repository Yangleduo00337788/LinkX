package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysBlacklist;
import com.linkx.server.mapper.SysBlacklistMapper;
import com.linkx.server.module.admin.dto.AdminBlacklistListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminBlacklistService {

    private final SysBlacklistMapper blacklistMapper;

    public Page<AdminBlacklistListItemDTO> list(int page, int size, Long userId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SysBlacklist::getUserId, userId);
        }
        wrapper.orderByDesc(SysBlacklist::getCreateTime);
        Page<SysBlacklist> raw = blacklistMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminBlacklistListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(b -> AdminBlacklistListItemDTO.builder()
                .id(b.getId())
                .userId(b.getUserId())
                .blacklistUserId(b.getBlacklistUserId())
                .createTime(b.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void remove(Long id, Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        SysBlacklist row = blacklistMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        blacklistMapper.deleteById(id);
        audit.record(adminId, adminUsername, "BLACKLIST_REMOVE", "blacklist", String.valueOf(id),
                row.getUserId() + "->" + row.getBlacklistUserId(), clientIp);
    }
}