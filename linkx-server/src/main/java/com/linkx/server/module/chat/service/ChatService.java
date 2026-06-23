package com.linkx.server.module.chat.service;

import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;

import java.util.List;

/**
 * 单聊/群聊：发消息、历史、会话列表、已读、撤回。
 */
public interface ChatService {
    MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Integer sessionType, String clientMessageId, Boolean mentionAll, List<Long> mentionUserIds);
    MessageDTO sendFileMessage(Long fromUserId, Long toUserId, Long fileId, Integer msgType, Integer sessionType, String clientMessageId);
    List<MessageDTO> getChatHistory(Long userId, Long targetId, Integer sessionType, int page, int size);
    List<ChatSessionDTO> getSessions(Long userId);
    void markAsRead(Long userId, Long targetId, Integer sessionType);
    void recallMessage(Long userId, Long messageId);
}
