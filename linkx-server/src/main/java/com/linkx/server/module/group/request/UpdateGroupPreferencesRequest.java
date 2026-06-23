package com.linkx.server.module.group.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

/** 当前用户在群内的备注、消息免打扰等偏好。 */
@Data
public class UpdateGroupPreferencesRequest {

    @Size(max = 100, message = "群备注长度不能超过100个字符")
    private String groupRemark;

    private Boolean notificationMuted;
}
