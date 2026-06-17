package com.linkx.server.module.blacklist.service;

import com.linkx.server.module.blacklist.dto.BlacklistUserDTO;

import java.util.List;

public interface BlacklistService {

    void addBlacklist(Long userId, Long targetUserId);

    void removeBlacklist(Long userId, Long targetUserId);

    List<BlacklistUserDTO> getBlacklist(Long userId);

    boolean isBlacklisted(Long userId, Long targetUserId);
}
