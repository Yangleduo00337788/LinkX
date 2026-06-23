package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 创建群并可选拉入初始成员（须为操作者好友）。 */
@Data
public class CreateGroupRequest {
    @NotBlank(message = "群名称不能为空")
    private String groupName;

    private String groupAvatar;

    private String notice;

    private List<Long> memberIds = new ArrayList<>();
}
