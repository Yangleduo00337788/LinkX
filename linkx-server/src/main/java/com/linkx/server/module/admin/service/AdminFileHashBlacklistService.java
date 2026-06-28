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

    private final SysFileHashBlacklistMapper blacklistMapper;

    public Page<SysFileHashBlacklist> list(int page, int size, String keyword) {
        LambdaQueryWrapper<SysFileHashBlacklist> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.like(SysFileHashBlacklist::getContentHash, keyword.trim());
        }
        w.orderByDesc(SysFileHashBlacklist::getCreateTime);
        return blacklistMapper.selectPage(new Page<>(page, size), w);
    }

    public void add(String contentHash, String reason) {
        if (!StringUtils.hasText(contentHash)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "哈希不能为空");
        }
        String hash = contentHash.trim().toLowerCase();
        SysFileHashBlacklist row = new SysFileHashBlacklist();
        row.setContentHash(hash);
        row.setReason(reason);
        row.setEnabled(1);
        blacklistMapper.insert(row);
    }

    public void setEnabled(Long id, int enabled) {
        SysFileHashBlacklist row = blacklistMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        row.setEnabled(enabled);
        blacklistMapper.updateById(row);
    }

    public void remove(Long id) {
        blacklistMapper.deleteById(id);
    }
}