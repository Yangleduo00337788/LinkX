package com.linkx.server.module.chat.service.impl;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.service.impl

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;  // 行注：引入 LambdaUpdateWrapper 类型
import com.linkx.server.common.AuditLogService;  // 行注：引入 AuditLogService 类型
import com.linkx.server.common.BusinessException;  // 行注：引入 BusinessException 类型
import com.linkx.server.common.ErrorCode;  // 行注：引入 ErrorCode 类型
import com.linkx.server.common.TextNormalizer;  // 行注：引入 TextNormalizer 类型
import com.linkx.server.entity.ImGroupInfo;  // 行注：引入 ImGroupInfo 类型
import com.linkx.server.entity.ImGroupMember;  // 行注：引入 ImGroupMember 类型
import com.linkx.server.entity.ImMessage;  // 行注：引入 ImMessage 类型
import com.linkx.server.entity.ImSession;  // 行注：引入 ImSession 类型
import com.linkx.server.entity.SysFriend;  // 行注：引入 SysFriend 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.ImGroupInfoMapper;  // 行注：引入 ImGroupInfoMapper 类型
import com.linkx.server.mapper.ImGroupMemberMapper;  // 行注：引入 ImGroupMemberMapper 类型
import com.linkx.server.mapper.ImMessageMapper;  // 行注：引入 ImMessageMapper 类型
import com.linkx.server.mapper.ImSessionMapper;  // 行注：引入 ImSessionMapper 类型
import com.linkx.server.mapper.SysFriendMapper;  // 行注：引入 SysFriendMapper 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.blacklist.service.BlacklistService;  // 行注：引入 BlacklistService 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.chat.dto.ChatSessionDTO;  // 行注：引入 ChatSessionDTO 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import com.linkx.server.module.chat.service.ChatService;  // 行注：引入 ChatService 类型
import com.linkx.server.module.chat.helper.ChatMessageHelper;  // 行注：引入 ChatMessageHelper 类型
import com.linkx.server.module.chat.ws.ChatEventPushService;  // 行注：引入 ChatEventPushService 类型
import com.linkx.server.module.chat.ws.ChatEventType;  // 行注：引入 ChatEventType 类型
import com.linkx.server.module.chat.ws.ChatMessagePayload;  // 行注：引入 ChatMessagePayload 类型
import com.linkx.server.module.chat.ws.ChatPresenceService;  // 行注：引入 ChatPresenceService 类型
import com.linkx.server.module.chat.ws.ChatReadReceiptPayload;  // 行注：引入 ChatReadReceiptPayload 类型
import com.linkx.server.module.chat.ws.ChatSessionPayload;  // 行注：引入 ChatSessionPayload 类型
import com.linkx.server.module.group.constant.GroupConstants;  // 行注：引入 GroupConstants 类型
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
import java.util.Collections;  // 行注：引入 Collections 类型
import java.util.Comparator;  // 行注：引入 Comparator 类型
import java.util.HashMap;  // 行注：引入 HashMap 类型
import java.util.HashSet;  // 行注：引入 HashSet 类型
import java.util.LinkedHashSet;  // 行注：引入 LinkedHashSet 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Map;  // 行注：引入 Map 类型
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/**
 * 聊天核心实现：单聊须好友、群聊须成员；消息入库后事务提交再 WebSocket 推送；
 * 支持 clientMessageId 幂等、@全员/成员、已读与撤回。
 */
@Slf4j  // 行注：应用 @Slf4j 注解
@Service  // 行注：应用 @Service 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatServiceImpl 类
public class ChatServiceImpl implements ChatService {

    private static final int SESSION_PREVIEW_MAX_LENGTH = 500;  // 行注：定义会话预览最大长度常量
    private static final int MESSAGE_CONTENT_MAX_LENGTH = 5000;  // 行注：定义消息内容最大长度常量

    // 行注：定义 MentionContext 记录类型
    private record MentionContext(boolean mentionAll, List<Long> mentionUserIds) {
        private static final MentionContext EMPTY = new MentionContext(false, List.of());  // 行注：定义空常量

        // 行注：定义是否包含@提醒方法
        private boolean hasMention() {
            return mentionAll || !mentionUserIds.isEmpty();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    private final ImSessionMapper sessionMapper;  // 行注：注入会话Mapper依赖
    private final ImMessageMapper messageMapper;  // 行注：注入消息Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖
    private final SysFriendMapper friendMapper;  // 行注：注入好友Mapper依赖
    private final SysFileMapper fileMapper;  // 行注：注入文件Mapper依赖
    private final ImGroupInfoMapper groupInfoMapper;  // 行注：注入群信息Mapper依赖
    private final ImGroupMemberMapper groupMemberMapper;  // 行注：注入群成员Mapper依赖
    private final BlacklistService blacklistService;  // 行注：注入黑名单服务依赖
    private final ChatEventPushService chatEventPushService;  // 行注：注入聊天事件推送服务依赖
    private final ChatPresenceService chatPresenceService;  // 行注：注入聊天在线状态服务依赖
    private final AuditLogService auditLogService;  // 行注：注入审计日志服务依赖
    private final ChatMessageHelper chatMessageHelper;  // 行注：注入聊天消息辅助器依赖

    /** {@inheritDoc} 单聊校验好友与黑名单；群聊校验成员与禁言。 */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义发送消息方法
    public MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Integer sessionType, String clientMessageId, Boolean mentionAll, List<Long> mentionUserIds) {
        // 文本消息统一做长度与换行规范化，既保护数据库也保证不同入口行为一致。
        // 行注：初始化规范化后的内容
        String normalizedContent = TextNormalizer.normalizeRequiredMultiline(content, MESSAGE_CONTENT_MAX_LENGTH, "消息内容");
        int resolvedMsgType = resolveTextMessageType(msgType);  // 行注：初始化解析后的消息类型
        int resolvedSessionType = resolveSessionType(sessionType);  // 行注：初始化解析后的会话类型
        // 群聊和单聊的校验规则差异很大，尽早分流到专门逻辑中处理。
        // 行注：判断是否满足当前条件
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP) {
            // 行注：返回处理结果
            return sendGroupMessage(fromUserId, toUserId, normalizedContent, resolvedMsgType, clientMessageId, mentionAll, mentionUserIds);
        }  // 行注：结束当前代码块
        // @提醒只在群聊里有意义，单聊直接拒绝，避免前后端出现语义不一致。
        // 行注：判断是否满足当前条件
        if (Boolean.TRUE.equals(mentionAll) || (mentionUserIds != null && !mentionUserIds.isEmpty())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "单聊消息不支持@提醒");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return sendSingleMessage(fromUserId, toUserId, normalizedContent, resolvedMsgType, clientMessageId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** {@inheritDoc} */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义发送文件消息方法
    public MessageDTO sendFileMessage(Long fromUserId, Long toUserId, Long fileId, Integer msgType, Integer sessionType, String clientMessageId) {
        // 文件消息本质是“引用已上传文件的 URL”，发送前先校验文件所有权。
        SysFile sysFile = fileMapper.selectById(fileId);  // 行注：初始化系统文件
        // 行注：判断是否满足当前条件
        if (sysFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文件不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!fromUserId.equals(sysFile.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权发送该文件");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        int resolvedMsgType = resolveFileMessageType(msgType);  // 行注：初始化解析后的消息类型
        int resolvedSessionType = resolveSessionType(sessionType);  // 行注：初始化解析后的会话类型
        // 行注：判断是否满足当前条件
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP) {
            // 行注：返回处理结果
            return sendGroupMessage(fromUserId, toUserId, sysFile.getFileUrl(), resolvedMsgType, clientMessageId, false, List.of());
        }  // 行注：结束当前代码块
        return sendSingleMessage(fromUserId, toUserId, sysFile.getFileUrl(), resolvedMsgType, clientMessageId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** {@inheritDoc} 无权限访问单聊时返回空列表而非抛错。 */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取聊天历史方法
    public List<MessageDTO> getChatHistory(Long userId, Long targetId, Integer sessionType, int page, int size) {
        int resolvedSessionType = resolveSessionType(sessionType);  // 行注：初始化解析后的会话类型
        return resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP  // 行注：返回处理结果
                // 行注：调用获取群历史
                ? getGroupHistory(userId, targetId, page, size)
                : getSingleHistory(userId, targetId, page, size);  // 行注：调用获取单聊历史
    }  // 行注：结束当前代码块

    /** {@inheritDoc} */
    @Override  // 行注：应用 @Override 注解
    // 行注：定义获取会话列表方法
    public List<ChatSessionDTO> getSessions(Long userId) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getUserId, userId)
                .orderByDesc(ImSession::getLastMessageTime, ImSession::getId);  // 行注：继续调用排序降序
        List<ImSession> sessions = sessionMapper.selectList(wrapper);  // 行注：初始化会话列表
        // 行注：判断是否满足当前条件
        if (sessions.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：调用流
        List<ImSession> singleSessions = sessions.stream()
                // 行注：继续调用过滤
                .filter(session -> session.getSessionType() == ChatConstants.SESSION_TYPE_SINGLE)
                .toList();  // 行注：继续调用转为列表
        // 行注：调用流
        List<ImSession> groupSessions = sessions.stream()
                // 行注：继续调用过滤
                .filter(session -> session.getSessionType() == ChatConstants.SESSION_TYPE_GROUP)
                .toList();  // 行注：继续调用转为列表

        // 单聊会话需要补齐对端资料、好友状态与黑名单状态。
        // 行注：初始化用户映射
        Map<Long, SysUser> userMap = chatMessageHelper.loadUserMap(singleSessions.stream().map(ImSession::getTargetId).collect(Collectors.toSet()));
        Map<Long, Boolean> blacklistCache = new HashMap<>();  // 行注：初始化黑名单缓存
        Map<Long, Boolean> friendshipCache = new HashMap<>();  // 行注：初始化好友关系缓存

        // 群聊会话则需要补齐群资料、我的成员身份和群成员数量。
        Set<Long> groupIds = groupSessions.stream().map(ImSession::getTargetId).collect(Collectors.toSet());  // 行注：初始化群ID列表
        Map<Long, ImGroupInfo> groupMap = loadActiveGroupMap(groupIds);  // 行注：初始化群映射
        Map<Long, ImGroupMember> myGroupMemberMap = loadGroupMembersByUser(userId, groupIds);  // 行注：初始化我的群成员映射
        Map<Long, Integer> groupMemberCountMap = loadGroupMemberCount(groupIds);  // 行注：初始化群成员数量映射

        return sessions.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(session -> buildSessionDTO(userId, session, userMap, blacklistCache, friendshipCache, groupMap, myGroupMemberMap, groupMemberCountMap))
                // 行注：继续调用过滤
                .filter(dto -> dto != null)
                // 行注：继续调用排序
                .sorted(Comparator.comparing(ChatSessionDTO::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder()))
                        // 行注：继续调用再比较
                        .thenComparing(ChatSessionDTO::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /** {@inheritDoc} 推送在事务提交后执行，避免读到未提交数据。 */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义标记已读方法
    public void markAsRead(Long userId, Long targetId, Integer sessionType) {
        int resolvedSessionType = resolveSessionType(sessionType);  // 行注：初始化解析后的会话类型
        // 行注：判断是否满足当前条件
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_SINGLE && !canAccessSingleChat(userId, targetId)) {
            // 行注：补充当前表达式片段
            log.warn("Chat mark read rejected, userId={}, targetId={}, sessionType={}, reason=conversation_unavailable",
                    userId, targetId, resolvedSessionType);  // 行注：完成当前语句
            throw new BusinessException(ErrorCode.FORBIDDEN, "无法标记该会话为已读");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        ImSession session = getSession(userId, targetId, resolvedSessionType);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session != null && session.getUnreadCount() != 0) {
            session.setUnreadCount(0);  // 行注：调用设置未读数量
            sessionMapper.updateById(session);  // 行注：调用更新ID
        }  // 行注：结束当前代码块

        LocalDateTime readTime = LocalDateTime.now();  // 行注：初始化已读时间

        // 行注：判断是否满足当前条件
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_SINGLE) {
            List<ImMessage> unreadMessages = listUnreadSingleMessages(targetId, userId);  // 行注：初始化未读消息
            List<Long> messageIds = batchMarkAsRead(unreadMessages, readTime);  // 行注：初始化消息ID列表
            // 行注：补充当前表达式片段
            log.info("Chat messages marked read, userId={}, targetId={}, sessionType={}, messageCount={}",
                    userId, targetId, resolvedSessionType, messageIds.size());  // 行注：调用大小

            // 行注：调用executeAfterCommit
            executeAfterCommit(() -> {
                pushSessionUpdate(userId, targetId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：调用推送会话更新
                // 行注：判断是否满足当前条件
                if (!messageIds.isEmpty()) {
                    // 行注：补充当前表达式片段
                    chatEventPushService.sendToUser(
                            // 行注：补充当前表达式片段
                            targetId,
                            // 行注：补充当前表达式片段
                            ChatEventType.READ_RECEIPT,
                            // 行注：调用聊天已读Receipt载荷
                            new ChatReadReceiptPayload(userId, ChatConstants.SESSION_TYPE_SINGLE, userId, readTime, messageIds)
                    );  // 行注：结束当前参数配置
                }  // 行注：结束当前代码块
            });  // 行注：结束当前代码块
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (resolvedSessionType == ChatConstants.SESSION_TYPE_GROUP) {
            requireActiveGroup(targetId);  // 行注：调用require启用群
            ImGroupMember member = requireGroupMember(targetId, userId);  // 行注：初始化成员
            member.setLastMessageReadTime(readTime);  // 行注：调用设置最后消息已读时间
            groupMemberMapper.updateById(member);  // 行注：调用更新ID
            log.info("Chat group session marked read, userId={}, groupId={}, readTime={}", userId, targetId, readTime);  // 行注：执行初始化操作
            // 行注：调用流
            List<Long> groupMemberUserIds = listGroupMembers(targetId).stream()
                    // 行注：继续调用映射
                    .map(ImGroupMember::getUserId)
                    // 行注：继续调用过滤
                    .filter(memberUserId -> !memberUserId.equals(userId))
                    .toList();  // 行注：继续调用转为列表
            // 行注：调用executeAfterCommit
            executeAfterCommit(() -> {
                pushSessionUpdate(userId, targetId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：调用推送会话更新
                // 行注：判断是否满足当前条件
                if (!groupMemberUserIds.isEmpty()) {
                    // 行注：补充当前表达式片段
                    chatEventPushService.sendToUsers(
                            // 行注：补充当前表达式片段
                            groupMemberUserIds,
                            // 行注：补充当前表达式片段
                            ChatEventType.READ_RECEIPT,
                            // 行注：调用of
                            new ChatReadReceiptPayload(targetId, ChatConstants.SESSION_TYPE_GROUP, userId, readTime, List.of())
                    );  // 行注：结束当前参数配置
                }  // 行注：结束当前代码块
            });  // 行注：结束当前代码块
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        executeAfterCommit(() -> pushSessionUpdate(userId, targetId, resolvedSessionType));  // 行注：调用executeAfterCommit
    }  // 行注：结束当前代码块

    /** {@inheritDoc} 默认 2 分钟内可撤回。 */
    @Override  // 行注：应用 @Override 注解
    @Transactional  // 行注：应用 @Transactional 注解
    // 行注：定义recall消息方法
    public void recallMessage(Long userId, Long messageId) {
        ImMessage message = messageMapper.selectById(messageId);  // 行注：初始化消息
        // 行注：判断是否满足当前条件
        if (message == null) {
            auditLogService.recordFailure("CHAT_RECALL_MESSAGE", userId, "MESSAGE", messageId, "message_not_found");  // 行注：调用记录Failure
            throw new BusinessException(ErrorCode.NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!message.getFromUserId().equals(userId)) {
            auditLogService.recordFailure("CHAT_RECALL_MESSAGE", userId, "MESSAGE", messageId, "operator_not_owner");  // 行注：调用记录Failure
            throw new BusinessException(ErrorCode.FORBIDDEN);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (message.getStatus() == ChatConstants.MESSAGE_STATUS_RECALLED) {
            auditLogService.recordFailure("CHAT_RECALL_MESSAGE", userId, "MESSAGE", messageId, "already_recalled");  // 行注：调用记录Failure
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息已撤回");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (message.getCreateTime() != null && message.getCreateTime().isBefore(LocalDateTime.now().minusMinutes(2))) {
            auditLogService.recordFailure("CHAT_RECALL_MESSAGE", userId, "MESSAGE", messageId, "recall_timeout");  // 行注：调用记录Failure
            throw new BusinessException(ErrorCode.BAD_REQUEST, "消息发送超过2分钟，无法撤回");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        int sessionType = resolveMessageSessionType(message);  // 行注：初始化会话类型
        message.setStatus(ChatConstants.MESSAGE_STATUS_RECALLED);  // 行注：调用设置状态
        messageMapper.updateById(message);  // 行注：调用更新ID
        // 行注：补充当前表达式片段
        log.info("Chat message recalled, userId={}, messageId={}, sessionType={}, targetId={}",
                userId, messageId, sessionType, message.getToUserId());  // 行注：调用获取转为用户ID
        // 行注：执行初始化操作
        auditLogService.recordSuccess("CHAT_RECALL_MESSAGE", userId, "MESSAGE", messageId, "sessionType=" + sessionType);

        // 行注：判断是否满足当前条件
        if (sessionType == ChatConstants.SESSION_TYPE_GROUP) {
            Long groupId = message.getToUserId();  // 行注：初始化群ID
            List<ImGroupMember> members = listGroupMembers(groupId);  // 行注：初始化members
            // 行注：初始化成员用户ID列表
            Set<Long> memberUserIds = members.stream().map(ImGroupMember::getUserId).collect(Collectors.toCollection(LinkedHashSet::new));
            refreshGroupSessionPreviews(groupId, memberUserIds);  // 行注：调用刷新群会话Previews
            Set<Long> relatedUserIds = new LinkedHashSet<>();  // 行注：初始化related用户ID列表
            relatedUserIds.add(message.getFromUserId());  // 行注：调用添加
            relatedUserIds.addAll(chatMessageHelper.parseMentionUserIds(message.getMentionUserIds()));  // 行注：调用添加全部
            // 行注：补充当前表达式片段
            MessageDTO messageDTO = chatMessageHelper.toMessageDTO(
                    message, ChatConstants.SESSION_TYPE_GROUP, chatMessageHelper.loadUserMap(relatedUserIds), Map.of());  // 行注：调用加载用户映射
            executeAfterCommit(() -> notifyGroupRecall(messageDTO, groupId, memberUserIds));  // 行注：调用executeAfterCommit
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        refreshSingleSessionPreviews(message.getFromUserId(), message.getToUserId());  // 行注：调用获取用户ID
        // 行注：补充当前表达式片段
        MessageDTO messageDTO = chatMessageHelper.toMessageDTO(
                // 行注：调用加载用户映射
                message, ChatConstants.SESSION_TYPE_SINGLE, chatMessageHelper.loadUserMap(Set.of(message.getFromUserId())), Map.of());
        executeAfterCommit(() -> notifySingleRecall(messageDTO, message.getFromUserId(), message.getToUserId()));  // 行注：调用获取用户ID
    }  // 行注：结束当前代码块

    // 行注：定义发送单聊消息方法
    private MessageDTO sendSingleMessage(Long fromUserId, Long toUserId, String content, Integer msgType, String clientMessageId) {
        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);  // 行注：初始化规范化后的客户端消息ID
        // 如果客户端因为超时或弱网重试，优先按 clientMessageId 返回已存在消息，保证幂等。
        // 行注：补充当前表达式片段
        MessageDTO existing = findExistingOutboundMessage(
                fromUserId, normalizedClientMessageId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：完成当前语句
        // 行注：判断是否满足当前条件
        if (existing != null) {
            return existing;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        SysUser toUser = userMapper.selectById(toUserId);  // 行注：初始化转为用户
        // 行注：判断是否满足当前条件
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 单聊必须满足双方可对话条件，例如存在好友关系且未互相拉黑。
        assertSingleChatAllowed(fromUserId, toUserId);  // 行注：调用assert单聊聊天允许

        LocalDateTime now = LocalDateTime.now();  // 行注：初始化now
        String preview = buildPreview(content, msgType);  // 行注：初始化预览
        String sessionPreview = truncateSessionPreview(preview);  // 行注：初始化会话预览

        // 发送者自己的会话只更新时间和预览，不增加未读数。
        ImSession selfSession = getOrCreateSession(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：初始化self会话
        selfSession.setLastMessage(sessionPreview);  // 行注：调用设置最后消息
        selfSession.setLastMessageTime(now);  // 行注：调用设置最后消息时间
        sessionMapper.updateById(selfSession);  // 行注：调用更新ID

        // 接收者会话除了更新时间，还要累加未读数，驱动列表红点与提醒。
        ImSession peerSession = getOrCreateSession(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：初始化peer会话
        peerSession.setLastMessage(sessionPreview);  // 行注：调用设置最后消息
        peerSession.setLastMessageTime(now);  // 行注：调用设置最后消息时间
        peerSession.setUnreadCount(peerSession.getUnreadCount() + 1);  // 行注：调用设置未读数量
        sessionMapper.updateById(peerSession);  // 行注：调用更新ID

        ImMessage message = new ImMessage();  // 行注：初始化消息
        message.setSessionId(selfSession.getId());  // 行注：调用设置会话ID
        message.setFromUserId(fromUserId);  // 行注：调用设置用户ID
        message.setToUserId(toUserId);  // 行注：调用设置转为用户ID
        message.setContent(content);  // 行注：调用设置内容
        message.setMsgType(msgType);  // 行注：调用设置消息类型
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);  // 行注：调用设置状态
        message.setClientMessageId(normalizedClientMessageId);  // 行注：调用设置客户端消息ID
        // 行注：尝试执行可能失败的逻辑
        try {
            messageMapper.insert(message);  // 行注：调用insert
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException exception) {
            // 数据库唯一索引是最终兜底，撞库时再次回查已有消息并按成功返回。
            // 行注：补充当前表达式片段
            MessageDTO duplicate = findExistingOutboundMessage(
                    fromUserId, normalizedClientMessageId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：完成当前语句
            // 行注：判断是否满足当前条件
            if (duplicate != null) {
                return duplicate;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            throw exception;  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // 行注：补充当前表达式片段
        MessageDTO messageDTO = chatMessageHelper.toMessageDTO(
                message, ChatConstants.SESSION_TYPE_SINGLE, chatMessageHelper.loadUserMap(Set.of(fromUserId)), Map.of());  // 行注：调用加载用户映射
        messageDTO.setClientMessageId(normalizedClientMessageId);  // 行注：调用设置客户端消息ID
        // 行注：补充当前表达式片段
        log.info("Chat single message sent, fromUserId={}, toUserId={}, messageId={}, clientMessageId={}",
                fromUserId, toUserId, messageDTO.getId(), messageDTO.getClientMessageId());  // 行注：调用获取ID
        // 等事务真正提交后再推送，避免接收端收到消息但数据库里还查不到。
        executeAfterCommit(() -> notifySingleMessage(messageDTO, fromUserId, toUserId));  // 行注：调用executeAfterCommit
        return messageDTO;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义发送群消息方法
    private MessageDTO sendGroupMessage(
            // 行注：补充当前表达式片段
            Long fromUserId,
            // 行注：补充当前表达式片段
            Long groupId,
            // 行注：补充当前表达式片段
            String content,
            // 行注：补充当前表达式片段
            Integer msgType,
            // 行注：补充当前表达式片段
            String clientMessageId,
            // 行注：补充当前表达式片段
            Boolean mentionAll,
            // 行注：补充当前表达式片段
            List<Long> mentionUserIds
    // 行注：开始当前语句对应的代码块
    ) {
        String normalizedClientMessageId = normalizeClientMessageId(clientMessageId);  // 行注：初始化规范化后的客户端消息ID
        // 群聊同样依赖 clientMessageId 抗重试，保证前端重复发送不会生成多条消息。
        // 行注：补充当前表达式片段
        MessageDTO existing = findExistingOutboundMessage(
                fromUserId, normalizedClientMessageId, groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：完成当前语句
        // 行注：判断是否满足当前条件
        if (existing != null) {
            return existing;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        requireActiveGroup(groupId);  // 行注：调用require启用群
        ImGroupMember senderMember = requireGroupMember(groupId, fromUserId);  // 行注：初始化sender成员
        // 群成员存在但被禁言时，仍可浏览历史和接收消息，但不能发送新消息。
        // 行注：判断是否满足当前条件
        if (isMuted(senderMember)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被禁言");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        List<ImGroupMember> members = listGroupMembers(groupId);  // 行注：初始化members
        // 行注：判断是否满足当前条件
        if (members.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "群成员不存在");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        // @全员/@成员需要结合发送者角色、消息类型和群成员列表做统一裁剪与校验。
        // 行注：初始化@提醒Context
        MentionContext mentionContext = resolveGroupMentionContext(senderMember, members, msgType, mentionAll, mentionUserIds);
        LocalDateTime now = LocalDateTime.now();  // 行注：初始化now
        String preview = buildPreview(content, msgType);  // 行注：初始化预览
        Set<Long> memberUserIds = new LinkedHashSet<>();  // 行注：初始化成员用户ID列表

        // 行注：遍历当前集合或范围
        for (ImGroupMember member : members) {
            memberUserIds.add(member.getUserId());  // 行注：调用添加
            // 群聊是“一条消息，多份会话视图”，每个成员的会话预览和未读数要分别更新。
            ImSession session = getOrCreateSession(member.getUserId(), groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：初始化会话
            // 行注：调用设置最后消息
            session.setLastMessage(truncateSessionPreview(buildGroupPreview(preview, mentionContext, fromUserId, member.getUserId())));
            session.setLastMessageTime(now);  // 行注：调用设置最后消息时间
            // 行注：判断是否满足当前条件
            if (!member.getUserId().equals(fromUserId)) {
                session.setUnreadCount(session.getUnreadCount() + 1);  // 行注：调用设置未读数量
            }  // 行注：结束当前代码块
            sessionMapper.updateById(session);  // 行注：调用更新ID
        }  // 行注：结束当前代码块

        ImMessage message = new ImMessage();  // 行注：初始化消息
        message.setSessionId(groupId);  // 行注：调用设置会话ID
        message.setFromUserId(fromUserId);  // 行注：调用设置用户ID
        message.setToUserId(groupId);  // 行注：调用设置转为用户ID
        message.setContent(content);  // 行注：调用设置内容
        message.setMsgType(msgType);  // 行注：调用设置消息类型
        message.setMentionAll(mentionContext.mentionAll());  // 行注：调用设置@提醒全部
        message.setMentionUserIds(joinMentionUserIds(mentionContext.mentionUserIds()));  // 行注：调用设置@提醒用户ID列表
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);  // 行注：调用设置状态
        message.setClientMessageId(normalizedClientMessageId);  // 行注：调用设置客户端消息ID
        // 行注：尝试执行可能失败的逻辑
        try {
            messageMapper.insert(message);  // 行注：调用insert
        // 行注：执行当前方法调用
        } catch (DuplicateKeyException exception) {
            // 群聊消息也由唯一索引兜底幂等，避免弱网重试造成重复入库。
            // 行注：补充当前表达式片段
            MessageDTO duplicate = findExistingOutboundMessage(
                    fromUserId, normalizedClientMessageId, groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：完成当前语句
            // 行注：判断是否满足当前条件
            if (duplicate != null) {
                return duplicate;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            throw exception;  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块

        Set<Long> relatedUserIds = new LinkedHashSet<>();  // 行注：初始化related用户ID列表
        relatedUserIds.add(fromUserId);  // 行注：调用添加
        relatedUserIds.addAll(mentionContext.mentionUserIds());  // 行注：调用添加全部
        // 行注：补充当前表达式片段
        MessageDTO messageDTO = chatMessageHelper.toMessageDTO(
                message, ChatConstants.SESSION_TYPE_GROUP, chatMessageHelper.loadUserMap(relatedUserIds), Map.of());  // 行注：调用加载用户映射
        messageDTO.setClientMessageId(normalizedClientMessageId);  // 行注：调用设置客户端消息ID
        // 行注：补充当前表达式片段
        log.info("Chat group message sent, fromUserId={}, groupId={}, messageId={}, mentionAll={}, mentionCount={}, clientMessageId={}",
                // 行注：调用获取ID
                fromUserId, groupId, messageDTO.getId(), mentionContext.mentionAll(), mentionContext.mentionUserIds().size(), messageDTO.getClientMessageId());
        // 群消息推送同样延迟到事务提交后，避免成员拉到不存在的消息或旧会话预览。
        executeAfterCommit(() -> notifyGroupMessage(messageDTO, groupId, memberUserIds));  // 行注：调用executeAfterCommit
        return messageDTO;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取单聊历史方法
    private List<MessageDTO> getSingleHistory(Long userId, Long targetId, int page, int size) {
        // 行注：判断是否满足当前条件
        if (!canAccessSingleChat(userId, targetId)) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：补充当前表达式片段
        wrapper.and(w -> w
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getFromUserId, userId)
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getToUserId, targetId)
                        // 行注：继续调用or
                        .or()
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getFromUserId, targetId)
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getToUserId, userId))
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size + " OFFSET " + Math.max(page - 1, 0) * size);  // 行注：继续调用最后
        return toMessageList(messageMapper.selectList(wrapper), ChatConstants.SESSION_TYPE_SINGLE);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取群历史方法
    private List<MessageDTO> getGroupHistory(Long userId, Long groupId, int page, int size) {
        requireActiveGroup(groupId);  // 行注：调用require启用群
        requireGroupMember(groupId, userId);  // 行注：调用require群成员

        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getToUserId, groupId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getSessionId, groupId)
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT " + size + " OFFSET " + Math.max(page - 1, 0) * size);  // 行注：继续调用最后
        return toMessageList(messageMapper.selectList(wrapper), ChatConstants.SESSION_TYPE_GROUP);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义转为消息列表方法
    private List<MessageDTO> toMessageList(List<ImMessage> rawMessages, Integer sessionType) {
        List<ImMessage> messages = new ArrayList<>(rawMessages);  // 行注：初始化消息
        Collections.reverse(messages);  // 行注：调用reverse
        Set<Long> userIds = new HashSet<>();  // 行注：初始化用户ID列表
        // 行注：遍历当前集合或范围
        for (ImMessage message : messages) {
            userIds.add(message.getFromUserId());  // 行注：调用添加
            userIds.addAll(chatMessageHelper.parseMentionUserIds(message.getMentionUserIds()));  // 行注：调用添加全部
        }  // 行注：结束当前代码块
        Map<Long, SysUser> userMap = chatMessageHelper.loadUserMap(userIds);  // 行注：初始化用户映射
        Map<String, SysFile> fileMap = chatMessageHelper.loadFileMap(messages);  // 行注：初始化文件映射
        return messages.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(message -> chatMessageHelper.toMessageDTO(message, sessionType, userMap, fileMap))
                .collect(Collectors.toList());  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义notify单聊消息方法
    private void notifySingleMessage(MessageDTO messageDTO, Long fromUserId, Long toUserId) {
        // 行注：补充当前表达式片段
        chatEventPushService.sendToUsers(
                // 行注：调用of
                List.of(fromUserId, toUserId),
                // 行注：补充当前表达式片段
                ChatEventType.MESSAGE,
                // 行注：调用聊天消息载荷
                new ChatMessagePayload(messageDTO)
        );  // 行注：结束当前参数配置
        pushSessionUpdate(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：调用推送会话更新
        pushSessionUpdate(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：调用推送会话更新
    }  // 行注：结束当前代码块

    // 行注：定义notify群消息方法
    private void notifyGroupMessage(MessageDTO messageDTO, Long groupId, Collection<Long> memberUserIds) {
        // 行注：补充当前表达式片段
        chatEventPushService.sendToUsers(
                // 行注：补充当前表达式片段
                memberUserIds,
                // 行注：补充当前表达式片段
                ChatEventType.MESSAGE,
                // 行注：调用聊天消息载荷
                new ChatMessagePayload(messageDTO)
        );  // 行注：结束当前参数配置
        // 行注：遍历当前集合或范围
        for (Long memberUserId : memberUserIds) {
            pushSessionUpdate(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：调用推送会话更新
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义notify单聊Recall方法
    private void notifySingleRecall(MessageDTO messageDTO, Long fromUserId, Long toUserId) {
        // 行注：补充当前表达式片段
        chatEventPushService.sendToUsers(
                // 行注：调用of
                List.of(fromUserId, toUserId),
                // 行注：补充当前表达式片段
                ChatEventType.MESSAGE_RECALLED,
                // 行注：调用聊天消息载荷
                new ChatMessagePayload(messageDTO)
        );  // 行注：结束当前参数配置
        pushSessionUpdate(fromUserId, toUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：调用推送会话更新
        pushSessionUpdate(toUserId, fromUserId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：调用推送会话更新
    }  // 行注：结束当前代码块

    // 行注：定义notify群Recall方法
    private void notifyGroupRecall(MessageDTO messageDTO, Long groupId, Collection<Long> memberUserIds) {
        // 行注：补充当前表达式片段
        chatEventPushService.sendToUsers(
                // 行注：补充当前表达式片段
                memberUserIds,
                // 行注：补充当前表达式片段
                ChatEventType.MESSAGE_RECALLED,
                // 行注：调用聊天消息载荷
                new ChatMessagePayload(messageDTO)
        );  // 行注：结束当前参数配置
        // 行注：遍历当前集合或范围
        for (Long memberUserId : memberUserIds) {
            pushSessionUpdate(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：调用推送会话更新
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义推送会话更新方法
    private void pushSessionUpdate(Long userId, Long targetId, Integer sessionType) {
        ChatSessionDTO sessionDTO = getSessionDTO(userId, targetId, sessionType);  // 行注：初始化会话DTO
        // 行注：判断是否满足当前条件
        if (sessionDTO == null) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        chatEventPushService.sendToUser(userId, ChatEventType.SESSION, new ChatSessionPayload(sessionDTO));  // 行注：调用发送转为用户
    }  // 行注：结束当前代码块

    // 行注：定义获取会话DTO方法
    private ChatSessionDTO getSessionDTO(Long userId, Long targetId, Integer sessionType) {
        ImSession session = getSession(userId, targetId, sessionType);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (sessionType == ChatConstants.SESSION_TYPE_SINGLE) {
            return buildSessionDTO(  // 行注：返回处理结果
                    // 行注：补充当前表达式片段
                    userId,
                    // 行注：补充当前表达式片段
                    session,
                    // 行注：调用加载用户映射
                    chatMessageHelper.loadUserMap(Set.of(targetId)),
                    // 行注：执行当前方法调用
                    new HashMap<>(),
                    // 行注：执行当前方法调用
                    new HashMap<>(),
                    // 行注：调用of
                    Map.of(),
                    // 行注：调用of
                    Map.of(),
                    // 行注：调用of
                    Map.of()
            );  // 行注：结束当前参数配置
        }  // 行注：结束当前代码块
        return buildSessionDTO(  // 行注：返回处理结果
                // 行注：补充当前表达式片段
                userId,
                // 行注：补充当前表达式片段
                session,
                // 行注：调用of
                Map.of(),
                // 行注：执行当前方法调用
                new HashMap<>(),
                // 行注：执行当前方法调用
                new HashMap<>(),
                // 行注：调用of
                loadActiveGroupMap(Set.of(targetId)),
                // 行注：调用of
                loadGroupMembersByUser(userId, Set.of(targetId)),
                // 行注：调用of
                loadGroupMemberCount(Set.of(targetId))
        );  // 行注：结束当前参数配置
    }  // 行注：结束当前代码块

    // 行注：定义构建会话DTO方法
    private ChatSessionDTO buildSessionDTO(
            // 行注：补充当前表达式片段
            Long userId,
            // 行注：补充当前表达式片段
            ImSession session,
            // 行注：补充当前表达式片段
            Map<Long, SysUser> userMap,
            // 行注：补充当前表达式片段
            Map<Long, Boolean> blacklistCache,
            // 行注：补充当前表达式片段
            Map<Long, Boolean> friendshipCache,
            // 行注：补充当前表达式片段
            Map<Long, ImGroupInfo> groupMap,
            // 行注：补充当前表达式片段
            Map<Long, ImGroupMember> myGroupMemberMap,
            // 行注：补充当前表达式片段
            Map<Long, Integer> groupMemberCountMap
    // 行注：开始当前语句对应的代码块
    ) {
        // 行注：判断是否满足当前条件
        if (session.getSessionType() == ChatConstants.SESSION_TYPE_SINGLE) {
            Boolean friendshipAllowed = friendshipCache.get(session.getTargetId());  // 行注：初始化friendship允许
            // 行注：判断是否满足当前条件
            if (friendshipAllowed == null) {
                friendshipAllowed = hasFriendRelation(userId, session.getTargetId());  // 行注：初始化friendship允许
                friendshipCache.put(session.getTargetId(), friendshipAllowed);  // 行注：调用put
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (!Boolean.TRUE.equals(friendshipAllowed)) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
            Boolean cached = blacklistCache.get(session.getTargetId());  // 行注：初始化cached
            // 行注：判断是否满足当前条件
            if (cached == null) {
                // 行注：调用是否Blacklisted
                cached = blacklistService.isBlacklisted(userId, session.getTargetId())
                        || blacklistService.isBlacklisted(session.getTargetId(), userId);  // 行注：调用是否Blacklisted
                blacklistCache.put(session.getTargetId(), cached);  // 行注：调用put
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (Boolean.TRUE.equals(cached)) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块

            SysUser targetUser = userMap.get(session.getTargetId());  // 行注：初始化target用户
            // 行注：判断是否满足当前条件
            if (targetUser == null) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块

            ChatSessionDTO dto = baseSessionDTO(session);  // 行注：初始化DTO
            dto.setTargetNickname(targetUser.getNickname());  // 行注：调用设置TargetNickname
            dto.setTargetUsername(targetUser.getUsername());  // 行注：调用设置TargetUsername
            dto.setTargetAvatar(targetUser.getAvatar());  // 行注：调用设置Target头像
            dto.setTargetOnline(chatPresenceService.isOnline(targetUser.getId()));  // 行注：调用设置Target在线
            return dto;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ImGroupInfo groupInfo = groupMap.get(session.getTargetId());  // 行注：初始化群信息
        ImGroupMember member = myGroupMemberMap.get(session.getTargetId());  // 行注：初始化成员
        // 行注：判断是否满足当前条件
        if (groupInfo == null || member == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        ChatSessionDTO dto = baseSessionDTO(session);  // 行注：初始化DTO
        dto.setTargetNickname(resolveGroupDisplayName(groupInfo, member));  // 行注：调用设置TargetNickname
        dto.setTargetUsername("group-" + groupInfo.getId());  // 行注：调用设置TargetUsername
        dto.setTargetAvatar(groupInfo.getGroupAvatar());  // 行注：调用设置Target头像
        dto.setMemberCount(groupMemberCountMap.getOrDefault(groupInfo.getId(), 0));  // 行注：调用设置成员数量
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

    // 行注：定义基础会话DTO方法
    private ChatSessionDTO baseSessionDTO(ImSession session) {
        ChatSessionDTO dto = new ChatSessionDTO();  // 行注：初始化DTO
        dto.setId(session.getId());  // 行注：调用设置ID
        dto.setUserId(session.getUserId());  // 行注：调用设置用户ID
        dto.setTargetId(session.getTargetId());  // 行注：调用设置TargetID
        dto.setSessionType(session.getSessionType());  // 行注：调用设置会话类型
        dto.setLastMessage(session.getLastMessage());  // 行注：调用设置最后消息
        dto.setLastMessageTime(session.getLastMessageTime());  // 行注：调用设置最后消息时间
        dto.setUnreadCount(session.getUnreadCount());  // 行注：调用设置未读数量
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取Or创建会话方法
    private ImSession getOrCreateSession(Long userId, Long targetId, Integer sessionType) {
        ImSession session = getSession(userId, targetId, sessionType);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session == null) {
            session = new ImSession();  // 行注：初始化会话
            session.setUserId(userId);  // 行注：调用设置用户ID
            session.setTargetId(targetId);  // 行注：调用设置TargetID
            session.setSessionType(sessionType);  // 行注：调用设置会话类型
            session.setUnreadCount(0);  // 行注：调用设置未读数量
            // 行注：尝试执行可能失败的逻辑
            try {
                sessionMapper.insert(session);  // 行注：调用insert
            // 行注：执行当前方法调用
            } catch (DuplicateKeyException exception) {
                return requireSession(userId, targetId, sessionType);  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return session;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义获取会话方法
    private ImSession getSession(Long userId, Long targetId, Integer sessionType) {
        LambdaQueryWrapper<ImSession> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImSession::getUserId, userId)
                // 行注：继续调用等值条件
                .eq(ImSession::getTargetId, targetId)
                .eq(ImSession::getSessionType, sessionType);  // 行注：继续调用等值条件
        return sessionMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义require会话方法
    private ImSession requireSession(Long userId, Long targetId, Integer sessionType) {
        ImSession session = getSession(userId, targetId, sessionType);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "会话初始化失败");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return session;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义列表未读单聊消息方法
    private List<ImMessage> listUnreadSingleMessages(Long fromUserId, Long toUserId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getFromUserId, fromUserId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getToUserId, toUserId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_NORMAL)
                // 行注：继续调用是否Null
                .isNull(ImMessage::getReadTime)
                .orderByAsc(ImMessage::getCreateTime, ImMessage::getId);  // 行注：继续调用排序升序
        return messageMapper.selectList(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义batch标记已读方法
    private List<Long> batchMarkAsRead(List<ImMessage> unreadMessages, LocalDateTime readTime) {
        // 行注：判断是否满足当前条件
        if (unreadMessages.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<Long> messageIds = unreadMessages.stream().map(ImMessage::getId).toList();  // 行注：初始化消息ID列表
        LambdaUpdateWrapper<ImMessage> updateWrapper = new LambdaUpdateWrapper<>();  // 行注：初始化更新条件封装器
        // 行注：调用in
        updateWrapper.in(ImMessage::getId, messageIds)
                .set(ImMessage::getReadTime, readTime);  // 行注：继续调用设置
        messageMapper.update(null, updateWrapper);  // 行注：调用更新
        return messageIds;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义刷新单聊会话Previews方法
    private void refreshSingleSessionPreviews(Long userA, Long userB) {
        updateSingleSessionPreview(userA, userB);  // 行注：调用更新单聊会话预览
        updateSingleSessionPreview(userB, userA);  // 行注：调用更新单聊会话预览
    }  // 行注：结束当前代码块

    // 行注：定义更新单聊会话预览方法
    private void updateSingleSessionPreview(Long userId, Long targetId) {
        ImSession session = getSession(userId, targetId, ChatConstants.SESSION_TYPE_SINGLE);  // 行注：初始化会话
        // 行注：判断是否满足当前条件
        if (session == null) {
            return;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        ImMessage latestMessage = getLatestSingleMessage(userId, targetId);  // 行注：初始化latest消息
        applyLatestPreview(session, latestMessage);  // 行注：调用applyLatest预览
    }  // 行注：结束当前代码块

    // 行注：定义获取Latest单聊消息方法
    private ImMessage getLatestSingleMessage(Long userId, Long targetId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用and
        wrapper.and(w -> w.eq(ImMessage::getFromUserId, userId)
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getToUserId, targetId)
                        // 行注：继续调用or
                        .or()
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getFromUserId, targetId)
                        // 行注：继续调用等值条件
                        .eq(ImMessage::getToUserId, userId))
                // 行注：继续调用ne
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT 1");  // 行注：继续调用最后
        return messageMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义刷新群会话Previews方法
    private void refreshGroupSessionPreviews(Long groupId, Collection<Long> memberUserIds) {
        ImMessage latestMessage = getLatestGroupMessage(groupId);  // 行注：初始化latest消息
        // 行注：遍历当前集合或范围
        for (Long memberUserId : memberUserIds) {
            ImSession session = getSession(memberUserId, groupId, ChatConstants.SESSION_TYPE_GROUP);  // 行注：初始化会话
            // 行注：判断是否满足当前条件
            if (session != null) {
                applyLatestPreview(session, latestMessage);  // 行注：调用applyLatest预览
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义获取Latest群消息方法
    private ImMessage getLatestGroupMessage(Long groupId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getToUserId, groupId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getSessionId, groupId)
                // 行注：继续调用ne
                .ne(ImMessage::getStatus, ChatConstants.MESSAGE_STATUS_RECALLED)
                // 行注：继续调用排序降序
                .orderByDesc(ImMessage::getCreateTime, ImMessage::getId)
                .last("LIMIT 1");  // 行注：继续调用最后
        return messageMapper.selectOne(wrapper);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义applyLatest预览方法
    private void applyLatestPreview(ImSession session, ImMessage latestMessage) {
        // 行注：判断是否满足当前条件
        if (latestMessage == null) {
            session.setLastMessage("");  // 行注：调用设置最后消息
            session.setLastMessageTime(null);  // 行注：调用设置最后消息时间
        // 行注：开始当前语句对应的代码块
        } else {
            // 行注：调用设置最后消息
            session.setLastMessage(truncateSessionPreview(buildPreview(latestMessage.getContent(), latestMessage.getMsgType(), latestMessage.getStatus())));
            session.setLastMessageTime(latestMessage.getCreateTime());  // 行注：调用设置最后消息时间
        }  // 行注：结束当前代码块
        sessionMapper.updateById(session);  // 行注：调用更新ID
    }  // 行注：结束当前代码块

    // 行注：定义解析消息会话类型方法
    private int resolveMessageSessionType(ImMessage message) {
        // 行注：判断是否满足当前条件
        if (message.getSessionId() != null && message.getSessionId().equals(message.getToUserId())) {
            ImGroupInfo groupInfo = groupInfoMapper.selectById(message.getToUserId());  // 行注：初始化群信息
            // 行注：判断是否满足当前条件
            if (groupInfo != null && !Integer.valueOf(1).equals(groupInfo.getDeleted())) {
                return ChatConstants.SESSION_TYPE_GROUP;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return ChatConstants.SESSION_TYPE_SINGLE;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加载启用群映射方法
    private Map<Long, ImGroupInfo> loadActiveGroupMap(Set<Long> groupIds) {
        // 行注：判断是否满足当前条件
        if (groupIds.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用in
        wrapper.in(ImGroupInfo::getId, groupIds)
                .eq(ImGroupInfo::getDeleted, 0);  // 行注：继续调用等值条件
        return groupInfoMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(ImGroupInfo::getId, group -> group, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义加载群Members用户方法
    private Map<Long, ImGroupMember> loadGroupMembersByUser(Long userId, Set<Long> groupIds) {
        // 行注：判断是否满足当前条件
        if (groupIds.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImGroupMember::getUserId, userId)
                .in(ImGroupMember::getGroupId, groupIds);  // 行注：继续调用in
        return groupMemberMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(ImGroupMember::getGroupId, member -> member, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    // 行注：定义加载群成员数量方法
    private Map<Long, Integer> loadGroupMemberCount(Set<Long> groupIds) {
        Map<Long, Integer> countMap = new HashMap<>();  // 行注：初始化数量映射
        // 行注：判断是否满足当前条件
        if (groupIds.isEmpty()) {
            return countMap;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.in(ImGroupMember::getGroupId, groupIds);  // 行注：调用in
        // 行注：遍历当前集合或范围
        for (ImGroupMember member : groupMemberMapper.selectList(wrapper)) {
            countMap.merge(member.getGroupId(), 1, Integer::sum);  // 行注：调用merge
        }  // 行注：结束当前代码块
        return countMap;  // 行注：返回处理结果
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

    // 行注：定义assert单聊聊天允许方法
    private void assertSingleChatAllowed(Long fromUserId, Long toUserId) {
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(toUserId, fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已将对方拉黑");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (!hasFriendRelation(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "仅支持与好友发起单聊");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义can访问单聊聊天方法
    private boolean canAccessSingleChat(Long userId, Long targetUserId) {
        // 行注：判断是否满足当前条件
        if (blacklistService.isBlacklisted(userId, targetUserId) || blacklistService.isBlacklisted(targetUserId, userId)) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return hasFriendRelation(userId, targetUserId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否包含好友Relation方法
    private boolean hasFriendRelation(Long userId, Long targetUserId) {
        LambdaQueryWrapper<SysFriend> forward = new LambdaQueryWrapper<>();  // 行注：初始化forward
        // 行注：调用等值条件
        forward.eq(SysFriend::getUserId, userId)
                .eq(SysFriend::getFriendId, targetUserId);  // 行注：继续调用等值条件
        // 行注：判断是否满足当前条件
        if (friendMapper.selectCount(forward) > 0) {
            return true;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFriend> reverse = new LambdaQueryWrapper<>();  // 行注：初始化reverse
        // 行注：调用等值条件
        reverse.eq(SysFriend::getUserId, targetUserId)
                .eq(SysFriend::getFriendId, userId);  // 行注：继续调用等值条件
        return friendMapper.selectCount(reverse) > 0;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义require群成员方法
    private ImGroupMember requireGroupMember(Long groupId, Long userId) {
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

    // 行注：定义列表群Members方法
    private List<ImGroupMember> listGroupMembers(Long groupId) {
        LambdaQueryWrapper<ImGroupMember> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.eq(ImGroupMember::getGroupId, groupId);  // 行注：调用等值条件
        return groupMemberMapper.selectList(wrapper);  // 行注：返回处理结果
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
        if (!StringUtils.hasText(groupInfo.getNotice()) || groupInfo.getNoticeUpdateTime() == null) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LocalDateTime noticeReadTime = member.getNoticeReadTime();  // 行注：初始化notice已读时间
        return noticeReadTime == null || noticeReadTime.isBefore(groupInfo.getNoticeUpdateTime());  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析群Display名称方法
    private String resolveGroupDisplayName(ImGroupInfo groupInfo, ImGroupMember member) {
        // 行注：判断是否满足当前条件
        if (member != null && StringUtils.hasText(member.getGroupRemark())) {
            return member.getGroupRemark().trim();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return groupInfo.getGroupName();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** WebSocket 推送须在 DB 提交成功后执行，避免客户端拉历史缺消息 */
    // 行注：定义executeAfterCommit方法
    private void executeAfterCommit(Runnable action) {
        // 行注：判断是否满足当前条件
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();  // 行注：调用run
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
                action.run();  // 行注：调用run
            }  // 行注：结束当前代码块
        });  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    // 行注：定义解析会话类型方法
    private int resolveSessionType(Integer sessionType) {
        // 行注：判断是否满足当前条件
        if (sessionType == null) {
            return ChatConstants.SESSION_TYPE_SINGLE;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (sessionType != ChatConstants.SESSION_TYPE_SINGLE && sessionType != ChatConstants.SESSION_TYPE_GROUP) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的会话类型");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return sessionType;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析文本消息类型方法
    private int resolveTextMessageType(Integer msgType) {
        // 行注：判断是否满足当前条件
        if (msgType == null) {
            return ChatConstants.MESSAGE_TYPE_TEXT;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (msgType != ChatConstants.MESSAGE_TYPE_TEXT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "普通消息仅支持文本类型");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return msgType;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析文件消息类型方法
    private int resolveFileMessageType(Integer msgType) {
        // 行注：判断是否满足当前条件
        if (msgType == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件消息类型不能为空");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (msgType != ChatConstants.MESSAGE_TYPE_IMAGE && msgType != ChatConstants.MESSAGE_TYPE_FILE) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件消息仅支持图片或文件类型");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return msgType;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建预览方法
    private String buildPreview(String content, Integer msgType) {
        return buildPreview(content, msgType, ChatConstants.MESSAGE_STATUS_NORMAL);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建预览方法
    private String buildPreview(String content, Integer msgType, Integer status) {
        // 行注：判断是否满足当前条件
        if (status != null && status == ChatConstants.MESSAGE_STATUS_RECALLED) {
            return "[消息已撤回]";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (msgType == null || msgType == ChatConstants.MESSAGE_TYPE_TEXT || msgType == ChatConstants.MESSAGE_TYPE_SYSTEM) {
            return content;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (msgType == ChatConstants.MESSAGE_TYPE_IMAGE) {
            return "[图片]";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (msgType == ChatConstants.MESSAGE_TYPE_FILE) {
            return "[文件]";  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return "[消息]";  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义查找ExistingOutbound消息方法
    private MessageDTO findExistingOutboundMessage(
            // 行注：补充当前表达式片段
            Long fromUserId,
            // 行注：补充当前表达式片段
            String clientMessageId,
            // 行注：补充当前表达式片段
            Long targetId,
            // 行注：补充当前表达式片段
            int sessionType
    // 行注：开始当前语句对应的代码块
    ) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(clientMessageId)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        // 行注：调用等值条件
        wrapper.eq(ImMessage::getFromUserId, fromUserId)
                // 行注：继续调用等值条件
                .eq(ImMessage::getClientMessageId, clientMessageId)
                .last("LIMIT 1");  // 行注：继续调用最后
        ImMessage message = messageMapper.selectOne(wrapper);  // 行注：初始化消息
        // 行注：判断是否满足当前条件
        if (message == null) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (sessionType == ChatConstants.SESSION_TYPE_GROUP) {
            // 行注：判断是否满足当前条件
            if (!targetId.equals(message.getToUserId()) || !targetId.equals(message.getSessionId())) {
                return null;  // 行注：返回处理结果
            }  // 行注：结束当前代码块
        // 行注：调用equals
        } else if (!targetId.equals(message.getToUserId())) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        Set<Long> relatedUserIds = new LinkedHashSet<>();  // 行注：初始化related用户ID列表
        relatedUserIds.add(message.getFromUserId());  // 行注：调用添加
        relatedUserIds.addAll(chatMessageHelper.parseMentionUserIds(message.getMentionUserIds()));  // 行注：调用添加全部
        // 行注：补充当前表达式片段
        MessageDTO dto = chatMessageHelper.toMessageDTO(
                message, sessionType, chatMessageHelper.loadUserMap(relatedUserIds), Map.of());  // 行注：调用加载用户映射
        dto.setClientMessageId(clientMessageId);  // 行注：调用设置客户端消息ID
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化客户端消息ID方法
    private String normalizeClientMessageId(String clientMessageId) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(clientMessageId)) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        String normalized = clientMessageId.trim();  // 行注：初始化规范化后的
        // 行注：判断是否满足当前条件
        if (normalized.length() > 64) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "clientMessageId 长度不能超过64");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        return normalized;  // 行注：返回处理结果
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

    // 行注：定义解析群@提醒Context方法
    private MentionContext resolveGroupMentionContext(
            // 行注：补充当前表达式片段
            ImGroupMember senderMember,
            // 行注：补充当前表达式片段
            List<ImGroupMember> members,
            // 行注：补充当前表达式片段
            Integer msgType,
            // 行注：补充当前表达式片段
            Boolean mentionAll,
            // 行注：补充当前表达式片段
            List<Long> mentionUserIds
    // 行注：开始当前语句对应的代码块
    ) {
        boolean normalizedMentionAll = Boolean.TRUE.equals(mentionAll);  // 行注：初始化规范化后的@提醒全部
        List<Long> normalizedMentionUserIds = normalizeMentionUserIds(mentionUserIds, senderMember.getUserId());  // 行注：初始化规范化后的@提醒用户ID列表

        // 行注：判断是否满足当前条件
        if (!normalizedMentionAll && normalizedMentionUserIds.isEmpty()) {
            return MentionContext.EMPTY;  // 行注：返回处理结果
        }  // 行注：结束当前代码块

        // 行注：判断是否满足当前条件
        if (msgType != ChatConstants.MESSAGE_TYPE_TEXT) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只有文本消息支持@提醒");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if (normalizedMentionAll && senderMember.getRole() < GroupConstants.ROLE_ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只有群主或管理员可以@所有人");  // 行注：抛出异常并中断当前流程
        }  // 行注：结束当前代码块
        // 行注：调用流
        Set<Long> memberUserIds = members.stream()
                // 行注：继续调用映射
                .map(ImGroupMember::getUserId)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：遍历当前集合或范围
        for (Long mentionUserId : normalizedMentionUserIds) {
            // 行注：判断是否满足当前条件
            if (!memberUserIds.contains(mentionUserId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "被@成员不在群聊中");  // 行注：抛出异常并中断当前流程
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return new MentionContext(normalizedMentionAll, normalizedMentionUserIds);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义规范化@提醒用户ID列表方法
    private List<Long> normalizeMentionUserIds(List<Long> mentionUserIds, Long senderUserId) {
        // 行注：判断是否满足当前条件
        if (mentionUserIds == null || mentionUserIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();  // 行注：初始化规范化后的
        // 行注：遍历当前集合或范围
        for (Long mentionUserId : mentionUserIds) {
            // 行注：判断是否满足当前条件
            if (mentionUserId == null || mentionUserId.equals(senderUserId)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            normalized.add(mentionUserId);  // 行注：调用添加
        }  // 行注：结束当前代码块
        return new ArrayList<>(normalized);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义是否@提醒Target方法
    private boolean isMentionTarget(Long targetUserId, Long senderUserId, MentionContext mentionContext) {
        // 行注：判断是否满足当前条件
        if (targetUserId == null || senderUserId == null || targetUserId.equals(senderUserId) || !mentionContext.hasMention()) {
            return false;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return mentionContext.mentionAll() || mentionContext.mentionUserIds().contains(targetUserId);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义构建群预览方法
    private String buildGroupPreview(String preview, MentionContext mentionContext, Long senderUserId, Long memberUserId) {
        // 行注：判断是否满足当前条件
        if (isMentionTarget(memberUserId, senderUserId, mentionContext)) {
            return "[特别提醒] " + preview;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return preview;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义加入@提醒用户ID列表方法
    private String joinMentionUserIds(List<Long> mentionUserIds) {
        // 行注：判断是否满足当前条件
        if (mentionUserIds == null || mentionUserIds.isEmpty()) {
            return null;  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return mentionUserIds.stream()  // 行注：返回处理结果
                // 行注：继续调用映射
                .map(String::valueOf)
                .collect(Collectors.joining(","));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

}  // 行注：结束当前代码块
