package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupRequest;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;
import com.linkx.server.module.admin.dto.AdminGroupRequestListItemDTO;
import com.linkx.server.module.group.constant.GroupConstants;
import com.linkx.server.module.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGroupRequestService {

    private final ImGroupRequestMapper groupRequestMapper;
    private final ImGroupInfoMapper groupInfoMapper;
    private final GroupService groupService;

    public Page<AdminGroupRequestListItemDTO> list(int page, int size, Integer status, Long groupId) {
        LambdaQueryWrapper<ImGroupRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ImGroupRequest::getStatus, status);
        }
        if (groupId != null) {
            wrapper.eq(ImGroupRequest::getGroupId, groupId);
        }
        wrapper.orderByDesc(ImGroupRequest::getCreateTime);
        Page<ImGroupRequest> raw = groupRequestMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminGroupRequestListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(r -> {
            ImGroupInfo g = groupInfoMapper.selectById(r.getGroupId());
            return AdminGroupRequestListItemDTO.builder()
                    .id(r.getId())
                    .groupId(r.getGroupId())
                    .groupName(g != null ? g.getGroupName() : null)
                    .fromUserId(r.getFromUserId())
                    .toUserId(r.getToUserId())
                    .requestType(r.getRequestType())
                    .message(r.getMessage())
                    .status(r.getStatus())
                    .createTime(r.getCreateTime())
                    .build();
        }).toList());
        return result;
    }

    public void acceptAsHandler(Long requestId) {
        ImGroupRequest req = groupRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() == null
                || !req.getStatus().equals(GroupConstants.REQUEST_STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在或已处理");
        }
        groupService.acceptRequest(req.getToUserId(), requestId);
    }

    public void rejectAsHandler(Long requestId) {
        ImGroupRequest req = groupRequestMapper.selectById(requestId);
        if (req == null || req.getStatus() == null
                || !req.getStatus().equals(GroupConstants.REQUEST_STATUS_PENDING)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "申请不存在或已处理");
        }
        groupService.rejectRequest(req.getToUserId(), requestId);
    }
}