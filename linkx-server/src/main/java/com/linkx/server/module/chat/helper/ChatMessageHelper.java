package com.linkx.server.module.chat.helper;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.helper

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;  // 行注：引入 LambdaQueryWrapper 类型
import com.linkx.server.entity.ImMessage;  // 行注：引入 ImMessage 类型
import com.linkx.server.entity.SysFile;  // 行注：引入 SysFile 类型
import com.linkx.server.entity.SysUser;  // 行注：引入 SysUser 类型
import com.linkx.server.mapper.SysFileMapper;  // 行注：引入 SysFileMapper 类型
import com.linkx.server.mapper.SysUserMapper;  // 行注：引入 SysUserMapper 类型
import com.linkx.server.module.chat.constant.ChatConstants;  // 行注：引入 ChatConstants 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.stereotype.Component;  // 行注：引入 Component 类型
import org.springframework.util.StringUtils;  // 行注：引入 StringUtils 类型

import java.util.ArrayList;  // 行注：引入 ArrayList 类型
import java.util.List;  // 行注：引入 List 类型
import java.util.Map;  // 行注：引入 Map 类型
import java.util.Objects;
import java.util.Set;  // 行注：引入 Set 类型
import java.util.stream.Collectors;  // 行注：引入 Collectors 类型

/** 消息实体转 DTO、@ 列表解析、发送者昵称/头像批量填充。 */
@Component  // 行注：应用 @Component 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatMessageHelper 类
public class ChatMessageHelper {

    private final SysFileMapper fileMapper;  // 行注：注入文件Mapper依赖
    private final SysUserMapper userMapper;  // 行注：注入用户Mapper依赖

    /** 将库中逗号分隔的 @ 用户 ID 解析为列表，非法片段跳过 */
    // 行注：定义parse@提醒用户ID列表方法
    public List<Long> parseMentionUserIds(String mentionUserIds) {
        // 行注：判断是否满足当前条件
        if (!StringUtils.hasText(mentionUserIds)) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<Long> result = new ArrayList<>();  // 行注：初始化结果
        // 行注：遍历当前集合或范围
        for (String item : mentionUserIds.split(",")) {
            // 行注：判断是否满足当前条件
            if (!StringUtils.hasText(item)) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：尝试执行可能失败的逻辑
            try {
                result.add(Long.parseLong(item.trim()));  // 行注：调用添加
            // 行注：执行当前方法调用
            } catch (NumberFormatException ignored) {
                // 历史脏数据中的非法 ID 跳过，避免整页历史加载失败
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return result;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 批量按消息 content（文件 URL）加载附件元数据 */
    // 行注：定义加载文件映射方法
    public Map<String, SysFile> loadFileMap(List<ImMessage> messages) {
        // 行注：调用流
        Set<String> fileUrls = messages.stream()
                // 行注：继续调用过滤
                .filter(message -> (message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                        // 行注：调用是否包含文本
                        && StringUtils.hasText(message.getContent()))
                // 行注：继续调用映射
                .map(ImMessage::getContent)
                .collect(Collectors.toSet());  // 行注：继续调用收集
        // 行注：判断是否满足当前条件
        if (fileUrls.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();  // 行注：初始化条件封装器
        wrapper.in(SysFile::getFileUrl, fileUrls);  // 行注：调用in
        return fileMapper.selectList(wrapper).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(SysFile::getFileUrl, file -> file, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /** 批量加载发送者/被 @ 用户资料 */
    // 行注：定义加载用户映射方法
    public Map<Long, SysUser> loadUserMap(Set<Long> userIds) {
        // 行注：判断是否满足当前条件
        if (userIds.isEmpty()) {
            return Map.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        return userMapper.selectBatchIds(userIds).stream()  // 行注：返回处理结果
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left));  // 行注：继续调用收集
    }  // 行注：结束当前代码块

    /**
     * 将持久化消息转为 API/WS 使用的 DTO，并填充昵称、头像与文件信息。
     */
    public MessageDTO toMessageDTO(ImMessage message, Integer sessionType, Map<Long, SysUser> userMap, Map<String, SysFile> fileMap) {
        return toMessageDTO(message, sessionType, userMap, fileMap, Map.of());
    }

    /**
     * 群聊场景可传入发送者在群内的展示名（群名片优先）。
     */
    public MessageDTO toMessageDTO(
            ImMessage message,
            Integer sessionType,
            Map<Long, SysUser> userMap,
            Map<String, SysFile> fileMap,
            Map<Long, String> groupSenderDisplayNames) {
        List<Long> mentionUserIds = parseMentionUserIds(message.getMentionUserIds());  // 行注：初始化@提醒用户ID列表
        MessageDTO dto = new MessageDTO();  // 行注：初始化DTO
        dto.setId(message.getId());  // 行注：调用设置ID
        dto.setSessionId(message.getSessionId());  // 行注：调用设置会话ID
        dto.setFromUserId(message.getFromUserId());  // 行注：调用设置用户ID
        dto.setToUserId(message.getToUserId());  // 行注：调用设置转为用户ID
        dto.setSessionType(sessionType);  // 行注：调用设置会话类型
        dto.setContent(message.getContent());  // 行注：调用设置内容
        dto.setMsgType(message.getMsgType());  // 行注：调用设置消息类型
        dto.setMentionAll(Boolean.TRUE.equals(message.getMentionAll()));  // 行注：调用设置@提醒全部
        dto.setMentionUserIds(mentionUserIds);  // 行注：调用设置@提醒用户ID列表
        dto.setMentionDisplayNames(resolveMentionDisplayNames(mentionUserIds, userMap));  // 行注：调用设置@提醒DisplayNames
        dto.setStatus(message.getStatus());  // 行注：调用设置状态
        dto.setReadTime(message.getReadTime());  // 行注：调用设置已读时间
        dto.setCreateTime(message.getCreateTime());  // 行注：调用设置创建时间

        SysUser fromUser = userMap.get(message.getFromUserId());  // 行注：初始化用户
        String senderDisplayName = null;
        if (Objects.equals(sessionType, ChatConstants.SESSION_TYPE_GROUP) && groupSenderDisplayNames != null) {
            senderDisplayName = groupSenderDisplayNames.get(message.getFromUserId());
        }
        if (!StringUtils.hasText(senderDisplayName)) {
            senderDisplayName = resolveUserDisplayName(fromUser);
        }
        if (StringUtils.hasText(senderDisplayName)) {
            dto.setFromNickname(senderDisplayName.trim());
        }
        if (fromUser != null) {
            dto.setFromAvatar(fromUser.getAvatar());  // 行注：调用设置头像
        }  // 行注：结束当前代码块
        // 行注：判断是否满足当前条件
        if ((message.getMsgType() == ChatConstants.MESSAGE_TYPE_FILE || message.getMsgType() == ChatConstants.MESSAGE_TYPE_IMAGE)
                // 行注：调用是否包含文本
                && StringUtils.hasText(message.getContent())) {
            SysFile file = fileMap.get(message.getContent());  // 行注：初始化文件
            // 行注：判断是否满足当前条件
            if (file != null) {
                dto.setFileName(file.getOriginalName());  // 行注：调用设置文件名称
                dto.setFileSize(file.getFileSize());  // 行注：调用设置文件大小
                dto.setFileType(file.getFileType());  // 行注：调用设置文件类型
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return dto;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    // 行注：定义解析@提醒DisplayNames方法
    private List<String> resolveMentionDisplayNames(List<Long> mentionUserIds, Map<Long, SysUser> userMap) {
        // 行注：判断是否满足当前条件
        if (mentionUserIds.isEmpty()) {
            return List.of();  // 行注：返回处理结果
        }  // 行注：结束当前代码块
        List<String> displayNames = new ArrayList<>();  // 行注：初始化displayNames
        // 行注：遍历当前集合或范围
        for (Long mentionUserId : mentionUserIds) {
            SysUser user = userMap.get(mentionUserId);  // 行注：初始化用户
            // 行注：判断是否满足当前条件
            if (user == null) {
                continue;  // 行注：完成当前语句
            }  // 行注：结束当前代码块
            // 行注：判断是否满足当前条件
            if (StringUtils.hasText(user.getNickname())) {
                displayNames.add(user.getNickname().trim());  // 行注：调用添加
            // 行注：调用是否包含文本
            } else if (StringUtils.hasText(user.getUsername())) {
                displayNames.add(user.getUsername().trim());  // 行注：调用添加
            }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        return displayNames;  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    private String resolveUserDisplayName(SysUser user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname().trim();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername().trim();
        }
        return null;
    }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
