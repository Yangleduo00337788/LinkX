package com.linkx.server.module.blacklist.service;

import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;

import java.util.List;

/** 用户黑名单的增删查及双向关系判断。 */
public interface BlacklistService {

    void addBlacklist(Long userId, Long targetUserId);

    void removeBlacklist(Long userId, Long targetUserId);

    List<BlacklistUserDTO> getBlacklist(Long userId);

    boolean isBlacklisted(Long userId, Long targetUserId);
}
