package com.linkx.server.module.group.service.impl;

import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupMember;
import com.linkx.server.entity.ImGroupRequest;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupMemberMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.mapper.ImSessionMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.ws.ChatGroupRealtimeService;
import com.linkx.server.module.group.constant.GroupConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private ImGroupInfoMapper groupInfoMapper;

    @Mock
    private ImGroupMemberMapper groupMemberMapper;

    @Mock
    private ImGroupRequestMapper groupRequestMapper;

    @Mock
    private ImMessageMapper messageMapper;

    @Mock
    private ImSessionMapper sessionMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private ChatGroupRealtimeService chatGroupRealtimeService;

    private GroupServiceImpl groupService;

    @BeforeEach
    void setUp() {
        groupService = new GroupServiceImpl(
                groupInfoMapper,
                groupMemberMapper,
                groupRequestMapper,
                messageMapper,
                sessionMapper,
                userMapper,
                chatGroupRealtimeService
        );
    }

    @Test
    void should_accept_selected_join_request_and_reject_other_pending_copies() {
        Long groupId = 10L;
        Long approverId = 200L;
        Long ownerId = 201L;
        Long applicantId = 300L;

        ImGroupRequest selectedRequest = new ImGroupRequest();
        selectedRequest.setId(1L);
        selectedRequest.setGroupId(groupId);
        selectedRequest.setFromUserId(applicantId);
        selectedRequest.setToUserId(approverId);
        selectedRequest.setRequestType(GroupConstants.REQUEST_TYPE_JOIN);
        selectedRequest.setStatus(GroupConstants.REQUEST_STATUS_PENDING);

        ImGroupRequest duplicateRequest = new ImGroupRequest();
        duplicateRequest.setId(2L);
        duplicateRequest.setGroupId(groupId);
        duplicateRequest.setFromUserId(applicantId);
        duplicateRequest.setToUserId(ownerId);
        duplicateRequest.setRequestType(GroupConstants.REQUEST_TYPE_JOIN);
        duplicateRequest.setStatus(GroupConstants.REQUEST_STATUS_PENDING);

        ImGroupInfo groupInfo = new ImGroupInfo();
        groupInfo.setId(groupId);
        groupInfo.setOwnerId(ownerId);
        groupInfo.setGroupName("项目群");
        groupInfo.setDeleted(0);
        groupInfo.setMaxMembers(100);
        groupInfo.setCreateTime(LocalDateTime.of(2026, 6, 16, 20, 0));

        ImGroupMember approverMember = new ImGroupMember();
        approverMember.setId(11L);
        approverMember.setGroupId(groupId);
        approverMember.setUserId(approverId);
        approverMember.setRole(GroupConstants.ROLE_ADMIN);

        ImGroupMember ownerMember = new ImGroupMember();
        ownerMember.setId(12L);
        ownerMember.setGroupId(groupId);
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(GroupConstants.ROLE_OWNER);

        ImGroupMember applicantMember = new ImGroupMember();
        applicantMember.setId(13L);
        applicantMember.setGroupId(groupId);
        applicantMember.setUserId(applicantId);
        applicantMember.setRole(GroupConstants.ROLE_MEMBER);

        SysUser approverUser = new SysUser();
        approverUser.setId(approverId);
        approverUser.setNickname("管理员A");
        approverUser.setUsername("admin-a");

        SysUser applicantUser = new SysUser();
        applicantUser.setId(applicantId);
        applicantUser.setNickname("申请人B");
        applicantUser.setUsername("member-b");

        when(groupRequestMapper.selectById(1L)).thenReturn(selectedRequest);
        when(groupInfoMapper.selectById(groupId)).thenReturn(groupInfo);
        when(groupMemberMapper.selectOne(any())).thenReturn(approverMember);
        when(groupMemberMapper.selectCount(any())).thenReturn(0L);
        when(groupMemberMapper.selectList(any()))
                .thenReturn(List.of(approverMember, ownerMember), List.of(approverMember, ownerMember, applicantMember));
        when(groupRequestMapper.selectList(any())).thenReturn(List.of(selectedRequest, duplicateRequest));
        when(userMapper.selectBatchIds(any())).thenReturn(List.of(approverUser, applicantUser));
        when(sessionMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ImMessage message = invocation.getArgument(0);
            message.setId(500L);
            return 1;
        }).when(messageMapper).insert(any(ImMessage.class));

        groupService.acceptRequest(approverId, 1L);

        ArgumentCaptor<ImGroupMember> memberCaptor = ArgumentCaptor.forClass(ImGroupMember.class);
        verify(groupMemberMapper).insert(memberCaptor.capture());
        ImGroupMember insertedMember = memberCaptor.getValue();
        assertEquals(applicantId, insertedMember.getUserId());
        assertEquals(GroupConstants.ROLE_MEMBER, insertedMember.getRole());

        ArgumentCaptor<ImGroupRequest> requestCaptor = ArgumentCaptor.forClass(ImGroupRequest.class);
        verify(groupRequestMapper, atLeastOnce()).updateById(requestCaptor.capture());
        List<ImGroupRequest> updatedRequests = requestCaptor.getAllValues();
        assertTrue(updatedRequests.stream().anyMatch(item ->
                item.getId().equals(2L) && item.getStatus().equals(GroupConstants.REQUEST_STATUS_REJECTED)));
        assertTrue(updatedRequests.stream().anyMatch(item ->
                item.getId().equals(1L) && item.getStatus().equals(GroupConstants.REQUEST_STATUS_ACCEPTED)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> userIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(chatGroupRealtimeService).pushGroupMutation(eq(groupId), eq(500L), userIdsCaptor.capture());
        List<Long> pushedUserIds = userIdsCaptor.getValue();
        assertNotNull(pushedUserIds);
        assertEquals(Set.of(approverId, ownerId, applicantId), Set.copyOf(pushedUserIds));
    }
}
