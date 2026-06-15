package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AddGroupMembersRequest {
    @NotEmpty(message = "请选择要添加的成员")
    private List<Long> memberIds = new ArrayList<>();
}
