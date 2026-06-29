package com.linkx.server.module.group.dto;

import lombok.Data;

@Data
public class UpdateGroupPreferencesRequest {
    private String groupRemark;
    private Boolean notificationMuted;
    /** 我在本群的群名片 */
    private String memberCardName;
}