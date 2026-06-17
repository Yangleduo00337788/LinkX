package com.linkx.server.module.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.SysFriend;
import com.linkx.server.entity.SysFriendRequest;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysFriendMapper;
import com.linkx.server.mapper.SysFriendRequestMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.blacklist.service.BlacklistService;
import com.linkx.server.module.friend.dto.FriendDTO;
import com.linkx.server.module.friend.dto.FriendRequestDTO;
import com.linkx.server.module.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {

    private final SysFriendMapper friendMapper;
    private final SysFriendRequestMapper requestMapper;
    private final SysUserMapper userMapper;
    private final BlacklistService blacklistService;

    @Override
    @Transactional
    public void sendFriendRequest(Long fromUserId, Long toUserId, String message) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        SysUser toUser = userMapper.selectById(toUserId);
        if (toUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (blacklistService.isBlacklisted(toUserId, fromUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "你已被对方拉黑");
        }
        if (blacklistService.isBlacklisted(fromUserId, toUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已将对方拉黑");
        }

        LambdaQueryWrapper<SysFriend> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.and(w -> w
                .eq(SysFriend::getUserId, fromUserId)
                .eq(SysFriend::getFriendId, toUserId)
        );
        if (friendMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你们已经是好友了");
        }

        LambdaQueryWrapper<SysFriendRequest> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(SysFriendRequest::getFromUserId, fromUserId)
                .eq(SysFriendRequest::getToUserId, toUserId)
                .eq(SysFriendRequest::getStatus, 0);
        if (requestMapper.selectCount(pendingWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "你已发送过好友申请，请等待对方处理");
        }

        LambdaQueryWrapper<SysFriendRequest> reversePendingWrapper = new LambdaQueryWrapper<>();
        reversePendingWrapper.eq(SysFriendRequest::getFromUserId, toUserId)
                .eq(SysFriendRequest::getToUserId, fromUserId)
                .eq(SysFriendRequest::getStatus, 0);
        if (requestMapper.selectCount(reversePendingWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "对方已向你发送好友申请，请先处理");
        }

        SysFriendRequest request = new SysFriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus(0);
        requestMapper.insert(request);
    }

    @Override
    public List<FriendRequestDTO> getPendingRequests(Long userId) {
        LambdaQueryWrapper<SysFriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriendRequest::getToUserId, userId)
                .eq(SysFriendRequest::getStatus, 0)
                .orderByDesc(SysFriendRequest::getCreateTime);

        List<SysFriendRequest> requests = requestMapper.selectList(wrapper);
        if (requests.isEmpty()) {
            return List.of();
        }

        Set<Long> fromUserIds = requests.stream()
                .map(SysFriendRequest::getFromUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(fromUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));

        return requests.stream().map(r -> {
            FriendRequestDTO dto = new FriendRequestDTO();
            dto.setId(r.getId());
            dto.setFromUserId(r.getFromUserId());
            dto.setMessage(r.getMessage());
            dto.setStatus(r.getStatus());
            dto.setCreateTime(r.getCreateTime());

            SysUser fromUser = userMap.get(r.getFromUserId());
            if (fromUser != null) {
                dto.setFromUsername(fromUser.getUsername());
                dto.setFromNickname(fromUser.getNickname());
                dto.setFromAvatar(fromUser.getAvatar());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void acceptRequest(Long userId, Long requestId) {
        SysFriendRequest request = requestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        request.setStatus(1);
        requestMapper.updateById(request);

        SysFriend friend1 = new SysFriend();
        friend1.setUserId(request.getFromUserId());
        friend1.setFriendId(request.getToUserId());
        friendMapper.insert(friend1);

        SysFriend friend2 = new SysFriend();
        friend2.setUserId(request.getToUserId());
        friend2.setFriendId(request.getFromUserId());
        friendMapper.insert(friend2);
    }

    @Override
    @Transactional
    public void rejectRequest(Long userId, Long requestId) {
        SysFriendRequest request = requestMapper.selectById(requestId);
        if (request == null || !request.getToUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (request.getStatus() != 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        request.setStatus(2);
        requestMapper.updateById(request);
    }

    @Override
    public List<FriendDTO> getFriendList(Long userId) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriend::getUserId, userId);

        List<SysFriend> friends = friendMapper.selectList(wrapper);
        if (friends.isEmpty()) {
            return List.of();
        }

        Set<Long> friendUserIds = friends.stream()
                .map(SysFriend::getFriendId)
                .collect(Collectors.toSet());
        java.util.Map<Long, SysUser> userMap = userMapper.selectBatchIds(friendUserIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));

        return friends.stream().map(f -> {
            SysUser friendUser = userMap.get(f.getFriendId());
            if (friendUser == null) {
                return null;
            }

            FriendDTO dto = new FriendDTO();
            dto.setId(f.getId());
            dto.setUserId(f.getUserId());
            dto.setFriendId(f.getFriendId());
            dto.setRemark(f.getRemark());
            dto.setCreateTime(f.getCreateTime());
            dto.setFriendUsername(friendUser.getUsername());
            dto.setFriendNickname(friendUser.getNickname());
            dto.setFriendAvatar(friendUser.getAvatar());
            return dto;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<SysFriend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFriend::getUserId, userId)
                .eq(SysFriend::getFriendId, friendId);
        friendMapper.delete(wrapper);

        LambdaQueryWrapper<SysFriend> reverseWrapper = new LambdaQueryWrapper<>();
        reverseWrapper.eq(SysFriend::getUserId, friendId)
                .eq(SysFriend::getFriendId, userId);
        friendMapper.delete(reverseWrapper);
    }
}
