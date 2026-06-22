package com.linkx.server.module.chat.service.impl;

import com.linkx.server.common.AuditLogService;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.ImSession;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.service.BlacklistService;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.ws.ChatEventPushService;
import com.linkx.server.module.chat.ws.ChatPresenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ImSessionMapper sessionMapper;

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysFriendMapper friendMapper;

    @Mock
    private SysFileMapper fileMapper;

    @Mock
    private ImGroupInfoMapper groupInfoMapper;

    @Mock
    private ImGroupMemberMapper groupMemberMapper;

    @Mock
    private BlacklistService blacklistService;

    @Mock
    private ChatEventPushService chatEventPushService;

    @Mock
    private ChatPresenceService chatPresenceService;

    @Mock
    private AuditLogService auditLogService;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(
                sessionMapper,
                messageMapper,
                userMapper,
                friendMapper,
                fileMapper,
                groupInfoMapper,
                groupMemberMapper,
                blacklistService,
                chatEventPushService,
                chatPresenceService,
                auditLogService
        );
    }

    @Test
    void should_reject_single_message_when_users_are_not_friends() {
        SysUser targetUser = new SysUser();
        targetUser.setId(2L);
        when(userMapper.selectById(2L)).thenReturn(targetUser);
        when(blacklistService.isBlacklisted(2L, 1L)).thenReturn(false);
        when(blacklistService.isBlacklisted(1L, 2L)).thenReturn(false);
        when(friendMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 2L, "hello", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_SINGLE, "client-1", false, java.util.List.of()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    @Test
    void should_reject_single_message_when_sender_has_blacklisted_target() {
        SysUser targetUser = new SysUser();
        targetUser.setId(2L);
        when(userMapper.selectById(2L)).thenReturn(targetUser);
        when(blacklistService.isBlacklisted(2L, 1L)).thenReturn(false);
        when(blacklistService.isBlacklisted(1L, 2L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 2L, "hello", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_SINGLE, "client-2", false, java.util.List.of()));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(friendMapper, never()).selectCount(any());
        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    @Test
    void should_reject_single_message_when_sender_is_blacklisted_by_target() {
        SysUser targetUser = new SysUser();
        targetUser.setId(2L);
        when(userMapper.selectById(2L)).thenReturn(targetUser);
        when(blacklistService.isBlacklisted(2L, 1L)).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 2L, "hello", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_SINGLE, "client-2b", false, java.util.List.of()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(friendMapper, never()).selectCount(any());
        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    @Test
    void should_reject_group_message_when_sender_is_muted() {
        ImGroupInfo groupInfo = new ImGroupInfo();
        groupInfo.setId(200L);
        groupInfo.setDeleted(0);
        ImGroupMember senderMember = new ImGroupMember();
        senderMember.setGroupId(200L);
        senderMember.setUserId(1L);
        senderMember.setMuteTime(LocalDateTime.now().plusMinutes(5));

        when(groupInfoMapper.selectById(200L)).thenReturn(groupInfo);
        when(groupMemberMapper.selectOne(any())).thenReturn(senderMember);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 200L, "group hello", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_GROUP, "client-3", false, java.util.List.of()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(messageMapper, never()).insert(any(ImMessage.class));
        verify(sessionMapper, never()).updateById(any(ImSession.class));
    }

    @Test
    void should_reject_group_message_when_sender_is_not_member() {
        ImGroupInfo groupInfo = new ImGroupInfo();
        groupInfo.setId(201L);
        groupInfo.setDeleted(0);

        when(groupInfoMapper.selectById(201L)).thenReturn(groupInfo);
        when(groupMemberMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 201L, "group hello", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_GROUP, "client-4", false, java.util.List.of()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(messageMapper, never()).insert(any(ImMessage.class));
    }

    @Test
    void should_reject_group_message_when_member_mentions_all_without_admin_permission() {
        ImGroupInfo groupInfo = new ImGroupInfo();
        groupInfo.setId(202L);
        groupInfo.setDeleted(0);

        ImGroupMember senderMember = new ImGroupMember();
        senderMember.setGroupId(202L);
        senderMember.setUserId(1L);
        senderMember.setRole(0);

        ImGroupMember otherMember = new ImGroupMember();
        otherMember.setGroupId(202L);
        otherMember.setUserId(2L);
        otherMember.setRole(0);

        when(groupInfoMapper.selectById(202L)).thenReturn(groupInfo);
        when(groupMemberMapper.selectOne(any())).thenReturn(senderMember);
        when(groupMemberMapper.selectList(any())).thenReturn(java.util.List.of(senderMember, otherMember));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.sendMessage(1L, 202L, "@all", ChatConstants.MESSAGE_TYPE_TEXT,
                        ChatConstants.SESSION_TYPE_GROUP, "client-5", true, java.util.List.of()));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(messageMapper, never()).insert(any(ImMessage.class));
        verify(sessionMapper, never()).updateById(any(ImSession.class));
    }

    @Test
    void should_reject_recall_when_message_is_older_than_two_minutes() {
        ImMessage message = new ImMessage();
        message.setId(301L);
        message.setFromUserId(1L);
        message.setToUserId(2L);
        message.setStatus(ChatConstants.MESSAGE_STATUS_NORMAL);
        message.setCreateTime(LocalDateTime.now().minusMinutes(3));
        when(messageMapper.selectById(301L)).thenReturn(message);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> chatService.recallMessage(1L, 301L));

        assertEquals(ErrorCode.BAD_REQUEST, exception.getErrorCode());
        verify(messageMapper, never()).updateById(any(ImMessage.class));
    }
}
