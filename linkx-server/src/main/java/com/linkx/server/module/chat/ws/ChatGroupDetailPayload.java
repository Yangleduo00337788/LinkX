package com.linkx.server.module.chat.ws;  // 行注：声明当前文件所在包 com.linkx.server.module.chat.ws

import com.linkx.server.module.group.dto.GroupDetailDTO;  // 行注：引入 GroupDetailDTO 类型

/** {@link ChatEventType#GROUP_DETAIL}：群资料/成员变更后推送最新详情。 */
// 行注：定义 ChatGroupDetailPayload 记录类型
public record ChatGroupDetailPayload(GroupDetailDTO detail) {
}  // 行注：结束当前代码块
