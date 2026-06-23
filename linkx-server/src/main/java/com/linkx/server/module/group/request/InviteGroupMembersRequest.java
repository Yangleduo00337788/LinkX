package com.linkx.server.module.group.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 邀请好友入群（可能产生待对方同意的申请）。 */
@Data
public class InviteGroupMembersRequest {
    @NotEmpty(message = "请选择要邀请的成员")
    private List<Long> memberIds = new ArrayList<>();

    private String message;
}
