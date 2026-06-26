package com.linkx.server.module.compliance.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SensitiveCheckResult {
    private final boolean blocked;
    private final String matchedWord;
    private final Long wordId;
    private final int action;
}