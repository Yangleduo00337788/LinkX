package com.linkx.server.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
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

    @Override
    public UserProfileDTO getProfile(Long userId) {
        SysUser user = userMapper.selectById(userId);
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
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getSignature() != null) {
            user.setSignature(request.getSignature());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getRegion() != null) {
            user.setRegion(request.getRegion());
        }

        userMapper.updateById(user);
        return toDTO(user);
    }

    @Override
    public List<UserProfileDTO> searchUsers(String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
        );
        wrapper.last("LIMIT 20");

        List<SysUser> users = userMapper.selectList(wrapper);
        return users.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private UserProfileDTO toDTO(SysUser user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setSignature(user.getSignature());
        dto.setGender(user.getGender());
        dto.setRegion(user.getRegion());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
