package com.linkx.server.module.session.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImSession;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSessionPreferencesService {

    private final ImSessionMapper sessionMapper;
    private final ChatService chatService;

    @Transactional
    public void updatePreferences(Long userId, Long targetId, Integer sessionType,
                                  String sessionRemark, Boolean pinned, Boolean notificationMuted) {
        ImSession session = requireSession(userId, targetId, sessionType);
        if (sessionRemark != null) {
            session.setSessionRemark(sessionRemark.trim().isEmpty() ? null : sessionRemark.trim());
        }
        if (pinned != null) {
            session.setPinned(pinned ? 1 : 0);
        }
        if (notificationMuted != null) {
            session.setNotificationMuted(notificationMuted ? 1 : 0);
        }
        sessionMapper.updateById(session);
        chatService.refreshSessionPush(userId, targetId, sessionType);
    }

    private ImSession requireSession(Long userId, Long targetId, Integer sessionType) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId)
                .eq(ImSession::getSessionType, sessionType);
        ImSession session = sessionMapper.selectOne(wrapper);
        if (session == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会话不存在");
        }
        return session;
    }
}