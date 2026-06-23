package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 修改群名称、头像等资料。 */
@Data
public class UpdateGroupProfileRequest {
    @NotBlank(message = "群名称不能为空")
    private String groupName;

    private String groupAvatar;
}
