package com.linkx.server.module.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkx.server.common.BusinessException;
import com.linkx.server.common.ErrorCode;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.mapper.ImMessageMapper;
import com.linkx.server.module.admin.dto.AdminMessageListItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminMessageService {

    private final ImMessageMapper messageMapper;

    public Page<AdminMessageListItemDTO> list(int page, int size, Long sessionId, Long fromUserId) {
        LambdaQueryWrapper<ImMessage> wrapper = new LambdaQueryWrapper<>();
        if (sessionId != null) {
            wrapper.eq(ImMessage::getSessionId, sessionId);
        }
        if (fromUserId != null) {
            wrapper.eq(ImMessage::getFromUserId, fromUserId);
        }
        wrapper.orderByDesc(ImMessage::getCreateTime);
        Page<ImMessage> raw = messageMapper.selectPage(new Page<>(page, size), wrapper);
        Page<AdminMessageListItemDTO> result = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        result.setRecords(raw.getRecords().stream().map(m -> AdminMessageListItemDTO.builder()
                .id(m.getId())
                .sessionId(m.getSessionId())
                .fromUserId(m.getFromUserId())
                .toUserId(m.getToUserId())
                .msgType(m.getMsgType())
                .contentPreview(truncate(m.getContent(), 120))
                .createTime(m.getCreateTime())
                .build()).toList());
        return result;
    }

    @Transactional
    public void delete(Long messageId, Long adminId, String adminUsername, String clientIp, AdminAuditService audit) {
        ImMessage msg = messageMapper.selectById(messageId);
        if (msg == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        messageMapper.deleteById(messageId);
        audit.record(adminId, adminUsername, "MESSAGE_DELETE", "message", String.valueOf(messageId), null, clientIp);
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}