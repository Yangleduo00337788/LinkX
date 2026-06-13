package com.linkx.server.module.friend.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.friend.dto.FriendDTO;
import com.linkx.server.module.friend.dto.FriendRequestDTO;
import com.linkx.server.module.friend.dto.SendFriendRequest;
import com.linkx.server.module.friend.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/request")
    public Result<Void> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendFriendRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
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
