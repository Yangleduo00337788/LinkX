package com.linkx.server.module.user.service;  // 行注：声明当前文件所在包 com.linkx.server.module.user.service

import com.linkx.server.module.user.dto.UpdateProfileRequest;  // 行注：引入 UpdateProfileRequest 类型
import com.linkx.server.module.user.dto.UserProfileDTO;  // 行注：引入 UserProfileDTO 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 用户资料与搜索业务。
 */
// 行注：定义 UserService 接口
public interface UserService {

    /**
     * 查看他人资料：须为双向好友，否则 403。
     */
    UserProfileDTO getProfile(Long requesterId, Long userId);  // 行注：调用获取Profile

    /**
     * 获取我的资料。
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    UserProfileDTO getMyProfile(Long userId);  // 行注：调用获取我的Profile

    /**
     * 更新资料。
     *
     * @param userId 用户 ID
     * @param request 当前请求或请求对象
     * @return 用户资料
     */
    UserProfileDTO updateProfile(Long userId, UpdateProfileRequest request);  // 行注：调用更新Profile

    /**
     * 按昵称/用户名模糊搜索，关键词至少 2 字，最多 20 条（限流在 Controller）。
     */
    List<UserProfileDTO> searchUsers(Long requesterId, String keyword);  // 行注：调用搜索Users
}  // 行注：结束当前代码块
