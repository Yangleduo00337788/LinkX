package com.linkx.server.module.chat.service;

import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;

import java.util.List;

public interface ChatService {
    MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Integer sessionType);
    MessageDTO sendFileMessage(Long fromUserId, Long toUserId, Long fileId, Integer msgType, Integer sessionType);
    List<MessageDTO> getChatHistory(Long userId, Long targetId, Integer sessionType, int page, int size);
    List<ChatSessionDTO> getSessions(Long userId);
    void markAsRead(Long userId, Long targetId, Integer sessionType);
    void recallMessage(Long userId, Long messageId);
}
