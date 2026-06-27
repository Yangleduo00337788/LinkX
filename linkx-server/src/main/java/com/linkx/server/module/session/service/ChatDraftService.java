package com.linkx.server.module.session.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.ImChatDraft;
import com.linkx.server.mapper.ImChatDraftMapper;
import com.linkx.server.module.session.dto.ChatDraftDTO;
import com.linkx.server.module.session.dto.SaveChatDraftRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatDraftService {

    private final ImChatDraftMapper draftMapper;

    public List<ChatDraftDTO> listDrafts(Long userId) {
        LambdaQueryWrapper<ImChatDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImChatDraft::getUserId, userId);
        return draftMapper.selectList(wrapper).stream()
                .map(d -> ChatDraftDTO.builder()
                        .targetId(d.getTargetId())
                        .sessionType(d.getSessionType())
                        .draftContent(d.getDraftContent())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void saveDraft(Long userId, SaveChatDraftRequest request) {
        LambdaQueryWrapper<ImChatDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImChatDraft::getUserId, userId)
                .eq(ImChatDraft::getTargetId, request.getTargetId())
                .eq(ImChatDraft::getSessionType, request.getSessionType());
        ImChatDraft existing = draftMapper.selectOne(wrapper);
        String content = request.getDraftContent();
        if (!StringUtils.hasText(content)) {
            if (existing != null) {
                draftMapper.deleteById(existing.getId());
            }
            return;
        }
        if (existing != null) {
            existing.setDraftContent(content.trim());
            draftMapper.updateById(existing);
        } else {
            ImChatDraft draft = new ImChatDraft();
            draft.setUserId(userId);
            draft.setTargetId(request.getTargetId());
            draft.setSessionType(request.getSessionType());
            draft.setDraftContent(content.trim());
            draftMapper.insert(draft);
        }
    }

    @Transactional
    public void deleteDraft(Long userId, Long targetId, Integer sessionType) {
        LambdaQueryWrapper<ImChatDraft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ImChatDraft::getUserId, userId)
                .eq(ImChatDraft::getTargetId, targetId)
                .eq(ImChatDraft::getSessionType, sessionType);
        draftMapper.delete(wrapper);
    }
}