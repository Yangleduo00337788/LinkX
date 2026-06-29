package com.linkx.server.module.friend.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.friend.dto.FriendDTO;  // 行注：引入 FriendDTO 类型
import com.linkx.server.module.friend.dto.FriendRequestDTO;  // 行注：引入 FriendRequestDTO 类型
import com.linkx.server.module.friend.dto.UpdateFriendRemarkRequest;
import com.linkx.server.module.friend.dto.SendFriendRequest;  // 行注：引入 SendFriendRequest 类型
import com.linkx.server.module.abuse.service.AbuseProtectionGuard;  // 行注：引入 AbuseProtectionGuard 类型
import com.linkx.server.module.friend.service.FriendService;  // 行注：引入 FriendService 类型
import jakarta.servlet.http.HttpServletRequest;  // 行注：引入 HttpServletRequest 类型
import jakarta.validation.Valid;  // 行注：引入 Valid 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 好友关系：发申请、待处理列表、同意/拒绝、好友列表、删除好友。
 * <p>发申请前经 {@link AbuseProtectionGuard#checkFriendRequestSend} 日限流。</p>
 */
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/friend")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 FriendController 类
public class FriendController {

    private final FriendService friendService;  // 行注：注入好友服务依赖
    private final AbuseProtectionGuard abuseProtectionGuard;  // 行注：注入abuseProtectionGuard依赖

    /** 向目标用户发送好友申请（含可选附言） */
    @PostMapping("/request")  // 行注：应用 @PostMapping 注解
    // 行注：定义发送请求方法
    public Result<Void> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody SendFriendRequest request,  // 行注：应用 @Valid @RequestBody SendFriendRequest request, 注解
            // 行注：开始当前语句对应的代码块
            HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        abuseProtectionGuard.checkFriendRequestSend(userId, httpRequest);  // 行注：调用检查好友请求发送
        friendService.sendFriendRequest(userId, request.getToUserId(), request.getMessage());  // 行注：调用发送好友请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前用户收到的待处理好友申请列表 */
    @GetMapping("/requests")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取请求方法
    public Result<List<FriendRequestDTO>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(friendService.getPendingRequests(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 同意指定申请记录，双方建立好友关系并通常创建单聊会话 */
    @PostMapping("/accept/{requestId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义accept请求方法
    public Result<Void> acceptRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "requestId") Long requestId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        friendService.acceptRequest(userId, requestId);  // 行注：调用accept请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 拒绝指定好友申请 */
    @PostMapping("/reject/{requestId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义reject请求方法
    public Result<Void> rejectRequest(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "requestId") Long requestId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        friendService.rejectRequest(userId, requestId);  // 行注：调用reject请求
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 已建立好友关系的用户列表（含备注等展示字段） */
    @GetMapping("/list")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取好友列表方法
    public Result<List<FriendDTO>> getFriendList(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(friendService.getFriendList(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 解除与指定用户的好友关系（双向） */
    @DeleteMapping("/{friendId}")  // 行注：应用 @DeleteMapping 注解
    // 行注：定义删除好友方法
    public Result<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "friendId") Long friendId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        friendService.deleteFriend(userId, friendId);  // 行注：调用删除好友
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 修改我对该好友的备注 */
    @PutMapping("/{friendId}/remark")
    public Result<Void> updateFriendRemark(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long friendId,
            @Valid @RequestBody UpdateFriendRemarkRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        friendService.updateFriendRemark(userId, friendId, request.getRemark());
        return Result.success();
    }
}  // 行注：结束当前代码块
