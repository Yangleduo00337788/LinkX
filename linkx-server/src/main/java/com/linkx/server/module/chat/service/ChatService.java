package com.linkx.server.module.chat.service;

import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;

import java.util.List;

public interface ChatService {
    MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType);
    List<MessageDTO> getChatHistory(Long userId, Long targetId, int page, int size);
    List<ChatSessionDTO> getSessions(Long userId);
    void markAsRead(Long userId, Long targetId);
}
