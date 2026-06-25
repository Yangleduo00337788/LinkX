package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.FieldFill;  // 行注：引入 FieldFill 类型
import com.baomidou.mybatisplus.annotation.IdType;  // 行注：引入 IdType 类型
import com.baomidou.mybatisplus.annotation.TableField;  // 行注：引入 TableField 类型
import com.baomidou.mybatisplus.annotation.TableId;  // 行注：引入 TableId 类型
import com.baomidou.mybatisplus.annotation.TableName;  // 行注：引入 TableName 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/** 入群申请/邀请记录。 */
@Data  // 行注：应用 @Data 注解
@TableName("im_group_request")  // 行注：应用 @TableName 注解
// 行注：定义 ImGroupRequest 类
public class ImGroupRequest {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 群 ID */
    private Long groupId;  // 行注：声明群ID字段

    /** 发起方用户 ID */
    private Long fromUserId;  // 行注：声明用户ID字段

    /** 目标用户 ID */
    private Long toUserId;  // 行注：声明转为用户ID字段

    /** 申请类型 */
    private Integer requestType;  // 行注：声明请求类型字段

    /** 申请或消息附言 */
    private String message;  // 行注：声明消息字段

    /** 状态 */
    private Integer status;  // 行注：声明状态字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段

    /** 处理时间 */
    private LocalDateTime handleTime;  // 行注：声明handle时间字段
}  // 行注：结束当前代码块
