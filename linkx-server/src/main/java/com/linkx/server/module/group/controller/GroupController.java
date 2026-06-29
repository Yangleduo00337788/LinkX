package com.linkx.server.module.group.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.group.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import com.linkx.server.module.group.dto.GroupDTO;  // 行注：引入 GroupDTO 类型
import com.linkx.server.module.group.dto.GroupDetailDTO;  // 行注：引入 GroupDetailDTO 类型
import com.linkx.server.module.group.dto.GroupNoticeItemDTO;
import com.linkx.server.module.group.dto.CreateGroupHighlightRequest;
import com.linkx.server.module.group.dto.CreateGroupNoticeRequest;
import com.linkx.server.module.group.dto.GroupHighlightItemDTO;
import com.linkx.server.module.group.dto.GroupRequestDTO;  // 行注：引入 GroupRequestDTO 类型
import com.linkx.server.module.group.request.AddGroupMembersRequest;  // 行注：引入 AddGroupMembersRequest 类型
import com.linkx.server.module.group.request.CreateGroupRequest;  // 行注：引入 CreateGroupRequest 类型
import com.linkx.server.module.group.request.GroupJoinRequest;  // 行注：引入 GroupJoinRequest 类型
import com.linkx.server.module.group.request.InviteGroupMembersRequest;  // 行注：引入 InviteGroupMembersRequest 类型
import com.linkx.server.module.group.request.MuteGroupMemberRequest;  // 行注：引入 MuteGroupMemberRequest 类型
import com.linkx.server.module.group.request.UpdateGroupProfileRequest;  // 行注：引入 UpdateGroupProfileRequest 类型
import com.linkx.server.module.group.request.UpdateGroupPreferencesRequest;  // 行注：引入 UpdateGroupPreferencesRequest 类型
import com.linkx.server.module.group.request.UpdateGroupNoticeRequest;  // 行注：引入 UpdateGroupNoticeRequest 类型
import com.linkx.server.module.group.service.GroupService;  // 行注：引入 GroupService 类型
import jakarta.validation.Valid;  // 行注：引入 Valid 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 群组 REST：创建/解散、资料与公告、成员与禁言、入群申请、群偏好等。
 */
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/group")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 GroupController 类
public class GroupController {

    private final GroupService groupService;  // 行注：注入群服务依赖

    /** 创建群并指定初始成员；创建者成为群主 */
    @PostMapping  // 行注：应用 @PostMapping 注解
    // 行注：定义创建群方法
    public Result<GroupDTO> createGroup(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody CreateGroupRequest request) {  // 行注：应用 @Valid @RequestBody CreateGroupRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(groupService.createGroup(  // 行注：返回处理结果
                // 行注：补充当前表达式片段
                userId,
                // 行注：调用获取群名称
                request.getGroupName(),
                // 行注：调用获取群头像
                request.getGroupAvatar(),
                // 行注：调用获取Notice
                request.getNotice(),
                // 行注：调用获取成员ID列表
                request.getMemberIds()
        ));  // 行注：完成当前语句
    }  // 行注：结束当前代码块

    /** 当前用户已加入的群列表摘要 */
    @GetMapping("/my")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取我的Groups方法
    public Result<List<GroupDTO>> getMyGroups(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(groupService.getMyGroups(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 待群主/管理员处理的入群申请列表 */
    @GetMapping("/requests")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取Pending请求方法
    public Result<List<GroupRequestDTO>> getPendingRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(groupService.getPendingRequests(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 用户主动申请加入指定群 */
    @PostMapping("/join-request")  // 行注：应用 @PostMapping 注解
    // 行注：定义submit加入请求方法
    public Result<Void> submitJoinRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody GroupJoinRequest request) {  // 行注：应用 @Valid @RequestBody GroupJoinRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.submitJoinRequest(userId, request.getGroupId(), request.getMessage());  // 行注：调用submit加入请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 群详情（成员、公告、当前用户角色等）；须为群成员 */
    @GetMapping("/{groupId}")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取群详情方法
    public Result<GroupDetailDTO> getGroupDetail(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId) {  // 行注：应用 @PathVariable Long groupId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(groupService.getGroupDetail(userId, groupId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 直接拉好友入群（须具备相应管理权限） */
    @PostMapping("/{groupId}/members")  // 行注：应用 @PostMapping 注解
    // 行注：定义添加Members方法
    public Result<Void> addMembers(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @Valid @RequestBody AddGroupMembersRequest request) {  // 行注：应用 @Valid @RequestBody AddGroupMembersRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.addMembers(userId, groupId, request.getMemberIds());  // 行注：调用添加Members
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 向用户发送入群邀请（对方同意后入群） */
    @PostMapping("/{groupId}/invites")  // 行注：应用 @PostMapping 注解
    // 行注：定义邀请Members方法
    public Result<Void> inviteMembers(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @Valid @RequestBody InviteGroupMembersRequest request) {  // 行注：应用 @Valid @RequestBody InviteGroupMembersRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.inviteMembers(userId, groupId, request.getMemberIds(), request.getMessage());  // 行注：调用邀请Members
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 将成员移出群聊 */
    @DeleteMapping("/{groupId}/members/{memberUserId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义移除成员方法
    public Result<Void> removeMember(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long memberUserId) {  // 行注：应用 @PathVariable Long memberUserId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.removeMember(userId, groupId, memberUserId);  // 行注：调用移除成员
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 设置群管理员 */
    @PutMapping("/{groupId}/admin/{memberUserId}")  // 行注：应用 @PutMapping 注解
    // 行注：定义设置Admin方法
    public Result<Void> setAdmin(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long memberUserId) {  // 行注：应用 @PathVariable Long memberUserId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.setAdmin(userId, groupId, memberUserId);  // 行注：调用设置Admin
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 取消成员的管理员身份 */
    @DeleteMapping("/{groupId}/admin/{memberUserId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义移除Admin方法
    public Result<Void> removeAdmin(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long memberUserId) {  // 行注：应用 @PathVariable Long memberUserId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.removeAdmin(userId, groupId, memberUserId);  // 行注：调用移除Admin
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 修改群名称、群头像 */
    @PutMapping("/{groupId}/profile")  // 行注：应用 @PutMapping 注解
    // 行注：定义更新Profile方法
    public Result<Void> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @Valid @RequestBody UpdateGroupProfileRequest request) {  // 行注：应用 @Valid @RequestBody UpdateGroupProfileRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.updateProfile(userId, groupId, request.getGroupName(), request.getGroupAvatar());  // 行注：调用更新Profile
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 更新群公告内容 */
    @PutMapping("/{groupId}/notice")  // 行注：应用 @PutMapping 注解
    // 行注：定义更新Notice方法
    public Result<Void> updateNotice(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @Valid @RequestBody UpdateGroupNoticeRequest request) {  // 行注：应用 @Valid @RequestBody UpdateGroupNoticeRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.updateNotice(userId, groupId, request.getNotice());  // 行注：调用更新Notice
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前用户在本群的备注名、消息免打扰等个人偏好 */
    @PutMapping("/{groupId}/preferences")  // 行注：应用 @PutMapping 注解
    // 行注：定义更新Preferences方法
    public Result<Void> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @Valid @RequestBody UpdateGroupPreferencesRequest request) {  // 行注：应用 @Valid @RequestBody UpdateGroupPreferencesRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.updatePreferences(userId, groupId, request.getGroupRemark(),
                request.getNotificationMuted(), request.getMemberCardName());
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 标记群公告已读（用于未读角标） */
    @PostMapping("/{groupId}/notice/read")  // 行注：应用 @PostMapping 注解
    // 行注：定义标记Notice已读方法
    public Result<Void> markNoticeRead(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId) {  // 行注：应用 @PathVariable Long groupId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.markNoticeRead(userId, groupId);  // 行注：调用标记Notice已读
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    @GetMapping("/{groupId}/notices")
    public Result<List<GroupNoticeItemDTO>> listNotices(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.listGroupNotices(userId, groupId));
    }

    @PostMapping("/{groupId}/notices")
    public Result<Void> createNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupNoticeRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.createGroupNotice(userId, groupId, request.getContent(), request.getPinned());
        return Result.success();
    }

    @PutMapping("/{groupId}/notices/{noticeId}/pin")
    public Result<Void> pinNotice(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long noticeId,
            @RequestParam(defaultValue = "true") Boolean pinned) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.setGroupNoticePinned(userId, groupId, noticeId, pinned);
        return Result.success();
    }

    @GetMapping("/{groupId}/highlights")
    public Result<List<GroupHighlightItemDTO>> listHighlights(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(groupService.listGroupHighlights(userId, groupId));
    }

    @PostMapping("/{groupId}/highlights")
    public Result<Void> addHighlight(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @Valid @RequestBody CreateGroupHighlightRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.addGroupHighlight(userId, groupId, request.getMessageId(), request.getTitle());
        return Result.success();
    }

    @DeleteMapping("/{groupId}/highlights/{highlightId}")
    public Result<Void> removeHighlight(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long groupId,
            @PathVariable Long highlightId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        groupService.removeGroupHighlight(userId, groupId, highlightId);
        return Result.success();
    }

    /** 禁言指定成员若干分钟 */
    @PostMapping("/{groupId}/mute/{memberUserId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义mute成员方法
    public Result<Void> muteMember(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long memberUserId,  // 行注：应用 @PathVariable Long memberUserId, 注解
            @Valid @RequestBody MuteGroupMemberRequest request) {  // 行注：应用 @Valid @RequestBody MuteGroupMemberRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.muteMember(userId, groupId, memberUserId, request.getMuteMinutes());  // 行注：调用mute成员
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 群内图片/文件等媒体消息列表，size 限制在 1～300 */
    @GetMapping("/{groupId}/media")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取群Media方法
    public Result<List<MessageDTO>> getGroupMedia(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @RequestParam(value = "mediaType", defaultValue = "all") String mediaType,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "keyword", required = false) String keyword,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "size", defaultValue = "200") int size) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        int safeSize = Math.min(Math.max(size, 1), 300);  // 行注：初始化safe大小
        return Result.success(groupService.getGroupMedia(userId, groupId, mediaType, keyword, safeSize));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 在群消息中按关键字搜索，size 限制在 1～200 */
    @GetMapping("/{groupId}/messages/search")  // 行注：应用 @GetMapping 注解
    // 行注：定义搜索群消息方法
    public Result<List<MessageDTO>> searchGroupMessages(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @RequestParam("keyword") String keyword,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "size", defaultValue = "100") int size) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        int safeSize = Math.min(Math.max(size, 1), 200);  // 行注：初始化safe大小
        return Result.success(groupService.searchGroupMessages(userId, groupId, keyword, safeSize));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 解除成员禁言 */
    @DeleteMapping("/{groupId}/mute/{memberUserId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义unmute成员方法
    public Result<Void> unmuteMember(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long memberUserId) {  // 行注：应用 @PathVariable Long memberUserId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.unmuteMember(userId, groupId, memberUserId);  // 行注：调用unmute成员
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 解散群（通常仅群主可操作） */
    @DeleteMapping("/{groupId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义dissolve群方法
    public Result<Void> dissolveGroup(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId) {  // 行注：应用 @PathVariable Long groupId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.dissolveGroup(userId, groupId);  // 行注：调用dissolve群
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前用户主动退群 */
    @PostMapping("/{groupId}/leave")  // 行注：应用 @PostMapping 注解
    // 行注：定义leave群方法
    public Result<Void> leaveGroup(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId) {  // 行注：应用 @PathVariable Long groupId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.leaveGroup(userId, groupId);  // 行注：调用leave群
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 将群主转让给群内其他成员 */
    @PostMapping("/{groupId}/transfer/{newOwnerId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义transfer所有者方法
    public Result<Void> transferOwner(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long groupId,  // 行注：应用 @PathVariable Long groupId, 注解
            @PathVariable Long newOwnerId) {  // 行注：应用 @PathVariable Long newOwnerId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.transferOwner(userId, groupId, newOwnerId);  // 行注：调用transfer所有者
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 管理员同意入群申请 */
    @PostMapping("/requests/{requestId}/accept")  // 行注：应用 @PostMapping 注解
    // 行注：定义accept请求方法
    public Result<Void> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long requestId) {  // 行注：应用 @PathVariable Long requestId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.acceptRequest(userId, requestId);  // 行注：调用accept请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 管理员拒绝入群申请 */
    @PostMapping("/requests/{requestId}/reject")  // 行注：应用 @PostMapping 注解
    // 行注：定义reject请求方法
    public Result<Void> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable Long requestId) {  // 行注：应用 @PathVariable Long requestId) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        groupService.rejectRequest(userId, requestId);  // 行注：调用reject请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 按关键字搜索可发现的群（具体可见性由业务层控制） */
    @GetMapping("/search")  // 行注：应用 @GetMapping 注解
    // 行注：定义搜索Groups方法
    public Result<List<GroupDTO>> searchGroups(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam String keyword) {  // 行注：应用 @RequestParam String keyword) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(groupService.searchGroups(userId, keyword));  // 行注：返回处理结果
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
