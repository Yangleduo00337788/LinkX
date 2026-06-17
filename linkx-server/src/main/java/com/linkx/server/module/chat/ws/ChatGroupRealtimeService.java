package com.linkx.server.module.chat.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.group.dto.GroupMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatGroupRealtimeService {

    private final ImGroupInfoMapper groupInfoMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImMessageMapper messageMapper;
    private final ImSessionMapper sessionMapper;
    private final SysUserMapper userMapper;
    private final ChatEventPushService chatEventPushService;

    public void pushGroupMutation(Long groupId, Long messageId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (groupId == null || messageId == null || normalizedUserIds.isEmpty()) {
            return;
        }

        MessageDTO message = buildMessage(messageId);
        if (message != null) {
            chatEventPushService.sendToUsers(normalizedUserIds, ChatEventType.MESSAGE, new ChatMessagePayload(message));
        }

        pushGroupSessions(groupId, normalizedUserIds);
        pushGroupDetails(groupId, normalizedUserIds);
    }

    public void pushGroupDetails(Long groupId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;
        }
        for (Long userId : normalizedUserIds) {
            GroupDetailDTO detail = buildGroupDetail(userId, groupId);
            if (detail == null) {
                continue;
            }
            chatEventPushService.sendToUser(userId, ChatEventType.GROUP_DETAIL, new ChatGroupDetailPayload(detail));
        }
    }

    public void pushGroupSessions(Long groupId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;
        }
        for (Long userId : normalizedUserIds) {
            ChatSessionDTO session = buildGroupSession(userId, groupId);
            if (session == null) {
                continue;
            }
            chatEventPushService.sendToUser(userId, ChatEventType.SESSION, new ChatSessionPayload(session));
        }
    }

    public void pushGroupRemoved(Long groupId, Collection<Long> userIds, String reason) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;
        }
        chatEventPushService.sendToUsers(normalizedUserIds, ChatEventType.GROUP_REMOVED, new ChatGroupRemovedPayload(groupId, reason));
    }

    private MessageDTO buildMessage(Long messageId) {
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            return null;
        }
        List<Long> mentionUserIds = parseMentionUserIds(message.getMentionUserIds());
        Set<Long> relatedUserIds = new LinkedHashSet<>();
        relatedUserIds.add(message.getFromUserId());
        relatedUserIds.addAll(mentionUserIds);
        Map<Long, SysUser> userMap = loadUserMap(relatedUserIds);
        SysUser fromUser = userMap.get(message.getFromUserId());
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setFromUserId(message.getFromUserId());
        dto.setFromNickname(fromUser != null ? fromUser.getNickname() : null);
        dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : null);
        dto.setToUserId(message.getToUserId());
        dto.setSessionType(ChatConstants.SESSION_TYPE_GROUP);
        dto.setContent(message.getContent());
        dto.setMsgType(message.getMsgType());
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));
        dto.setMentionUserIds(mentionUserIds);
        dto.setMentionDisplayNames(resolveMentionDisplayNames(mentionUserIds, userMap));
        dto.setFileName(null);
        dto.setFileSize(null);
        dto.setStatus(message.getStatus());
        dto.setReadTime(message.getReadTime());
        dto.setCreateTime(message.getCreateTime());
        return dto;
    }

    private GroupDetailDTO buildGroupDetail(Long userId, Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            return null;
        }

        ImGroupMember currentMember = getMember(groupId, userId);
        if (currentMember == null) {
            return null;
        }

        List<ImGroupMember> members = listMembersByGroupId(groupId);
        Map<Long, SysUser> userMap = loadUserMap(members.stream().map(ImGroupMember::getUserId).collect(Collectors.toSet()));

        GroupDetailDTO detailDTO = new GroupDetailDTO();
        detailDTO.setId(groupInfo.getId());
        detailDTO.setGroupName(groupInfo.getGroupName());
        detailDTO.setGroupAvatar(groupInfo.getGroupAvatar());
        detailDTO.setNotice(groupInfo.getNotice());
        detailDTO.setNoticeUpdateTime(groupInfo.getNoticeUpdateTime());
        detailDTO.setOwnerId(groupInfo.getOwnerId());
        detailDTO.setMaxMembers(groupInfo.getMaxMembers());
        detailDTO.setMemberCount(members.size());
        detailDTO.setMyRole(currentMember.getRole());
        detailDTO.setMuted(isMuted(currentMember));
        detailDTO.setMuteTime(currentMember.getMuteTime());
        detailDTO.setCreateTime(groupInfo.getCreateTime());
        detailDTO.setMembers(members.stream()
                .sorted((left, right) -> {
                    int roleCompare = Integer.compare(right.getRole(), left.getRole());
                    if (roleCompare != 0) {
                        return roleCompare;
                    }
                    LocalDateTime leftTime = left.getCreateTime();
                    LocalDateTime rightTime = right.getCreateTime();
                    if (leftTime == null && rightTime == null) {
                        return 0;
                    }
                    if (leftTime == null) {
                        return 1;
                    }
                    if (rightTime == null) {
                        return -1;
                    }
                    return leftTime.compareTo(rightTime);
                })
                .map(member -> toMemberDTO(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList()));
        return detailDTO;
    }

    private ChatSessionDTO buildGroupSession(Long userId, Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            return null;
        }

        ImGroupMember member = getMember(groupId, userId);
        if (member == null) {
            return null;
        }

        ImSession session = getGroupSession(userId, groupId);
        if (session == null) {
            return null;
        }

        ChatSessionDTO dto = new ChatSessionDTO();
        dto.setId(session.getId());
        dto.setUserId(session.getUserId());
        dto.setTargetId(session.getTargetId());
        dto.setSessionType(session.getSessionType());
        dto.setTargetNickname(groupInfo.getGroupName());
        dto.setTargetUsername("group-" + groupInfo.getId());
        dto.setTargetAvatar(groupInfo.getGroupAvatar());
        dto.setLastMessage(session.getLastMessage());
        dto.setLastMessageTime(session.getLastMessageTime());
        dto.setUnreadCount(session.getUnreadCount());
        dto.setMemberCount(listMembersByGroupId(groupId).size());
        dto.setMyRole(member.getRole());
        dto.setNotice(groupInfo.getNotice());
        dto.setMuted(isMuted(member));
        dto.setMuteTime(member.getMuteTime());
        dto.setTargetOnline(false);
        return dto;
    }

    private GroupMemberDTO toMemberDTO(ImGroupMember member, SysUser user) {
        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(member.getId());
        dto.setGroupId(member.getGroupId());
        dto.setUserId(member.getUserId());
        dto.setRole(member.getRole());
        dto.setMuted(isMuted(member));
        dto.setMuteTime(member.getMuteTime());
        dto.setCreateTime(member.getCreateTime());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setNickname(user.getNickname());
            dto.setAvatar(user.getAvatar());
        }
        return dto;
    }

    private ImGroupMember getMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        return groupMemberMapper.selectOne(wrapper);
    }

    private ImSession getGroupSession(Long userId, Long groupId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);
        return sessionMapper.selectOne(wrapper);
    }

    private List<ImGroupMember> listMembersByGroupId(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId);
        return groupMemberMapper.selectList(wrapper);
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));
    }

    private Set<Long> normalizeUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        return userIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean isMuted(ImGroupMember member) {
        return member.getMuteTime() != null && member.getMuteTime().isAfter(LocalDateTime.now());
    }

    private List<Long> parseMentionUserIds(String mentionUserIds) {
        if (mentionUserIds == null || mentionUserIds.isBlank()) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String item : mentionUserIds.split(",")) {
            if (item == null || item.isBlank()) {
                continue;
            }
            try {
                result.add(Long.parseLong(item.trim()));
            } catch (NumberFormatException ignored) {
                // Skip malformed legacy values.
            }
        }
        return result;
    }

    private List<String> resolveMentionDisplayNames(List<Long> mentionUserIds, Map<Long, SysUser> userMap) {
        if (mentionUserIds.isEmpty()) {
            return List.of();
        }
        List<String> displayNames = new ArrayList<>();
        for (Long mentionUserId : mentionUserIds) {
            SysUser user = userMap.get(mentionUserId);
            if (user == null) {
                continue;
            }
            if (user.getNickname() != null && !user.getNickname().isBlank()) {
                displayNames.add(user.getNickname().trim());
            } else if (user.getUsername() != null && !user.getUsername().isBlank()) {
                displayNames.add(user.getUsername().trim());
            }
        }
        return displayNames;
    }
}
