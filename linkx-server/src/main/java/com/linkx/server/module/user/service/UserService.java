package com.linkx.server.module.user.service;

import com.linkx.server.module.user.dto.UpdateProfileRequest;
import com.linkx.server.module.user.dto.UserProfileDTO;

import java.util.List;

public interface UserService {
    UserProfileDTO getProfile(Long userId);
    UserProfileDTO getMyProfile(Long userId);
    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);
    List<UserProfileDTO> searchUsers(String keyword);
}
