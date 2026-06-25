package com.linkx.server.module.blacklist.service;  // 行注：声明当前文件所在包 com.linkx.server.module.blacklist.service

import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;  // 行注：引入 BlacklistUserDTO 类型

import java.util.List;  // 行注：引入 List 类型

/** 用户黑名单的增删查及双向关系判断。 */
// 行注：定义 BlacklistService 接口
public interface BlacklistService {

    /**
     * 添加黑名单。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     */
    void addBlacklist(Long userId, Long targetUserId);  // 行注：调用添加黑名单

    /**
     * 移除黑名单。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     */
    void removeBlacklist(Long userId, Long targetUserId);  // 行注：调用移除黑名单

    /**
     * 获取黑名单。
     *
     * @param userId 用户 ID
     * @return 黑名单用户列表
     */
    List<BlacklistUserDTO> getBlacklist(Long userId);  // 行注：调用获取黑名单

    /**
     * 判断Blacklisted。
     *
     * @param userId 用户 ID
     * @param targetUserId 目标用户 ID
     * @return 是否满足条件
     */
    boolean isBlacklisted(Long userId, Long targetUserId);  // 行注：调用是否Blacklisted
}  // 行注：结束当前代码块
