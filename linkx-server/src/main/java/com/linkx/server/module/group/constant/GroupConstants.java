package com.linkx.server.module.group.constant;  // 行注：声明当前文件所在包 com.linkx.server.module.group.constant

/** 群成员角色、入群/邀请申请类型与状态。 */
// 行注：定义 GroupConstants 类
public final class GroupConstants {

    // 行注：定义群Constants方法
    private GroupConstants() {
    }  // 行注：结束当前代码块

    public static final int ROLE_MEMBER = 0;  // 行注：定义角色成员常量
    public static final int ROLE_ADMIN = 1;  // 行注：定义角色ADMIN常量
    public static final int ROLE_OWNER = 2;  // 行注：定义角色所有者常量

    public static final int REQUEST_TYPE_JOIN = 0;  // 行注：定义请求类型加入常量
    public static final int REQUEST_TYPE_INVITE = 1;  // 行注：定义请求类型邀请常量

    public static final int REQUEST_STATUS_PENDING = 0;  // 行注：定义请求状态PENDING常量
    public static final int REQUEST_STATUS_ACCEPTED = 1;  // 行注：定义请求状态ACCEPTED常量
    public static final int REQUEST_STATUS_REJECTED = 2;  // 行注：定义请求状态REJECTED常量
}  // 行注：结束当前代码块
