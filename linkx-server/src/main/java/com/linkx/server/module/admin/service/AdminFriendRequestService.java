package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.SysFriendRequest;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysFriendRequestMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.module.admin.dto.AdminFriendRequestListItemDTO;
import com.linkx.server.module.friend.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminFriendRequestService {

    private final SysFriendRequestMapper friendRequestMapper;
    private final SysUserMapper userMapper;
    private final FriendService friendService;

    public Page<AdminFriendRequestListItemDTO> list(int page, int size, Integer status) {
        LambdaQueryWrapper<SysFriendRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SysFriendRequest::getStatus, status);
        }
        wrapper.orderByDesc(SysFriendRequest::getCreateTime);
        Page<SysFriendRequest> raw = friendRequestMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminFriendRequestListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(r -> {
            SysUser from = userMapper.selectById(r.getFromUserId());
            SysUser to = userMapper.selectById(r.getToUserId());
            return AdminFriendRequestListItemDTO.builder()
                    .id(r.getId())
                    .fromUserId(r.getFromUserId())
                    .fromUsername(from != null ? from.getUsername() : null)
                    .toUserId(r.getToUserId())
                    .toUsername(to != null ? to.getUsername() : null)
                    .message(r.getMessage())
                    .status(r.getStatus())
                    .createTime(r.getCreateTime())
                    .build();
        }).toList());
        return result;
    }

    public void acceptAsReceiver(Long requestId) {
        SysFriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() == null || req.getStatus() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在或已处理");
        }
        friendService.acceptRequest(req.getToUserId(), requestId);
    }

    public void rejectAsReceiver(Long requestId) {
        SysFriendRequest req = friendRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() == null || req.getStatus() != 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在或已处理");
        }
        friendService.rejectRequest(req.getToUserId(), requestId);
    }
}