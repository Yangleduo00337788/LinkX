package com.linkx.server.module.friend.service;  // 行注：声明当前文件所在包 com.linkx.server.module.friend.service

import com.linkx.server.module.friend.dto.FriendDTO;  // 行注：引入 FriendDTO 类型
import com.linkx.server.module.friend.dto.FriendRequestDTO;  // 行注：引入 FriendRequestDTO 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 好友申请与双向好友关系维护。
 */
// 行注：定义 FriendService 接口
public interface FriendService {
    /**
     * 发送好友申请。
     *
     * @param fromUserId from用户Id
     * @param toUserId to用户Id
     * @param message 附言内容
     */
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);  // 行注：调用发送好友请求
    /**
     * 获取待处理申请。
     *
     * @param userId 用户 ID
     * @return 好友申请DTO列表
     */
    List<FriendRequestDTO> getPendingRequests(Long userId);  // 行注：调用获取Pending请求
    /**
     * 通过申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    void acceptRequest(Long userId, Long requestId);  // 行注：调用accept请求
    /**
     * 拒绝申请。
     *
     * @param userId 用户 ID
     * @param requestId 申请 ID
     */
    void rejectRequest(Long userId, Long requestId);  // 行注：调用reject请求
    /**
     * 获取好友列表。
     *
     * @param userId 用户 ID
     * @return 好友DTO列表
     */
    List<FriendDTO> getFriendList(Long userId);  // 行注：调用获取好友列表
    /**
     * 删除好友。
     *
     * @param userId 用户 ID
     * @param friendId 好友Id
     */
    void deleteFriend(Long userId, Long friendId);  // 行注：调用删除好友
}  // 行注：结束当前代码块
