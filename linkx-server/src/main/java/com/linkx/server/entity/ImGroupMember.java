package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.FieldFill;  // 行注：引入 FieldFill 类型
import com.baomidou.mybatisplus.annotation.IdType;  // 行注：引入 IdType 类型
import com.baomidou.mybatisplus.annotation.TableField;  // 行注：引入 TableField 类型
import com.baomidou.mybatisplus.annotation.TableId;  // 行注：引入 TableId 类型
import com.baomidou.mybatisplus.annotation.TableName;  // 行注：引入 TableName 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 群成员：角色、禁言、群备注、公告/消息已读游标、免打扰等。 */
@Data  // 行注：应用 @Data 注解
@TableName("im_group_member")  // 行注：应用 @TableName 注解
// 行注：定义 ImGroupMember 类
public class ImGroupMember {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 群 ID */
    private Long groupId;  // 行注：声明群ID字段

    /** 用户 ID */
    private Long userId;  // 行注：声明用户ID字段

    /** 角色 */
    private Integer role;  // 行注：声明角色字段

    /** 禁言截止时间 */
    private LocalDateTime muteTime;  // 行注：声明mute时间字段

    /** 公告已读时间 */
    private LocalDateTime noticeReadTime;  // 行注：声明notice已读时间字段

    /** 群内备注 */
    private String groupRemark;  // 行注：声明群Remark字段

    /** 我在本群的群名片 */
    private String memberCardName;

    /** 是否开启消息免打扰 */
    private Boolean notificationMuted;  // 行注：声明通知Muted字段

    /** 最近消息已读时间 */
    private LocalDateTime lastMessageReadTime;  // 行注：声明最后消息已读时间字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段
}  // 行注：结束当前代码块
