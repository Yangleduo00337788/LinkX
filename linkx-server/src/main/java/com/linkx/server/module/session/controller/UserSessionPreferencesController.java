package com.linkx.server.module.session.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.session.dto.UpdateSessionPreferencesRequest;
import com.linkx.server.module.session.service.UserSessionPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/sessions")
@RequiredArgsConstructor
public class UserSessionPreferencesController {

    private final UserSessionPreferencesService preferencesService;

    @PutMapping("/{targetId}/preferences")
    public Result<Void> updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "1") Integer sessionType,
            @Valid @RequestBody UpdateSessionPreferencesRequest body) {
        Long userId = Long.parseLong(userDetails.getUsername());
        preferencesService.updatePreferences(userId, targetId, sessionType,
                body.getSessionRemark(), body.getPinned(), body.getNotificationMuted());
        return Result.success();
    }
}