package com.linkx.server.module.group.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.group.dto.GroupDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;
import com.linkx.server.module.group.request.AddGroupMembersRequest;
import com.linkx.server.module.group.request.CreateGroupRequest;
import com.linkx.server.module.group.request.GroupJoinRequest;
import com.linkx.server.module.group.request.InviteGroupMembersRequest;
import com.linkx.server.module.group.request.MuteGroupMemberRequest;
import com.linkx.server.module.group.request.UpdateGroupProfileRequest;
import com.linkx.server.module.group.request.UpdateGroupNoticeRequest;
import com.linkx.server.module.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public Result<GroupDTO> createGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateGroupRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.createGroup(
                userId,
                request.getGroupName(),
                request.getGroupAvatar(),
                request.getNotice(),
                request.getMemberIds()
        ));
    }

    @GetMapping("/my")
    public Result<List<GroupDTO>> getMyGroups(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.getMyGroups(userId));
    }

    @GetMapping("/requests")
    public Result<List<GroupRequestDTO>> getPendingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.getPendingRequests(userId));
    }

    @PostMapping("/join-request")
    public Result<Void> submitJoinRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody GroupJoinRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.submitJoinRequest(userId, request.getGroupId(), request.getMessage());
        return Result.success();
    }

    @GetMapping("/{groupId}")
    public Result<GroupDetailDTO> getGroupDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.getGroupDetail(userId, groupId));
    }

    @PostMapping("/{groupId}/members")
    public Result<Void> addMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody AddGroupMembersRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.addMembers(userId, groupId, request.getMemberIds());
        return Result.success();
    }

    @PostMapping("/{groupId}/invites")
    public Result<Void> inviteMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody InviteGroupMembersRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.inviteMembers(userId, groupId, request.getMemberIds(), request.getMessage());
        return Result.success();
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    public Result<Void> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.removeMember(userId, groupId, memberUserId);
        return Result.success();
    }

    @PutMapping("/{groupId}/admin/{memberUserId}")
    public Result<Void> setAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.setAdmin(userId, groupId, memberUserId);
        return Result.success();
    }

    @DeleteMapping("/{groupId}/admin/{memberUserId}")
    public Result<Void> removeAdmin(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.removeAdmin(userId, groupId, memberUserId);
        return Result.success();
    }

    @PutMapping("/{groupId}/profile")
    public Result<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupProfileRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.updateProfile(userId, groupId, request.getGroupName(), request.getGroupAvatar());
        return Result.success();
    }

    @PutMapping("/{groupId}/notice")
    public Result<Void> updateNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupNoticeRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.updateNotice(userId, groupId, request.getNotice());
        return Result.success();
    }

    @PostMapping("/{groupId}/notice/read")
    public Result<Void> markNoticeRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.markNoticeRead(userId, groupId);
        return Result.success();
    }

    @PostMapping("/{groupId}/mute/{memberUserId}")
    public Result<Void> muteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberUserId,
            @Valid @RequestBody MuteGroupMemberRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.muteMember(userId, groupId, memberUserId, request.getMuteMinutes());
        return Result.success();
    }

    @DeleteMapping("/{groupId}/mute/{memberUserId}")
    public Result<Void> unmuteMember(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long memberUserId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.unmuteMember(userId, groupId, memberUserId);
        return Result.success();
    }

    @DeleteMapping("/{groupId}")
    public Result<Void> dissolveGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.dissolveGroup(userId, groupId);
        return Result.success();
    }

    @PostMapping("/{groupId}/leave")
    public Result<Void> leaveGroup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.leaveGroup(userId, groupId);
        return Result.success();
    }

    @PostMapping("/{groupId}/transfer/{newOwnerId}")
    public Result<Void> transferOwner(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long newOwnerId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.transferOwner(userId, groupId, newOwnerId);
        return Result.success();
    }

    @PostMapping("/requests/{requestId}/accept")
    public Result<Void> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.acceptRequest(userId, requestId);
        return Result.success();
    }

    @PostMapping("/requests/{requestId}/reject")
    public Result<Void> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.rejectRequest(userId, requestId);
        return Result.success();
    }

    @GetMapping("/search")
    public Result<List<GroupDTO>> searchGroups(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.searchGroups(userId, keyword));
    }
}
