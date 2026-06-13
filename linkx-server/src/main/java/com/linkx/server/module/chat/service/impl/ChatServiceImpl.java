package com.linkx.server.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ImSessionMapper sessionMapper;
    private final ImMessageMapper messageMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType) {
        SysUser toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        ImSession session = getOrCreateSession(fromUserId, toUserId);
        session.setLastMessage(content);
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        ImSession reverseSession = getOrCreateSession(toUserId, fromUserId);
        reverseSession.setLastMessage(content);
        reverseSession.setLastMessageTime(LocalDateTime.now());
        reverseSession.setUnreadCount(reverseSession.getUnreadCount() + 1);
        sessionMapper.updateById(reverseSession);

        ImMessage message = new ImMessage();
        message.setSessionId(session.getId());
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setStatus(0);
        messageMapper.insert(message);

        return toMessageDTO(message, fromUserId);
    }

    @Override
    public List<MessageDTO> getChatHistory(Long userId, Long targetId, int page, int size) {
        ImSession session = findSession(userId, targetId);
        if (session == null) {
            return List.of();
        }

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getSessionId, session.getId())
                .orderByDesc(ImMessage::getCreateTime)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size);

        List<ImMessage> messages = messageMapper.selectList(wrapper);

        return messages.stream()
                .sorted((a, b) -> a.getCreateTime().compareTo(b.getCreateTime()))
                .map(m -> toMessageDTO(m, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatSessionDTO> getSessions(Long userId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .orderByDesc(ImSession::getLastMessageTime);

        List<ImSession> sessions = sessionMapper.selectList(wrapper);

        return sessions.stream().map(s -> {
            ChatSessionDTO dto = new ChatSessionDTO();
            dto.setId(s.getId());
            dto.setUserId(s.getUserId());
            dto.setTargetId(s.getTargetId());
            dto.setLastMessage(s.getLastMessage());
            dto.setLastMessageTime(s.getLastMessageTime());
            dto.setUnreadCount(s.getUnreadCount());

            SysUser targetUser = userMapper.selectById(s.getTargetId());
            if (targetUser != null) {
                dto.setTargetNickname(targetUser.getNickname());
                dto.setTargetUsername(targetUser.getUsername());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long targetId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId);

        ImSession session = sessionMapper.selectOne(wrapper);
        if (session != null) {
            session.setUnreadCount(0);
            sessionMapper.updateById(session);
        }
    }

    private ImSession getOrCreateSession(Long userId, Long targetId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId);

        ImSession session = sessionMapper.selectOne(wrapper);
        if (session == null) {
            session = new ImSession();
            session.setUserId(userId);
            session.setTargetId(targetId);
            session.setSessionType(1);
            session.setUnreadCount(0);
            sessionMapper.insert(session);
        }
        return session;
    }

    private ImSession findSession(Long userId, Long targetId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId);
        return sessionMapper.selectOne(wrapper);
    }

    private MessageDTO toMessageDTO(ImMessage message, Long currentUserId) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setFromUserId(message.getFromUserId());
        dto.setToUserId(message.getToUserId());
        dto.setContent(message.getContent());
        dto.setMsgType(message.getMsgType());
        dto.setStatus(message.getStatus());
        dto.setCreateTime(message.getCreateTime());

        SysUser fromUser = userMapper.selectById(message.getFromUserId());
        if (fromUser != null) {
            dto.setFromNickname(fromUser.getNickname());
        }
        return dto;
    }
}
