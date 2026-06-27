package com.linkx.server.module.session.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.session.dto.ChatDraftDTO;
import com.linkx.server.module.session.dto.SaveChatDraftRequest;
import com.linkx.server.module.session.service.ChatDraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/chat-drafts")
@RequiredArgsConstructor
public class ChatDraftController {

    private final ChatDraftService chatDraftService;

    @GetMapping
    public Result<List<ChatDraftDTO>> list(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(chatDraftService.listDrafts(userId));
    }

    @PutMapping
    public Result<Void> save(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @RequestBody SaveChatDraftRequest body) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatDraftService.saveDraft(userId, body);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long targetId,
            @RequestParam Integer sessionType) {
        Long userId = Long.parseLong(userDetails.getUsername());
        chatDraftService.deleteDraft(userId, targetId, sessionType);
        return Result.success();
    }
}