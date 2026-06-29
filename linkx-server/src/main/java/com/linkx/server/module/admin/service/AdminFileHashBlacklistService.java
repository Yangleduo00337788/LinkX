package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysFileHashBlacklist;
import com.linkx.server.mapper.SysFileHashBlacklistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminFileHashBlacklistService {

    private final SysFileHashBlacklistMapper mapper;

    public Page<SysFileHashBlacklist> list(int page, int size, String keyword) {
        LambdaQueryWrapper<SysFileHashBlacklist> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(SysFileHashBlacklist::getContentHash, keyword.trim());
        }
        w.orderByDesc(SysFileHashBlacklist::getCreateTime);
        return mapper.selectPage(new Page<>(page, size), w);
    }

    public void create(String contentHash, String reason, Integer enabled) {
        if (!StringUtils.hasText(contentHash)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "哈希不能为空");
        }
        SysFileHashBlacklist row = new SysFileHashBlacklist();
        row.setContentHash(contentHash.trim().toLowerCase());
        row.setReason(reason);
        row.setEnabled(enabled != null ? enabled : 1);
        mapper.insert(row);
    }

    public void update(Long id, String reason, Integer enabled) {
        SysFileHashBlacklist row = mapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (reason != null) {
            row.setReason(reason);
        }
        if (enabled != null) {
            row.setEnabled(enabled);
        }
        mapper.updateById(row);
    }

    public void delete(Long id) {
        mapper.deleteById(id);
    }
}