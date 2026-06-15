package com.linkx.server.module.blacklist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysBlacklist;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysBlacklistMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final SysBlacklistMapper blacklistMapper;
    private final SysUserMapper userMapper;

    @Override
    public void addBlacklist(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        SysUser targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        if (blacklistMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已在黑名单中");
        }

        SysBlacklist blacklist = new SysBlacklist();
        blacklist.setUserId(userId);
        blacklist.setBlacklistUserId(targetUserId);
        blacklistMapper.insert(blacklist);
    }

    @Override
    public void removeBlacklist(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        blacklistMapper.delete(wrapper);
    }

    @Override
    public List<SysUser> getBlacklist(Long userId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .orderByDesc(SysBlacklist::getCreateTime);
        List<SysBlacklist> list = blacklistMapper.selectList(wrapper);

        return list.stream()
                .map(b -> userMapper.selectById(b.getBlacklistUserId()))
                .filter(u -> u != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBlacklisted(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        return blacklistMapper.selectCount(wrapper) > 0;
    }
}
