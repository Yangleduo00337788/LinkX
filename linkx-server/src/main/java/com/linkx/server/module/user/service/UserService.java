package com.linkx.server.module.user.service;

import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;

import java.util.List;

/**
 * 用户资料与搜索业务。
 */
public interface UserService {

    /**
     * 查看他人资料：须为双向好友，否则 403。
     */
    UserProfileDTO getProfile(Long requesterId, Long userId);

    UserProfileDTO getMyProfile(Long userId);

    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 按昵称/用户名模糊搜索，关键词至少 2 字，最多 20 条（限流在 Controller）。
     */
    List<UserProfileDTO> searchUsers(Long requesterId, String keyword);
}