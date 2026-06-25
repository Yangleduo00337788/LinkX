package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.FieldFill;  // 行注：引入 FieldFill 类型
import com.baomidou.mybatisplus.annotation.IdType;  // 行注：引入 IdType 类型
import com.baomidou.mybatisplus.annotation.TableField;  // 行注：引入 TableField 类型
import com.baomidou.mybatisplus.annotation.TableId;  // 行注：引入 TableId 类型
import com.baomidou.mybatisplus.annotation.TableName;  // 行注：引入 TableName 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 群组资料：名称、头像、公告、群主、最大人数等。 */
@Data  // 行注：应用 @Data 注解
@TableName("im_group_info")  // 行注：应用 @TableName 注解
// 行注：定义 ImGroupInfo 类
public class ImGroupInfo {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 群名称 */
    private String groupName;  // 行注：声明群名称字段

    /** 群头像 URL */
    private String groupAvatar;  // 行注：声明群头像字段

    /** 群主用户 ID */
    private Long ownerId;  // 行注：声明所有者ID字段

    /** 最大成员数 */
    private Integer maxMembers;  // 行注：声明最大Members字段

    /** 群公告 */
    private String notice;  // 行注：声明notice字段

    /** 公告更新时间 */
    private LocalDateTime noticeUpdateTime;  // 行注：声明notice更新时间字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 行注：应用 @TableField 注解
    private LocalDateTime updateTime;  // 行注：声明更新时间字段

    /** 逻辑删除标记 */
    private Integer deleted;  // 行注：声明deleted字段
}  // 行注：结束当前代码块
