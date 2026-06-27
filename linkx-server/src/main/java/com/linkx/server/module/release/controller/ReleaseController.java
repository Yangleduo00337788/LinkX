package com.linkx.server.module.release.controller;

import com.linkx.server.common.Result;
import com.linkx.server.module.release.dto.LatestReleaseDTO;
import com.linkx.server.module.release.service.ReleaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final ReleaseService releaseService;

    @GetMapping("/latest")
    public Result<LatestReleaseDTO> latest(@RequestParam String platform) {
        return Result.success(releaseService.getLatestPublished(platform));
    }
}