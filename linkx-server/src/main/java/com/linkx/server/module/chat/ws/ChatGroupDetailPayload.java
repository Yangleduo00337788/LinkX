package com.linkx.server.module.chat.ws;

import com.linkx.server.module.group.dto.GroupDetailDTO;

/** {@link ChatEventType#GROUP_DETAIL}：群资料/成员变更后推送最新详情。 */
public record ChatGroupDetailPayload(GroupDetailDTO detail) {
}
