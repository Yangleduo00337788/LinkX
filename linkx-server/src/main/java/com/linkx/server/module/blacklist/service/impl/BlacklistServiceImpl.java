package com.linkx.server.module.blacklist.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysBlacklist;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysBlacklistMapper;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;
import com.linkx.server.module.blacklist.service.BlacklistService;
import com.linkx.server.module.chat.constant.ChatConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final SysBlacklistMapper blacklistMapper;
    private final SysUserMapper userMapper;
    private final SysFriendMapper friendMapper;
    private final ImSessionMapper sessionMapper;

    @Override
    @Transactional
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

        LambdaQueryWrapper<SysFriend> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.and(w -> w
                .eq(SysFriend::getUserId, userId).eq(SysFriend::getFriendId, targetUserId)
                .or()
                .eq(SysFriend::getUserId, targetUserId).eq(SysFriend::getFriendId, userId));
        friendMapper.delete(deleteWrapper);

        deleteSingleSessions(userId, targetUserId);
    }

    @Override
    public void removeBlacklist(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        blacklistMapper.delete(wrapper);
    }

    @Override
    public List<BlacklistUserDTO> getBlacklist(Long userId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .orderByDesc(SysBlacklist::getCreateTime);
        List<SysBlacklist> list = blacklistMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return List.of();
        }

        java.util.Set<Long> blacklistedUserIds = list.stream()
                .map(SysBlacklist::getBlacklistUserId)
                .collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(blacklistedUserIds).stream()
                .collect(java.util.stream.Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));

        return list.stream()
                .map(b -> userMap.get(b.getBlacklistUserId()))
                .filter(u -> u != null)
                .map(this::toBlacklistUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBlacklisted(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysBlacklist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysBlacklist::getUserId, userId)
                .eq(SysBlacklist::getBlacklistUserId, targetUserId);
        return blacklistMapper.selectCount(wrapper) > 0;
    }

    private BlacklistUserDTO toBlacklistUserDTO(SysUser user) {
        BlacklistUserDTO dto = new BlacklistUserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        return dto;
    }

    private void deleteSingleSessions(Long userId, Long targetUserId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_SINGLE)
                .and(w -> w.eq(ImSession::getUserId, userId).eq(ImSession::getTargetId, targetUserId)
                        .or()
                        .eq(ImSession::getUserId, targetUserId).eq(ImSession::getTargetId, userId));
        sessionMapper.delete(wrapper);
    }
}
