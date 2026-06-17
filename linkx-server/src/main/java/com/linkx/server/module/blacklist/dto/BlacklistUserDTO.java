package com.linkx.server.module.blacklist.dto;

import lombok.Data;

@Data
public class BlacklistUserDTO {

    private Long id;

    private String username;

    private String nickname;

    private String avatar;
}
