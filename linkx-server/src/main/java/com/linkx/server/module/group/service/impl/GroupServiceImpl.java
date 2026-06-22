package com.linkx.server.module.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.linkx.server.common.AuditLogService;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.common.TextNormalizer;
import com.linkx.server.common.UploadAssetUrlUtils;
import com.linkx.server.config.LinkxAppProperties;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImGroupRequest;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysFile;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.ws.ChatGroupRealtimeService;
import com.linkx.server.module.group.constant.GroupConstants;
import com.linkx.server.module.group.dto.GroupDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.group.dto.GroupMemberDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;
import com.linkx.server.module.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final int DEFAULT_MAX_MEMBERS = 500;
    private static final int SESSION_PREVIEW_MAX_LENGTH = 500;

    private final ImGroupInfoMapper groupInfoMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImGroupRequestMapper groupRequestMapper;
    private final ImMessageMapper messageMapper;
    private final ImSessionMapper sessionMapper;
    private final SysFileMapper fileMapper;
    private final SysUserMapper userMapper;
    private final ChatGroupRealtimeService chatGroupRealtimeService;
    private final LinkxAppProperties linkxAppProperties;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public GroupDTO createGroup(Long operatorId, String groupName, String groupAvatar, String notice, List<Long> memberIds) {
        String normalizedName = normalizeGroupName(groupName);
        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, operatorId);
        validateUsersExist(normalizedMemberIds);

        ImGroupInfo groupInfo = new ImGroupInfo();
        groupInfo.setGroupName(normalizedName);
        groupInfo.setGroupAvatar(normalizeAvatarUrl(groupAvatar));
        groupInfo.setOwnerId(operatorId);
        groupInfo.setMaxMembers(DEFAULT_MAX_MEMBERS);
        groupInfo.setNotice(normalizeNullableText(notice));
        if (StringUtils.hasText(groupInfo.getNotice())) {
            groupInfo.setNoticeUpdateTime(LocalDateTime.now());
        }
        groupInfo.setDeleted(0);
        groupInfoMapper.insert(groupInfo);

        insertMember(groupInfo.getId(), operatorId, GroupConstants.ROLE_OWNER, groupInfo.getNoticeUpdateTime());
        for (Long memberId : normalizedMemberIds) {
            insertMember(groupInfo.getId(), memberId, GroupConstants.ROLE_MEMBER);
        }

        List<Long> allMembers = new ArrayList<>();
        allMembers.add(operatorId);
        allMembers.addAll(normalizedMemberIds);
        ensureGroupSessions(groupInfo, allMembers);
        Map<Long, SysUser> userMap = loadUserMap(new LinkedHashSet<>(allMembers));
        appendGroupSystemMessage(
                groupInfo.getId(),
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 创建了群聊“" + normalizedName + "”"
        );
        log.info("Group created, groupId={}, operatorId={}, memberCount={}, groupName={}",
                groupInfo.getId(), operatorId, allMembers.size(), normalizedName);

        return buildGroupDTO(groupInfo, requireMember(groupInfo.getId(), operatorId), allMembers.size());
    }

    @Override
    public List<GroupDTO> getMyGroups(Long userId) {
        List<ImGroupMember> myMemberships = listMembersByUserId(userId);
        if (myMemberships.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = myMemberships.stream().map(ImGroupMember::getGroupId).distinct().toList();
        List<ImGroupInfo> groups = listActiveGroups(groupIds);
        Map<Long, ImGroupInfo> groupMap = groups.stream().collect(Collectors.toMap(ImGroupInfo::getId, item -> item, (left, right) -> left));
        Map<Long, Long> memberCountMap = countMembers(groupIds);
        Map<Long, ImGroupMember> myMemberMap = myMemberships.stream()
                .filter(item -> groupMap.containsKey(item.getGroupId()))
                .collect(Collectors.toMap(ImGroupMember::getGroupId, item -> item, (left, right) -> left));

        return groups.stream()
                .sorted(Comparator
                        .comparing(ImGroupInfo::getUpdateTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        .reversed()
                        .thenComparing(
                                ImGroupInfo::getCreateTime,
                                Comparator.nullsLast(LocalDateTime::compareTo).reversed()
                        ))
                .map(group -> buildGroupDTO(group, myMemberMap.get(group.getId()), memberCountMap.getOrDefault(group.getId(), 0L).intValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupRequestDTO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupRequest::getToUserId, userId)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING)
                .orderByDesc(ImGroupRequest::getCreateTime);

        List<ImGroupRequest> requests = groupRequestMapper.selectList(wrapper);
        if (requests.isEmpty()) {
            return List.of();
        }

        Set<Long> groupIds = requests.stream()
                .map(ImGroupRequest::getGroupId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ImGroupInfo> groupMap = listActiveGroups(new ArrayList<>(groupIds)).stream()
                .collect(Collectors.toMap(ImGroupInfo::getId, item -> item, (left, right) -> left));

        Set<Long> userIds = new LinkedHashSet<>();
        for (ImGroupRequest request : requests) {
            if (request.getFromUserId() != null) {
                userIds.add(request.getFromUserId());
            }
            if (request.getToUserId() != null) {
                userIds.add(request.getToUserId());
            }
        }
        Map<Long, SysUser> userMap = loadUserMap(userIds);

        return requests.stream()
                .map(request -> {
                    ImGroupInfo groupInfo = groupMap.get(request.getGroupId());
                    if (groupInfo == null) {
                        return null;
                    }
                    SysUser fromUser = userMap.get(request.getFromUserId());

                    GroupRequestDTO dto = new GroupRequestDTO();
                    dto.setId(request.getId());
                    dto.setGroupId(request.getGroupId());
                    dto.setGroupName(groupInfo.getGroupName());
                    dto.setGroupAvatar(groupInfo.getGroupAvatar());
                    dto.setFromUserId(request.getFromUserId());
                    dto.setToUserId(request.getToUserId());
                    dto.setRequestType(request.getRequestType());
                    dto.setMessage(request.getMessage());
                    dto.setStatus(request.getStatus());
                    dto.setCreateTime(request.getCreateTime());
                    if (fromUser != null) {
                        dto.setFromUsername(fromUser.getUsername());
                        dto.setFromNickname(fromUser.getNickname());
                        dto.setFromAvatar(fromUser.getAvatar());
                    }
                    return dto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void submitJoinRequest(Long userId, Long groupId, String message) {
        requireActiveGroup(groupId);
        if (isGroupMember(groupId, userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已加入该群聊");
        }

        LambdaQueryWrapper<ImGroupRequest> dupWrapper = new LambdaQueryWrapper<>();
        dupWrapper.eq(ImGroupRequest::getGroupId, groupId)
                .eq(ImGroupRequest::getFromUserId, userId)
                .eq(ImGroupRequest::getRequestType, GroupConstants.REQUEST_TYPE_JOIN)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);
        if (groupRequestMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已提交过入群申请，请勿重复提交");
        }

        List<ImGroupMember> admins = listMembersByGroupId(groupId).stream()
                .filter(m -> m.getRole() >= GroupConstants.ROLE_ADMIN)
                .toList();

        String normalizedMessage = normalizeRequestMessage(message);
        for (ImGroupMember admin : admins) {
            ImGroupRequest request = new ImGroupRequest();
            request.setGroupId(groupId);
            request.setFromUserId(userId);
            request.setToUserId(admin.getUserId());
            request.setRequestType(GroupConstants.REQUEST_TYPE_JOIN);
            request.setMessage(normalizedMessage);
            request.setStatus(GroupConstants.REQUEST_STATUS_PENDING);
            groupRequestMapper.insert(request);
        }
    }

    @Override
    @Transactional
    public void inviteMembers(Long operatorId, Long groupId, List<Long> memberIds, String message) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        assertCanManageMembers(operatorMember);

        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, operatorId);
        if (normalizedMemberIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要邀请的成员");
        }
        validateUsersExist(normalizedMemberIds);

        Set<Long> existingUserIds = listMembersByGroupId(groupId).stream()
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toSet());
        String normalizedMessage = normalizeRequestMessage(message);

        for (Long memberUserId : normalizedMemberIds) {
            if (existingUserIds.contains(memberUserId)) {
                continue;
            }

            LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImGroupRequest::getGroupId, groupId)
                    .eq(ImGroupRequest::getToUserId, memberUserId)
                    .eq(ImGroupRequest::getRequestType, GroupConstants.REQUEST_TYPE_INVITE)
                    .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);
            if (groupRequestMapper.selectCount(wrapper) > 0) {
                continue;
            }

            ImGroupRequest request = new ImGroupRequest();
            request.setGroupId(groupId);
            request.setFromUserId(operatorId);
            request.setToUserId(memberUserId);
            request.setRequestType(GroupConstants.REQUEST_TYPE_INVITE);
            request.setMessage(normalizedMessage);
            request.setStatus(GroupConstants.REQUEST_STATUS_PENDING);
            groupRequestMapper.insert(request);
        }
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        ImGroupRequest request = requirePendingRequest(userId, requestId);
        ImGroupInfo groupInfo = lockActiveGroup(request.getGroupId());
        if (!claimPendingRequest(requestId, userId, GroupConstants.REQUEST_STATUS_ACCEPTED)) {
            return;
        }

        if (request.getRequestType() == GroupConstants.REQUEST_TYPE_JOIN) {
            ImGroupMember approverMember = requireMember(groupInfo.getId(), userId);
            assertCanManageMembers(approverMember);
            boolean joined = addMemberIfAbsent(groupInfo, request.getFromUserId());
            if (joined) {
                ensureGroupSessions(groupInfo, List.of(request.getFromUserId()));
                Map<Long, SysUser> userMap = loadUserMap(Set.of(userId, request.getFromUserId()));
                appendGroupSystemMessage(
                        groupInfo.getId(),
                        userId,
                        getUserDisplayName(userId, userMap) + " 同意 " + getUserDisplayName(request.getFromUserId(), userMap) + " 加入了群聊"
                );
            }

            completePendingRequests(
                    groupInfo.getId(),
                    request.getFromUserId(),
                    GroupConstants.REQUEST_TYPE_JOIN,
                    true,
                    GroupConstants.REQUEST_STATUS_REJECTED
            );
        } else if (request.getRequestType() == GroupConstants.REQUEST_TYPE_INVITE) {
            boolean joined = addMemberIfAbsent(groupInfo, userId);
            if (joined) {
                ensureGroupSessions(groupInfo, List.of(userId));
                Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));
                appendGroupSystemMessage(
                        groupInfo.getId(),
                        userId,
                        getUserDisplayName(userId, userMap) + " 接受邀请加入了群聊"
                );
            }
            completePendingRequests(
                    groupInfo.getId(),
                    userId,
                    GroupConstants.REQUEST_TYPE_INVITE,
                    false,
                    GroupConstants.REQUEST_STATUS_ACCEPTED
            );
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的群申请类型");
        }
        log.info("Group request accepted, requestId={}, groupId={}, operatorId={}, requestType={}",
                requestId, groupInfo.getId(), userId, request.getRequestType());
    }

    @Override
    @Transactional
    public void rejectRequest(Long userId, Long requestId) {
        requirePendingRequest(userId, requestId);
        claimPendingRequest(requestId, userId, GroupConstants.REQUEST_STATUS_REJECTED);
    }

    @Override
    public GroupDetailDTO getGroupDetail(Long userId, Long groupId) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        ImGroupMember currentMember = requireMember(groupId, userId);
        List<ImGroupMember> members = listMembersByGroupId(groupId);
        Map<Long, SysUser> userMap = loadUserMap(members.stream().map(ImGroupMember::getUserId).collect(Collectors.toSet()));

        GroupDetailDTO detailDTO = new GroupDetailDTO();
        detailDTO.setId(groupInfo.getId());
        detailDTO.setGroupName(groupInfo.getGroupName());
        detailDTO.setGroupAvatar(groupInfo.getGroupAvatar());
        detailDTO.setGroupRemark(currentMember.getGroupRemark());
        detailDTO.setNotice(groupInfo.getNotice());
        detailDTO.setNoticeUpdateTime(groupInfo.getNoticeUpdateTime());
        detailDTO.setNoticeReadTime(currentMember.getNoticeReadTime());
        detailDTO.setNoticeUnread(hasUnreadNotice(groupInfo, currentMember));
        detailDTO.setOwnerId(groupInfo.getOwnerId());
        detailDTO.setMaxMembers(groupInfo.getMaxMembers());
        detailDTO.setMemberCount(members.size());
        detailDTO.setMyRole(currentMember.getRole());
        detailDTO.setMuted(isMuted(currentMember));
        detailDTO.setMuteTime(currentMember.getMuteTime());
        detailDTO.setNotificationMuted(Boolean.TRUE.equals(currentMember.getNotificationMuted()));
        detailDTO.setCreateTime(groupInfo.getCreateTime());
        detailDTO.setMembers(members.stream()
                .sorted(Comparator.comparing(ImGroupMember::getRole).reversed()
                        .thenComparing(ImGroupMember::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(member -> toMemberDTO(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList()));
        return detailDTO;
    }

    @Override
    @Transactional
    public void addMembers(Long operatorId, Long groupId, List<Long> memberIds) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        assertCanManageMembers(operatorMember);

        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, null);
        if (normalizedMemberIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要添加的成员");
        }
        validateUsersExist(normalizedMemberIds);

        Set<Long> existingUserIds = listMembersByGroupId(groupId).stream()
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toSet());
        List<Long> toAddUserIds = normalizedMemberIds.stream()
                .filter(userId -> !existingUserIds.contains(userId))
                .toList();
        if (toAddUserIds.isEmpty()) {
            return;
        }

        if (existingUserIds.size() + toAddUserIds.size() > groupInfo.getMaxMembers()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员数量已达到上限");
        }

        for (Long userId : toAddUserIds) {
            insertMember(groupId, userId, GroupConstants.ROLE_MEMBER);
        }
        ensureGroupSessions(groupInfo, toAddUserIds);
        Set<Long> relatedUserIds = new LinkedHashSet<>(toAddUserIds);
        relatedUserIds.add(operatorId);
        Map<Long, SysUser> userMap = loadUserMap(relatedUserIds);
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 邀请 " + joinUserNames(toAddUserIds, userMap) + " 加入了群聊"
        );
    }

    @Override
    @Transactional
    public void removeMember(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        ImGroupMember targetMember = requireMember(groupId, memberUserId);

        if (operatorId.equals(memberUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "暂不支持自行退群，请由群主或管理员操作");
        }
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能移除群主");
        }
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以移除成员");
        }
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER && targetMember.getRole() != GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能移除管理员");
        }

        removeGroupMemberData(groupId, memberUserId);

        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 将 " + getUserDisplayName(memberUserId, userMap) + " 移出了群聊"
        );
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, List.of(memberUserId), "REMOVED"));
    }

    @Override
    @Transactional
    public void setAdmin(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        ImGroupMember targetMember = requireMember(groupId, memberUserId);

        assertCanManageAdmin(operatorMember, targetMember, operatorId, memberUserId);
        if (targetMember.getRole() == GroupConstants.ROLE_ADMIN) {
            return;
        }

        targetMember.setRole(GroupConstants.ROLE_ADMIN);
        groupMemberMapper.updateById(targetMember);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 将 " + getUserDisplayName(memberUserId, userMap) + " 设为管理员"
        );
    }

    @Override
    @Transactional
    public void removeAdmin(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        ImGroupMember targetMember = requireMember(groupId, memberUserId);

        assertCanManageAdmin(operatorMember, targetMember, operatorId, memberUserId);
        if (targetMember.getRole() == GroupConstants.ROLE_MEMBER) {
            return;
        }

        targetMember.setRole(GroupConstants.ROLE_MEMBER);
        groupMemberMapper.updateById(targetMember);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 取消了 " + getUserDisplayName(memberUserId, userMap) + " 的管理员身份"
        );
    }

    @Override
    @Transactional
    public void dissolveGroup(Long operatorId, Long groupId) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);
        if (!groupInfo.getOwnerId().equals(operatorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以解散群聊");
        }

        List<Long> memberUserIds = listMembersByGroupId(groupId).stream()
                .map(ImGroupMember::getUserId)
                .toList();

        groupInfo.setDeleted(1);
        groupInfoMapper.updateById(groupInfo);

        LambdaQueryWrapper<ImSession> deleteSessionWrapper = new LambdaQueryWrapper<>();
        deleteSessionWrapper.eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);
        sessionMapper.delete(deleteSessionWrapper);
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, memberUserIds, "DISSOLVED"));
    }

    @Override
    @Transactional
    public void muteMember(Long operatorId, Long groupId, Long memberUserId, Integer muteMinutes) {
        lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        ImGroupMember targetMember = requireMember(groupId, memberUserId);

        assertCanOperateMute(operatorMember, targetMember);
        targetMember.setMuteTime(LocalDateTime.now().plusMinutes(muteMinutes));
        groupMemberMapper.updateById(targetMember);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 禁言了 " + getUserDisplayName(memberUserId, userMap) + " " + muteMinutes + " 分钟"
        );
        auditLogService.recordSuccess("GROUP_MUTE_MEMBER", operatorId, "GROUP_MEMBER", memberUserId,
                "groupId=" + groupId + ", muteMinutes=" + muteMinutes);
        log.info("Group member muted, groupId={}, operatorId={}, memberUserId={}, muteMinutes={}",
                groupId, operatorId, memberUserId, muteMinutes);
    }

    @Override
    @Transactional
    public void unmuteMember(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        ImGroupMember targetMember = requireMember(groupId, memberUserId);

        assertCanOperateMute(operatorMember, targetMember);
        LambdaUpdateWrapper<ImGroupMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ImGroupMember::getId, targetMember.getId())
                .set(ImGroupMember::getMuteTime, null);
        groupMemberMapper.update(null, updateWrapper);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 解除禁言 " + getUserDisplayName(memberUserId, userMap)
        );
        auditLogService.recordSuccess("GROUP_UNMUTE_MEMBER", operatorId, "GROUP_MEMBER", memberUserId, "groupId=" + groupId);
        log.info("Group member unmuted, groupId={}, operatorId={}, memberUserId={}",
                groupId, operatorId, memberUserId);
    }

    @Override
    @Transactional
    public void updateProfile(Long operatorId, Long groupId, String groupName, String groupAvatar) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以修改群资料");
        }

        String oldGroupName = groupInfo.getGroupName();
        String oldGroupAvatar = groupInfo.getGroupAvatar();
        String normalizedGroupName = normalizeGroupName(groupName);
        String normalizedGroupAvatar = normalizeAvatarUrl(groupAvatar);
        if (normalizedGroupName.equals(oldGroupName) && equalsNullableText(normalizedGroupAvatar, oldGroupAvatar)) {
            return;
        }

        groupInfo.setGroupName(normalizedGroupName);
        groupInfo.setGroupAvatar(normalizedGroupAvatar);
        groupInfoMapper.updateById(groupInfo);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                buildProfileUpdatedSystemMessage(getUserDisplayName(operatorId, userMap), oldGroupName, normalizedGroupName, oldGroupAvatar, normalizedGroupAvatar)
        );
        log.info("Group profile updated, groupId={}, operatorId={}, oldGroupName={}, newGroupName={}",
                groupId, operatorId, oldGroupName, normalizedGroupName);
    }

    @Override
    @Transactional
    public void updateNotice(Long operatorId, Long groupId, String notice) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以修改群公告");
        }

        String normalizedNotice = TextNormalizer.normalizeOptionalMultiline(notice, 1000, "群公告");
        if (normalizedNotice == null) {
            normalizedNotice = "";
        }
        String previousNotice = normalizeNullableText(groupInfo.getNotice());
        String nextNotice = normalizeNullableText(normalizedNotice);
        if (equalsNullableText(previousNotice, nextNotice)) {
            return;
        }

        LocalDateTime noticeUpdateTime = LocalDateTime.now();
        groupInfo.setNotice(normalizedNotice);
        groupInfo.setNoticeUpdateTime(noticeUpdateTime);
        groupInfoMapper.updateById(groupInfo);
        operatorMember.setNoticeReadTime(noticeUpdateTime);
        groupMemberMapper.updateById(operatorMember);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + (StringUtils.hasText(nextNotice) ? " 更新了群公告" : " 清空了群公告")
        );
        log.info("Group notice updated, groupId={}, operatorId={}, noticeLength={}",
                groupId, operatorId, nextNotice == null ? 0 : nextNotice.length());
    }

    @Override
    @Transactional
    public void updatePreferences(Long userId, Long groupId, String groupRemark, Boolean notificationMuted) {
        requireActiveGroup(groupId);
        ImGroupMember member = requireMember(groupId, userId);
        String normalizedRemark = normalizeGroupRemark(groupRemark);
        boolean nextNotificationMuted = Boolean.TRUE.equals(notificationMuted);
        boolean changed = !Objects.equals(normalizedRemark, normalizeNullableText(member.getGroupRemark()))
                || nextNotificationMuted != Boolean.TRUE.equals(member.getNotificationMuted());
        if (!changed) {
            return;
        }
        member.setGroupRemark(normalizedRemark);
        member.setNotificationMuted(nextNotificationMuted);
        groupMemberMapper.updateById(member);
        executeAfterCommit(() -> {
            chatGroupRealtimeService.pushGroupSessions(groupId, List.of(userId));
            chatGroupRealtimeService.pushGroupDetails(groupId, List.of(userId));
        });
    }

    @Override
    @Transactional
    public void markNoticeRead(Long userId, Long groupId) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        ImGroupMember member = requireMember(groupId, userId);
        if (!StringUtils.hasText(groupInfo.getNotice()) || groupInfo.getNoticeUpdateTime() == null) {
            return;
        }
        if (!hasUnreadNotice(groupInfo, member)) {
            return;
        }
        member.setNoticeReadTime(LocalDateTime.now());
        groupMemberMapper.updateById(member);
        executeAfterCommit(() -> {
            chatGroupRealtimeService.pushGroupSessions(groupId, List.of(userId));
            chatGroupRealtimeService.pushGroupDetails(groupId, List.of(userId));
        });
    }

    @Override
    public List<MessageDTO> getGroupMedia(Long userId, Long groupId, String mediaType, String keyword, int size) {
        requireActiveGroup(groupId);
        requireMember(groupId, userId);

        List<Integer> messageTypes = resolveMediaMessageTypes(mediaType);
        List<ImMessage> messages;
        if (StringUtils.hasText(keyword)) {
            Map<String, SysFile> matchedFileMap = loadMatchedFileMap(keyword.trim());
            if (matchedFileMap.isEmpty()) {
                return List.of();
            }
            LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ImMessage::getToUserId, groupId)
                    .eq(ImMessage::getSessionId, groupId)
                    .in(ImMessage::getMsgType, messageTypes)
                    .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                    .in(ImMessage::getContent, matchedFileMap.keySet())
                    .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                    .last("LIMIT " + size);
            messages = messageMapper.selectList(wrapper);
            return toMessageList(messages, ChatConstants.SESSION_TYPE_GROUP, matchedFileMap);
        }

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImMessage::getToUserId, groupId)
                .eq(ImMessage::getSessionId, groupId)
                .in(ImMessage::getMsgType, messageTypes)
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size);
        messages = messageMapper.selectList(wrapper);
        return toMessageList(messages, ChatConstants.SESSION_TYPE_GROUP, loadFileMap(messages));
    }

    @Override
    public List<MessageDTO> searchGroupMessages(Long userId, Long groupId, String keyword, int size) {
        requireActiveGroup(groupId);
        requireMember(groupId, userId);

        String normalizedKeyword = normalizeSearchKeyword(keyword);
        if (normalizedKeyword == null) {
            return List.of();
        }

        LinkedHashSet<Long> matchedMessageIds = new LinkedHashSet<>();

        LambdaQueryWrapper<ImMessage> textWrapper = new LambdaQueryWrapper<>();
        textWrapper.eq(ImMessage::getToUserId, groupId)
                .eq(ImMessage::getSessionId, groupId)
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                .like(ImMessage::getContent, normalizedKeyword)
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size);
        messageMapper.selectList(textWrapper).stream()
                .map(ImMessage::getId)
                .forEach(matchedMessageIds::add);

        if (matchedMessageIds.size() < size) {
            Map<String, SysFile> matchedFileMap = loadMatchedFileMap(normalizedKeyword);
            if (!matchedFileMap.isEmpty()) {
                LambdaQueryWrapper<ImMessage> fileWrapper = new LambdaQueryWrapper<>();
                fileWrapper.eq(ImMessage::getToUserId, groupId)
                        .eq(ImMessage::getSessionId, groupId)
                        .in(ImMessage::getMsgType, List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE))
                        .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                        .in(ImMessage::getContent, matchedFileMap.keySet())
                        .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                        .last("LIMIT " + size);
                messageMapper.selectList(fileWrapper).stream()
                        .map(ImMessage::getId)
                        .forEach(matchedMessageIds::add);
            }
        }

        if (matchedMessageIds.isEmpty()) {
            return List.of();
        }

        List<ImMessage> matchedMessages = messageMapper.selectBatchIds(matchedMessageIds);
        matchedMessages.sort(this::compareMessagesByNewest);
        if (matchedMessages.size() > size) {
            matchedMessages = new ArrayList<>(matchedMessages.subList(0, size));
        }
        return toMessageList(matchedMessages, ChatConstants.SESSION_TYPE_GROUP, loadFileMap(matchedMessages));
    }

    private GroupDTO buildGroupDTO(ImGroupInfo groupInfo, ImGroupMember currentMember, int memberCount) {
        GroupDTO dto = new GroupDTO();
        dto.setId(groupInfo.getId());
        dto.setGroupName(groupInfo.getGroupName());
        dto.setGroupAvatar(groupInfo.getGroupAvatar());
        dto.setNotice(groupInfo.getNotice());
        dto.setOwnerId(groupInfo.getOwnerId());
        dto.setMaxMembers(groupInfo.getMaxMembers());
        dto.setMemberCount(memberCount);
        dto.setMyRole(currentMember != null ? currentMember.getRole() : null);
        dto.setMuted(currentMember != null && isMuted(currentMember));
        dto.setMuteTime(currentMember != null ? currentMember.getMuteTime() : null);
        dto.setCreateTime(groupInfo.getCreateTime());
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

    private void assertCanManageMembers(ImGroupMember operatorMember) {
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以管理成员");
        }
    }

    private void assertCanManageAdmin(ImGroupMember operatorMember, ImGroupMember targetMember, Long operatorId, Long memberUserId) {
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以设置管理员");
        }
        if (operatorId.equals(memberUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能操作自己的管理员身份");
        }
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作群主角色");
        }
    }

    private void assertCanOperateMute(ImGroupMember operatorMember, ImGroupMember targetMember) {
        assertCanManageMembers(operatorMember);
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁言群主");
        }
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER && targetMember.getRole() != GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能禁言管理员");
        }
    }

    private void ensureGroupSessions(ImGroupInfo groupInfo, Collection<Long> userIds) {
        for (Long userId : userIds) {
            ImSession existing = getOrCreateGroupSession(userId, groupInfo.getId());
            if (existing.getLastMessageTime() != null) {
                continue;
            }
            existing.setLastMessage(null);
            existing.setLastMessageTime(groupInfo.getCreateTime());
            sessionMapper.updateById(existing);
        }
    }

    private void insertMember(Long groupId, Long userId, Integer role) {
        insertMember(groupId, userId, role, null);
    }

    private void insertMember(Long groupId, Long userId, Integer role, LocalDateTime noticeReadTime) {
        ImGroupMember member = new ImGroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role);
        member.setNoticeReadTime(noticeReadTime);
        member.setNotificationMuted(false);
        groupMemberMapper.insert(member);
    }

    private ImGroupRequest requirePendingRequest(Long userId, Long requestId) {
        ImGroupRequest request = groupRequestMapper.selectById(requestId);
        if (request == null || !Objects.equals(request.getToUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群申请不存在");
        }
        if (!Objects.equals(request.getStatus(), GroupConstants.REQUEST_STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该群申请已处理");
        }
        return request;
    }

    private void completePendingRequests(Long groupId, Long userId, Integer requestType, boolean matchFromUserId, int status) {
        LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupRequest::getGroupId, groupId)
                .eq(ImGroupRequest::getRequestType, requestType)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);
        if (matchFromUserId) {
            wrapper.eq(ImGroupRequest::getFromUserId, userId);
        } else {
            wrapper.eq(ImGroupRequest::getToUserId, userId);
        }
        List<ImGroupRequest> pendingRequests = groupRequestMapper.selectList(wrapper);
        if (pendingRequests.isEmpty()) {
            return;
        }
        LocalDateTime handleTime = LocalDateTime.now();
        for (ImGroupRequest pendingRequest : pendingRequests) {
            pendingRequest.setStatus(status);
            pendingRequest.setHandleTime(handleTime);
            groupRequestMapper.updateById(pendingRequest);
        }
    }

    private boolean addMemberIfAbsent(ImGroupInfo groupInfo, Long userId) {
        if (isGroupMember(groupInfo.getId(), userId)) {
            return false;
        }
        if (listMembersByGroupId(groupInfo.getId()).size() >= groupInfo.getMaxMembers()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员数量已达到上限");
        }
        try {
            insertMember(groupInfo.getId(), userId, GroupConstants.ROLE_MEMBER);
            return true;
        } catch (DuplicateKeyException ignored) {
            return false;
        }
    }

    private boolean isGroupMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        return groupMemberMapper.selectCount(wrapper) > 0;
    }

    private List<Long> normalizeMemberIds(List<Long> memberIds, Long excludeUserId) {
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        for (Long memberId : memberIds) {
            if (memberId == null) {
                continue;
            }
            if (excludeUserId != null && excludeUserId.equals(memberId)) {
                continue;
            }
            result.add(memberId);
        }
        return new ArrayList<>(result);
    }

    private void validateUsersExist(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        List<SysUser> users = userMapper.selectBatchIds(userIds);
        if (users.size() != userIds.size()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
    }

    private String normalizeGroupName(String groupName) {
        return TextNormalizer.normalizeRequiredSingleLine(groupName, 100, "群名称");
    }

    private String normalizeNullableText(String value) {
        return UploadAssetUrlUtils.normalizeNullableText(value);
    }

    private String normalizeAvatarUrl(String rawAvatarUrl) {
        return UploadAssetUrlUtils.normalizeAvatarUrl(rawAvatarUrl, linkxAppProperties.getUpload().getUrl(), "群头像");
    }

    private String normalizeGroupRemark(String groupRemark) {
        return TextNormalizer.normalizeOptionalSingleLine(groupRemark, 100, "群备注");
    }

    private String normalizeSearchKeyword(String keyword) {
        return TextNormalizer.normalizeOptionalSingleLine(keyword, 100, "搜索关键词");
    }

    private String normalizeRequestMessage(String message) {
        return TextNormalizer.normalizeOptionalMultiline(message, 255, "申请说明");
    }

    private ImGroupInfo requireActiveGroup(Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群聊不存在");
        }
        return groupInfo;
    }

    private ImGroupInfo lockActiveGroup(Long groupId) {
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupInfo::getId, groupId)
                .eq(ImGroupInfo::getDeleted, 0)
                .last("LIMIT 1 FOR UPDATE");
        ImGroupInfo groupInfo = groupInfoMapper.selectOne(wrapper);
        if (groupInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群聊不存在");
        }
        return groupInfo;
    }

    private ImGroupMember requireMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        ImGroupMember member = groupMemberMapper.selectOne(wrapper);
        if (member == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你不在该群聊中");
        }
        return member;
    }

    private List<ImGroupMember> listMembersByGroupId(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getGroupId, groupId);
        return groupMemberMapper.selectList(wrapper);
    }

    private List<ImGroupMember> listMembersByUserId(Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupMember::getUserId, userId);
        return groupMemberMapper.selectList(wrapper);
    }

    private List<ImGroupInfo> listActiveGroups(List<Long> groupIds) {
        if (groupIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ImGroupInfo::getId, groupIds)
                .eq(ImGroupInfo::getDeleted, 0);
        return groupInfoMapper.selectList(wrapper);
    }

    private Map<Long, Long> countMembers(List<Long> groupIds) {
        Map<Long, Long> countMap = new HashMap<>();
        if (groupIds.isEmpty()) {
            return countMap;
        }
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ImGroupMember::getGroupId, groupIds);
        for (ImGroupMember member : groupMemberMapper.selectList(wrapper)) {
            countMap.merge(member.getGroupId(), 1L, Long::sum);
        }
        return countMap;
    }

    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        lockActiveGroup(groupId);
        ImGroupMember member = requireMember(groupId, userId);

        if (member.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群主不能退群，请先转让群主或解散群聊");
        }

        removeGroupMemberData(groupId, userId);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));
        appendGroupSystemMessage(
                groupId,
                userId,
                getUserDisplayName(userId, userMap) + " 退出了群聊"
        );
        auditLogService.recordSuccess("GROUP_LEAVE", userId, "GROUP", groupId, "");
        log.info("Group left, groupId={}, userId={}", groupId, userId);
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, List.of(userId), "LEFT"));
    }

    @Override
    @Transactional
    public void transferOwner(Long operatorId, Long groupId, Long newOwnerId) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);

        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以转让群主");
        }
        if (operatorId.equals(newOwnerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将群主转让给自己");
        }

        ImGroupMember newOwnerMember = requireMember(groupId, newOwnerId);

        operatorMember.setRole(GroupConstants.ROLE_MEMBER);
        groupMemberMapper.updateById(operatorMember);

        newOwnerMember.setRole(GroupConstants.ROLE_OWNER);
        groupMemberMapper.updateById(newOwnerMember);

        groupInfo.setOwnerId(newOwnerId);
        groupInfoMapper.updateById(groupInfo);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, newOwnerId));
        appendGroupSystemMessage(
                groupId,
                operatorId,
                getUserDisplayName(operatorId, userMap) + " 将群主转让给了 " + getUserDisplayName(newOwnerId, userMap)
        );
        auditLogService.recordSuccess("GROUP_TRANSFER_OWNER", operatorId, "GROUP", groupId, "newOwnerId=" + newOwnerId);
        log.info("Group owner transferred, groupId={}, operatorId={}, newOwnerId={}", groupId, operatorId, newOwnerId);
    }

    private boolean isMuted(ImGroupMember member) {
        return member.getMuteTime() != null && member.getMuteTime().isAfter(LocalDateTime.now());
    }

    private void appendGroupSystemMessage(Long groupId, Long operatorId, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        List<ImGroupMember> members = listMembersByGroupId(groupId);
        if (members.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        ImMessage message = new ImMessage();
        message.setSessionId(groupId);
        message.setFromUserId(operatorId);
        message.setToUserId(groupId);
        message.setContent(content);
        message.setMsgType(ChatConstants.MESSAGE_TYPE_SYSTEM);
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);
        messageMapper.insert(message);
        List<Long> memberUserIds = members.stream()
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toList());

        for (ImGroupMember member : members) {
            ImSession session = getOrCreateGroupSession(member.getUserId(), groupId);
            session.setLastMessage(truncateSessionPreview(content));
            session.setLastMessageTime(now);
            if (!member.getUserId().equals(operatorId)) {
                session.setUnreadCount((session.getUnreadCount() == null ? 0 : session.getUnreadCount()) + 1);
            }
            sessionMapper.updateById(session);
        }
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupMutation(groupId, message.getId(), memberUserIds));
    }

    private ImSession getOrCreateGroupSession(Long userId, Long groupId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);
        ImSession session = sessionMapper.selectOne(wrapper);
        if (session != null) {
            return session;
        }

        session = new ImSession();
        session.setUserId(userId);
        session.setTargetId(groupId);
        session.setSessionType(ChatConstants.SESSION_TYPE_GROUP);
        session.setUnreadCount(0);
        try {
            sessionMapper.insert(session);
            return session;
        } catch (DuplicateKeyException exception) {
            ImSession existingSession = sessionMapper.selectOne(wrapper);
            if (existingSession != null) {
                return existingSession;
            }
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "群会话初始化失败");
        }
    }

    private String getUserDisplayName(Long userId, Map<Long, SysUser> userMap) {
        SysUser user = userMap.get(userId);
        if (user == null) {
            return "成员";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return "成员";
    }

    private String joinUserNames(List<Long> userIds, Map<Long, SysUser> userMap) {
        return userIds.stream()
                .map(userId -> getUserDisplayName(userId, userMap))
                .collect(Collectors.joining("、"));
    }

    private boolean equalsNullableText(String left, String right) {
        return Objects.equals(normalizeNullableText(left), normalizeNullableText(right));
    }

    private boolean hasUnreadNotice(ImGroupInfo groupInfo, ImGroupMember member) {
        if (groupInfo == null || member == null) {
            return false;
        }
        if (!StringUtils.hasText(groupInfo.getNotice()) || groupInfo.getNoticeUpdateTime() == null) {
            return false;
        }
        LocalDateTime noticeReadTime = member.getNoticeReadTime();
        return noticeReadTime == null || noticeReadTime.isBefore(groupInfo.getNoticeUpdateTime());
    }

    private List<Integer> resolveMediaMessageTypes(String mediaType) {
        String normalizedMediaType = normalizeNullableText(mediaType);
        if (normalizedMediaType == null || "all".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE);
        }
        if ("image".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_IMAGE);
        }
        if ("file".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_FILE);
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的媒体类型");
    }

    private Map<String, SysFile> loadMatchedFileMap(String keyword) {
        String normalizedKeyword = normalizeSearchKeyword(keyword);
        if (normalizedKeyword == null) {
            return Map.of();
        }
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(SysFile::getOriginalName, normalizedKeyword)
                .or()
                .like(SysFile::getFileType, normalizedKeyword);
        return fileMapper.selectList(wrapper).stream()
                .filter(file -> StringUtils.hasText(file.getFileUrl()))
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));
    }

    private boolean claimPendingRequest(Long requestId, Long userId, int targetStatus) {
        LambdaUpdateWrapper<ImGroupRequest> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ImGroupRequest::getId, requestId)
                .eq(ImGroupRequest::getToUserId, userId)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING)
                .set(ImGroupRequest::getStatus, targetStatus)
                .set(ImGroupRequest::getHandleTime, LocalDateTime.now());
        return groupRequestMapper.update(null, wrapper) > 0;
    }

    private Map<String, SysFile> loadFileMap(List<ImMessage> messages) {
        Set<String> fileUrls = messages.stream()
                .filter(message -> (message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE)
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

    private List<MessageDTO> toMessageList(List<ImMessage> rawMessages, Integer sessionType, Map<String, SysFile> fileMap) {
        if (rawMessages == null || rawMessages.isEmpty()) {
            return List.of();
        }
        List<ImMessage> messages = new ArrayList<>(rawMessages);
        messages.sort(this::compareMessagesByNewest);
        Set<Long> userIds = messages.stream()
                .map(ImMessage::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, SysUser> userMap = loadUserMap(userIds);
        return messages.stream()
                .map(message -> toMessageDTO(message, sessionType, userMap, fileMap))
                .collect(Collectors.toList());
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
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));
        dto.setMentionUserIds(parseMentionUserIds(message.getMentionUserIds()));
        dto.setMentionDisplayNames(List.of());
        dto.setStatus(message.getStatus());
        dto.setReadTime(message.getReadTime());
        dto.setCreateTime(message.getCreateTime());
        SysUser fromUser = userMap.get(message.getFromUserId());
        if (fromUser != null) {
            dto.setFromNickname(fromUser.getNickname());
            dto.setFromAvatar(fromUser.getAvatar());
        }
        SysFile file = fileMap.get(message.getContent());
        if (file != null) {
            dto.setFileName(file.getOriginalName());
            dto.setFileSize(file.getFileSize());
            dto.setFileType(file.getFileType());
        }
        return dto;
    }

    private int compareMessagesByNewest(ImMessage left, ImMessage right) {
        LocalDateTime leftTime = left != null ? left.getCreateTime() : null;
        LocalDateTime rightTime = right != null ? right.getCreateTime() : null;
        if (leftTime == null && rightTime != null) {
            return 1;
        }
        if (leftTime != null && rightTime == null) {
            return -1;
        }
        if (leftTime != null && rightTime != null) {
            int timeCompare = rightTime.compareTo(leftTime);
            if (timeCompare != 0) {
                return timeCompare;
            }
        }

        Long leftId = left != null ? left.getId() : null;
        Long rightId = right != null ? right.getId() : null;
        if (leftId == null && rightId != null) {
            return 1;
        }
        if (leftId != null && rightId == null) {
            return -1;
        }
        if (leftId == null) {
            return 0;
        }
        return rightId.compareTo(leftId);
    }

    private List<Long> parseMentionUserIds(String mentionUserIds) {
        if (!StringUtils.hasText(mentionUserIds)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String item : mentionUserIds.split(",")) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            try {
                result.add(Long.parseLong(item.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy values when reading search results.
            }
        }
        return result;
    }

    private String buildProfileUpdatedSystemMessage(
            String operatorName,
            String oldGroupName,
            String newGroupName,
            String oldGroupAvatar,
            String newGroupAvatar
    ) {
        boolean nameChanged = !equalsNullableText(oldGroupName, newGroupName);
        boolean avatarChanged = !equalsNullableText(oldGroupAvatar, newGroupAvatar);
        if (nameChanged && avatarChanged) {
            return operatorName + " 更新了群名称和群头像";
        }
        if (nameChanged) {
            return operatorName + " 将群名称修改为“" + newGroupName + "”";
        }
        return operatorName + " 更新了群头像";
    }

    private String truncateSessionPreview(String preview) {
        if (!StringUtils.hasText(preview)) {
            return preview;
        }
        if (preview.length() <= SESSION_PREVIEW_MAX_LENGTH) {
            return preview;
        }
        return preview.substring(0, SESSION_PREVIEW_MAX_LENGTH);
    }

    private void removeGroupMemberData(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> deleteMemberWrapper = new LambdaQueryWrapper<>();
        deleteMemberWrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);
        groupMemberMapper.delete(deleteMemberWrapper);

        LambdaQueryWrapper<ImSession> deleteSessionWrapper = new LambdaQueryWrapper<>();
        deleteSessionWrapper.eq(ImSession::getUserId, userId)
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);
        sessionMapper.delete(deleteSessionWrapper);
    }

    private void executeAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    @Override
    public List<GroupDTO> searchGroups(Long userId, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String trimmedKeyword = keyword.trim();

        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupInfo::getDeleted, 0);

        Long parsedId;
        try {
            parsedId = Long.parseLong(trimmedKeyword);
        } catch (NumberFormatException ignored) {
            parsedId = null;
        }

        final Long searchId = parsedId;
        if (searchId != null) {
            wrapper.and(w -> w.eq(ImGroupInfo::getId, searchId)
                    .or().like(ImGroupInfo::getGroupName, trimmedKeyword));
        } else {
            wrapper.like(ImGroupInfo::getGroupName, trimmedKeyword);
        }
        wrapper.last("LIMIT 20");

        List<ImGroupInfo> groups = groupInfoMapper.selectList(wrapper);
        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream().map(ImGroupInfo::getId).toList();
        Map<Long, Long> memberCountMap = countMembers(groupIds);
        List<ImGroupMember> myMemberships = listMembersByUserId(userId);
        Map<Long, ImGroupMember> myMemberMap = myMemberships.stream()
                .filter(item -> groupIds.contains(item.getGroupId()))
                .collect(Collectors.toMap(ImGroupMember::getGroupId, item -> item, (left, right) -> left));

        return groups.stream()
                .map(group -> buildGroupDTO(group, myMemberMap.get(group.getId()), memberCountMap.getOrDefault(group.getId(), 0L).intValue()))
                .collect(Collectors.toList());
    }
}
