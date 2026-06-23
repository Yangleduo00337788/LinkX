package com.linkx.server.module.group.constant;

/** 群成员角色、入群/邀请申请类型与状态。 */
public final class GroupConstants {

    private GroupConstants() {
    }

    public static final int ROLE_MEMBER = 0;
    public static final int ROLE_ADMIN = 1;
    public static final int ROLE_OWNER = 2;

    public static final int REQUEST_TYPE_JOIN = 0;
    public static final int REQUEST_TYPE_INVITE = 1;

    public static final int REQUEST_STATUS_PENDING = 0;
    public static final int REQUEST_STATUS_ACCEPTED = 1;
    public static final int REQUEST_STATUS_REJECTED = 2;
}
