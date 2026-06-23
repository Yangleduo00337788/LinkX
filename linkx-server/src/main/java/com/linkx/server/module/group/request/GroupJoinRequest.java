package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 用户主动申请加入某群。 */
@Data
public class GroupJoinRequest {
    @NotNull(message = "请输入群ID")
    private Long groupId;

    private String message;
}
