package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.admin.dto.AdminFriendListItemDTO;
import com.linkx.server.module.admin.dto.AdminUserDetailDTO;
import com.linkx.server.module.admin.dto.AdminUserListItemDTO;
import com.linkx.server.module.auth.service.RefreshTokenSessionService;
import com.linkx.server.module.auth.service.UserSessionInvalidationService;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.mapper.SysFriendMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final SysUserMapper userMapper;
    private final SysFriendMapper friendMapper;
    private final RefreshTokenSessionService refreshTokenSessionService;
    private final UserSessionInvalidationService userSessionInvalidationService;

    public Page<AdminUserListItemDTO> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, k).or().like(SysUser::getNickname, k));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> raw = userMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminUserListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(u -> AdminUserListItemDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .status(u.getStatus())
                .createTime(u.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void setUserStatus(Long userId, int status, Long adminId, String adminUsername, String clientIp,
                              AdminAuditService audit) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        userMapper.updateById(user);
        audit.record(adminId, adminUsername, status == 1 ? "USER_ENABLE" : "USER_DISABLE",
                "user", String.valueOf(userId), "status=" + status, clientIp);
    }

    public AdminUserDetailDTO getUserDetail(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return AdminUserDetailDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    @Transactional
    public void kickUser(Long userId, Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        refreshTokenSessionService.revokeToken(userId);
        userSessionInvalidationService.invalidateAllSessions(userId);
        audit.record(adminId, adminUsername, "USER_KICK", "user", String.valueOf(userId), null, clientIp);
    }

    public Page<AdminFriendListItemDTO> listFriends(Long userId, int page, int size) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriend::getUserId, userId).orderByDesc(SysFriend::getCreateTime);
        Page<SysFriend> raw = friendMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminFriendListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(f -> {
            SysUser friend = userMapper.selectById(f.getFriendId());
            return AdminFriendListItemDTO.builder()
                    .id(f.getId())
                    .userId(f.getUserId())
                    .friendId(f.getFriendId())
                    .friendUsername(friend != null ? friend.getUsername() : null)
                    .friendNickname(friend != null ? friend.getNickname() : null)
                    .remark(f.getRemark())
                    .createTime(f.getCreateTime())
                    .build();
        }).toList());
        return result;
    }
}