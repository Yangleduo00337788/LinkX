package com.linkx.server.module.chat.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.chat.dto.ChatSessionDTO;
import com.linkx.server.module.chat.dto.ChatWsTicketDTO;
import com.linkx.server.module.chat.dto.MessageDTO;
import com.linkx.server.module.chat.dto.SendFileMessageRequest;
import com.linkx.server.module.chat.dto.SendMessageRequest;
import com.linkx.server.module.chat.service.ChatService;
import com.linkx.server.module.chat.ws.ChatWebSocketTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatWebSocketTicketService chatWebSocketTicketService;

    @PostMapping("/send")
    public Result<MessageDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        MessageDTO message = chatService.sendMessage(
                userId,
                request.getToUserId(),
                request.getContent(),
                request.getMsgType(),
                request.getSessionType(),
                null
        );
        return Result.success(message);
    }

    @GetMapping("/history")
    public Result<List<MessageDTO>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "targetId") Long targetId,
            @RequestParam(value = "sessionType", defaultValue = "1") Integer sessionType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        Long userId = Long.parseLong(userDetails.getUsername());
        int safeSize = Math.min(Math.max(size, 1), 200);
        int safePage = Math.max(page, 1);
        return Result.success(chatService.getChatHistory(userId, targetId, sessionType, safePage, safeSize));
    }

    @GetMapping("/sessions")
    public Result<List<ChatSessionDTO>> getSessions(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(chatService.getSessions(userId));
    }

    @PostMapping("/read/{targetId}")
    public Result<Void> markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "targetId") Long targetId,
            @RequestParam(value = "sessionType", defaultValue = "1") Integer sessionType) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.markAsRead(userId, targetId, sessionType);
        return Result.success();
    }

    @PostMapping("/recall/{messageId}")
    public Result<Void> recallMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(value = "messageId") Long messageId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatService.recallMessage(userId, messageId);
        return Result.success();
    }

    @PostMapping("/send-file")
    public Result<MessageDTO> sendFileMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendFileMessageRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(chatService.sendFileMessage(userId, request.getToUserId(), request.getFileId(), request.getMsgType(), request.getSessionType(), null));
    }

    @PostMapping("/ws-ticket")
    public Result<ChatWsTicketDTO> createWsTicket(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(new ChatWsTicketDTO(chatWebSocketTicketService.createTicket(userId)));
    }
}
