package com.linkx.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.UploadAssetUrlUtils;
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户资料实现：好友关系校验、头像 URL 规范化、搜索去重与条数限制。
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int SEARCH_KEYWORD_MIN_LENGTH = 2;
    private static final int SEARCH_RESULT_LIMIT = 20;

    private final SysUserMapper userMapper;
    private final SysFriendMapper friendMapper;
    private final LinkxAppProperties linkxAppProperties;

    @Override
    public UserProfileDTO getProfile(Long requesterId, Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户ID无效");
        }
        if (requesterId != null && requesterId.equals(userId)) {
            return getMyProfile(userId);
        }
        if (requesterId == null || !hasFriendRelation(requesterId, userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅可查看好友资料");
        }
        SysUser user = requireActiveUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toDTO(user);
    }

    @Override
    public UserProfileDTO getMyProfile(Long userId) {
        SysUser user = requireActiveUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toDTO(user);
    }

    @Override
    public UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = requireActiveUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            String trimmedNickname = request.getNickname().trim();
            if (trimmedNickname.isEmpty()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "昵称不能为空");
            }
            user.setNickname(trimmedNickname);
        }
        if (request.getAvatar() != null) {
            user.setAvatar(UploadAssetUrlUtils.normalizeAvatarUrl(request.getAvatar(), linkxAppProperties.getUpload().getUrl(), "头像"));
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }

        userMapper.updateById(user);
        return toDTO(user);
    }

    @Override
    public List<UserProfileDTO> searchUsers(Long requesterId, String keyword) {
        if (requesterId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (keyword == null || keyword.trim().length() < SEARCH_KEYWORD_MIN_LENGTH) {
            return List.of();
        }
        String trimmedKeyword = keyword.trim();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1)
                .ne(SysUser::getId, requesterId)
                .and(w -> w
                        .like(SysUser::getUsername, trimmedKeyword)
                        .or()
                        .like(SysUser::getNickname, trimmedKeyword)
                );
        wrapper.last("LIMIT " + SEARCH_RESULT_LIMIT);

        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private boolean hasFriendRelation(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysFriend> forward = new LambdaQueryWrapper<>();
        forward.eq(SysFriend::getUserId, userId)
                .eq(SysFriend::getFriendId, targetUserId);
        if (friendMapper.selectCount(forward) > 0) {
            return true;
        }
        LambdaQueryWrapper<SysFriend> reverse = new LambdaQueryWrapper<>();
        reverse.eq(SysFriend::getUserId, targetUserId)
                .eq(SysFriend::getFriendId, userId);
        return friendMapper.selectCount(reverse) > 0;
    }

    private SysUser requireActiveUser(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return null;
        }
        return user;
    }

    private UserProfileDTO toDTO(SysUser user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setGender(user.getGender());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}