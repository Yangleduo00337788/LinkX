package com.linkx.server.module.chat.controller;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.controller

import com.linkx.server.common.Result;  // 行注：引入 Result 类型
import com.linkx.server.module.chat.dto.ChatSessionDTO;  // 行注：引入 ChatSessionDTO 类型
import com.linkx.server.module.chat.dto.ChatWsTicketDTO;  // 行注：引入 ChatWsTicketDTO 类型
import com.linkx.server.module.chat.dto.MessageDTO;  // 行注：引入 MessageDTO 类型
import com.linkx.server.module.chat.dto.SendFileMessageRequest;  // 行注：引入 SendFileMessageRequest 类型
import com.linkx.server.module.chat.dto.SendMessageRequest;  // 行注：引入 SendMessageRequest 类型
import com.linkx.server.module.chat.dto.UpdateSessionSettingsRequest;
import com.linkx.server.module.chat.constant.ChatHistoryLimits;  // 行注：引入 ChatHistoryLimits 类型
import com.linkx.server.module.chat.service.ChatService;  // 行注：引入 ChatService 类型
import com.linkx.server.module.chat.ws.ChatWebSocketTicketService;  // 行注：引入 ChatWebSocketTicketService 类型
import jakarta.validation.Valid;  // 行注：引入 Valid 类型
import lombok.RequiredArgsConstructor;  // 行注：引入 RequiredArgsConstructor 类型
import org.springframework.security.core.annotation.AuthenticationPrincipal;  // 行注：引入 AuthenticationPrincipal 类型
import org.springframework.security.core.userdetails.UserDetails;  // 行注：引入 UserDetails 类型
import org.springframework.web.bind.annotation.*;  // 行注：引入 * 类型

import java.util.List;  // 行注：引入 List 类型

/**
 * 聊天 REST：会话列表、历史、发消息/文件、已读、撤回、WebSocket 连接 ticket。
 */
@RestController  // 行注：应用 @RestController 注解
@RequestMapping("/api/chat")  // 行注：应用 @RequestMapping 注解
@RequiredArgsConstructor  // 行注：应用 @RequiredArgsConstructor 注解
// 行注：定义 ChatController 类
public class ChatController {

    private final ChatService chatService;  // 行注：注入聊天服务依赖
    private final ChatWebSocketTicketService chatWebSocketTicketService;  // 行注：注入聊天Web连接票据服务依赖

    /** 发送文本或富文本消息（单聊/群聊由 sessionType 与 target 决定） */
    @PostMapping("/send")  // 行注：应用 @PostMapping 注解
    // 行注：定义发送消息方法
    public Result<MessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody SendMessageRequest request) {  // 行注：应用 @Valid @RequestBody SendMessageRequest request) { 注解
        // Security 上下文中的 username 在本项目约定存的是 userId 字符串。
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        // 行注：补充当前表达式片段
        MessageDTO message = chatService.sendMessage(
                // 行注：补充当前表达式片段
                userId,
                // 行注：调用获取转为用户ID
                request.getToUserId(),
                // 行注：调用获取内容
                request.getContent(),
                // 行注：调用获取消息类型
                request.getMsgType(),
                // 行注：调用获取会话类型
                request.getSessionType(),
                // 行注：调用获取客户端消息ID
                request.getClientMessageId(),
                // 行注：调用获取@提醒全部
                request.getMentionAll(),
                // 行注：调用获取@提醒用户ID列表
                request.getMentionUserIds()
        );  // 行注：结束当前参数配置
        return Result.success(message);  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 分页拉取与某用户或某群的历史消息，page/size 经 {@link ChatHistoryLimits} 钳制 */
    @GetMapping("/history")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取历史方法
    public Result<List<MessageDTO>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @RequestParam(value = "targetId") Long targetId,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "sessionType", defaultValue = "1") Integer sessionType,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "page", defaultValue = "1") int page,  // 行注：应用 @RequestParam 注解
            @RequestParam(value = "size", defaultValue = "50") int size) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        // REST 入口先做一层分页钳制，避免把异常 page/size 直接透传给服务层。
        int safeSize = ChatHistoryLimits.clampSize(size);  // 行注：初始化safe大小
        int safePage = ChatHistoryLimits.clampPage(page);  // 行注：初始化safe分页
        return Result.success(chatService.getChatHistory(userId, targetId, sessionType, safePage, safeSize));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 当前用户的聊天会话列表（最近消息摘要等） */
    @GetMapping("/sessions")  // 行注：应用 @GetMapping 注解
    // 行注：定义获取会话列表方法
    public Result<List<ChatSessionDTO>> getSessions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        return Result.success(chatService.getSessions(userId));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 将会话内消息标记为已读并更新未读数 */
    @PostMapping("/read/{targetId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义标记已读方法
    public Result<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "targetId") Long targetId,  // 行注：应用 @PathVariable 注解
            @RequestParam(value = "sessionType", defaultValue = "1") Integer sessionType) {  // 行注：应用 @RequestParam 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        chatService.markAsRead(userId, targetId, sessionType);  // 行注：调用标记已读
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 撤回自己发送的消息（在允许的时间窗内） */
    @PostMapping("/recall/{messageId}")  // 行注：应用 @PostMapping 注解
    // 行注：定义recall消息方法
    public Result<Void> recallMessage(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @PathVariable(value = "messageId") Long messageId) {  // 行注：应用 @PathVariable 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        chatService.recallMessage(userId, messageId);  // 行注：调用recall消息
        return Result.success();  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 发送已上传文件/图片类消息（引用 fileId） */
    @PostMapping("/send-file")  // 行注：应用 @PostMapping 注解
    // 行注：定义发送文件消息方法
    public Result<MessageDTO> sendFileMessage(
            @AuthenticationPrincipal UserDetails userDetails,  // 行注：应用 @AuthenticationPrincipal UserDetails userDetails, 注解
            @Valid @RequestBody SendFileMessageRequest request) {  // 行注：应用 @Valid @RequestBody SendFileMessageRequest request) { 注解
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        // 行注：返回处理结果
        return Result.success(chatService.sendFileMessage(userId, request.getToUserId(), request.getFileId(), request.getMsgType(), request.getSessionType(), request.getClientMessageId()));
    }  // 行注：结束当前代码块

    /** 签发短时 WebSocket 连接票据，避免在 URL 中直接携带 JWT */
    @PostMapping("/ws-ticket")  // 行注：应用 @PostMapping 注解
    // 行注：定义创建WebSocket票据方法
    public Result<ChatWsTicketDTO> createWsTicket(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());  // 行注：初始化用户ID
        // 先通过受保护的 HTTP 接口换取短时 ticket，再用 ticket 建立 WebSocket 连接。
        return Result.success(new ChatWsTicketDTO(chatWebSocketTicketService.createTicket(userId)));  // 行注：返回处理结果
    }  // 行注：结束当前代码块

    /** 会话置顶、免打扰、备注 */
    @PutMapping("/session/settings")
    public Result<Void> updateSessionSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateSessionSettingsRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.updateSessionSettings(
                userId,
                request.getTargetId(),
                request.getSessionType() != null ? request.getSessionType() : 1,
                request.getPinned(),
                request.getNotificationMuted(),
                request.getSessionRemark());
        return Result.success();
    }

    /** 清空当前用户侧聊天记录展示（不删除服务端消息） */
    @PostMapping("/session/clear-history")
    public Result<Void> clearChatHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long targetId,
            @RequestParam(defaultValue = "1") Integer sessionType) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.clearChatHistoryLocal(userId, targetId, sessionType);
        return Result.success();
    }
}  // 行注：结束当前代码块
