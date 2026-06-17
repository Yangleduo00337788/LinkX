package com.linkx.server.module.group.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateGroupPreferencesRequest {

    @Size(max = 100, message = "群备注长度不能超过100个字符")
    private String groupRemark;

    private Boolean notificationMuted;
}
