package com.linkx.server.module.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 对外展示的用户资料（不含密码、邮箱等敏感字段）。 */
@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDateTime createTime;
}
