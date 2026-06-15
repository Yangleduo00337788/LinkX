package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupJoinRequest {
    @NotNull(message = "请输入群ID")
    private Long groupId;

    private String message;
}
