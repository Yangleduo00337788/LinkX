package com.linkx.server.entity;  // 行注：声明当前文件所在包 com.linkx.server.entity

import com.baomidou.mybatisplus.annotation.*;  // 行注：引入 * 类型
import lombok.Data;  // 行注：引入 Data 类型

import java.time.LocalDateTime;  // 行注：引入 LocalDateTime 类型

/**
 * 系统用户表 {@code sys_user}：登录账号、资料、状态与逻辑删除。
 */
@Data  // 行注：应用 @Data 注解
@TableName("sys_user")  // 行注：应用 @TableName 注解
// 行注：定义 SysUser 类
public class SysUser {

    /** 主键 ID */
    @TableId(type = IdType.ASSIGN_ID)  // 行注：应用 @TableId 注解
    private Long id;  // 行注：声明ID字段

    /** 用户名 */
    private String username;  // 行注：声明username字段

    /** 昵称 */
    private String nickname;  // 行注：声明nickname字段

    /** 密码哈希 */
    @TableField("password_hash")  // 行注：应用 @TableField 注解
    private String password;  // 行注：声明密码字段

    /** 手机号 */
    @TableField("mobile")  // 行注：应用 @TableField 注解
    private String phone;  // 行注：声明phone字段

    /** 邮箱 */
    private String email;  // 行注：声明email字段

    /** 头像 URL */
    @TableField("avatar_url")  // 行注：应用 @TableField 注解
    private String avatar;  // 行注：声明头像字段

    /** 性别 */
    private Integer gender;  // 行注：声明gender字段

    /** 状态 */
    private Integer status;  // 行注：声明状态字段

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)  // 行注：应用 @TableField 注解
    private LocalDateTime createTime;  // 行注：声明创建时间字段

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)  // 行注：应用 @TableField 注解
    private LocalDateTime updateTime;  // 行注：声明更新时间字段

    /** 逻辑删除标记 */
    @TableLogic  // 行注：应用 @TableLogic 注解
    private Integer deleted;  // 行注：声明deleted字段
}  // 行注：结束当前代码块
