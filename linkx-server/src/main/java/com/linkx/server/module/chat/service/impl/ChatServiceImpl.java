package com.linkx.server.module.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysFile;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.service.BlacklistService;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.service.ChatService;
import com.linkx.server.module.chat.ws.ChatEventPushService;
import com.linkx.server.module.chat.ws.ChatEventType;
import com.linkx.server.module.chat.ws.ChatMessagePayload;
import com.linkx.server.module.chat.ws.ChatPresenceService;
import com.linkx.server.module.chat.ws.ChatReadReceiptPayload;
import com.linkx.server.module.chat.ws.ChatSessionPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ImSessionMapper sessionMapper;
    private final ImMessageMapper messageMapper;
    private final SysUserMapper userMapper;
    private final SysFileMapper fileMapper;
    private final ImGroupInfoMapper groupInfoMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final BlacklistService blacklistService;
    private final ChatEventPushService chatEventPushService;
    private final ChatPresenceService chatPresenceService;

    @Override
    @Transactional
    public MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Integer sessionType) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息内容不能为空");
        }
        int resolvedMsgType = resolveTextMessageType(msgType);
        int resolvedSessionType = resolveSessionType(sessionType);
        String normalizedContent = content.trim();
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP) {
            return sendGroupMessage(fromUserId, toUserId, normalizedContent, resolvedMsgType);
        }
        return sendSingleMessage(fromUserId, toUserId, normalizedContent, resolvedMsgType);
    }

    @Override
    @Transactional
    public MessageDTO sendFileMessage(Long fromUserId, Long toUserId, Long fileId, Integer msgType, Integer sessionType) {
        SysFile sysFile = fileMapper.selectById(fileId);
        if (sysFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");
        }
        if (!fromUserId.equals(sysFile.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权发送该文件");
        }
        int resolvedMsgType = resolveFileMessageType(msgType);
        int resolvedSessionType = resolveSessionType(sessionType);
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP) {
            return sendGroupMessage(fromUserId, toUserId, sysFile.getFileUrl(), resolvedMsgType);
        }
        return sendSingleMessage(fromUserId, toUserId, sysFile.getFileUrl(), resolvedMsgType);
    }

    @Override
    public List<MessageDTO> getChatHistory(Long userId, Long targetId, Integer sessionType, int page, int size) {
        int resolvedSessionType = resolveSessionType(sessionType);
        return resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP
                ? getGroupHistory(userId, targetId, page, size)
                : getSingleHistory(userId, targetId, page, size);
    }

    @Override
    public List<ChatSessionDTO> getSessions(Long userId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .orderByDesc(ImSession::getLastMessageTime, ImSession::getId);
        List<ImSession> sessions = sessionMapper.selectList(wrapper);
        if (sessions.isEmpty()) {
            return List.of();
        }

        List<ImSession> singleSessions = sessions.stream()
                .filter(session -> session.getSessionType() == ChatConstants.SESSION_TYPE_SINGLE)
                .toList();
        List<ImSession> groupSessions = sessions.stream()
                .filter(session -> session.getSessionType() == ChatConstants.SESSION_TYPE_GROUP)
                .toList();

        Map<Long, SysUser> userMap = loadUserMap(singleSessions.stream().map(ImSession::getTargetId).collect(Collectors.toSet()));
        Map<Long, Boolean> blacklistCache = new HashMap<>();

        Set<Long> groupIds = groupSessions.stream().map(ImSession::getTargetId).collect(Collectors.toSet());
        Map<Long, ImGroupInfo> groupMap = loadActiveGroupMap(groupIds);
        Map<Long, ImGroupMember> myGroupMemberMap = loadGroupMembersByUser(userId, groupIds);
        Map<Long, Integer> groupMemberCountMap = loadGroupMemberCount(groupIds);

        return sessions.stream()
                .map(session -> buildSessionDTO(userId, session, userMap, blacklistCache, groupMap, myGroupMemberMap, groupMemberCountMap))
                .filter(dto -> dto != null)
                .sorted(Comparator.comparing(ChatSessionDTO::getLastMessageTime, Comparator.nullsLast(LocalDateTime::compareTo)).reversed()
                        .thenComparing(ChatSessionDTO::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long targetId, Integer sessionType) {
        int resolvedSessionType = resolveSessionType(sessionType);
        ImSession session = getSession(userId, targetId, resolvedSessionType);
        if (session != null && session.getUnreadCount() != 0) {
            session.setUnreadCount(0);
            sessionMapper.updateById(session);
        }

        if (resolvedSessionType == ChatConstants.SESSION_TYPE_SINGLE) {
            LocalDateTime readTime = LocalDateTime.now();
            List<ImMessage> unreadMessages = listUnreadSingleMessages(targetId, userId);
            List<Long> messageIds = new ArrayList<>(unreadMessages.size());
            for (ImMessage unreadMessage : unreadMessages) {
                unreadMessage.setReadTime(readTime);
                messageMapper.updateById(unreadMessage);
                messageIds.add(unreadMessage.getId());
            }

            executeAfterCommit(() -> {
                pushSessionUpdate(userId, targetId, ChatConstants.SESSION_TYPE_SINGLE);
                if (!messageIds.isEmpty()) {
                    chatEventPushService.sendToUser(
                            targetId,
                            ChatEventType.READ_RECEIPT,
                            new ChatReadReceiptPayload(userId, ChatConstants.SESSION_TYPE_SINGLE, userId, readTime, messageIds)
                    );
                }
            });
            return;
        }

        executeAfterCommit(() -> pushSessionUpdate(userId, targetId, resolvedSessionType));
    }

    @Override
    @Transactional
    public void recallMessage(Long userId, Long messageId) {
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (!message.getFromUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (message.getStatus() == ChatConstants.MESSAGE_STATUS_RECALLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息已撤回");
        }

        int sessionType = resolveMessageSessionType(message);
        message.setStatus(ChatConstants.MESSAGE_STATUS_RECALLED);
        messageMapper.updateById(message);

        if (sessionType == ChatConstants.SESSION_TYPE_GROUP) {
            Long groupId = message.getToUserId();
            List<ImGroupMember> members = listGroupMembers(groupId);
            Set<Long> memberUserIds = members.stream().map(ImGroupMember::getUserId).collect(Collectors.toCollection(LinkedHashSet::new));
            refreshGroupSessionPreviews(groupId, memberUserIds);
            MessageDTO messageDTO = toMessageDTO(message, ChatConstants.SESSION_TYPE_GROUP, loadUserMap(Set.of(message.getFromUserId())), Map.of());
            executeAfterCommit(() -> notifyGroupRecall(messageDTO, groupId, memberUserIds));
            return;
        }

        refreshSingleSessionPreviews(message.getFromUserId(), message.getToUserId());
        MessageDTO messageDTO = toMessageDTO(message, ChatConstants.SESSION_TYPE_SINGLE, loadUserMap(Set.of(message.getFromUserId())), Map.of());
        executeAfterCommit(() -> notifySingleRecall(messageDTO, message.getFromUserId(), message.getToUserId()));
    }

    private MessageDTO sendSingleMessage(Long fromUserId, Long toUserId, String content, Integer msgType) {
        SysUser toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (blacklistService.isBlacklisted(toUserId, fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被对方拉黑");
        }

        LocalDateTime now = LocalDateTime.now();
        String preview = buildPreview(content, msgType);

        ImSession selfSession = getOrCreateSession(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);
        selfSession.setLastMessage(preview);
        selfSession.setLastMessageTime(now);
        sessionMapper.updateById(selfSession);

        ImSession peerSession = getOrCreateSession(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);
        peerSession.setLastMessage(preview);
        peerSession.setLastMessageTime(now);
        peerSession.setUnreadCount(peerSession.getUnreadCount() + 1);
        sessionMapper.updateById(peerSession);

        ImMessage message = new ImMessage();
        message.setSessionId(selfSession.getId());
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);
        messageMapper.insert(message);

        MessageDTO messageDTO = toMessageDTO(message, ChatConstants.SESSION_TYPE_SINGLE, loadUserMap(Set.of(fromUserId)), Map.of());
        executeAfterCommit(() -> notifySingleMessage(messageDTO, fromUserId, toUserId));
        return messageDTO;
    }

    private MessageDTO sendGroupMessage(Long fromUserId, Long groupId, String content, Integer msgType) {
        requireActiveGroup(groupId);
        ImGroupMember senderMember = requireGroupMember(groupId, fromUserId);
        if (isMuted(senderMember)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被禁言");
        }

        List<ImGroupMember> members = listGroupMembers(groupId);
        if (members.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String preview = buildPreview(content, msgType);
        Set<Long> memberUserIds = new LinkedHashSet<>();

        for (ImGroupMember member : members) {
            memberUserIds.add(member.getUserId());
            ImSession session = getOrCreateSession(member.getUserId(), groupId, ChatConstants.SESSION_TYPE_GROUP);
            session.setLastMessage(preview);
            session.setLastMessageTime(now);
            if (!member.getUserId().equals(fromUserId)) {
                session.setUnreadCount(session.getUnreadCount() + 1);
            }
            sessionMapper.updateById(session);
        }

        ImMessage message = new ImMessage();
        message.setSessionId(groupId);
        message.setFromUserId(fromUserId);
        message.setToUserId(groupId);
        message.setContent(content);
        message.setMsgType(msgType);
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);
        messageMapper.insert(message);

        MessageDTO messageDTO = toMessageDTO(message, ChatConstants.SESSION_TYPE_GROUP, loadUserMap(Set.of(fromUserId)), Map.of());
        executeAfterCommit(() -> notifyGroupMessage(messageDTO, groupId, memberUserIds));
        return messageDTO;
    }

    private List<MessageDTO> getSingleHistory(Long userId, Long targetId, int page, int size) {
        if (blacklistService.isBlacklisted(userId, targetId) || blacklistService.isBlacklisted(targetId, userId)) {
            return List.of();
        }

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                        .eq(ImMessage::getFromUserId, userId)
                        .eq(ImMessage::getToUserId, targetId)
                        .or()
                        .eq(ImMessage::getFromUserId, targetId)
                        .eq(ImMessage::getToUserId, userId))
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size + " OFFSET " + Math.max(page - 1, 0) * size);
        return toMessageList(messageMapper.selectList(wrapper), ChatConstants.SESSION_TYPE_SINGLE);
    }

    private List<MessageDTO> getGroupHistory(Long userId, Long groupId, int page, int size) {
        requireActiveGroup(groupId);
        requireGroupMember(groupId, userId);

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getToUserId, groupId)
                .eq(ImMessage::getSessionId, groupId)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size + " OFFSET " + Math.max(page - 1, 0) * size);
        return toMessageList(messageMapper.selectList(wrapper), ChatConstants.SESSION_TYPE_GROUP);
    }

    private List<MessageDTO> toMessageList(List<ImMessage> rawMessages, Integer sessionType) {
        List<ImMessage> messages = new ArrayList<>(rawMessages);
        Collections.reverse(messages);
        Set<Long> userIds = new HashSet<>();
        for (ImMessage message : messages) {
            userIds.add(message.getFromUserId());
        }
        Map<Long, SysUser> userMap = loadUserMap(userIds);
        Map<String, SysFile> fileMap = loadFileMap(messages);
        return messages.stream()
                .map(message -> toMessageDTO(message, sessionType, userMap, fileMap))
                .collect(Collectors.toList());
    }

    private void notifySingleMessage(MessageDTO messageDTO, Long fromUserId, Long toUserId) {
        chatEventPushService.sendToUsers(
                List.of(fromUserId, toUserId),
                ChatEventType.MESSAGE,
                new ChatMessagePayload(messageDTO)
        );
        pushSessionUpdate(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);
        pushSessionUpdate(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);
    }

    private void notifyGroupMessage(MessageDTO messageDTO, Long groupId, Collection<Long> memberUserIds) {
        chatEventPushService.sendToUsers(
                memberUserIds,
                ChatEventType.MESSAGE,
                new ChatMessagePayload(messageDTO)
        );
        for (Long memberUserId : memberUserIds) {
            pushSessionUpdate(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);
        }
    }

    private void notifySingleRecall(MessageDTO messageDTO, Long fromUserId, Long toUserId) {
        chatEventPushService.sendToUsers(
                List.of(fromUserId, toUserId),
                ChatEventType.MESSAGE_RECALLED,
                new ChatMessagePayload(messageDTO)
        );
        pushSessionUpdate(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);
        pushSessionUpdate(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);
    }

    private void notifyGroupRecall(MessageDTO messageDTO, Long groupId, Collection<Long> memberUserIds) {
        chatEventPushService.sendToUsers(
                memberUserIds,
                ChatEventType.MESSAGE_RECALLED,
                new ChatMessagePayload(messageDTO)
        );
        for (Long memberUserId : memberUserIds) {
            pushSessionUpdate(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);
        }
    }

    private void pushSessionUpdate(Long userId, Long targetId, Integer sessionType) {
        ChatSessionDTO sessionDTO = getSessionDTO(userId, targetId, sessionType);
        if (sessionDTO == null) {
            return;
        }
        chatEventPushService.sendToUser(userId, ChatEventType.SESSION, new ChatSessionPayload(sessionDTO));
    }

    private ChatSessionDTO getSessionDTO(Long userId, Long targetId, Integer sessionType) {
        ImSession session = getSession(userId, targetId, sessionType);
        if (session == null) {
            return null;
        }
        if (sessionType == ChatConstants.SESSION_TYPE_SINGLE) {
            return buildSessionDTO(
                    userId,
                    session,
                    loadUserMap(Set.of(targetId)),
                    new HashMap<>(),
                    Map.of(),
                    Map.of(),
                    Map.of()
            );
        }
        return buildSessionDTO(
                userId,
                session,
                Map.of(),
                new HashMap<>(),
                loadActiveGroupMap(Set.of(targetId)),
                loadGroupMembersByUser(userId, Set.of(targetId)),
                loadGroupMemberCount(Set.of(targetId))
        );
    }

    private ChatSessionDTO buildSessionDTO(
            Long userId,
            ImSession session,
            Map<Long, SysUser> userMap,
            Map<Long, Boolean> blacklistCache,
            Map<Long, ImGroupInfo> groupMap,
            Map<Long, ImGroupMember> myGroupMemberMap,
            Map<Long, Integer> groupMemberCountMap
    ) {
        if (session.getSessionType() == ChatConstants.SESSION_TYPE_SINGLE) {
            Boolean cached = blacklistCache.get(session.getTargetId());
            if (cached == null) {
                cached = blacklistService.isBlacklisted(userId, session.getTargetId())
                        || blacklistService.isBlacklisted(session.getTargetId(), userId);
                blacklistCache.put(session.getTargetId(), cached);
            }
            if (Boolean.TRUE.equals(cached)) {
                return null;
            }

            SysUser targetUser = userMap.get(session.getTargetId());
            if (targetUser == null) {
                return null;
            }

            ChatSessionDTO dto = baseSessionDTO(session);
            dto.setTargetNickname(targetUser.getNickname());
            dto.setTargetUsername(targetUser.getUsername());
            dto.setTargetAvatar(targetUser.getAvatar());
            dto.setTargetOnline(chatPresenceService.isOnline(targetUser.getId()));
            return dto;
        }

        ImGroupInfo groupInfo = groupMap.get(session.getTargetId());
        ImGroupMember member = myGroupMemberMap.get(session.getTargetId());
        if (groupInfo == null || member == null) {
            return null;
        }

        ChatSessionDTO dto = baseSessionDTO(session);
        dto.setTargetNickname(groupInfo.getGroupName());
        dto.setTargetUsername("group-" + groupInfo.getId());
        dto.setTargetAvatar(groupInfo.getGroupAvatar());
        dto.setMemberCount(groupMemberCountMap.getOrDefault(groupInfo.getId(), 0));
        dto.setMyRole(member.getRole());
        dto.setNotice(groupInfo.getNotice());
        dto.setMuted(isMuted(member));
        dto.setMuteTime(member.getMuteTime());
        dto.setTargetOnline(false);
        return dto;
    }

    private ChatSessionDTO baseSessionDTO(ImSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setTargetId(session.getTargetId());
        dto.setSessionType(session.getSessionType());
        dto.setLastMessage(session.getLastMessage());
        dto.setLastMessageTime(session.getLastMessageTime());
        dto.setUnreadCount(session.getUnreadCount());
        return dto;
    }

    private ImSession getOrCreateSession(Long userId, Long targetId, Integer sessionType) {
        ImSession session = getSession(userId, targetId, sessionType);
        if (session == null) {
            session = new ImSession();
            session.setUserId(userId);
            session.setTargetId(targetId);
            session.setSessionType(sessionType);
            session.setUnreadCount(0);
            sessionMapper.insert(session);
        }
        return session;
    }

    private ImSession getSession(Long userId, Long targetId, Integer sessionType) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId)
                .eq(ImSession::getSessionType, sessionType);
        return sessionMapper.selectOne(wrapper);
    }

    private List<ImMessage> listUnreadSingleMessages(Long fromUserId, Long toUserId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getFromUserId, fromUserId)
                .eq(ImMessage::getToUserId, toUserId)
                .eq(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_NORMAL)
                .isNull(ImMessage::getReadTime)
                .orderByAsc(ImMessage::getCreateTime, ImMessage::getId);
        return messageMapper.selectList(wrapper);
    }

    private void refreshSingleSessionPreviews(Long userA, Long userB) {
        updateSingleSessionPreview(userA, userB);
        updateSingleSessionPreview(userB, userA);
    }

    private void updateSingleSessionPreview(Long userId, Long targetId) {
        ImSession session = getSession(userId, targetId, ChatConstants.SESSION_TYPE_SINGLE);
        if (session == null) {
            return;
        }
        ImMessage latestMessage = getLatestSingleMessage(userId, targetId);
        applyLatestPreview(session, latestMessage);
    }

    private ImMessage getLatestSingleMessage(Long userId, Long targetId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ImMessage::getFromUserId, userId)
                        .eq(ImMessage::getToUserId, targetId)
                        .or()
                        .eq(ImMessage::getFromUserId, targetId)
                        .eq(ImMessage::getToUserId, userId))
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT 1");
        return messageMapper.selectOne(wrapper);
    }

    private void refreshGroupSessionPreviews(Long groupId, Collection<Long> memberUserIds) {
        ImMessage latestMessage = getLatestGroupMessage(groupId);
        for (Long memberUserId : memberUserIds) {
            ImSession session = getSession(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);
            if (session != null) {
                applyLatestPreview(session, latestMessage);
            }
        }
    }

    private ImMessage getLatestGroupMessage(Long groupId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getToUserId, groupId)
                .eq(ImMessage::getSessionId, groupId)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT 1");
        return messageMapper.selectOne(wrapper);
    }

    private void applyLatestPreview(ImSession session, ImMessage latestMessage) {
        if (latestMessage == null) {
            session.setLastMessage("");
            session.setLastMessageTime(null);
        } else {
            session.setLastMessage(buildPreview(latestMessage.getContent(), latestMessage.getMsgType(), latestMessage.getStatus()));
            session.setLastMessageTime(latestMessage.getCreateTime());
        }
        sessionMapper.updateById(session);
    }

    private int resolveMessageSessionType(ImMessage message) {
        if (message.getSessionId() != null && message.getSessionId().equals(message.getToUserId())) {
            ImGroupInfo groupInfo = groupInfoMapper.selectById(message.getToUserId());
            if (groupInfo != null && !Integer.valueOf(1).equals(groupInfo.getDeleted())) {
                return ChatConstants.SESSION_TYPE_GROUP;
            }
        }
        return ChatConstants.SESSION_TYPE_SINGLE;
    }

    private MessageDTO toMessageDTO(ImMessage message, Integer sessionType, Map<Long, SysUser> userMap, Map<String, SysFile> fileMap) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setFromUserId(message.getFromUserId());
        dto.setToUserId(message.getToUserId());
        dto.setSessionType(sessionType);
        dto.setContent(message.getContent());
        dto.setMsgType(message.getMsgType());
        dto.setStatus(message.getStatus());
        dto.setReadTime(message.getReadTime());
        dto.setCreateTime(message.getCreateTime());

        SysUser fromUser = userMap.get(message.getFromUserId());
        if (fromUser != null) {
            dto.setFromNickname(fromUser.getNickname());
            dto.setFromAvatar(fromUser.getAvatar());
        }
        if ((message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                && StringUtils.hasText(message.getContent())) {
            SysFile file = fileMap.get(message.getContent());
            if (file != null) {
                dto.setFileName(file.getOriginalName());
                dto.setFileSize(file.getFileSize());
            }
        }
        return dto;
    }

    private Map<String, SysFile> loadFileMap(List<ImMessage> messages) {
        Set<String> fileUrls = messages.stream()
                .filter(message -> (message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                        && StringUtils.hasText(message.getContent()))
                .map(ImMessage::getContent)
                .collect(Collectors.toSet());
        if (fileUrls.isEmpty()) {
            return Map.of();
        }

        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysFile::getFileUrl, fileUrls);
        return fileMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
    }

    private Map<Long, ImGroupInfo> loadActiveGroupMap(Set<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ImGroupInfo::getId, groupIds)
                .eq(ImGroupInfo::getDeleted, 0);
        return groupInfoMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(ImGroupInfo::getId, group -> group));
    }

    private Map<Long, ImGroupMember> loadGroupMembersByUser(Long userId, Set<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getUserId, userId)
                .in(ImGroupMember::getGroupId, groupIds);
        return groupMemberMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(ImGroupMember::getGroupId, member -> member));
    }

    private Map<Long, Integer> loadGroupMemberCount(Set<Long> groupIds) {
        Map<Long, Integer> countMap = new HashMap<>();
        if (groupIds.isEmpty()) {
            return countMap;
        }
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ImGroupMember::getGroupId, groupIds);
        for (ImGroupMember member : groupMemberMapper.selectList(wrapper)) {
            countMap.merge(member.getGroupId(), 1, Integer::sum);
        }
        return countMap;
    }

    private ImGroupInfo requireActiveGroup(Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群聊不存在");
        }
        return groupInfo;
    }

    private ImGroupMember requireGroupMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        ImGroupMember member = groupMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你不在该群聊中");
        }
        return member;
    }

    private List<ImGroupMember> listGroupMembers(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId);
        return groupMemberMapper.selectList(wrapper);
    }

    private boolean isMuted(ImGroupMember member) {
        return member.getMuteTime() != null && member.getMuteTime().isAfter(LocalDateTime.now());
    }

    private void executeAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    private int resolveSessionType(Integer sessionType) {
        if (sessionType == null) {
            return ChatConstants.SESSION_TYPE_SINGLE;
        }
        if (sessionType != ChatConstants.SESSION_TYPE_SINGLE && sessionType != ChatConstants.SESSION_TYPE_GROUP) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的会话类型");
        }
        return sessionType;
    }

    private int resolveTextMessageType(Integer msgType) {
        if (msgType == null) {
            return ChatConstants.MESSAGE_TYPE_TEXT;
        }
        if (msgType != ChatConstants.MESSAGE_TYPE_TEXT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "普通消息仅支持文本类型");
        }
        return msgType;
    }

    private int resolveFileMessageType(Integer msgType) {
        if (msgType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件消息类型不能为空");
        }
        if (msgType != ChatConstants.MESSAGE_TYPE_IMAGE && msgType != ChatConstants.MESSAGE_TYPE_FILE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件消息仅支持图片或文件类型");
        }
        return msgType;
    }

    private String buildPreview(String content, Integer msgType) {
        return buildPreview(content, msgType, ChatConstants.MESSAGE_STATUS_NORMAL);
    }

    private String buildPreview(String content, Integer msgType, Integer status) {
        if (status != null && status == ChatConstants.MESSAGE_STATUS_RECALLED) {
            return "[消息已撤回]";
        }
        if (msgType == null || msgType == ChatConstants.MESSAGE_TYPE_TEXT || msgType == ChatConstants.MESSAGE_TYPE_SYSTEM) {
            return content;
        }
        if (msgType == ChatConstants.MESSAGE_TYPE_IMAGE) {
            return "[图片]";
        }
        if (msgType == ChatConstants.MESSAGE_TYPE_FILE) {
            return "[文件]";
        }
        return "[消息]";
    }
}
