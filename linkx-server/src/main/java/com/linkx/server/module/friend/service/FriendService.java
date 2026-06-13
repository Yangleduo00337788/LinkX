package com.linkx.server.module.friend.service;

import com.linkx.server.module.friend.dto.FriendDTO;
import com.linkx.server.module.friend.dto.FriendRequestDTO;

import java.util.List;

public interface FriendService {
    void sendFriendRequest(Long fromUserId, Long toUserId, String message);
    List<FriendRequestDTO> getPendingRequests(Long userId);
    void acceptRequest(Long userId, Long requestId);
    void rejectRequest(Long userId, Long requestId);
    List<FriendDTO> getFriendList(Long userId);
    void deleteFriend(Long userId, Long friendId);
}
