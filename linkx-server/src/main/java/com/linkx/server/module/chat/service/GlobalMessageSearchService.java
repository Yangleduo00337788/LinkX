package com.linkx.server.module.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.GlobalMessageSearchHitDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalMessageSearchService {

    private static final int MAX_SIZE = 50;
    private static final int PREVIEW_LEN = 120;

    private final ImMessageMapper messageMapper;
    private final ImSessionMapper sessionMapper;
    private final SysUserMapper userMapper;
    private final SysFriendMapper friendMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImGroupInfoMapper groupInfoMapper;

    public List<GlobalMessageSearchHitDTO> search(Long userId, String keyword, int size) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.length() < 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "关键词至少 2 个字符");
        }
        int limit = Math.min(Math.max(size, 1), MAX_SIZE);

        Set<Long> friendIds = loadFriendIds(userId);
        Set<Long> groupIds = loadGroupIds(userId);

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                .like(ImMessage::getContent, kw)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + (limit * 3));

        List<ImMessage> candidates = messageMapper.selectList(wrapper);
        List<GlobalMessageSearchHitDTO> hits = new ArrayList<>();
        Map<Long, SysUser> userCache = new HashMap<>();
        Map<Long, String> groupNameCache = new HashMap<>();

        for (ImMessage msg : candidates) {
            if (hits.size() >= limit) {
                break;
            }
            GlobalMessageSearchHitDTO hit = mapIfAccessible(userId, msg, friendIds, groupIds, userCache, groupNameCache);
            if (hit != null) {
                hits.add(hit);
            }
        }
        return hits;
    }

    private GlobalMessageSearchHitDTO mapIfAccessible(
            Long userId,
            ImMessage msg,
            Set<Long> friendIds,
            Set<Long> groupIds,
            Map<Long, SysUser> userCache,
            Map<Long, String> groupNameCache) {
        int sessionType = resolveSessionType(msg);
        Long targetId;
        String targetLabel;

        if (sessionType == ChatConstants.SESSION_TYPE_GROUP) {
            targetId = msg.getToUserId();
            if (targetId == null || !groupIds.contains(targetId)) {
                return null;
            }
            if (isClearedForUser(userId, targetId, sessionType, msg.getCreateTime())) {
                return null;
            }
            targetLabel = groupNameCache.computeIfAbsent(targetId, id -> {
                ImGroupInfo g = groupInfoMapper.selectById(id);
                return g != null && g.getDeleted() != null && g.getDeleted() == 0 ? g.getGroupName() : "群聊";
            });
        } else {
            Long peer = msg.getFromUserId().equals(userId) ? msg.getToUserId() : msg.getFromUserId();
            if (peer == null || !friendIds.contains(peer)) {
                return null;
            }
            targetId = peer;
            if (isClearedForUser(userId, targetId, sessionType, msg.getCreateTime())) {
                return null;
            }
            SysUser peerUser = userCache.computeIfAbsent(peer, id -> userMapper.selectById(id));
            targetLabel = peerUser != null ? peerUser.getNickname() : String.valueOf(peer);
        }

        SysUser from = userCache.computeIfAbsent(msg.getFromUserId(), id -> userMapper.selectById(id));
        return GlobalMessageSearchHitDTO.builder()
                .messageId(msg.getId())
                .sessionType(sessionType)
                .targetId(targetId)
                .targetLabel(targetLabel)
                .contentPreview(preview(msg.getContent()))
                .msgType(msg.getMsgType())
                .fromUserId(msg.getFromUserId())
                .fromNickname(from != null ? from.getNickname() : null)
                .createTime(msg.getCreateTime())
                .build();
    }

    private boolean isClearedForUser(Long userId, Long targetId, int sessionType, java.time.LocalDateTime msgTime) {
        if (msgTime == null) {
            return false;
        }
        LambdaQueryWrapper<ImSession> w = new LambdaQueryWrapper<>();
        w.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, targetId)
                .eq(ImSession::getSessionType, sessionType);
        ImSession session = sessionMapper.selectOne(w);
        if (session == null || session.getHistoryClearTime() == null) {
            return false;
        }
        return msgTime.isBefore(session.getHistoryClearTime());
    }

    private static int resolveSessionType(ImMessage message) {
        if (message.getSessionId() != null && message.getSessionId().equals(message.getToUserId())) {
            return ChatConstants.SESSION_TYPE_GROUP;
        }
        return ChatConstants.SESSION_TYPE_SINGLE;
    }

    private Set<Long> loadFriendIds(Long userId) {
        Set<Long> ids = new HashSet<>();
        LambdaQueryWrapper<SysFriend> w = new LambdaQueryWrapper<>();
        w.eq(SysFriend::getUserId, userId);
        for (SysFriend f : friendMapper.selectList(w)) {
            if (f.getFriendId() != null) {
                ids.add(f.getFriendId());
            }
        }
        LambdaQueryWrapper<SysFriend> w2 = new LambdaQueryWrapper<>();
        w2.eq(SysFriend::getFriendId, userId);
        for (SysFriend f : friendMapper.selectList(w2)) {
            if (f.getUserId() != null) {
                ids.add(f.getUserId());
            }
        }
        return ids;
    }

    private Set<Long> loadGroupIds(Long userId) {
        LambdaQueryWrapper<ImGroupMember> w = new LambdaQueryWrapper<>();
        w.eq(ImGroupMember::getUserId, userId);
        return groupMemberMapper.selectList(w).stream()
                .map(ImGroupMember::getGroupId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    private static String preview(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String c = content.trim();
        return c.length() <= PREVIEW_LEN ? c : c.substring(0, PREVIEW_LEN) + "…";
    }
}