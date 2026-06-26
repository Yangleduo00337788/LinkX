package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImGroupInfo;
import com.linkx.server.mapper.ImGroupInfoMapper;
import com.linkx.server.module.admin.dto.AdminGroupListItemDTO;
import com.linkx.server.module.group.dto.GroupDetailDTO;
import com.linkx.server.module.group.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminGroupService {

    private final ImGroupInfoMapper groupInfoMapper;
    private final GroupService groupService;

    public Page<AdminGroupListItemDTO> list(int page, int size, String keyword) {
        LambdaQueryWrapper<ImGroupInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ImGroupInfo::getGroupName, keyword.trim());
        }
        wrapper.orderByDesc(ImGroupInfo::getCreateTime);
        Page<ImGroupInfo> raw = groupInfoMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminGroupListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(g -> AdminGroupListItemDTO.builder()
                .id(g.getId())
                .groupName(g.getGroupName())
                .ownerId(g.getOwnerId())
                .maxMembers(g.getMaxMembers())
                .createTime(g.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void dissolve(Long groupId, Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        ImGroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        group.setDeleted(1);
        groupInfoMapper.updateById(group);
        audit.record(adminId, adminUsername, "GROUP_DISSOLVE", "group", String.valueOf(groupId),
                group.getGroupName(), clientIp);
    }

    public GroupDetailDTO getDetail(Long groupId) {
        ImGroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null || (group.getDeleted() != null && group.getDeleted() == 1)) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return groupService.getGroupDetail(group.getOwnerId(), groupId);
    }

    @Transactional
    public void muteMember(Long groupId, Long memberUserId, Integer muteMinutes,
                           Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        ImGroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        groupService.muteMember(group.getOwnerId(), groupId, memberUserId, muteMinutes);
        audit.record(adminId, adminUsername, "GROUP_MUTE", "group", String.valueOf(groupId),
                "userId=" + memberUserId + ",minutes=" + muteMinutes, clientIp);
    }

    @Transactional
    public void unmuteMember(Long groupId, Long memberUserId,
                             Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        ImGroupInfo group = groupInfoMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        groupService.unmuteMember(group.getOwnerId(), groupId, memberUserId);
        audit.record(adminId, adminUsername, "GROUP_UNMUTE", "group", String.valueOf(groupId),
                "userId=" + memberUserId, clientIp);
    }
}