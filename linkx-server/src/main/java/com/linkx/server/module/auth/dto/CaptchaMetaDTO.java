package com.linkx.server.module.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CaptchaMetaDTO {
    private boolean enabled;
    private List<String> scenes;
}
