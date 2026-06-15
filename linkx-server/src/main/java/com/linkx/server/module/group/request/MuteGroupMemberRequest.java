package com.linkx.server.module.group.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MuteGroupMemberRequest {
    @NotNull(message = "禁言时长不能为空")
    @Min(value = 1, message = "禁言时长至少为1分钟")
    @Max(value = 43200, message = "禁言时长不能超过43200分钟")
    private Integer muteMinutes;
}
