package com.linkx.server.module.group.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.group.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;  // 行注：引入 LambdaUpdateWrapper 类型
import com.linkx.server.common.AuditLogService;  // 行注：引入 AuditLogService 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.TextNormalizer;  // 行注：引入 TextNormalizer 类型
import com.linkx.server.common.UploadAssetUrlUtils;  // 行注：引入 UploadAssetUrlUtils 类型
import com.linkx.server.config.LinkxAppProperties;  // 行注：引入 LinkxAppProperties 类型
import com.linkx.server.entity.ImGroupInfo;  // 行注：引入 ImGroupInfo 类型
import com.linkx.server.entity.ImGroupMember;  // 行注：引入 ImGroupMember 类型
import com.linkx.server.entity.ImGroupHighlight;
import com.linkx.server.entity.ImGroupNotice;
import com.linkx.server.entity.ImGroupRequest;  // 行注：引入 ImGroupRequest 类型
import com.linkx.server.entity.ImMessage;  // 行注：引入 ImMessage 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.ImGroupInfoMapper;  // 行注：引入 ImGroupInfoMapper 类型
import com.linkx.server.mapper.ImGroupMemberMapper;  // 行注：引入 ImGroupMemberMapper 类型
import com.linkx.server.mapper.ImGroupHighlightMapper;
import com.linkx.server.mapper.ImGroupNoticeMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;  // 行注：引入 ImGroupRequestMapper 类型
import com.linkx.server.mapper.ImMessageMapper;  // 行注：引入 ImMessageMapper 类型
import com.linkx.server.mapper.ImSessionMapper;  // 行注：引入 ImSessionMapper 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import com.linkx.server.module.chat.ws.ChatGroupRealtimeService;  // 行注：引入 ChatGroupRealtimeService 类型
import com.linkx.server.module.group.constant.GroupConstants;  // 行注：引入 GroupConstants 类型
import com.linkx.server.module.group.dto.GroupDTO;  // 行注：引入 GroupDTO 类型
import com.linkx.server.module.group.dto.GroupDetailDTO;  // 行注：引入 GroupDetailDTO 类型
import com.linkx.server.module.group.dto.GroupMemberDTO;  // 行注：引入 GroupMemberDTO 类型
import com.linkx.server.module.group.dto.GroupHighlightItemDTO;
import com.linkx.server.module.group.dto.GroupNoticeItemDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;  // 行注：引入 GroupRequestDTO 类型
import com.linkx.server.module.group.service.GroupService;  // 行注：引入 GroupService 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import lombok.extern.slf4j.Slf4j;  // 行注：引入 Slf4j 类型
import org.springframework.dao.DuplicateKeyException;  // 行注：引入 DuplicateKeyException 类型
import org.springframework.stereotype.Service;  // 行注：引入 Service 类型
import org.springframework.transaction.annotation.Transactional;  // 行注：引入 Transactional 类型
import org.springframework.transaction.support.TransactionSynchronization;  // 行注：引入 TransactionSynchronization 类型
import org.springframework.transaction.support.TransactionSynchronizationManager;  // 行注：引入 TransactionSynchronizationManager 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型
import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.Collection;  // 行注：引入 Collection 类型
import java.util.Comparator;  // 行注：引入 Comparator 类型
import java.util.HashMap;  // 行注：引入 HashMap 类型
import java.util.LinkedHashSet;  // 行注：引入 LinkedHashSet 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Map;  // 行注：引入 Map 类型
import java.util.Objects;  // 行注：引入 Objects 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 群组业务实现：权限按群主/管理员校验；变更后通过 {@link ChatGroupRealtimeService} 推送；
 * 事务提交后再发 WebSocket 事件。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 GroupServiceImpl 类
public class GroupServiceImpl implements GroupService {

    private static final int DEFAULT_MAX_MEMBERS = 500;  // 行注：定义默认最大MEMBERS常量
    private static final int SESSION_PREVIEW_MAX_LENGTH = 500;  // 行注：定义会话预览最大长度常量

    private final ImGroupInfoMapper groupInfoMapper;  // 行注：注入群信息Mapper依赖
    private final ImGroupMemberMapper groupMemberMapper;  // 行注：注入群成员Mapper依赖
    private final ImGroupNoticeMapper groupNoticeMapper;
    private final ImGroupHighlightMapper groupHighlightMapper;
    private final ImGroupRequestMapper groupRequestMapper;  // 行注：注入群请求Mapper依赖
    private final ImMessageMapper messageMapper;  // 行注：注入消息Mapper依赖
    private final ImSessionMapper sessionMapper;  // 行注：注入会话Mapper依赖
    private final SysFileMapper fileMapper;  // 行注：注入文件Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final ChatGroupRealtimeService chatGroupRealtimeService;  // 行注：注入聊天群Realtime服务依赖
    private final LinkxAppProperties linkxAppProperties;  // 行注：注入linkx应用属性依赖
    private final AuditLogService auditLogService;  // 行注：注入审计日志服务依赖

    /**
     * 创建群。
     *
     * @param operatorId 操作人用户 ID
     * @param groupName 群名称
     * @param groupAvatar 群头像 URL
     * @param notice 群公告
     * @param memberIds 成员 ID 列表
     * @return 群信息
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义创建群方法
    public GroupDTO createGroup(Long operatorId, String groupName, String groupAvatar, String notice, List<Long> memberIds) {
        // 建群时统一清洗群名称和初始成员列表，避免重复成员、本人重复加入或非法输入。
        String normalizedName = normalizeGroupName(groupName);  // 行注：初始化规范化后的名称
        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, operatorId);  // 行注：初始化规范化后的成员ID列表
        validateUsersExist(normalizedMemberIds);  // 行注：调用validateUsersExist

        ImGroupInfo groupInfo = new ImGroupInfo();  // 行注：初始化群信息
        groupInfo.setGroupName(normalizedName);  // 行注：调用设置群名称
        groupInfo.setGroupAvatar(normalizeAvatarUrl(groupAvatar));  // 行注：调用设置群头像
        groupInfo.setOwnerId(operatorId);  // 行注：调用设置所有者ID
        groupInfo.setMaxMembers(DEFAULT_MAX_MEMBERS);  // 行注：调用设置最大Members
        groupInfo.setNotice(normalizeNullableText(notice));  // 行注：调用设置Notice
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(groupInfo.getNotice())) {
            groupInfo.setNoticeUpdateTime(LocalDateTime.now());  // 行注：调用设置Notice更新时间
        }  // 行注：结束当前代码块
        groupInfo.setDeleted(0);  // 行注：调用设置Deleted
        groupInfoMapper.insert(groupInfo);  // 行注：调用insert

        // 建群人默认就是群主；其余初始成员按普通成员写入。
        insertMember(groupInfo.getId(), operatorId, GroupConstants.ROLE_OWNER, groupInfo.getNoticeUpdateTime());  // 行注：调用获取ID
        // 行注：遍历当前集合或范围
        for (Long memberId : normalizedMemberIds) {
            insertMember(groupInfo.getId(), memberId, GroupConstants.ROLE_MEMBER);  // 行注：调用获取ID
        }  // 行注：结束当前代码块

        // 群创建完成后立即为所有成员创建群会话，保证客户端会话列表能马上看到新群。
        List<Long> allMembers = new ArrayList<>();  // 行注：初始化全部Members
        allMembers.add(operatorId);  // 行注：调用添加
        allMembers.addAll(normalizedMemberIds);  // 行注：调用添加全部
        ensureGroupSessions(groupInfo, allMembers);  // 行注：调用ensure群会话列表
        Map<Long, SysUser> userMap = loadUserMap(new LinkedHashSet<>(allMembers));  // 行注：初始化用户映射
        // 追加一条系统消息，给后续成员变更、审计和会话预览提供统一展示口径。
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：调用获取ID
                groupInfo.getId(),
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 创建了群聊“" + normalizedName + "”"
        );  // 行注：结束当前参数配置
        // 行注：补充当前表达式片段
        log.info("Group created, groupId={}, operatorId={}, memberCount={}, groupName={}",
                groupInfo.getId(), operatorId, allMembers.size(), normalizedName);  // 行注：调用获取ID

        return buildGroupDTO(groupInfo, requireMember(groupInfo.getId(), operatorId), allMembers.size());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 获取我的群。
     *
     * @param userId 用户 ID
     * @return 群信息列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取我的Groups方法
    public List<GroupDTO> getMyGroups(Long userId) {
        List<ImGroupMember> myMemberships = listMembersByUserId(userId);  // 行注：初始化我的Memberships
        // 行注：判断是否满足当前条件
        if (myMemberships.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        List<Long> groupIds = myMemberships.stream().map(ImGroupMember::getGroupId).distinct().toList();  // 行注：初始化群ID列表
        List<ImGroupInfo> groups = listActiveGroups(groupIds);  // 行注：初始化groups
        // 行注：执行初始化操作
        Map<Long, ImGroupInfo> groupMap = groups.stream().collect(Collectors.toMap(ImGroupInfo::getId, item -> item, (left, right) -> left));
        Map<Long, Long> memberCountMap = countMembers(groupIds);  // 行注：初始化成员数量映射
        // 行注：调用流
        Map<Long, ImGroupMember> myMemberMap = myMemberships.stream()
                // 行注：继续调用过滤
                .filter(item -> groupMap.containsKey(item.getGroupId()))
                .collect(Collectors.toMap(ImGroupMember::getGroupId, item -> item, (left, right) -> left));  // 行注：继续调用收集

        return groups.stream()  // 行注：返回处理结果
                // 行注：继续调用排序
                .sorted(Comparator
                        // 行注：继续调用比较
                        .comparing(ImGroupInfo::getUpdateTime, Comparator.nullsLast(LocalDateTime::compareTo))
                        // 行注：继续调用reversed
                        .reversed()
                        // 行注：继续调用再比较
                        .thenComparing(
                                // 行注：补充当前表达式片段
                                ImGroupInfo::getCreateTime,
                                // 行注：调用空值最后
                                Comparator.nullsLast(LocalDateTime::compareTo).reversed()
                        // 行注：补充当前表达式片段
                        ))
                // 行注：继续调用映射
                .map(group -> buildGroupDTO(group, myMemberMap.get(group.getId()), memberCountMap.getOrDefault(group.getId(), 0L).intValue()))
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /**
     * 获取待处理申请。
     *
     * @param userId 用户 ID
     * @return 群申请列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取Pending请求方法
    public List<GroupRequestDTO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupRequest::getToUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING)
                .orderByDesc(ImGroupRequest::getCreateTime);  // 行注：继续调用排序降序

        List<ImGroupRequest> requests = groupRequestMapper.selectList(wrapper);  // 行注：初始化请求
        // 行注：判断是否满足当前条件
        if (requests.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：调用流
        Set<Long> groupIds = requests.stream()
                // 行注：继续调用映射
                .map(ImGroupRequest::getGroupId)
                // 行注：继续调用过滤
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：调用流
        Map<Long, ImGroupInfo> groupMap = listActiveGroups(new ArrayList<>(groupIds)).stream()
                .collect(Collectors.toMap(ImGroupInfo::getId, item -> item, (left, right) -> left));  // 行注：继续调用收集

        Set<Long> userIds = new LinkedHashSet<>();  // 行注：初始化用户ID列表
        // 行注：遍历当前集合或范围
        for (ImGroupRequest request : requests) {
            // 行注：判断是否满足当前条件
            if (request.getFromUserId() != null) {
                userIds.add(request.getFromUserId());  // 行注：调用添加
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (request.getToUserId() != null) {
                userIds.add(request.getToUserId());  // 行注：调用添加
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        Map<Long, SysUser> userMap = loadUserMap(userIds);  // 行注：初始化用户映射

        return requests.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(request -> {
                    ImGroupInfo groupInfo = groupMap.get(request.getGroupId());  // 行注：初始化群信息
                    // 行注：判断是否满足当前条件
                    if (groupInfo == null) {
                        return null;  // 行注：返回处理结果
                    }  // 行注：结束当前代码块
                    SysUser fromUser = userMap.get(request.getFromUserId());  // 行注：初始化用户

                    GroupRequestDTO dto = new GroupRequestDTO();  // 行注：初始化DTO
                    dto.setId(request.getId());  // 行注：调用设置ID
                    dto.setGroupId(request.getGroupId());  // 行注：调用设置群ID
                    dto.setGroupName(groupInfo.getGroupName());  // 行注：调用设置群名称
                    dto.setGroupAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置群头像
                    dto.setFromUserId(request.getFromUserId());  // 行注：调用设置用户ID
                    dto.setToUserId(request.getToUserId());  // 行注：调用设置转为用户ID
                    dto.setRequestType(request.getRequestType());  // 行注：调用设置请求类型
                    dto.setMessage(request.getMessage());  // 行注：调用设置消息
                    dto.setStatus(request.getStatus());  // 行注：调用设置状态
                    dto.setCreateTime(request.getCreateTime());  // 行注：调用设置创建时间
                    // 行注：判断是否满足当前条件
                    if (fromUser != null) {
                        dto.setFromUsername(fromUser.getUsername());  // 行注：调用设置Username
                        dto.setFromNickname(fromUser.getNickname());  // 行注：调用设置Nickname
                        dto.setFromAvatar(fromUser.getAvatar());  // 行注：调用设置头像
                    }  // 行注：结束当前代码块
                    return dto;  // 行注：返回处理结果
                // 行注：补充当前表达式片段
                })
                // 行注：继续调用过滤
                .filter(Objects::nonNull)
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /**
     * 提交加入申请。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param message 附言内容
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义submit加入请求方法
    public void submitJoinRequest(Long userId, Long groupId, String message) {
        requireActiveGroup(groupId);  // 行注：调用require启用群
        // 行注：判断是否满足当前条件
        if (isGroupMember(groupId, userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已加入该群聊");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 同一用户对同一群只保留一条待处理申请，避免管理员端出现重复审批。
        LambdaQueryWrapper<ImGroupRequest> dupWrapper = new LambdaQueryWrapper<>();  // 行注：初始化dup条件封装器
        // 行注：调用等值条件
        dupWrapper.eq(ImGroupRequest::getGroupId, groupId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getFromUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getRequestType, GroupConstants.REQUEST_TYPE_JOIN)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (groupRequestMapper.selectCount(dupWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已提交过入群申请，请勿重复提交");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：调用流
        List<ImGroupMember> admins = listMembersByGroupId(groupId).stream()
                // 行注：继续调用过滤
                .filter(m -> m.getRole() >= GroupConstants.ROLE_ADMIN)
                .toList();  // 行注：继续调用转为列表

        String normalizedMessage = normalizeRequestMessage(message);  // 行注：初始化规范化后的消息
        // 一个申请会复制给所有管理员/群主，谁先审批就以谁的结果为准。
        // 行注：遍历当前集合或范围
        for (ImGroupMember admin : admins) {
            ImGroupRequest request = new ImGroupRequest();  // 行注：初始化请求
            request.setGroupId(groupId);  // 行注：调用设置群ID
            request.setFromUserId(userId);  // 行注：调用设置用户ID
            request.setToUserId(admin.getUserId());  // 行注：调用设置转为用户ID
            request.setRequestType(GroupConstants.REQUEST_TYPE_JOIN);  // 行注：调用设置请求类型
            request.setMessage(normalizedMessage);  // 行注：调用设置消息
            request.setStatus(GroupConstants.REQUEST_STATUS_PENDING);  // 行注：调用设置状态
            groupRequestMapper.insert(request);  // 行注：调用insert
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 邀请成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberIds 成员 ID 列表
     * @param message 附言内容
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义邀请Members方法
    public void inviteMembers(Long operatorId, Long groupId, List<Long> memberIds, String message) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        // 邀请成员属于管理动作，只允许管理员及以上角色执行。
        assertCanManageMembers(operatorMember);  // 行注：调用assertCanManageMembers

        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, operatorId);  // 行注：初始化规范化后的成员ID列表
        // 行注：判断是否满足当前条件
        if (normalizedMemberIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要邀请的成员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        validateUsersExist(normalizedMemberIds);  // 行注：调用validateUsersExist

        // 行注：调用流
        Set<Long> existingUserIds = listMembersByGroupId(groupId).stream()
                // 行注：继续调用映射
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        String normalizedMessage = normalizeRequestMessage(message);  // 行注：初始化规范化后的消息

        // 行注：遍历当前集合或范围
        for (Long memberUserId : normalizedMemberIds) {
            // 已在群里的用户直接跳过，邀请接口保持“尽量成功”的批处理行为。
            // 行注：判断是否满足当前条件
            if (existingUserIds.contains(memberUserId)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块

            LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
            // 行注：调用等值条件
            wrapper.eq(ImGroupRequest::getGroupId, groupId)
                    // 行注：继续调用等值条件
                    .eq(ImGroupRequest::getToUserId, memberUserId)
                    // 行注：继续调用等值条件
                    .eq(ImGroupRequest::getRequestType, GroupConstants.REQUEST_TYPE_INVITE)
                    .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);  // 行注：继续调用等值条件
            // 行注：判断是否满足当前条件
            if (groupRequestMapper.selectCount(wrapper) > 0) {
                // 已有待处理邀请时不重复创建记录，避免同一人收到多条相同邀请。
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块

            ImGroupRequest request = new ImGroupRequest();  // 行注：初始化请求
            request.setGroupId(groupId);  // 行注：调用设置群ID
            request.setFromUserId(operatorId);  // 行注：调用设置用户ID
            request.setToUserId(memberUserId);  // 行注：调用设置转为用户ID
            request.setRequestType(GroupConstants.REQUEST_TYPE_INVITE);  // 行注：调用设置请求类型
            request.setMessage(normalizedMessage);  // 行注：调用设置消息
            request.setStatus(GroupConstants.REQUEST_STATUS_PENDING);  // 行注：调用设置状态
            groupRequestMapper.insert(request);  // 行注：调用insert
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 通过申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义accept请求方法
    public void acceptRequest(Long userId, Long requestId) {
        ImGroupRequest request = requirePendingRequest(userId, requestId);  // 行注：初始化请求
        ImGroupInfo groupInfo = lockActiveGroup(request.getGroupId());  // 行注：初始化群信息
        // 用条件更新先抢占这条待处理申请，避免重复审批或并发审批。
        // 行注：判断是否满足当前条件
        if (!claimPendingRequest(requestId, userId, GroupConstants.REQUEST_STATUS_ACCEPTED)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (request.getRequestType() == GroupConstants.REQUEST_TYPE_JOIN) {
            ImGroupMember approverMember = requireMember(groupInfo.getId(), userId);  // 行注：初始化approver成员
            // 入群申请只能由群主/管理员审批。
            assertCanManageMembers(approverMember);  // 行注：调用assertCanManageMembers
            boolean joined = addMemberIfAbsent(groupInfo, request.getFromUserId());  // 行注：初始化joined
            // 行注：判断是否满足当前条件
            if (joined) {
                // 审批通过后要补建会话，并写入系统消息同步到所有成员。
                ensureGroupSessions(groupInfo, List.of(request.getFromUserId()));  // 行注：调用of
                Map<Long, SysUser> userMap = loadUserMap(Set.of(userId, request.getFromUserId()));  // 行注：初始化用户映射
                // 行注：补充当前表达式片段
                appendGroupSystemMessage(
                        // 行注：调用获取ID
                        groupInfo.getId(),
                        // 行注：补充当前表达式片段
                        userId,
                        // 行注：调用获取用户ID
                        getUserDisplayName(userId, userMap) + " 同意 " + getUserDisplayName(request.getFromUserId(), userMap) + " 加入了群聊"
                );  // 行注：结束当前参数配置
            }  // 行注：结束当前代码块

            // 行注：补充当前表达式片段
            completePendingRequests(
                    // 行注：调用获取ID
                    groupInfo.getId(),
                    // 行注：调用获取用户ID
                    request.getFromUserId(),
                    // 行注：补充当前表达式片段
                    GroupConstants.REQUEST_TYPE_JOIN,
                    // 行注：补充当前表达式片段
                    true,
                    // 行注：补充当前表达式片段
                    GroupConstants.REQUEST_STATUS_REJECTED
            );  // 行注：结束当前参数配置
        // 行注：调用获取请求类型
        } else if (request.getRequestType() == GroupConstants.REQUEST_TYPE_INVITE) {
            // 邀请入群是被邀请人本人确认，通过后直接把自己加入群成员表。
            boolean joined = addMemberIfAbsent(groupInfo, userId);  // 行注：初始化joined
            // 行注：判断是否满足当前条件
            if (joined) {
                ensureGroupSessions(groupInfo, List.of(userId));  // 行注：调用of
                Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));  // 行注：初始化用户映射
                // 行注：补充当前表达式片段
                appendGroupSystemMessage(
                        // 行注：调用获取ID
                        groupInfo.getId(),
                        // 行注：补充当前表达式片段
                        userId,
                        // 行注：调用获取用户Display名称
                        getUserDisplayName(userId, userMap) + " 接受邀请加入了群聊"
                );  // 行注：结束当前参数配置
            }  // 行注：结束当前代码块
            // 行注：补充当前表达式片段
            completePendingRequests(
                    // 行注：调用获取ID
                    groupInfo.getId(),
                    // 行注：补充当前表达式片段
                    userId,
                    // 行注：补充当前表达式片段
                    GroupConstants.REQUEST_TYPE_INVITE,
                    // 行注：补充当前表达式片段
                    false,
                    // 行注：补充当前表达式片段
                    GroupConstants.REQUEST_STATUS_ACCEPTED
            );  // 行注：结束当前参数配置
        // 行注：开始当前语句对应的代码块
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的群申请类型");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：补充当前表达式片段
        log.info("Group request accepted, requestId={}, groupId={}, operatorId={}, requestType={}",
                requestId, groupInfo.getId(), userId, request.getRequestType());  // 行注：调用获取ID
    }  // 行注：结束当前代码块

    /**
     * 拒绝申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义reject请求方法
    public void rejectRequest(Long userId, Long requestId) {
        requirePendingRequest(userId, requestId);  // 行注：调用requirePending请求
        claimPendingRequest(requestId, userId, GroupConstants.REQUEST_STATUS_REJECTED);  // 行注：调用claimPending请求
    }  // 行注：结束当前代码块

    /**
     * 获取群详情。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @return 群详情
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取群详情方法
    public GroupDetailDTO getGroupDetail(Long userId, Long groupId) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember currentMember = requireMember(groupId, userId);  // 行注：初始化当前成员
        List<ImGroupMember> members = listMembersByGroupId(groupId);  // 行注：初始化members
        // 行注：初始化用户映射
        Map<Long, SysUser> userMap = loadUserMap(members.stream().map(ImGroupMember::getUserId).collect(Collectors.toSet()));

        GroupDetailDTO detailDTO = new GroupDetailDTO();  // 行注：初始化详情DTO
        detailDTO.setId(groupInfo.getId());  // 行注：调用设置ID
        detailDTO.setGroupName(groupInfo.getGroupName());  // 行注：调用设置群名称
        detailDTO.setGroupAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置群头像
        detailDTO.setGroupRemark(currentMember.getGroupRemark());  // 行注：调用设置群Remark
        detailDTO.setMemberCardName(currentMember.getMemberCardName());
        List<GroupNoticeItemDTO> notices = listNoticesForGroup(groupId, userMap);
        detailDTO.setNotices(notices);
        GroupNoticeItemDTO pinned = pickDisplayNotice(notices, groupInfo);
        if (pinned != null) {
            detailDTO.setNotice(pinned.getContent());
            detailDTO.setNoticeUpdateTime(pinned.getUpdateTime() != null ? pinned.getUpdateTime() : pinned.getCreateTime());
        } else {
            detailDTO.setNotice(groupInfo.getNotice());
            detailDTO.setNoticeUpdateTime(groupInfo.getNoticeUpdateTime());
        }
        detailDTO.setNoticeReadTime(currentMember.getNoticeReadTime());  // 行注：调用设置Notice已读时间
        detailDTO.setNoticeUnread(hasUnreadNotice(detailDTO.getNotice(), detailDTO.getNoticeUpdateTime(), currentMember));
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
                .sorted(Comparator.comparing(ImGroupMember::getRole).reversed()
                        // 行注：继续调用再比较
                        .thenComparing(ImGroupMember::getCreateTime, Comparator.nullsLast(LocalDateTime::compareTo)))
                // 行注：继续调用映射
                .map(member -> toMemberDTO(member, userMap.get(member.getUserId())))
                .collect(Collectors.toList()));  // 行注：继续调用收集
        return detailDTO;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 添加成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberIds 成员 ID 列表
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义添加Members方法
    public void addMembers(Long operatorId, Long groupId, List<Long> memberIds) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        // 直接加人属于更强权限操作，必须先锁定群并校验管理员身份。
        assertCanManageMembers(operatorMember);  // 行注：调用assertCanManageMembers

        List<Long> normalizedMemberIds = normalizeMemberIds(memberIds, null);  // 行注：初始化规范化后的成员ID列表
        // 行注：判断是否满足当前条件
        if (normalizedMemberIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要添加的成员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        validateUsersExist(normalizedMemberIds);  // 行注：调用validateUsersExist

        // 行注：调用流
        Set<Long> existingUserIds = listMembersByGroupId(groupId).stream()
                // 行注：继续调用映射
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：调用流
        List<Long> toAddUserIds = normalizedMemberIds.stream()
                // 行注：继续调用过滤
                .filter(userId -> !existingUserIds.contains(userId))
                .toList();  // 行注：继续调用转为列表
        // 行注：判断是否满足当前条件
        if (toAddUserIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 锁定群后再校验人数上限，避免并发加人突破最大成员数。
        // 行注：判断是否满足当前条件
        if (existingUserIds.size() + toAddUserIds.size() > groupInfo.getMaxMembers()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员数量已达到上限");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：遍历当前集合或范围
        for (Long userId : toAddUserIds) {
            insertMember(groupId, userId, GroupConstants.ROLE_MEMBER);  // 行注：调用insert成员
        }  // 行注：结束当前代码块
        ensureGroupSessions(groupInfo, toAddUserIds);  // 行注：调用ensure群会话列表
        Set<Long> relatedUserIds = new LinkedHashSet<>(toAddUserIds);  // 行注：初始化related用户ID列表
        relatedUserIds.add(operatorId);  // 行注：调用添加
        Map<Long, SysUser> userMap = loadUserMap(relatedUserIds);  // 行注：初始化用户映射
        // 统一写系统消息，让新成员加入在会话流和群动态中都有可见痕迹。
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 邀请 " + joinUserNames(toAddUserIds, userMap) + " 加入了群聊"
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块

    /**
     * 移除成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义移除成员方法
    public void removeMember(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        ImGroupMember targetMember = requireMember(groupId, memberUserId);  // 行注：初始化target成员

        // 行注：判断是否满足当前条件
        if (operatorId.equals(memberUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "暂不支持自行退群，请由群主或管理员操作");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能移除群主");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以移除成员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER && targetMember.getRole() != GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能移除管理员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        removeGroupMemberData(groupId, memberUserId);  // 行注：调用移除群成员Data

        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 将 " + getUserDisplayName(memberUserId, userMap) + " 移出了群聊"
        );  // 行注：结束当前参数配置
        // 行注：调用推送群Removed
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, List.of(memberUserId), "REMOVED"));
    }  // 行注：结束当前代码块

    /**
     * 设置管理员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义设置Admin方法
    public void setAdmin(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        ImGroupMember targetMember = requireMember(groupId, memberUserId);  // 行注：初始化target成员

        assertCanManageAdmin(operatorMember, targetMember, operatorId, memberUserId);  // 行注：调用assertCanManageAdmin
        // 行注：判断是否满足当前条件
        if (targetMember.getRole() == GroupConstants.ROLE_ADMIN) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        targetMember.setRole(GroupConstants.ROLE_ADMIN);  // 行注：调用设置角色
        groupMemberMapper.updateById(targetMember);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 将 " + getUserDisplayName(memberUserId, userMap) + " 设为管理员"
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块

    /**
     * 移除管理员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义移除Admin方法
    public void removeAdmin(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        ImGroupMember targetMember = requireMember(groupId, memberUserId);  // 行注：初始化target成员

        assertCanManageAdmin(operatorMember, targetMember, operatorId, memberUserId);  // 行注：调用assertCanManageAdmin
        // 行注：判断是否满足当前条件
        if (targetMember.getRole() == GroupConstants.ROLE_MEMBER) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        targetMember.setRole(GroupConstants.ROLE_MEMBER);  // 行注：调用设置角色
        groupMemberMapper.updateById(targetMember);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 取消了 " + getUserDisplayName(memberUserId, userMap) + " 的管理员身份"
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块

    /**
     * 处理dissolve群。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义dissolve群方法
    public void dissolveGroup(Long operatorId, Long groupId) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);  // 行注：初始化群信息
        // 行注：判断是否满足当前条件
        if (!groupInfo.getOwnerId().equals(operatorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以解散群聊");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：调用流
        List<Long> memberUserIds = listMembersByGroupId(groupId).stream()
                // 行注：继续调用映射
                .map(ImGroupMember::getUserId)
                .toList();  // 行注：继续调用转为列表

        groupInfo.setDeleted(1);  // 行注：调用设置Deleted
        groupInfoMapper.updateById(groupInfo);  // 行注：调用更新ID

        LambdaQueryWrapper<ImSession> deleteSessionWrapper = new LambdaQueryWrapper<>();  // 行注：初始化删除会话条件封装器
        // 行注：调用等值条件
        deleteSessionWrapper.eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);  // 行注：继续调用等值条件
        sessionMapper.delete(deleteSessionWrapper);  // 行注：调用删除
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, memberUserIds, "DISSOLVED"));  // 行注：调用推送群Removed
    }  // 行注：结束当前代码块

    /**
     * 设置成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     * @param muteMinutes 禁言分钟数
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义mute成员方法
    public void muteMember(Long operatorId, Long groupId, Long memberUserId, Integer muteMinutes) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        ImGroupMember targetMember = requireMember(groupId, memberUserId);  // 行注：初始化target成员

        assertCanOperateMute(operatorMember, targetMember);  // 行注：调用assertCanOperateMute
        targetMember.setMuteTime(LocalDateTime.now().plusMinutes(muteMinutes));  // 行注：调用设置Mute时间
        groupMemberMapper.updateById(targetMember);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 禁言了 " + getUserDisplayName(memberUserId, userMap) + " " + muteMinutes + " 分钟"
        );  // 行注：结束当前参数配置
        // 行注：补充当前表达式片段
        auditLogService.recordSuccess("GROUP_MUTE_MEMBER", operatorId, "GROUP_MEMBER", memberUserId,
                "groupId=" + groupId + ", muteMinutes=" + muteMinutes);  // 行注：执行初始化操作
        // 行注：补充当前表达式片段
        log.info("Group member muted, groupId={}, operatorId={}, memberUserId={}, muteMinutes={}",
                groupId, operatorId, memberUserId, muteMinutes);  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    /**
     * 解除成员。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param memberUserId 成员用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义unmute成员方法
    public void unmuteMember(Long operatorId, Long groupId, Long memberUserId) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        ImGroupMember targetMember = requireMember(groupId, memberUserId);  // 行注：初始化target成员

        assertCanOperateMute(operatorMember, targetMember);  // 行注：调用assertCanOperateMute
        LambdaUpdateWrapper<ImGroupMember> updateWrapper = new LambdaUpdateWrapper<>();  // 行注：初始化更新条件封装器
        // 行注：调用等值条件
        updateWrapper.eq(ImGroupMember::getId, targetMember.getId())
                .set(ImGroupMember::getMuteTime, null);  // 行注：继续调用设置
        groupMemberMapper.update(null, updateWrapper);  // 行注：调用更新
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, memberUserId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 解除禁言 " + getUserDisplayName(memberUserId, userMap)
        );  // 行注：结束当前参数配置
        // 行注：执行初始化操作
        auditLogService.recordSuccess("GROUP_UNMUTE_MEMBER", operatorId, "GROUP_MEMBER", memberUserId, "groupId=" + groupId);
        // 行注：补充当前表达式片段
        log.info("Group member unmuted, groupId={}, operatorId={}, memberUserId={}",
                groupId, operatorId, memberUserId);  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    /**
     * 更新资料。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param groupName 群名称
     * @param groupAvatar 群头像 URL
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义更新Profile方法
    public void updateProfile(Long operatorId, Long groupId, String groupName, String groupAvatar) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以修改群资料");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        String oldGroupName = groupInfo.getGroupName();  // 行注：初始化old群名称
        String oldGroupAvatar = groupInfo.getGroupAvatar();  // 行注：初始化old群头像
        String normalizedGroupName = normalizeGroupName(groupName);  // 行注：初始化规范化后的群名称
        String normalizedGroupAvatar = normalizeAvatarUrl(groupAvatar);  // 行注：初始化规范化后的群头像
        // 行注：判断是否满足当前条件
        if (normalizedGroupName.equals(oldGroupName) && equalsNullableText(normalizedGroupAvatar, oldGroupAvatar)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        groupInfo.setGroupName(normalizedGroupName);  // 行注：调用设置群名称
        groupInfo.setGroupAvatar(normalizedGroupAvatar);  // 行注：调用设置群头像
        groupInfoMapper.updateById(groupInfo);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用构建ProfileUpdatedSystem消息
                buildProfileUpdatedSystemMessage(getUserDisplayName(operatorId, userMap), oldGroupName, normalizedGroupName, oldGroupAvatar, normalizedGroupAvatar)
        );  // 行注：结束当前参数配置
        // 行注：补充当前表达式片段
        log.info("Group profile updated, groupId={}, operatorId={}, oldGroupName={}, newGroupName={}",
                groupId, operatorId, oldGroupName, normalizedGroupName);  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    /**
     * 更新公告。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param notice 群公告
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义更新Notice方法
    public void updateNotice(Long operatorId, Long groupId, String notice) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以修改群公告");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        String normalizedNotice = TextNormalizer.normalizeOptionalMultiline(notice, 1000, "群公告");  // 行注：初始化规范化后的Notice
        // 行注：判断是否满足当前条件
        if (normalizedNotice == null) {
            normalizedNotice = "";  // 行注：初始化规范化后的Notice
        }  // 行注：结束当前代码块
        String previousNotice = normalizeNullableText(groupInfo.getNotice());  // 行注：初始化previousNotice
        String nextNotice = normalizeNullableText(normalizedNotice);  // 行注：初始化nextNotice
        // 行注：判断是否满足当前条件
        if (equalsNullableText(previousNotice, nextNotice)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        LocalDateTime noticeUpdateTime = LocalDateTime.now();  // 行注：初始化notice更新时间
        ImGroupNotice noticeRow = new ImGroupNotice();
        noticeRow.setGroupId(groupId);
        noticeRow.setContent(normalizedNotice);
        noticeRow.setPinned(1);
        noticeRow.setPublisherId(operatorId);
        noticeRow.setCreateTime(noticeUpdateTime);
        noticeRow.setUpdateTime(noticeUpdateTime);
        noticeRow.setDeleted(0);
        groupNoticeMapper.insert(noticeRow);
        groupInfo.setNotice(normalizedNotice);  // 行注：调用设置Notice
        groupInfo.setNoticeUpdateTime(noticeUpdateTime);  // 行注：调用设置Notice更新时间
        groupInfoMapper.updateById(groupInfo);  // 行注：调用更新ID
        operatorMember.setNoticeReadTime(noticeUpdateTime);  // 行注：调用设置Notice已读时间
        groupMemberMapper.updateById(operatorMember);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用是否包含文本
                getUserDisplayName(operatorId, userMap) + (StringUtils.hasText(nextNotice) ? " 更新了群公告" : " 清空了群公告")
        );  // 行注：结束当前参数配置
        // 行注：补充当前表达式片段
        log.info("Group notice updated, groupId={}, operatorId={}, noticeLength={}",
                groupId, operatorId, nextNotice == null ? 0 : nextNotice.length());  // 行注：执行初始化操作
    }  // 行注：结束当前代码块

    /**
     * 更新Preferences。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param groupRemark 群内备注
     * @param notificationMuted 是否开启消息免打扰
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义更新Preferences方法
    public void updatePreferences(Long userId, Long groupId, String groupRemark, Boolean notificationMuted,
                                  String memberCardName) {
        requireActiveGroup(groupId);  // 行注：调用require启用群
        ImGroupMember member = requireMember(groupId, userId);  // 行注：初始化成员
        String normalizedRemark = groupRemark != null
                ? normalizeGroupRemark(groupRemark)
                : member.getGroupRemark();
        String normalizedCard = memberCardName != null
                ? TextNormalizer.normalizeOptionalSingleLine(memberCardName, 64, "群名片")
                : member.getMemberCardName();
        boolean nextNotificationMuted = notificationMuted != null
                ? Boolean.TRUE.equals(notificationMuted)
                : Boolean.TRUE.equals(member.getNotificationMuted());
        boolean changed = (groupRemark != null
                && !Objects.equals(normalizedRemark, normalizeNullableText(member.getGroupRemark())))
                || (notificationMuted != null
                && nextNotificationMuted != Boolean.TRUE.equals(member.getNotificationMuted()))
                || (memberCardName != null
                && !Objects.equals(normalizedCard, normalizeNullableText(member.getMemberCardName())));
        if (!changed) {
            return;
        }
        if (groupRemark != null) {
            member.setGroupRemark(normalizedRemark);
        }
        if (notificationMuted != null) {
            member.setNotificationMuted(nextNotificationMuted);
        }
        if (memberCardName != null) {
            member.setMemberCardName(normalizedCard);
        }
        groupMemberMapper.updateById(member);
        // 行注：调用executeAfterCommit
        executeAfterCommit(() -> {
            chatGroupRealtimeService.pushGroupSessions(groupId, List.of(userId));  // 行注：调用推送群会话列表
            chatGroupRealtimeService.pushGroupDetails(groupId, List.of(userId));  // 行注：调用推送群Details
        });  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 标记公告已读。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义标记Notice已读方法
    public void markNoticeRead(Long userId, Long groupId) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember member = requireMember(groupId, userId);  // 行注：初始化成员
        Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));
        List<GroupNoticeItemDTO> notices = listNoticesForGroup(groupId, userMap);
        GroupNoticeItemDTO display = pickDisplayNotice(notices, groupInfo);
        String noticeText = display != null ? display.getContent() : groupInfo.getNotice();
        LocalDateTime noticeTime = display != null
                ? (display.getUpdateTime() != null ? display.getUpdateTime() : display.getCreateTime())
                : groupInfo.getNoticeUpdateTime();
        if (!StringUtils.hasText(noticeText) || noticeTime == null) {
            return;
        }
        if (!hasUnreadNotice(noticeText, noticeTime, member)) {
            return;
        }
        member.setNoticeReadTime(LocalDateTime.now());  // 行注：调用设置Notice已读时间
        groupMemberMapper.updateById(member);  // 行注：调用更新ID
        // 行注：调用executeAfterCommit
        executeAfterCommit(() -> {
            chatGroupRealtimeService.pushGroupSessions(groupId, List.of(userId));  // 行注：调用推送群会话列表
            chatGroupRealtimeService.pushGroupDetails(groupId, List.of(userId));  // 行注：调用推送群Details
        });  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 获取群媒体。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param mediaType 媒体类型
     * @param keyword 搜索关键词
     * @param size 返回条数
     * @return 消息列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取群Media方法
    public List<MessageDTO> getGroupMedia(Long userId, Long groupId, String mediaType, String keyword, int size) {
        requireActiveGroup(groupId);  // 行注：调用require启用群
        requireMember(groupId, userId);  // 行注：调用require成员

        List<Integer> messageTypes = resolveMediaMessageTypes(mediaType);  // 行注：初始化消息Types
        List<ImMessage> messages;  // 行注：完成当前语句
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(keyword)) {
            Map<String, SysFile> matchedFileMap = loadMatchedFileMap(keyword.trim());  // 行注：初始化matched文件映射
            // 行注：判断是否满足当前条件
            if (matchedFileMap.isEmpty()) {
                return List.of();  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
            // 行注：调用等值条件
            wrapper.eq(ImMessage::getToUserId, groupId)
                    // 行注：继续调用等值条件
                    .eq(ImMessage::getSessionId, groupId)
                    // 行注：继续调用in
                    .in(ImMessage::getMsgType, messageTypes)
                    // 行注：继续调用ne
                    .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                    // 行注：继续调用in
                    .in(ImMessage::getContent, matchedFileMap.keySet())
                    // 行注：继续调用排序降序
                    .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                    .last("LIMIT " + size);  // 行注：继续调用最后
            messages = messageMapper.selectList(wrapper);  // 行注：初始化消息
            return toMessageList(messages, ChatConstants.SESSION_TYPE_GROUP, matchedFileMap);  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getToUserId, groupId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getSessionId, groupId)
                // 行注：继续调用in
                .in(ImMessage::getMsgType, messageTypes)
                // 行注：继续调用ne
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size);  // 行注：继续调用最后
        messages = messageMapper.selectList(wrapper);  // 行注：初始化消息
        return toMessageList(messages, ChatConstants.SESSION_TYPE_GROUP, loadFileMap(messages));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /**
     * 搜索群Messages。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     * @param keyword 搜索关键词
     * @param size 返回条数
     * @return 消息列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义搜索群消息方法
    public List<MessageDTO> searchGroupMessages(Long userId, Long groupId, String keyword, int size) {
        requireActiveGroup(groupId);  // 行注：调用require启用群
        requireMember(groupId, userId);  // 行注：调用require成员

        String normalizedKeyword = normalizeSearchKeyword(keyword);  // 行注：初始化规范化后的Keyword
        // 行注：判断是否满足当前条件
        if (normalizedKeyword == null) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        LinkedHashSet<Long> matchedMessageIds = new LinkedHashSet<>();  // 行注：初始化matched消息ID列表

        LambdaQueryWrapper<ImMessage> textWrapper = new LambdaQueryWrapper<>();  // 行注：初始化文本条件封装器
        // 行注：调用等值条件
        textWrapper.eq(ImMessage::getToUserId, groupId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getSessionId, groupId)
                // 行注：继续调用ne
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                // 行注：继续调用like
                .like(ImMessage::getContent, normalizedKeyword)
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size);  // 行注：继续调用最后
        // 行注：调用select列表
        messageMapper.selectList(textWrapper).stream()
                // 行注：继续调用映射
                .map(ImMessage::getId)
                .forEach(matchedMessageIds::add);  // 行注：继续调用Each

        // 行注：判断是否满足当前条件
        if (matchedMessageIds.size() < size) {
            Map<String, SysFile> matchedFileMap = loadMatchedFileMap(normalizedKeyword);  // 行注：初始化matched文件映射
            // 行注：判断是否满足当前条件
            if (!matchedFileMap.isEmpty()) {
                LambdaQueryWrapper<ImMessage> fileWrapper = new LambdaQueryWrapper<>();  // 行注：初始化文件条件封装器
                // 行注：调用等值条件
                fileWrapper.eq(ImMessage::getToUserId, groupId)
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getSessionId, groupId)
                        // 行注：继续调用in
                        .in(ImMessage::getMsgType, List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE))
                        // 行注：继续调用ne
                        .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                        // 行注：继续调用in
                        .in(ImMessage::getContent, matchedFileMap.keySet())
                        // 行注：继续调用排序降序
                        .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                        .last("LIMIT " + size);  // 行注：继续调用最后
                // 行注：调用select列表
                messageMapper.selectList(fileWrapper).stream()
                        // 行注：继续调用映射
                        .map(ImMessage::getId)
                        .forEach(matchedMessageIds::add);  // 行注：继续调用Each
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (matchedMessageIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        List<ImMessage> matchedMessages = messageMapper.selectBatchIds(matchedMessageIds);  // 行注：初始化matched消息
        matchedMessages.sort(this::compareMessagesByNewest);  // 行注：调用sort
        // 行注：判断是否满足当前条件
        if (matchedMessages.size() > size) {
            matchedMessages = new ArrayList<>(matchedMessages.subList(0, size));  // 行注：初始化matched消息
        }  // 行注：结束当前代码块
        return toMessageList(matchedMessages, ChatConstants.SESSION_TYPE_GROUP, loadFileMap(matchedMessages));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建群DTO方法
    private GroupDTO buildGroupDTO(ImGroupInfo groupInfo, ImGroupMember currentMember, int memberCount) {
        GroupDTO dto = new GroupDTO();  // 行注：初始化DTO
        dto.setId(groupInfo.getId());  // 行注：调用设置ID
        dto.setGroupName(groupInfo.getGroupName());  // 行注：调用设置群名称
        dto.setGroupAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置群头像
        dto.setNotice(groupInfo.getNotice());  // 行注：调用设置Notice
        dto.setOwnerId(groupInfo.getOwnerId());  // 行注：调用设置所有者ID
        dto.setMaxMembers(groupInfo.getMaxMembers());  // 行注：调用设置最大Members
        dto.setMemberCount(memberCount);  // 行注：调用设置成员数量
        dto.setMyRole(currentMember != null ? currentMember.getRole() : null);  // 行注：执行初始化操作
        dto.setMuted(currentMember != null && isMuted(currentMember));  // 行注：执行初始化操作
        dto.setMuteTime(currentMember != null ? currentMember.getMuteTime() : null);  // 行注：执行初始化操作
        dto.setCreateTime(groupInfo.getCreateTime());  // 行注：调用设置创建时间
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
        if (StringUtils.hasText(member.getMemberCardName())) {
            dto.setMemberCardName(member.getMemberCardName().trim());
        }
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义assertCanManageMembers方法
    private void assertCanManageMembers(ImGroupMember operatorMember) {
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以管理成员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义assertCanManageAdmin方法
    private void assertCanManageAdmin(ImGroupMember operatorMember, ImGroupMember targetMember, Long operatorId, Long memberUserId) {
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以设置管理员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (operatorId.equals(memberUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能操作自己的管理员身份");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能操作群主角色");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义assertCanOperateMute方法
    private void assertCanOperateMute(ImGroupMember operatorMember, ImGroupMember targetMember) {
        assertCanManageMembers(operatorMember);  // 行注：调用assertCanManageMembers
        // 行注：判断是否满足当前条件
        if (targetMember.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁言群主");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER && targetMember.getRole() != GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "管理员不能禁言管理员");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义ensure群会话列表方法
    private void ensureGroupSessions(ImGroupInfo groupInfo, Collection<Long> userIds) {
        // 行注：遍历当前集合或范围
        for (Long userId : userIds) {
            ImSession existing = getOrCreateGroupSession(userId, groupInfo.getId());  // 行注：初始化existing
            // 行注：判断是否满足当前条件
            if (existing.getLastMessageTime() != null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            existing.setLastMessage(null);  // 行注：调用设置最后消息
            existing.setLastMessageTime(groupInfo.getCreateTime());  // 行注：调用设置最后消息时间
            sessionMapper.updateById(existing);  // 行注：调用更新ID
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义insert成员方法
    private void insertMember(Long groupId, Long userId, Integer role) {
        insertMember(groupId, userId, role, null);  // 行注：调用insert成员
    }  // 行注：结束当前代码块

    // 行注：定义insert成员方法
    private void insertMember(Long groupId, Long userId, Integer role, LocalDateTime noticeReadTime) {
        ImGroupMember member = new ImGroupMember();  // 行注：初始化成员
        member.setGroupId(groupId);  // 行注：调用设置群ID
        member.setUserId(userId);  // 行注：调用设置用户ID
        member.setRole(role);  // 行注：调用设置角色
        member.setNoticeReadTime(noticeReadTime);  // 行注：调用设置Notice已读时间
        member.setNotificationMuted(false);  // 行注：调用设置通知Muted
        groupMemberMapper.insert(member);  // 行注：调用insert
    }  // 行注：结束当前代码块

    // 行注：定义requirePending请求方法
    private ImGroupRequest requirePendingRequest(Long userId, Long requestId) {
        ImGroupRequest request = groupRequestMapper.selectById(requestId);  // 行注：初始化请求
        // 行注：判断是否满足当前条件
        if (request == null || !Objects.equals(request.getToUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群申请不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!Objects.equals(request.getStatus(), GroupConstants.REQUEST_STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该群申请已处理");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return request;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义completePending请求方法
    private void completePendingRequests(Long groupId, Long userId, Integer requestType, boolean matchFromUserId, int status) {
        LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupRequest::getGroupId, groupId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getRequestType, requestType)
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (matchFromUserId) {
            wrapper.eq(ImGroupRequest::getFromUserId, userId);  // 行注：调用等值条件
        // 行注：开始当前语句对应的代码块
        } else {
            wrapper.eq(ImGroupRequest::getToUserId, userId);  // 行注：调用等值条件
        }  // 行注：结束当前代码块
        List<ImGroupRequest> pendingRequests = groupRequestMapper.selectList(wrapper);  // 行注：初始化pending请求
        // 行注：判断是否满足当前条件
        if (pendingRequests.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LocalDateTime handleTime = LocalDateTime.now();  // 行注：初始化handle时间
        // 行注：遍历当前集合或范围
        for (ImGroupRequest pendingRequest : pendingRequests) {
            pendingRequest.setStatus(status);  // 行注：调用设置状态
            pendingRequest.setHandleTime(handleTime);  // 行注：调用设置Handle时间
            groupRequestMapper.updateById(pendingRequest);  // 行注：调用更新ID
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义添加成员IfAbsent方法
    private boolean addMemberIfAbsent(ImGroupInfo groupInfo, Long userId) {
        // 行注：判断是否满足当前条件
        if (isGroupMember(groupInfo.getId(), userId)) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (listMembersByGroupId(groupInfo.getId()).size() >= groupInfo.getMaxMembers()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员数量已达到上限");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：尝试执行可能失败的逻辑
        try {
            insertMember(groupInfo.getId(), userId, GroupConstants.ROLE_MEMBER);  // 行注：调用获取ID
            return true;  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException ignored) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义是否群成员方法
    private boolean isGroupMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);  // 行注：继续调用等值条件
        return groupMemberMapper.selectCount(wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化成员ID列表方法
    private List<Long> normalizeMemberIds(List<Long> memberIds, Long excludeUserId) {
        // 行注：判断是否满足当前条件
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LinkedHashSet<Long> result = new LinkedHashSet<>();  // 行注：初始化结果
        // 行注：遍历当前集合或范围
        for (Long memberId : memberIds) {
            // 行注：判断是否满足当前条件
            if (memberId == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (excludeUserId != null && excludeUserId.equals(memberId)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            result.add(memberId);  // 行注：调用添加
        }  // 行注：结束当前代码块
        return new ArrayList<>(result);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义validateUsersExist方法
    private void validateUsersExist(List<Long> userIds) {
        // 行注：判断是否满足当前条件
        if (userIds.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<SysUser> users = userMapper.selectBatchIds(userIds);  // 行注：初始化users
        // 行注：判断是否满足当前条件
        if (users.size() != userIds.size()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义规范化群名称方法
    private String normalizeGroupName(String groupName) {
        return TextNormalizer.normalizeRequiredSingleLine(groupName, 100, "群名称");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化Nullable文本方法
    private String normalizeNullableText(String value) {
        return UploadAssetUrlUtils.normalizeNullableText(value);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化头像URL方法
    private String normalizeAvatarUrl(String rawAvatarUrl) {
        return UploadAssetUrlUtils.normalizeAvatarUrl(rawAvatarUrl, linkxAppProperties.getUpload().getUrl(), "群头像");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化群Remark方法
    private String normalizeGroupRemark(String groupRemark) {
        return TextNormalizer.normalizeOptionalSingleLine(groupRemark, 100, "群备注");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化搜索Keyword方法
    private String normalizeSearchKeyword(String keyword) {
        return TextNormalizer.normalizeOptionalSingleLine(keyword, 100, "搜索关键词");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化请求消息方法
    private String normalizeRequestMessage(String message) {
        return TextNormalizer.normalizeOptionalMultiline(message, 255, "申请说明");  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义require启用群方法
    private ImGroupInfo requireActiveGroup(Long groupId) {
        ImGroupInfo groupInfo = groupInfoMapper.selectById(groupId);  // 行注：初始化群信息
        // 行注：判断是否满足当前条件
        if (groupInfo == null || Integer.valueOf(1).equals(groupInfo.getDeleted())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群聊不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return groupInfo;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义lock启用群方法
    private ImGroupInfo lockActiveGroup(Long groupId) {
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupInfo::getId, groupId)
                // 行注：继续调用等值条件
                .eq(ImGroupInfo::getDeleted, 0)
                .last("LIMIT 1 FOR UPDATE");  // 行注：继续调用最后
        ImGroupInfo groupInfo = groupInfoMapper.selectOne(wrapper);  // 行注：初始化群信息
        // 行注：判断是否满足当前条件
        if (groupInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群聊不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return groupInfo;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义require成员方法
    private ImGroupMember requireMember(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);  // 行注：继续调用等值条件
        ImGroupMember member = groupMemberMapper.selectOne(wrapper);  // 行注：初始化成员
        // 行注：判断是否满足当前条件
        if (member == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你不在该群聊中");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return member;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义列表Members群ID方法
    private List<ImGroupMember> listMembersByGroupId(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(ImGroupMember::getGroupId, groupId);  // 行注：调用等值条件
        return groupMemberMapper.selectList(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义列表Members用户ID方法
    private List<ImGroupMember> listMembersByUserId(Long userId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(ImGroupMember::getUserId, userId);  // 行注：调用等值条件
        return groupMemberMapper.selectList(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义列表启用Groups方法
    private List<ImGroupInfo> listActiveGroups(List<Long> groupIds) {
        // 行注：判断是否满足当前条件
        if (groupIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用in
        wrapper.in(ImGroupInfo::getId, groupIds)
                .eq(ImGroupInfo::getDeleted, 0);  // 行注：继续调用等值条件
        return groupInfoMapper.selectList(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义数量Members方法
    private Map<Long, Long> countMembers(List<Long> groupIds) {
        Map<Long, Long> countMap = new HashMap<>();  // 行注：初始化数量映射
        // 行注：判断是否满足当前条件
        if (groupIds.isEmpty()) {
            return countMap;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.in(ImGroupMember::getGroupId, groupIds);  // 行注：调用in
        // 行注：遍历当前集合或范围
        for (ImGroupMember member : groupMemberMapper.selectList(wrapper)) {
            countMap.merge(member.getGroupId(), 1L, Long::sum);  // 行注：调用merge
        }  // 行注：结束当前代码块
        return countMap;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加载用户映射方法
    private Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        // 行注：判断是否满足当前条件
        if (userIds.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return userMapper.selectBatchIds(userIds).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(SysUser::getId, item -> item, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /**
     * 退出群。
     *
     * @param userId 用户 ID
     * @param groupId 群 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义leave群方法
    public void leaveGroup(Long userId, Long groupId) {
        lockActiveGroup(groupId);  // 行注：调用lock启用群
        ImGroupMember member = requireMember(groupId, userId);  // 行注：初始化成员

        // 行注：判断是否满足当前条件
        if (member.getRole() == GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群主不能退群，请先转让群主或解散群聊");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        removeGroupMemberData(groupId, userId);  // 行注：调用移除群成员Data
        Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                userId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(userId, userMap) + " 退出了群聊"
        );  // 行注：结束当前参数配置
        auditLogService.recordSuccess("GROUP_LEAVE", userId, "GROUP", groupId, "");  // 行注：调用记录Success
        log.info("Group left, groupId={}, userId={}", groupId, userId);  // 行注：执行初始化操作
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupRemoved(groupId, List.of(userId), "LEFT"));  // 行注：调用推送群Removed
    }  // 行注：结束当前代码块

    /**
     * 转让Owner。
     *
     * @param operatorId 操作人用户 ID
     * @param groupId 群 ID
     * @param newOwnerId 新群主用户 ID
     */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义transfer所有者方法
    public void transferOwner(Long operatorId, Long groupId, Long newOwnerId) {
        ImGroupInfo groupInfo = lockActiveGroup(groupId);  // 行注：初始化群信息
        ImGroupMember operatorMember = requireMember(groupId, operatorId);  // 行注：初始化operator成员

        // 行注：判断是否满足当前条件
        if (operatorMember.getRole() != GroupConstants.ROLE_OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主可以转让群主");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (operatorId.equals(newOwnerId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能将群主转让给自己");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        ImGroupMember newOwnerMember = requireMember(groupId, newOwnerId);  // 行注：初始化新建所有者成员

        operatorMember.setRole(GroupConstants.ROLE_MEMBER);  // 行注：调用设置角色
        groupMemberMapper.updateById(operatorMember);  // 行注：调用更新ID

        newOwnerMember.setRole(GroupConstants.ROLE_OWNER);  // 行注：调用设置角色
        groupMemberMapper.updateById(newOwnerMember);  // 行注：调用更新ID

        groupInfo.setOwnerId(newOwnerId);  // 行注：调用设置所有者ID
        groupInfoMapper.updateById(groupInfo);  // 行注：调用更新ID
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId, newOwnerId));  // 行注：初始化用户映射
        // 行注：补充当前表达式片段
        appendGroupSystemMessage(
                // 行注：补充当前表达式片段
                groupId,
                // 行注：补充当前表达式片段
                operatorId,
                // 行注：调用获取用户Display名称
                getUserDisplayName(operatorId, userMap) + " 将群主转让给了 " + getUserDisplayName(newOwnerId, userMap)
        );  // 行注：结束当前参数配置
        // 行注：执行初始化操作
        auditLogService.recordSuccess("GROUP_TRANSFER_OWNER", operatorId, "GROUP", groupId, "newOwnerId=" + newOwnerId);
        // 行注：执行初始化操作
        log.info("Group owner transferred, groupId={}, operatorId={}, newOwnerId={}", groupId, operatorId, newOwnerId);
    }  // 行注：结束当前代码块

    // 行注：定义是否Muted方法
    private boolean isMuted(ImGroupMember member) {
        return member.getMuteTime() != null && member.getMuteTime().isAfter(LocalDateTime.now());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义append群System消息方法
    private void appendGroupSystemMessage(Long groupId, Long operatorId, String content) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(content)) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<ImGroupMember> members = listMembersByGroupId(groupId);  // 行注：初始化members
        // 行注：判断是否满足当前条件
        if (members.isEmpty()) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        LocalDateTime now = LocalDateTime.now();  // 行注：初始化now
        ImMessage message = new ImMessage();  // 行注：初始化消息
        message.setSessionId(groupId);  // 行注：调用设置会话ID
        message.setFromUserId(operatorId);  // 行注：调用设置用户ID
        message.setToUserId(groupId);  // 行注：调用设置转为用户ID
        message.setContent(content);  // 行注：调用设置内容
        message.setMsgType(ChatConstants.MESSAGE_TYPE_SYSTEM);  // 行注：调用设置消息类型
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);  // 行注：调用设置状态
        messageMapper.insert(message);  // 行注：调用insert
        // 行注：调用流
        List<Long> memberUserIds = members.stream()
                // 行注：继续调用映射
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toList());  // 行注：继续调用收集

        // 行注：遍历当前集合或范围
        for (ImGroupMember member : members) {
            ImSession session = getOrCreateGroupSession(member.getUserId(), groupId);  // 行注：初始化会话
            session.setLastMessage(truncateSessionPreview(content));  // 行注：调用设置最后消息
            session.setLastMessageTime(now);  // 行注：调用设置最后消息时间
            // 行注：判断是否满足当前条件
            if (!member.getUserId().equals(operatorId)) {
                session.setUnreadCount((session.getUnreadCount() == null ? 0 : session.getUnreadCount()) + 1);  // 行注：执行初始化操作
            }  // 行注：结束当前代码块
            sessionMapper.updateById(session);  // 行注：调用更新ID
        }  // 行注：结束当前代码块
        // 行注：调用推送群Mutation
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupMutation(groupId, message.getId(), memberUserIds));
    }  // 行注：结束当前代码块

    // 行注：定义获取Or创建群会话方法
    private ImSession getOrCreateGroupSession(Long userId, Long groupId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);  // 行注：继续调用等值条件
        ImSession session = sessionMapper.selectOne(wrapper);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session != null) {
            return session;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        session = new ImSession();  // 行注：初始化会话
        session.setUserId(userId);  // 行注：调用设置用户ID
        session.setTargetId(groupId);  // 行注：调用设置TargetID
        session.setSessionType(ChatConstants.SESSION_TYPE_GROUP);  // 行注：调用设置会话类型
        session.setUnreadCount(0);  // 行注：调用设置未读数量
        // 行注：尝试执行可能失败的逻辑
        try {
            sessionMapper.insert(session);  // 行注：调用insert
            return session;  // 行注：返回处理结果
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException exception) {
            ImSession existingSession = sessionMapper.selectOne(wrapper);  // 行注：初始化existing会话
            // 行注：判断是否满足当前条件
            if (existingSession != null) {
                return existingSession;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "群会话初始化失败");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义获取用户Display名称方法
    private String getUserDisplayName(Long userId, Map<Long, SysUser> userMap) {
        SysUser user = userMap.get(userId);  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (user == null) {
            return "成员";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return "成员";  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加入用户Names方法
    private String joinUserNames(List<Long> userIds, Map<Long, SysUser> userMap) {
        return userIds.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(userId -> getUserDisplayName(userId, userMap))
                .collect(Collectors.joining("、"));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义equalsNullable文本方法
    private boolean equalsNullableText(String left, String right) {
        return Objects.equals(normalizeNullableText(left), normalizeNullableText(right));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否包含未读Notice方法
    private boolean hasUnreadNotice(ImGroupInfo groupInfo, ImGroupMember member) {
        if (groupInfo == null || member == null) {
            return false;
        }
        return hasUnreadNotice(groupInfo.getNotice(), groupInfo.getNoticeUpdateTime(), member);
    }

    private boolean hasUnreadNotice(String notice, LocalDateTime noticeUpdateTime, ImGroupMember member) {
        if (member == null || !StringUtils.hasText(notice) || noticeUpdateTime == null) {
            return false;
        }
        LocalDateTime noticeReadTime = member.getNoticeReadTime();
        return noticeReadTime == null || noticeReadTime.isBefore(noticeUpdateTime);
    }

    private List<GroupNoticeItemDTO> listNoticesForGroup(Long groupId, Map<Long, SysUser> userMap) {
        LambdaQueryWrapper<ImGroupNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupNotice::getGroupId, groupId)
                .orderByDesc(ImGroupNotice::getPinned)
                .orderByDesc(ImGroupNotice::getUpdateTime)
                .orderByDesc(ImGroupNotice::getCreateTime)
                .last("LIMIT 50");
        return groupNoticeMapper.selectList(wrapper).stream()
                .map(row -> toNoticeItemDTO(row, userMap))
                .collect(Collectors.toList());
    }

    private GroupNoticeItemDTO pickDisplayNotice(List<GroupNoticeItemDTO> notices, ImGroupInfo groupInfo) {
        if (notices != null && !notices.isEmpty()) {
            return notices.stream()
                    .filter(n -> Boolean.TRUE.equals(n.getPinned()))
                    .findFirst()
                    .orElse(notices.get(0));
        }
        if (groupInfo != null && StringUtils.hasText(groupInfo.getNotice())) {
            GroupNoticeItemDTO fallback = new GroupNoticeItemDTO();
            fallback.setContent(groupInfo.getNotice());
            fallback.setPinned(true);
            fallback.setUpdateTime(groupInfo.getNoticeUpdateTime());
            fallback.setCreateTime(groupInfo.getNoticeUpdateTime());
            return fallback;
        }
        return null;
    }

    private GroupNoticeItemDTO toNoticeItemDTO(ImGroupNotice row, Map<Long, SysUser> userMap) {
        GroupNoticeItemDTO dto = new GroupNoticeItemDTO();
        dto.setId(row.getId());
        dto.setContent(row.getContent());
        dto.setPinned(row.getPinned() != null && row.getPinned() == 1);
        dto.setPublisherId(row.getPublisherId());
        dto.setCreateTime(row.getCreateTime());
        dto.setUpdateTime(row.getUpdateTime());
        SysUser publisher = userMap.get(row.getPublisherId());
        if (publisher != null) {
            dto.setPublisherNickname(StringUtils.hasText(publisher.getNickname())
                    ? publisher.getNickname() : publisher.getUsername());
        }
        return dto;
    }

    private void syncLegacyGroupNoticeFields(ImGroupInfo groupInfo) {
        List<GroupNoticeItemDTO> notices = listNoticesForGroup(groupInfo.getId(), Map.of());
        GroupNoticeItemDTO display = pickDisplayNotice(notices, null);
        if (display != null) {
            groupInfo.setNotice(display.getContent());
            groupInfo.setNoticeUpdateTime(display.getUpdateTime() != null
                    ? display.getUpdateTime() : display.getCreateTime());
        } else {
            groupInfo.setNotice(null);
            groupInfo.setNoticeUpdateTime(null);
        }
    }

    @Override
    public List<GroupNoticeItemDTO> listGroupNotices(Long userId, Long groupId) {
        requireActiveGroup(groupId);
        requireMember(groupId, userId);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(userId));
        return listNoticesForGroup(groupId, userMap);
    }

    @Override
    @Transactional
    public void createGroupNotice(Long operatorId, Long groupId, String content, Boolean pinned) {
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以发布公告");
        }
        String normalized = TextNormalizer.normalizeOptionalMultiline(content, 2000, "群公告");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "公告内容不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        ImGroupNotice notice = new ImGroupNotice();
        notice.setGroupId(groupId);
        notice.setContent(normalized);
        notice.setPinned(Boolean.TRUE.equals(pinned) ? 1 : 0);
        notice.setPublisherId(operatorId);
        notice.setCreateTime(now);
        notice.setUpdateTime(now);
        notice.setDeleted(0);
        groupNoticeMapper.insert(notice);
        groupInfo.setNotice(normalized);
        groupInfo.setNoticeUpdateTime(now);
        groupInfoMapper.updateById(groupInfo);
        operatorMember.setNoticeReadTime(now);
        groupMemberMapper.updateById(operatorMember);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));
        appendGroupSystemMessage(groupId, operatorId,
                getUserDisplayName(operatorId, userMap) + " 发布了群公告");
        executeAfterCommit(() -> chatGroupRealtimeService.pushGroupDetails(groupId,
                listMembersByGroupId(groupId).stream().map(ImGroupMember::getUserId).toList()));
    }

    @Override
    @Transactional
    public void setGroupNoticePinned(Long operatorId, Long groupId, Long noticeId, Boolean pinned) {
        requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        if (operatorMember.getRole() == GroupConstants.ROLE_MEMBER) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以置顶公告");
        }
        ImGroupNotice notice = groupNoticeMapper.selectById(noticeId);
        if (notice == null || !groupId.equals(notice.getGroupId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "公告不存在");
        }
        notice.setPinned(Boolean.TRUE.equals(pinned) ? 1 : 0);
        notice.setUpdateTime(LocalDateTime.now());
        groupNoticeMapper.updateById(notice);
        ImGroupInfo groupInfo = requireActiveGroup(groupId);
        syncLegacyGroupNoticeFields(groupInfo);
        groupInfoMapper.updateById(groupInfo);
    }

    @Override
    public List<GroupHighlightItemDTO> listGroupHighlights(Long userId, Long groupId) {
        requireActiveGroup(groupId);
        requireMember(groupId, userId);
        LambdaQueryWrapper<ImGroupHighlight> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImGroupHighlight::getGroupId, groupId)
                .orderByDesc(ImGroupHighlight::getCreateTime)
                .last("LIMIT 100");
        List<ImGroupHighlight> rows = groupHighlightMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<Long> messageIds = rows.stream().map(ImGroupHighlight::getMessageId).distinct().toList();
        List<ImMessage> messages = messageMapper.selectBatchIds(messageIds);
        Map<Long, ImMessage> messageMap = messages.stream()
                .collect(Collectors.toMap(ImMessage::getId, m -> m, (a, b) -> a));
        Map<String, SysFile> fileMap = loadFileMap(messages);
        Set<Long> userIds = new LinkedHashSet<>();
        rows.forEach(r -> {
            if (r.getCreatedBy() != null) {
                userIds.add(r.getCreatedBy());
            }
            ImMessage msg = messageMap.get(r.getMessageId());
            if (msg != null && msg.getFromUserId() != null) {
                userIds.add(msg.getFromUserId());
            }
        });
        Map<Long, SysUser> userMap = loadUserMap(userIds);
        return rows.stream()
                .map(row -> toHighlightItemDTO(row, messageMap.get(row.getMessageId()), userMap, fileMap))
                .filter(dto -> dto.getMessage() != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addGroupHighlight(Long operatorId, Long groupId, Long messageId, String title) {
        requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        assertCanManageMembers(operatorMember);
        ImMessage message = requireHighlightableGroupMessage(groupId, messageId);
        LambdaQueryWrapper<ImGroupHighlight> dup = new LambdaQueryWrapper<>();
        dup.eq(ImGroupHighlight::getGroupId, groupId).eq(ImGroupHighlight::getMessageId, messageId);
        if (groupHighlightMapper.selectCount(dup) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该消息已是群精华");
        }
        String normalizedTitle = TextNormalizer.normalizeOptionalSingleLine(title, 200, "精华标题");
        if (!StringUtils.hasText(normalizedTitle)) {
            normalizedTitle = buildDefaultHighlightTitle(message);
        }
        ImGroupHighlight row = new ImGroupHighlight();
        row.setGroupId(groupId);
        row.setMessageId(messageId);
        row.setTitle(normalizedTitle);
        row.setCreatedBy(operatorId);
        row.setCreateTime(LocalDateTime.now());
        try {
            groupHighlightMapper.insert(row);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该消息已是群精华");
        }
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));
        appendGroupSystemMessage(groupId, operatorId,
                getUserDisplayName(operatorId, userMap) + " 将一条消息设为群精华");
    }

    @Override
    @Transactional
    public void removeGroupHighlight(Long operatorId, Long groupId, Long highlightId) {
        requireActiveGroup(groupId);
        ImGroupMember operatorMember = requireMember(groupId, operatorId);
        assertCanManageMembers(operatorMember);
        ImGroupHighlight row = groupHighlightMapper.selectById(highlightId);
        if (row == null || !groupId.equals(row.getGroupId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "群精华不存在");
        }
        groupHighlightMapper.deleteById(highlightId);
        Map<Long, SysUser> userMap = loadUserMap(Set.of(operatorId));
        appendGroupSystemMessage(groupId, operatorId,
                getUserDisplayName(operatorId, userMap) + " 移除了群精华");
    }

    private ImMessage requireHighlightableGroupMessage(Long groupId, Long messageId) {
        if (messageId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息 ID 不能为空");
        }
        ImMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "消息不存在");
        }
        if (!groupId.equals(message.getToUserId()) || !groupId.equals(message.getSessionId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能将本群消息设为精华");
        }
        if (message.getMsgType() != null && message.getMsgType() == ChatConstants.MESSAGE_TYPE_SYSTEM) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "系统消息不能设为精华");
        }
        if (message.getStatus() != null && message.getStatus() == ChatConstants.MESSAGE_STATUS_RECALLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "已撤回的消息不能设为精华");
        }
        return message;
    }

    private String buildDefaultHighlightTitle(ImMessage message) {
        if (message.getMsgType() != null && message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE) {
            return "[图片]";
        }
        if (message.getMsgType() != null && message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE) {
            return "[文件]";
        }
        String content = message.getContent();
        if (!StringUtils.hasText(content)) {
            return "群精华";
        }
        String trimmed = content.trim().replaceAll("\\s+", " ");
        return trimmed.length() <= 80 ? trimmed : trimmed.substring(0, 80) + "…";
    }

    private GroupHighlightItemDTO toHighlightItemDTO(
            ImGroupHighlight row,
            ImMessage message,
            Map<Long, SysUser> userMap,
            Map<String, SysFile> fileMap) {
        GroupHighlightItemDTO dto = new GroupHighlightItemDTO();
        dto.setId(row.getId());
        dto.setGroupId(row.getGroupId());
        dto.setMessageId(row.getMessageId());
        dto.setTitle(row.getTitle());
        dto.setCreatedBy(row.getCreatedBy());
        dto.setCreateTime(row.getCreateTime());
        SysUser creator = userMap.get(row.getCreatedBy());
        if (creator != null) {
            dto.setCreatedByNickname(StringUtils.hasText(creator.getNickname())
                    ? creator.getNickname() : creator.getUsername());
        }
        if (message != null) {
            dto.setMessage(toMessageDTO(message, ChatConstants.SESSION_TYPE_GROUP, userMap, fileMap));
        }
        return dto;
    }

    // 行注：定义解析Media消息Types方法
    private List<Integer> resolveMediaMessageTypes(String mediaType) {
        String normalizedMediaType = normalizeNullableText(mediaType);  // 行注：初始化规范化后的Media类型
        // 行注：判断是否满足当前条件
        if (normalizedMediaType == null || "all".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_IMAGE, ChatConstants.MESSAGE_TYPE_FILE);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if ("image".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_IMAGE);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if ("file".equalsIgnoreCase(normalizedMediaType)) {
            return List.of(ChatConstants.MESSAGE_TYPE_FILE);  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的媒体类型");  // 行注：抛出异常并中断当前流程
    }  // 行注：结束当前代码块

    // 行注：定义加载Matched文件映射方法
    private Map<String, SysFile> loadMatchedFileMap(String keyword) {
        String normalizedKeyword = normalizeSearchKeyword(keyword);  // 行注：初始化规范化后的Keyword
        // 行注：判断是否满足当前条件
        if (normalizedKeyword == null) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用like
        wrapper.like(SysFile::getOriginalName, normalizedKeyword)
                // 行注：继续调用or
                .or()
                .like(SysFile::getFileType, normalizedKeyword);  // 行注：继续调用like
        return fileMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                // 行注：继续调用过滤
                .filter(file -> StringUtils.hasText(file.getFileUrl()))
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义claimPending请求方法
    private boolean claimPendingRequest(Long requestId, Long userId, int targetStatus) {
        LambdaUpdateWrapper<ImGroupRequest> wrapper = new LambdaUpdateWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupRequest::getId, requestId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getToUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImGroupRequest::getStatus, GroupConstants.REQUEST_STATUS_PENDING)
                // 行注：继续调用设置
                .set(ImGroupRequest::getStatus, targetStatus)
                .set(ImGroupRequest::getHandleTime, LocalDateTime.now());  // 行注：继续调用设置
        return groupRequestMapper.update(null, wrapper) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加载文件映射方法
    private Map<String, SysFile> loadFileMap(List<ImMessage> messages) {
        // 行注：调用流
        Set<String> fileUrls = messages.stream()
                // 行注：继续调用过滤
                .filter(message -> (message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE)
                        // 行注：调用是否包含文本
                        && StringUtils.hasText(message.getContent()))
                // 行注：继续调用映射
                .map(ImMessage::getContent)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：判断是否满足当前条件
        if (fileUrls.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.in(SysFile::getFileUrl, fileUrls);  // 行注：调用in
        return fileMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义转为消息列表方法
    private List<MessageDTO> toMessageList(List<ImMessage> rawMessages, Integer sessionType, Map<String, SysFile> fileMap) {
        // 行注：判断是否满足当前条件
        if (rawMessages == null || rawMessages.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<ImMessage> messages = new ArrayList<>(rawMessages);  // 行注：初始化消息
        messages.sort(this::compareMessagesByNewest);  // 行注：调用sort
        // 行注：调用流
        Set<Long> userIds = messages.stream()
                // 行注：继续调用映射
                .map(ImMessage::getFromUserId)
                // 行注：继续调用过滤
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));  // 行注：继续调用收集
        Map<Long, SysUser> userMap = loadUserMap(userIds);  // 行注：初始化用户映射
        return messages.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(message -> toMessageDTO(message, sessionType, userMap, fileMap))
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义转为消息DTO方法
    private MessageDTO toMessageDTO(ImMessage message, Integer sessionType, Map<Long, SysUser> userMap, Map<String, SysFile> fileMap) {
        MessageDTO dto = new MessageDTO();  // 行注：初始化DTO
        dto.setId(message.getId());  // 行注：调用设置ID
        dto.setSessionId(message.getSessionId());  // 行注：调用设置会话ID
        dto.setFromUserId(message.getFromUserId());  // 行注：调用设置用户ID
        dto.setToUserId(message.getToUserId());  // 行注：调用设置转为用户ID
        dto.setSessionType(sessionType);  // 行注：调用设置会话类型
        dto.setContent(message.getContent());  // 行注：调用设置内容
        dto.setMsgType(message.getMsgType());  // 行注：调用设置消息类型
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));  // 行注：调用设置@提醒全部
        dto.setMentionUserIds(parseMentionUserIds(message.getMentionUserIds()));  // 行注：调用设置@提醒用户ID列表
        dto.setMentionDisplayNames(List.of());  // 行注：调用设置@提醒DisplayNames
        dto.setStatus(message.getStatus());  // 行注：调用设置状态
        dto.setReadTime(message.getReadTime());  // 行注：调用设置已读时间
        dto.setCreateTime(message.getCreateTime());  // 行注：调用设置创建时间
        SysUser fromUser = userMap.get(message.getFromUserId());  // 行注：初始化用户
        // 行注：判断是否满足当前条件
        if (fromUser != null) {
            dto.setFromNickname(fromUser.getNickname());  // 行注：调用设置Nickname
            dto.setFromAvatar(fromUser.getAvatar());  // 行注：调用设置头像
        }  // 行注：结束当前代码块
        SysFile file = fileMap.get(message.getContent());  // 行注：初始化文件
        // 行注：判断是否满足当前条件
        if (file != null) {
            dto.setFileName(file.getOriginalName());  // 行注：调用设置文件名称
            dto.setFileSize(file.getFileSize());  // 行注：调用设置文件大小
            dto.setFileType(file.getFileType());  // 行注：调用设置文件类型
        }  // 行注：结束当前代码块
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义compare消息Newest方法
    private int compareMessagesByNewest(ImMessage left, ImMessage right) {
        LocalDateTime leftTime = left != null ? left.getCreateTime() : null;  // 行注：执行初始化操作
        LocalDateTime rightTime = right != null ? right.getCreateTime() : null;  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (leftTime == null && rightTime != null) {
            return 1;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (leftTime != null && rightTime == null) {
            return -1;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (leftTime != null && rightTime != null) {
            int timeCompare = rightTime.compareTo(leftTime);  // 行注：初始化时间Compare
            // 行注：判断是否满足当前条件
            if (timeCompare != 0) {
                return timeCompare;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块

        Long leftId = left != null ? left.getId() : null;  // 行注：执行初始化操作
        Long rightId = right != null ? right.getId() : null;  // 行注：执行初始化操作
        // 行注：判断是否满足当前条件
        if (leftId == null && rightId != null) {
            return 1;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (leftId != null && rightId == null) {
            return -1;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (leftId == null) {
            return 0;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return rightId.compareTo(leftId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义parse@提醒用户ID列表方法
    private List<Long> parseMentionUserIds(String mentionUserIds) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(mentionUserIds)) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<Long> result = new ArrayList<>();  // 行注：初始化结果
        // 行注：遍历当前集合或范围
        for (String item : mentionUserIds.split(",")) {
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(item)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：尝试执行可能失败的逻辑
            try {
                result.add(Long.parseLong(item.trim()));  // 行注：调用添加
            // 行注：执行当前方法调用
            } catch (NumberFormatException ignored) {
                // Ignore malformed legacy values when reading search results.
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return result;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建ProfileUpdatedSystem消息方法
    private String buildProfileUpdatedSystemMessage(
            // 行注：补充当前表达式片段
            String operatorName,
            // 行注：补充当前表达式片段
            String oldGroupName,
            // 行注：补充当前表达式片段
            String newGroupName,
            // 行注：补充当前表达式片段
            String oldGroupAvatar,
            // 行注：补充当前表达式片段
            String newGroupAvatar
    // 行注：开始当前语句对应的代码块
    ) {
        boolean nameChanged = !equalsNullableText(oldGroupName, newGroupName);  // 行注：初始化名称Changed
        boolean avatarChanged = !equalsNullableText(oldGroupAvatar, newGroupAvatar);  // 行注：初始化头像Changed
        // 行注：判断是否满足当前条件
        if (nameChanged && avatarChanged) {
            return operatorName + " 更新了群名称和群头像";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (nameChanged) {
            return operatorName + " 将群名称修改为“" + newGroupName + "”";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return operatorName + " 更新了群头像";  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义truncate会话预览方法
    private String truncateSessionPreview(String preview) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(preview)) {
            return preview;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (preview.length() <= SESSION_PREVIEW_MAX_LENGTH) {
            return preview;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return preview.substring(0, SESSION_PREVIEW_MAX_LENGTH);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义移除群成员Data方法
    private void removeGroupMemberData(Long groupId, Long userId) {
        LambdaQueryWrapper<ImGroupMember> deleteMemberWrapper = new LambdaQueryWrapper<>();  // 行注：初始化删除成员条件封装器
        // 行注：调用等值条件
        deleteMemberWrapper.eq(ImGroupMember::getGroupId, groupId)
                .eq(ImGroupMember::getUserId, userId);  // 行注：继续调用等值条件
        groupMemberMapper.delete(deleteMemberWrapper);  // 行注：调用删除

        LambdaQueryWrapper<ImSession> deleteSessionWrapper = new LambdaQueryWrapper<>();  // 行注：初始化删除会话条件封装器
        // 行注：调用等值条件
        deleteSessionWrapper.eq(ImSession::getUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImSession::getTargetId, groupId)
                .eq(ImSession::getSessionType, ChatConstants.SESSION_TYPE_GROUP);  // 行注：继续调用等值条件
        sessionMapper.delete(deleteSessionWrapper);  // 行注：调用删除
    }  // 行注：结束当前代码块

    // 行注：定义executeAfterCommit方法
    private void executeAfterCommit(Runnable task) {
        // 行注：判断是否满足当前条件
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();  // 行注：调用run
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：调用注册Synchronization
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * 在事务提交后执行回调逻辑。
             */
            @Override  // 行注：应用 @Override 注解
            // 行注：定义afterCommit方法
            public void afterCommit() {
                task.run();  // 行注：调用run
            }  // 行注：结束当前代码块
        });  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    /**
     * 搜索群。
     *
     * @param userId 用户 ID
     * @param keyword 搜索关键词
     * @return 群信息列表
     */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义搜索Groups方法
    public List<GroupDTO> searchGroups(Long userId, String keyword) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(keyword)) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String trimmedKeyword = keyword.trim();  // 行注：初始化trimmedKeyword

        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(ImGroupInfo::getDeleted, 0);  // 行注：调用等值条件

        Long parsedId;  // 行注：完成当前语句
        // 行注：尝试执行可能失败的逻辑
        try {
            parsedId = Long.parseLong(trimmedKeyword);  // 行注：初始化parsedID
        // 行注：执行当前方法调用
        } catch (NumberFormatException ignored) {
            parsedId = null;  // 行注：初始化parsedID
        }  // 行注：结束当前代码块

        final Long searchId = parsedId;  // 行注：初始化搜索ID
        // 行注：判断是否满足当前条件
        if (searchId != null) {
            // 行注：调用and
            wrapper.and(w -> w.eq(ImGroupInfo::getId, searchId)
                    .or().like(ImGroupInfo::getGroupName, trimmedKeyword));  // 行注：继续调用or
        // 行注：开始当前语句对应的代码块
        } else {
            wrapper.like(ImGroupInfo::getGroupName, trimmedKeyword);  // 行注：调用like
        }  // 行注：结束当前代码块
        wrapper.last("LIMIT 20");  // 行注：调用最后

        List<ImGroupInfo> groups = groupInfoMapper.selectList(wrapper);  // 行注：初始化groups
        // 行注：判断是否满足当前条件
        if (groups.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        List<Long> groupIds = groups.stream().map(ImGroupInfo::getId).toList();  // 行注：初始化群ID列表
        Map<Long, Long> memberCountMap = countMembers(groupIds);  // 行注：初始化成员数量映射
        List<ImGroupMember> myMemberships = listMembersByUserId(userId);  // 行注：初始化我的Memberships
        // 行注：调用流
        Map<Long, ImGroupMember> myMemberMap = myMemberships.stream()
                // 行注：继续调用过滤
                .filter(item -> groupIds.contains(item.getGroupId()))
                .collect(Collectors.toMap(ImGroupMember::getGroupId, item -> item, (left, right) -> left));  // 行注：继续调用收集

        return groups.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(group -> buildGroupDTO(group, myMemberMap.get(group.getId()), memberCountMap.getOrDefault(group.getId(), 0L).intValue()))
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
