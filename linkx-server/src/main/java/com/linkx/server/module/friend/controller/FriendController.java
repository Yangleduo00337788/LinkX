package com.linkx.server.module.friend.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.friend.dto.FriendDTO;
import com.linkx.server.module.friend.dto.FriendRequestDTO;
import com.linkx.server.module.friend.dto.SendFriendRequest;
import com.linkx.server.module.abuse.service.AbuseProtectionGuard;
import com.linkx.server.module.friend.service.FriendService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 好友关系：发申请、待处理列表、同意/拒绝、好友列表、删除好友。
 * <p>发申请前经 {@link AbuseProtectionGuard#checkFriendRequestSend} 日限流。</p>
 */
@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final AbuseProtectionGuard abuseProtectionGuard;

    @PostMapping("/request")
    public Result<Void> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendFriendRequest request,
            HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(userDetails.getUsername());
        abuseProtectionGuard.checkFriendRequestSend(userId, httpRequest);
        friendService.sendFriendRequest(userId, request.getToUserId(), request.getMessage());
        return Result.success();
    }

    @GetMapping("/requests")
    public Result<List<FriendRequestDTO>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(friendService.getPendingRequests(userId));
    }

    @PostMapping("/accept/{requestId}")
    public Result<Void> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "requestId") Long requestId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.acceptRequest(userId, requestId);
        return Result.success();
    }

    @PostMapping("/reject/{requestId}")
    public Result<Void> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "requestId") Long requestId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.rejectRequest(userId, requestId);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<List<FriendDTO>> getFriendList(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(friendService.getFriendList(userId));
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "friendId") Long friendId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.deleteFriend(userId, friendId);
        return Result.success();
    }
}
