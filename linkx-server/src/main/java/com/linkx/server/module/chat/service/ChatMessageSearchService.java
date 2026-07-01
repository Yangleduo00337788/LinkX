package com.linkx.server.module.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.service.BlacklistService;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.dto.MessageSearchHitDTO;
import com.linkx.server.module.chat.helper.ChatMessageHelper;
import com.linkx.server.module.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
public class ChatMessageSearchService {

    private static final int MAX_SIZE = 50;

    private final ImSessionMapper sessionMapper;
    private final ImMessageMapper messageMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImGroupInfoMapper groupInfoMapper;
    private final SysUserMapper userMapper;
    private final BlacklistService blacklistService;
    private final ChatMessageHelper chatMessageHelper;
    private final GroupService groupService;
    private final SysFriendMapper friendMapper;

    public List<MessageSearchHitDTO> searchGlobal(Long userId, String keyword, Integer sessionTypeFilter, int size) {
        int limit = Math.min(Math.max(size, 1), MAX_SIZE);
        String normalized = normalizeKeyword(keyword);
        if (normalized == null) {
            return List.of();
        }

        List<MessageSearchHitDTO> hits = new ArrayList<>();
        Set<Long> seenMessageIds = new HashSet<>();

        if (sessionTypeFilter == null || sessionTypeFilter == ChatConstants.SESSION_TYPE_SINGLE) {
            for (Long friendId : listSingleChatFriendIds(userId)) {
                if (hits.size() >= limit) {
                    break;
                }
                appendSingleHits(userId, friendId, normalized, limit - hits.size(), hits, seenMessageIds);
            }
        }

        if (sessionTypeFilter == null || sessionTypeFilter == ChatConstants.SESSION_TYPE_GROUP) {
            for (Long groupId : listMyGroupIds(userId)) {
                if (hits.size() >= limit) {
                    break;
                }
                List<MessageDTO> groupMsgs = groupService.searchGroupMessages(userId, groupId, normalized, limit - hits.size());
                ImGroupInfo group = groupInfoMapper.selectById(groupId);
                String title = group != null ? group.getGroupName() : "群聊";
                for (MessageDTO dto : groupMsgs) {
                    if (dto.getId() != null && seenMessageIds.add(dto.getId())) {
                        hits.add(toHit(dto, groupId, title));
                    }
                }
            }
        }

        hits.sort(Comparator.comparing(MessageSearchHitDTO::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        if (hits.size() > limit) {
            return hits.subList(0, limit);
        }
        return hits;
    }

    private void appendSingleHits(Long userId, Long friendId, String keyword, int remaining,
                                  List<MessageSearchHitDTO> hits, Set<Long> seenMessageIds) {
        if (remaining <= 0 || !canAccessSingle(userId, friendId)) {
            return;
        }
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                        .eq(ImMessage::getFromUserId, userId).eq(ImMessage::getToUserId, friendId)
                        .or()
                        .eq(ImMessage::getFromUserId, friendId).eq(ImMessage::getToUserId, userId))
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                .like(ImMessage::getContent, keyword)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + remaining);
        List<ImMessage> rows = messageMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            return;
        }
        Map<Long, SysUser> users = chatMessageHelper.loadUserMap(Set.of(userId, friendId));
        SysUser friend = users.get(friendId);
        String title = friend != null ? (StringUtils.hasText(friend.getNickname()) ? friend.getNickname() : friend.getUsername()) : String.valueOf(friendId);
        for (ImMessage row : rows) {
            if (row.getId() != null && seenMessageIds.add(row.getId())) {
                MessageDTO dto = chatMessageHelper.toMessageDTO(row, ChatConstants.SESSION_TYPE_SINGLE, users, Map.of());
                hits.add(toHit(dto, friendId, title));
            }
        }
    }

    private MessageSearchHitDTO toHit(MessageDTO dto, Long targetId, String sessionTitle) {
        String content = dto.getContent();
        String preview = content;
        if (StringUtils.hasText(content) && content.length() > 120) {
            preview = content.substring(0, 117) + "...";
        }
        return MessageSearchHitDTO.builder()
                .messageId(dto.getId())
                .targetId(targetId)
                .sessionType(dto.getSessionType())
                .sessionTitle(sessionTitle)
                .contentPreview(preview)
                .fromUserId(dto.getFromUserId())
                .fromNickname(dto.getFromNickname())
                .createTime(dto.getCreateTime())
                .build();
    }

    private Set<Long> listSingleChatFriendIds(Long userId) {
        LambdaQueryWrapper<ImSession> w = new LambdaQueryWrapper<>();
        w.eq(ImSession::getUserId, userId).eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_SINGLE);
        LinkedHashSet<Long> ids = new LinkedHashSet<>();
        for (ImSession s : sessionMapper.selectList(w)) {
            if (s.getTargetId() != null) {
                ids.add(s.getTargetId());
            }
        }
        return ids;
    }

    private List<Long> listMyGroupIds(Long userId) {
        LambdaQueryWrapper<ImGroupMember> w = new LambdaQueryWrapper<>();
        w.eq(ImGroupMember::getUserId, userId).select(ImGroupMember::getGroupId);
        return groupMemberMapper.selectList(w).stream()
                .map(ImGroupMember::getGroupId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean canAccessSingle(Long userId, Long targetUserId) {
        if (blacklistService.isBlacklisted(userId, targetUserId) || blacklistService.isBlacklisted(targetUserId, userId)) {
            return false;
        }
        LambdaQueryWrapper<SysFriend> forward = new LambdaQueryWrapper<>();
        forward.eq(SysFriend::getUserId, userId).eq(SysFriend::getFriendId, targetUserId);
        if (friendMapper.selectCount(forward) > 0) {
            return true;
        }
        LambdaQueryWrapper<SysFriend> reverse = new LambdaQueryWrapper<>();
        reverse.eq(SysFriend::getUserId, targetUserId).eq(SysFriend::getFriendId, userId);
        return friendMapper.selectCount(reverse) > 0;
    }

    private String normalizeKeyword(String keyword) {
        try {
            return TextNormalizer.normalizeRequiredSingleLine(keyword, 100, "关键词");
        } catch (Exception e) {
            return null;
        }
    }
}