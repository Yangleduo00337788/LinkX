package com.linkx.server.module.release.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LatestReleaseDTO {
    private String platform;
    private String version;
    private String downloadUrl;
    private String releaseNotes;
    private boolean forceUpdate;
}