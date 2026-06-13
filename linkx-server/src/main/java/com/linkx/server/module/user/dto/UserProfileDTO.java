package com.linkx.server.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String signature;
    private Integer gender;
    private String region;
    private LocalDateTime createTime;
}
