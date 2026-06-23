package com.linkx.server.module.blacklist.dto;

import lombok.Data;

/** 黑名单列表中的用户摘要。 */
@Data
public class BlacklistUserDTO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;
}
