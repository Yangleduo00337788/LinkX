package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.entity.ImGroupRequest;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.mapper.ImGroupRequestMapper;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupRequest;
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

    public void accept(Long requestId) {
        ImGroupRequest req = requirePending(requestId);
        Long operatorId = resolveOperatorForAccept(req);
        groupService.acceptRequest(operatorId, requestId);
    }

    public void reject(Long requestId) {
        ImGroupRequest req = requirePending(requestId);
        Long operatorId = resolveOperatorForReject(req);
        groupService.rejectRequest(operatorId, requestId);
    }

    private ImGroupRequest requirePending(Long requestId) {
        ImGroupRequest req = groupRequestMapper.selectById(requestId);
        if (req == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (req.getStatus() == null || req.getStatus() != GroupConstants.REQUEST_STATUS_PENDING) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "申请已处理");
        }
        return req;
    }

    /** 入群申请由被申请方（群主侧 toUserId）审批；邀请由被邀请人 fromUserId 确认。 */
    private Long resolveOperatorForAccept(ImGroupRequest req) {
        if (req.getRequestType() != null && req.getRequestType() == GroupConstants.REQUEST_TYPE_JOIN) {
            return req.getToUserId();
        }
        if (req.getRequestType() != null && req.getRequestType() == GroupConstants.REQUEST_TYPE_INVITE) {
            return req.getFromUserId();
        }
        throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的申请类型");
    }

    private Long resolveOperatorForReject(ImGroupRequest req) {
        return resolveOperatorForAccept(req);
    }
}