package com.linkx.server.module.group.request;

import lombok.Data;

/** 更新群公告（管理员/群主）。 */
@Data
public class UpdateGroupNoticeRequest {
    private String notice;
}
