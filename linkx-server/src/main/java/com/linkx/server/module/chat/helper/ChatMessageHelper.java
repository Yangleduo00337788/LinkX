package com.linkx.server.module.chat.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.linkx.server.entity.ImMessage;
import com.linkx.server.entity.SysFile;
import com.linkx.server.entity.SysUser;
import com.linkx.server.mapper.SysFileMapper;
import com.linkx.server.mapper.SysUserMapper;
import com.linkx.server.module.chat.constant.ChatConstants;
import com.linkx.server.module.chat.dto.MessageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatMessageHelper {

    private final SysFileMapper fileMapper;
    private final SysUserMapper userMapper;

    public List<Long> parseMentionUserIds(String mentionUserIds) {
        if (!StringUtils.hasText(mentionUserIds)) {
            return List.of();
        }
        List<Long> result = new ArrayList<>();
        for (String item : mentionUserIds.split(",")) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            try {
                result.add(Long.parseLong(item.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return result;
    }

    public Map<String, SysFile> loadFileMap(List<ImMessage> messages) {
        Set<String> fileUrls = messages.stream()
                .filter(message -> (message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                        && StringUtils.hasText(message.getContent()))
                .map(ImMessage::getContent)
                .collect(Collectors.toSet());
        if (fileUrls.isEmpty()) {
            return Map.of();
        }
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysFile::getFileUrl, fileUrls);
        return fileMapper.selectList(wrapper).stream()
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));
    }

    public Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));
    }

    public MessageDTO toMessageDTO(ImMessage message, Integer sessionType, Map<Long, SysUser> userMap, Map<String, SysFile> fileMap) {
        List<Long> mentionUserIds = parseMentionUserIds(message.getMentionUserIds());
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSessionId(message.getSessionId());
        dto.setFromUserId(message.getFromUserId());
        dto.setToUserId(message.getToUserId());
        dto.setSessionType(sessionType);
        dto.setContent(message.getContent());
        dto.setMsgType(message.getMsgType());
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));
        dto.setMentionUserIds(mentionUserIds);
        dto.setMentionDisplayNames(List.of());
        dto.setStatus(message.getStatus());
        dto.setReadTime(message.getReadTime());
        dto.setCreateTime(message.getCreateTime());

        SysUser fromUser = userMap.get(message.getFromUserId());
        if (fromUser != null) {
            dto.setFromNickname(fromUser.getNickname());
            dto.setFromAvatar(fromUser.getAvatar());
        }
        if ((message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                && StringUtils.hasText(message.getContent())) {
            SysFile file = fileMap.get(message.getContent());
            if (file != null) {
                dto.setFileName(file.getOriginalName());
                dto.setFileSize(file.getFileSize());
                dto.setFileType(file.getFileType());
            }
        }
        return dto;
    }
}