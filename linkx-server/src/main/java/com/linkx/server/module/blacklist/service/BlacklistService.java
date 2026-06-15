package com.linkx.server.module.blacklist.service;

import com.linkx.server.entity.SysUser;

import java.util.List;

public interface BlacklistService {

    void addBlacklist(Long userId, Long targetUserId);

    void removeBlacklist(Long userId, Long targetUserId);

    List<SysUser> getBlacklist(Long userId);

    boolean isBlacklisted(Long userId, Long targetUserId);
}
