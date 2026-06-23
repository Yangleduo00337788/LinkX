package com.linkx.server.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 群成员：角色、禁言、群备注、公告/消息已读游标、免打扰等。 */
@Data
@TableName("im_group_member")
public class ImGroupMember {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long groupId;

    private Long userId;

    private Integer role;

    private LocalDateTime muteTime;

    private LocalDateTime noticeReadTime;

    private String groupRemark;

    private Boolean notificationMuted;

    private LocalDateTime lastMessageReadTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
