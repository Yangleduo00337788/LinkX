# -*- coding: utf-8 -*-
"""为缺少类级 Javadoc 的 Java 文件插入简短中文注释。"""
import os
import re

ROOT = os.path.join(os.path.dirname(__file__), "..", "linkx-server", "src", "main", "java")

# rel_path -> 插入在 import 之后、类声明之前的 Javadoc（单行或多行）
COMMENTS = {
    "com/linkx/server/module/chat/service/impl/ChatServiceImpl.java": (
        "/**\n * 聊天核心实现：权限校验、消息入库、会话更新、事务提交后 WebSocket 推送。\n */"
    ),
    "com/linkx/server/module/chat/helper/ChatMessageHelper.java": "/** 消息实体转 DTO、@成员列表序列化等。 */",
    "com/linkx/server/module/chat/constant/ChatConstants.java": "/** 会话类型、消息类型、状态等常量。 */",
    "com/linkx/server/module/chat/constant/ChatHistoryLimits.java": "/** 历史消息单次拉取条数上限。 */",
    "com/linkx/server/module/chat/dto/SendMessageRequest.java": "/** REST/WebSocket 发送文本或富文本消息请求。 */",
    "com/linkx/server/module/chat/dto/SendFileMessageRequest.java": "/** 发送图片/文件类消息（引用已上传 fileId）。 */",
    "com/linkx/server/module/chat/dto/MessageDTO.java": "/** 单条消息返回体。 */",
    "com/linkx/server/module/chat/dto/ChatSessionDTO.java": "/** 会话列表项：对方/群摘要、未读、最后一条消息。 */",
    "com/linkx/server/module/chat/dto/ChatWsTicketDTO.java": "/** WebSocket 握手用短期 ticket。 */",
    "com/linkx/server/module/chat/ws/ChatWebSocketHandler.java": "/** 处理客户端 WS 帧：连接、心跳、发消息、已读等。 */",
    "com/linkx/server/module/chat/ws/ChatHandshakeInterceptor.java": "/** 握手阶段校验 ws ticket 并绑定 userId。 */",
    "com/linkx/server/module/chat/ws/ChatWebSocketTicketService.java": "/** 为 REST 登录用户签发 WS 连接 ticket（Redis TTL）。 */",
    "com/linkx/server/module/chat/ws/ChatSocketSessionRegistry.java": "/** userId 到 WebSocketSession 的注册表（多端可多条）。 */",
    "com/linkx/server/module/chat/ws/ChatEventPushService.java": "/** 封装向用户/群成员推送 {@link ChatRealtimeEvent}。 */",
    "com/linkx/server/module/chat/ws/ChatPresenceService.java": "/** 用户在线状态维护与变更推送。 */",
    "com/linkx/server/module/chat/ws/ChatGroupRealtimeService.java": "/** 群资料/成员变更的实时广播。 */",
    "com/linkx/server/module/chat/ws/ChatEventType.java": "/** 服务端推送事件类型。 */",
    "com/linkx/server/module/chat/ws/ChatSocketAction.java": "/** 客户端上行动作类型。 */",
    "com/linkx/server/module/chat/ws/ChatRealtimeEvent.java": "/** type + payload 实时事件信封。 */",
    "com/linkx/server/module/chat/ws/ChatConnectedPayload.java": "/** 连接成功下行载荷。 */",
    "com/linkx/server/module/chat/ws/ChatMessagePayload.java": "/** 新消息推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatSessionPayload.java": "/** 会话列表变更推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatReadReceiptPayload.java": "/** 已读回执推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatOnlineStatusPayload.java": "/** 在线状态推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatGroupDetailPayload.java": "/** 群详情变更推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatGroupRemovedPayload.java": "/** 被移出群/群解散推送载荷。 */",
    "com/linkx/server/module/chat/ws/ChatCommandResultPayload.java": "/** WS 命令执行结果（成功/失败）。 */",
    "com/linkx/server/module/group/controller/GroupController.java": "/** 群创建、资料、成员、公告、入群申请、个人偏好等 API。 */",
    "com/linkx/server/module/group/service/GroupService.java": "/** 群组领域服务接口。 */",
    "com/linkx/server/module/group/service/impl/GroupServiceImpl.java": "/** 群业务实现：角色权限、成员管理、实时通知。 */",
    "com/linkx/server/module/group/constant/GroupConstants.java": "/** 群角色、成员状态、申请状态等。 */",
    "com/linkx/server/module/group/dto/GroupDTO.java": "/** 群列表简要信息。 */",
    "com/linkx/server/module/group/dto/GroupDetailDTO.java": "/** 群详情（含公告、成员数等）。 */",
    "com/linkx/server/module/group/dto/GroupMemberDTO.java": "/** 群成员展示信息。 */",
    "com/linkx/server/module/group/dto/GroupRequestDTO.java": "/** 入群/邀请申请记录。 */",
    "com/linkx/server/module/group/request/CreateGroupRequest.java": "/** 创建群请求。 */",
    "com/linkx/server/module/group/request/AddGroupMembersRequest.java": "/** 批量加群成员。 */",
    "com/linkx/server/module/group/request/InviteGroupMembersRequest.java": "/** 邀请好友入群。 */",
    "com/linkx/server/module/group/request/GroupJoinRequest.java": "/** 申请加入群。 */",
    "com/linkx/server/module/group/request/UpdateGroupProfileRequest.java": "/** 修改群名称/头像等。 */",
    "com/linkx/server/module/group/request/UpdateGroupNoticeRequest.java": "/** 修改群公告。 */",
    "com/linkx/server/module/group/request/UpdateGroupPreferencesRequest.java": "/** 当前用户在群内的偏好（免打扰、备注等）。 */",
    "com/linkx/server/module/group/request/MuteGroupMemberRequest.java": "/** 禁言群成员。 */",
    "com/linkx/server/module/blacklist/controller/BlacklistController.java": "/** 黑名单列表、拉黑、取消拉黑。 */",
    "com/linkx/server/module/blacklist/service/BlacklistService.java": "/** 黑名单查询与拦截。 */",
    "com/linkx/server/module/blacklist/service/impl/BlacklistServiceImpl.java": "/** 黑名单持久化实现。 */",
    "com/linkx/server/module/blacklist/dto/BlacklistUserDTO.java": "/** 黑名单中的用户摘要。 */",
    "com/linkx/server/module/file/service/impl/FileServiceImpl.java": "/** 上传校验、落盘、元数据、ticket 访问与删除。 */",
    "com/linkx/server/module/file/dto/FileDTO.java": "/** 文件元数据返回。 */",
    "com/linkx/server/module/file/dto/FileAccessDTO.java": "/** 带 ticket 的临时访问 URL。 */",
    "com/linkx/server/module/friend/dto/FriendDTO.java": "/** 好友列表项。 */",
    "com/linkx/server/module/friend/dto/FriendRequestDTO.java": "/** 待处理好友申请。 */",
    "com/linkx/server/module/friend/dto/SendFriendRequest.java": "/** 发起好友申请请求体。 */",
    "com/linkx/server/module/user/dto/UserProfileDTO.java": "/** 用户资料（对外展示字段）。 */",
    "com/linkx/server/module/user/dto/UpdateProfileRequest.java": "/** 修改本人资料。 */",
    "com/linkx/server/entity/SysUser.java": "/** 表 {@code sys_user} 用户账号与资料。 */",
    "com/linkx/server/entity/SysFriend.java": "/** 表 {@code sys_friend} 好友关系。 */",
    "com/linkx/server/entity/SysFriendRequest.java": "/** 表 {@code sys_friend_request} 好友申请。 */",
    "com/linkx/server/entity/SysBlacklist.java": "/** 表 {@code sys_blacklist} 黑名单。 */",
    "com/linkx/server/entity/SysFile.java": "/** 表 {@code sys_file} 上传文件记录。 */",
    "com/linkx/server/entity/ImSession.java": "/** 表 {@code im_session} 聊天会话。 */",
    "com/linkx/server/entity/ImMessage.java": "/** 表 {@code im_message} 聊天消息。 */",
    "com/linkx/server/entity/ImGroupInfo.java": "/** 表 {@code im_group_info} 群资料。 */",
    "com/linkx/server/entity/ImGroupMember.java": "/** 表 {@code im_group_member} 群成员与偏好。 */",
    "com/linkx/server/entity/ImGroupRequest.java": "/** 表 {@code im_group_request} 入群申请。 */",
    "com/linkx/server/mapper/SysUserMapper.java": "/** {@link SysUser} MyBatis-Plus Mapper。 */",
    "com/linkx/server/mapper/SysFriendMapper.java": "/** {@link SysFriend} Mapper。 */",
    "com/linkx/server/mapper/SysFriendRequestMapper.java": "/** {@link SysFriendRequest} Mapper。 */",
    "com/linkx/server/mapper/SysBlacklistMapper.java": "/** {@link SysBlacklist} Mapper。 */",
    "com/linkx/server/mapper/SysFileMapper.java": "/** {@link SysFile} Mapper。 */",
    "com/linkx/server/mapper/ImSessionMapper.java": "/** {@link ImSession} Mapper。 */",
    "com/linkx/server/mapper/ImMessageMapper.java": "/** {@link ImMessage} Mapper。 */",
    "com/linkx/server/mapper/ImGroupInfoMapper.java": "/** {@link ImGroupInfo} Mapper。 */",
    "com/linkx/server/mapper/ImGroupMemberMapper.java": "/** {@link ImGroupMember} Mapper。 */",
    "com/linkx/server/mapper/ImGroupRequestMapper.java": "/** {@link ImGroupRequest} Mapper。 */",
}


def has_javadoc_before_class(text: str) -> bool:
    # 找第一个 public/@interface/@RestController 等
    m = re.search(r"\n(@[A-Za-z]|public\s+(class|interface|enum|record)\s)", text)
    if not m:
        return True
    head = text[: m.start()]
    # 去掉 package 和 import 后看是否有 /**
    tail = head
    if "import " in tail:
        parts = tail.rsplit("import ", 1)
        tail = parts[-1]
        # 实际上要看最后一个 import 之后
        idx = head.rfind("\nimport ")
        if idx >= 0:
            tail = head[idx:]
    return "/**" in tail


def insert_javadoc(content: str, javadoc: str) -> str:
    lines = content.split("\n")
    j = 0
    while j < len(lines) and not lines[j].startswith("package "):
        j += 1
    j += 1
    while j < len(lines) and (lines[j].strip() == "" or lines[j].startswith("import ")):
        j += 1
  # skip existing javadoc block
    if j < len(lines) and lines[j].strip().startswith("/**"):
        return content
    lines.insert(j, javadoc)
    if j > 0 and lines[j - 1].strip() != "":
        lines.insert(j, "")
        j += 1
    return "\n".join(lines)


def main():
    ok, skip, miss = 0, 0, 0
    for rel, doc in COMMENTS.items():
        path = os.path.normpath(os.path.join(ROOT, rel.replace("/", os.sep)))
        if not os.path.isfile(path):
            print("MISSING", rel)
            miss += 1
            continue
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        if has_javadoc_before_class(content):
            skip += 1
            continue
        new_content = insert_javadoc(content, doc)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            f.write(new_content)
        print("OK", rel)
        ok += 1
    print(f"done ok={ok} skip={skip} missing={miss}")


if __name__ == "__main__":
    main()