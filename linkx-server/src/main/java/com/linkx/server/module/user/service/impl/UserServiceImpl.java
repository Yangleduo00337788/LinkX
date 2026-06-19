package com.linkx.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.UploadAssetUrlUtils;
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;
import com.linkx.server.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper userMapper;
    private final LinkxAppProperties linkxAppProperties;

    @Override
    public UserProfileDTO getProfile(Long userId) {
        SysUser user = requireActiveUser(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return toDTO(user);
    }

    @Override
    public UserProfileDTO getMyProfile(Long userId) {
        return getProfile(userId);
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
    public List<UserProfileDTO> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String trimmedKeyword = keyword.trim();
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getStatus, 1)
                .and(w -> w
                        .like(SysUser::getUsername, trimmedKeyword)
                        .or()
                        .like(SysUser::getNickname, trimmedKeyword)
                );
        wrapper.last("LIMIT 20");

        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
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
