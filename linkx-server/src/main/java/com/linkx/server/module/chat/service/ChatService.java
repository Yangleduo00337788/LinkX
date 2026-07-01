package com.linkx.server.module.chat.service;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.service

import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.dto.MessageSearchHitDTO;

import java.util.List;  // 行注：引入 List 类型

/**
 * 单聊/群聊：发消息、历史、会话列表、已读、撤回。
 */
// 行注：定义 ChatService 接口
public interface ChatService {

    /**
     * 发送文本类消息；群聊时 {@code toUserId} 为群 ID，可 @全员或指定成员。
     *
     * @param fromUserId        发送者
     * @param toUserId          单聊对端用户 ID 或群 ID
     * @param content           正文（已在校验层规范化长度）
     * @param msgType           消息类型，见 {@link com.linkx.server.module.chat.constant.ChatConstants}
     * @param sessionType       单聊/群聊
     * @param clientMessageId   客户端幂等键，重复提交返回已存在消息
     * @param mentionAll        群 @全员
     * @param mentionUserIds    群 @指定用户
     */
    // 行注：调用发送消息
    MessageDTO sendMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Integer sessionType, String clientMessageId, Boolean mentionAll, List<Long> mentionUserIds);

    /**
     * 发送图片/文件消息，内容为文件 URL（须为发送者本人上传的文件）。
     */
    // 行注：调用发送文件消息
    MessageDTO sendFileMessage(Long fromUserId, Long toUserId, Long fileId, Integer msgType, Integer sessionType, String clientMessageId);

    /**
     * 分页历史消息，按时间倒序查询后再反转为正序返回。
     */
    List<MessageDTO> getChatHistory(Long userId, Long targetId, Integer sessionType, int page, int size);  // 行注：调用获取聊天历史

    /** 当前用户会话列表（过滤非好友、黑名单、已退群等） */
    List<ChatSessionDTO> getSessions(Long userId);  // 行注：调用获取会话列表

    /** 清零会话未读；单聊会写消息 readTime 并推送已读回执 */
    void markAsRead(Long userId, Long targetId, Integer sessionType);  // 行注：调用标记已读

    /** 发送者撤回消息（通常 2 分钟内） */
    void recallMessage(Long userId, Long messageId);  // 行注：调用recall消息

    /** 会话偏好等变更后，向该用户推送最新会话列表项 */
    void refreshSessionPush(Long userId, Long targetId, Integer sessionType);

    /** 置顶、免打扰、会话备注（单聊/群聊会话级） */
    void updateSessionSettings(Long userId, Long targetId, Integer sessionType, Boolean pinned, Boolean notificationMuted, String sessionRemark);

    /** 清空本地聊天记录（仅当前用户侧会话预览与已读游标，不删他人消息） */
    void clearChatHistoryLocal(Long userId, Long targetId, Integer sessionType);

    /** 跨会话搜索当前用户可访问的文本消息（单聊+群聊） */
    List<MessageSearchHitDTO> searchMessagesGlobal(Long userId, String keyword, int size);
}
