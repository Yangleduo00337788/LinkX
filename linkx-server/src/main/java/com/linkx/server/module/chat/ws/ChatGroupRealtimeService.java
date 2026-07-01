package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.entity.ImGroupInfo;  // 行注：引入 ImGroupInfo 类型
import com.linkx.server.entity.ImGroupMember;  // 行注：引入 ImGroupMember 类型
import com.linkx.server.entity.ImMessage;  // 行注：引入 ImMessage 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.ImGroupInfoMapper;  // 行注：引入 ImGroupInfoMapper 类型
import com.linkx.server.mapper.ImGroupMemberMapper;  // 行注：引入 ImGroupMemberMapper 类型
import com.linkx.server.mapper.ImMessageMapper;  // 行注：引入 ImMessageMapper 类型
import com.linkx.server.mapper.ImSessionMapper;  // 行注：引入 ImSessionMapper 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.chat.dto.ChatSessionDTO;  // 行注：引入 ChatSessionDTO 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import com.linkx.server.module.group.dto.GroupDetailDTO;  // 行注：引入 GroupDetailDTO 类型
import com.linkx.server.module.group.dto.GroupMemberDTO;  // 行注：引入 GroupMemberDTO 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.Collection;  // 行注：引入 Collection 类型
import java.util.LinkedHashSet;  // 行注：引入 LinkedHashSet 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Map;  // 行注：引入 Map 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 群相关实时推送：群详情变更、成员变动、群会话列表刷新等，推送给群内在线成员。
 */
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatGroupRealtimeService 类
public class ChatGroupRealtimeService {

    private final ImGroupInfoMapper groupInfoMapper;  // 行注：注入群信息Mapper依赖
    private final ImGroupMemberMapper groupMemberMapper;  // 行注：注入群成员Mapper依赖
    private final ImMessageMapper messageMapper;  // 行注：注入消息Mapper依赖
    private final ImSessionMapper sessionMapper;  // 行注：注入会话Mapper依赖
    private final SysFileMapper sysFileMapper;  // 行注：注入系统文件Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final ChatEventPushService chatEventPushService;  // 行注：注入聊天事件推送服务依赖

    /**
     * 推送群成员变更事件。
     *
     * @param groupId 群 ID
     * @param messageId 消息Id
     * @param userIds 用户 ID 列表
     */
    // 行注：定义推送群Mutation方法
    public void pushGroupMutation(Long groupId, Long messageId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);  // 行注：初始化规范化后的用户ID列表
        // 行注：判断是否满足当前条件
        if (groupId == null || messageId == null || normalizedUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        MessageDTO message = buildMessage(messageId);  // 行注：初始化消息
        // 行注：判断是否满足当前条件
        if (message != null) {
            chatEventPushService.sendToUsers(normalizedUserIds, ChatEventType.MESSAGE, new ChatMessagePayload(message));  // 行注：调用发送转为Users
        }  // 行注：结束当前代码块

        pushGroupSessions(groupId, normalizedUserIds);  // 行注：调用推送群会话列表
        pushGroupDetails(groupId, normalizedUserIds);  // 行注：调用推送群Details
    }  // 行注：结束当前代码块

    /**
     * 推送群详情变更事件。
     *
     * @param groupId 群 ID
     * @param userIds 用户 ID 列表
     */
    // 行注：定义推送群Details方法
    public void pushGroupDetails(Long groupId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);  // 行注：初始化规范化后的用户ID列表
        // 行注：判断是否满足当前条件
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (Long userId : normalizedUserIds) {
            GroupDetailDTO detail = buildGroupDetail(userId, groupId);  // 行注：初始化详情
            // 行注：判断是否满足当前条件
            if (detail == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            chatEventPushService.sendToUser(userId, ChatEventType.GROUP_DETAIL, new ChatGroupDetailPayload(detail));  // 行注：调用发送转为用户
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 推送群会话。
     *
     * @param groupId 群 ID
     * @param userIds 用户 ID 列表
     */
    // 行注：定义推送群会话列表方法
    public void pushGroupSessions(Long groupId, Collection<Long> userIds) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);  // 行注：初始化规范化后的用户ID列表
        // 行注：判断是否满足当前条件
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：遍历当前集合或范围
        for (Long userId : normalizedUserIds) {
            ChatSessionDTO session = buildGroupSession(userId, groupId);  // 行注：初始化会话
            // 行注：判断是否满足当前条件
            if (session == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            chatEventPushService.sendToUser(userId, ChatEventType.SESSION, new ChatSessionPayload(session));  // 行注：调用发送转为用户
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 推送群Removed。
     *
     * @param groupId 群 ID
     * @param userIds 用户 ID 列表
     * @param reason reason
     */
    // 行注：定义推送群Removed方法
    public void pushGroupRemoved(Long groupId, Collection<Long> userIds, String reason) {
        Set<Long> normalizedUserIds = normalizeUserIds(userIds);  // 行注：初始化规范化后的用户ID列表
        // 行注：判断是否满足当前条件
        if (groupId == null || normalizedUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：调用发送转为Users
        chatEventPushService.sendToUsers(normalizedUserIds, ChatEventType.GROUP_REMOVED, new ChatGroupRemovedPayload(groupId, reason));
    }  // 行注：结束当前代码块

    // 行注：定义构建消息方法
    private MessageDTO buildMessage(Long messageId) {
        ImMessage message = messageMapper.selectById(messageId);  // 行注：初始化消息
        // 行注：判断是否满足当前条件
        if (message == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<Long> mentionUserIds = parseMentionUserIds(message.getMentionUserIds());  // 行注：初始化@提醒用户ID列表
        Set<Long> relatedUserIds = new LinkedHashSet<>();  // 行注：初始化related用户ID列表
        relatedUserIds.add(message.getFromUserId());  // 行注：调用添加
        relatedUserIds.addAll(mentionUserIds);  // 行注：调用添加全部
        Map<Long, SysUser> userMap = loadUserMap(relatedUserIds);  // 行注：初始化用户映射
        SysUser fromUser = userMap.get(message.getFromUserId());  // 行注：初始化用户
        ImGroupMember senderMember = getMember(message.getToUserId(), message.getFromUserId());
        MessageDTO dto = new MessageDTO();  // 行注：初始化DTO
        dto.setId(message.getId());  // 行注：调用设置ID
        dto.setSessionId(message.getSessionId());  // 行注：调用设置会话ID
        dto.setFromUserId(message.getFromUserId());  // 行注：调用设置用户ID
        dto.setFromNickname(resolveGroupSenderDisplayName(senderMember, fromUser));
        dto.setFromAvatar(fromUser != null ? fromUser.getAvatar() : null);  // 行注：执行初始化操作
        dto.setToUserId(message.getToUserId());  // 行注：调用设置转为用户ID
        dto.setSessionType(ChatConstants.SESSION_TYPE_GROUP);  // 行注：调用设置会话类型
        dto.setContent(message.getContent());  // 行注：调用设置内容
        dto.setMsgType(message.getMsgType());  // 行注：调用设置消息类型
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));  // 行注：调用设置@提醒全部
        dto.setMentionUserIds(mentionUserIds);  // 行注：调用设置@提醒用户ID列表
        dto.setMentionDisplayNames(resolveMentionDisplayNames(mentionUserIds, userMap));  // 行注：调用设置@提醒DisplayNames
        SysFile file = resolveFile(message);  // 行注：初始化文件
        dto.setFileName(file != null ? file.getOriginalName() : null);  // 行注：执行初始化操作
        dto.setFileSize(file != null ? file.getFileSize() : null);  // 行注：执行初始化操作
        dto.setFileType(file != null ? file.getFileType() : null);  // 行注：执行初始化操作
        dto.setStatus(message.getStatus());  // 行注：调用设置状态
        dto.setReadTime(message.getReadTime());  // 行注：调用设置已读时间
        dto.setCreateTime(message.getCreateTime());  // 行注：调用设置创建时间
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建群详情方法
    private GroupDetailDTO buildGroupDetail(Long userId, Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);  // 行注：初始化群信息
        // 行注：判断是否满足当前条件
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ImGroupMember currentMember = getMember(groupId, userId);  // 行注：初始化当前成员
        // 行注：判断是否满足当前条件
        if (currentMember == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        List<ImGroupMember> members = listMembersByGroupId(groupId);  // 行注：初始化members
        // 行注：初始化用户映射
        Map<Long, SysUser> userMap = loadUserMap(members.stream().map(ImGroupMember::getUserId).collect(Collectors.toSet()));

        GroupDetailDTO detailDTO = new GroupDetailDTO();  // 行注：初始化详情DTO
        detailDTO.setId(groupInfo.getId());  // 行注：调用设置ID
        detailDTO.setGroupName(groupInfo.getGroupName());  // 行注：调用设置群名称
        detailDTO.setGroupAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置群头像
        detailDTO.setGroupRemark(currentMember.getGroupRemark());  // 行注：调用设置群Remark
        detailDTO.setNotice(groupInfo.getNotice());  // 行注：调用设置Notice
        detailDTO.setNoticeUpdateTime(groupInfo.getNoticeUpdateTime());  // 行注：调用设置Notice更新时间
        detailDTO.setNoticeReadTime(currentMember.getNoticeReadTime());  // 行注：调用设置Notice已读时间
        detailDTO.setNoticeUnread(hasUnreadNotice(groupInfo, currentMember));  // 行注：调用设置Notice未读
        detailDTO.setOwnerId(groupInfo.getOwnerId());  // 行注：调用设置所有者ID
        detailDTO.setMaxMembers(groupInfo.getMaxMembers());  // 行注：调用设置最大Members
        detailDTO.setMemberCount(members.size());  // 行注：调用设置成员数量
        detailDTO.setMyRole(currentMember.getRole());  // 行注：调用设置我的角色
        detailDTO.setMuted(isMuted(currentMember));  // 行注：调用设置Muted
        detailDTO.setMuteTime(currentMember.getMuteTime());  // 行注：调用设置Mute时间
        detailDTO.setNotificationMuted(Boolean.TRUE.equals(currentMember.getNotificationMuted()));  // 行注：调用设置通知Muted
        detailDTO.setCreateTime(groupInfo.getCreateTime());  // 行注：调用设置创建时间
        // 行注：调用设置Members
        detailDTO.setMembers(members.stream()
                // 行注：继续调用排序
                .sorted((left, right) -> {
                    int roleCompare = Integer.compare(right.getRole(), left.getRole());  // 行注：初始化角色Compare
                    // 行注：判断是否满足当前条件
                    if (roleCompare != 0) {
                        return roleCompare;  // 行注：返回处理结果
                    }  // 行注：结束当前代码块
                    LocalDateTime leftTime = left.getCreateTime();  // 行注：初始化left时间
                    LocalDateTime rightTime = right.getCreateTime();  // 行注：初始化right时间
                    // 行注：判断是否满足当前条件
                    if (leftTime == null && rightTime == null) {
                        return 0;  // 行注：返回处理结果
                    }  // 行注：结束当前代码块
                    // 行注：判断是否满足当前条件
                    if (leftTime == null) {
                        return 1;  // 行注：返回处理结果
                    }  // 行注：结束当前代码块
                    // 行注：判断是否满足当前条件
                    if (rightTime == null) {
                        return -1;  // 行注：返回处理结果
                    }  // 行注：结束当前代码块
                    return leftTime.compareTo(rightTime);  // 行注：返回处理结果
                // 行注：补充当前表达式片段
                })
                // 行注：继续调用映射
                .map(member -> toMemberDTO(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList()));  // 行注：继续调用收集
        return detailDTO;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建群会话方法
    private ChatSessionDTO buildGroupSession(Long userId, Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);  // 行注：初始化群信息
        // 行注：判断是否满足当前条件
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ImGroupMember member = getMember(groupId, userId);  // 行注：初始化成员
        // 行注：判断是否满足当前条件
        if (member == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ImSession session = getGroupSession(userId, groupId);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ChatSessionDTO dto = new ChatSessionDTO();  // 行注：初始化DTO
        dto.setId(session.getId());  // 行注：调用设置ID
        dto.setUserId(session.getUserId());  // 行注：调用设置用户ID
        dto.setTargetId(session.getTargetId());  // 行注：调用设置TargetID
        dto.setSessionType(session.getSessionType());  // 行注：调用设置会话类型
        dto.setTargetNickname(resolveGroupDisplayName(groupInfo, member));  // 行注：调用设置TargetNickname
        dto.setTargetUsername("group-" + groupInfo.getId());  // 行注：调用设置TargetUsername
        dto.setTargetAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置Target头像
        dto.setLastMessage(session.getLastMessage());  // 行注：调用设置最后消息
        dto.setLastMessageTime(session.getLastMessageTime());  // 行注：调用设置最后消息时间
        dto.setUnreadCount(session.getUnreadCount());  // 行注：调用设置未读数量
        dto.setMemberCount(listMembersByGroupId(groupId).size());  // 行注：调用设置成员数量
        dto.setMyRole(member.getRole());  // 行注：调用设置我的角色
        dto.setGroupRemark(member.getGroupRemark());  // 行注：调用设置群Remark
        dto.setNotice(groupInfo.getNotice());  // 行注：调用设置Notice
        dto.setNoticeUnread(hasUnreadNotice(groupInfo, member));  // 行注：调用设置Notice未读
        dto.setMuted(isMuted(member));  // 行注：调用设置Muted
        dto.setMuteTime(member.getMuteTime());  // 行注：调用设置Mute时间
        dto.setNotificationMuted(Boolean.TRUE.equals(member.getNotificationMuted()));  // 行注：调用设置通知Muted
        dto.setTargetOnline(false);  // 行注：调用设置Target在线
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为成员DTO方法
    private GroupMemberDTO toMemberDTO(ImGroupMember member, SysUser user) {
        GroupMemberDTO dto = new GroupMemberDTO();  // 行注：初始化DTO
        dto.setId(member.getId());  // 行注：调用设置ID
        dto.setGroupId(member.getGroupId());  // 行注：调用设置群ID
        dto.setUserId(member.getUserId());  // 行注：调用设置用户ID
        dto.setRole(member.getRole());  // 行注：调用设置角色
        dto.setMuted(isMuted(member));  // 行注：调用设置Muted
        dto.setMuteTime(member.getMuteTime());  // 行注：调用设置Mute时间
        dto.setCreateTime(member.getCreateTime());  // 行注：调用设置创建时间
        // 行注：判断是否满足当前条件
        if (user != null) {
            dto.setUsername(user.getUsername());  // 行注：调用设置Username
            dto.setNickname(user.getNickname());  // 行注：调用设置Nickname
            dto.setAvatar(user.getAvatar());  // 行注：调用设置头像
        }  // 行注：结束当前代码块
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取成员方法
    private ImGroupMember getMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);  // 行注：继续调用等值条件
        return groupMemberMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取群会话方法
    private ImSession getGroupSession(Long userId, Long groupId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);  // 行注：继续调用等值条件
        return sessionMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义列表Members群ID方法
    private List<ImGroupMember> listMembersByGroupId(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(ImGroupMember::getGroupId, groupId);  // 行注：调用等值条件
        return groupMemberMapper.selectList(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加载用户映射方法
    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        // 行注：判断是否满足当前条件
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return userMapper.selectBatchIds(userIds).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义规范化用户ID列表方法
    private Set<Long> normalizeUserIds(Collection<Long> userIds) {
        // 行注：判断是否满足当前条件
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return userIds.stream()  // 行注：返回处理结果
                // 行注：继续调用过滤
                .filter(id -> id != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义是否Muted方法
    private boolean isMuted(ImGroupMember member) {
        return member.getMuteTime() != null && member.getMuteTime().isAfter(LocalDateTime.now());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否包含未读Notice方法
    private boolean hasUnreadNotice(ImGroupInfo groupInfo, ImGroupMember member) {
        // 行注：判断是否满足当前条件
        if (groupInfo == null || member == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (groupInfo.getNotice() == null || groupInfo.getNotice().isBlank() || groupInfo.getNoticeUpdateTime() == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LocalDateTime noticeReadTime = member.getNoticeReadTime();  // 行注：初始化notice已读时间
        return noticeReadTime == null || noticeReadTime.isBefore(groupInfo.getNoticeUpdateTime());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析文件方法
    private SysFile resolveFile(ImMessage message) {
        // 行注：判断是否满足当前条件
        if (message == null || !StringUtils.hasText(message.getContent())) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (message.getMsgType() != ChatConstants.MESSAGE_TYPE_FILE && message.getMsgType() != ChatConstants.MESSAGE_TYPE_IMAGE) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(SysFile::getFileUrl, message.getContent()).last("LIMIT 1");  // 行注：调用等值条件
        return sysFileMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析群Display名称方法
    private String resolveGroupDisplayName(ImGroupInfo groupInfo, ImGroupMember member) {
        // 行注：判断是否满足当前条件
        if (member != null && StringUtils.hasText(member.getGroupRemark())) {
            return member.getGroupRemark().trim();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return groupInfo.getGroupName();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义parse@提醒用户ID列表方法
    private List<Long> parseMentionUserIds(String mentionUserIds) {
        // 行注：判断是否满足当前条件
        if (mentionUserIds == null || mentionUserIds.isBlank()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<Long> result = new ArrayList<>();  // 行注：初始化结果
        // 行注：遍历当前集合或范围
        for (String item : mentionUserIds.split(",")) {
            // 行注：判断是否满足当前条件
            if (item == null || item.isBlank()) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：尝试执行可能失败的逻辑
            try {
                result.add(Long.parseLong(item.trim()));  // 行注：调用添加
            // 行注：执行当前方法调用
            } catch (NumberFormatException ignored) {
                // Skip malformed legacy values.
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return result;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析@提醒DisplayNames方法
    private List<String> resolveMentionDisplayNames(List<Long> mentionUserIds, Map<Long, SysUser> userMap) {
        // 行注：判断是否满足当前条件
        if (mentionUserIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<String> displayNames = new ArrayList<>();  // 行注：初始化displayNames
        // 行注：遍历当前集合或范围
        for (Long mentionUserId : mentionUserIds) {
            SysUser user = userMap.get(mentionUserId);  // 行注：初始化用户
            // 行注：判断是否满足当前条件
            if (user == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (user.getNickname() != null && !user.getNickname().isBlank()) {
                displayNames.add(user.getNickname().trim());  // 行注：调用添加
            // 行注：调用获取Username
            } else if (user.getUsername() != null && !user.getUsername().isBlank()) {
                displayNames.add(user.getUsername().trim());  // 行注：调用添加
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return displayNames;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    private String resolveGroupSenderDisplayName(ImGroupMember member, SysUser user) {
        if (member != null && member.getMemberCardName() != null && !member.getMemberCardName().isBlank()) {
            return member.getMemberCardName().trim();
        }
        return resolveSenderDisplayName(user);
    }

    private String resolveSenderDisplayName(SysUser user) {
        if (user == null) {
            return null;
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname().trim();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername().trim();
        }
        return null;
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
