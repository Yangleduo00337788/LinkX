/**
 * chat 类型定义，约束前后端数据结构和页面状态。
 */
export const SESSION_TYPE_SINGLE = 1  // 行注：导出当前能力
export const SESSION_TYPE_GROUP = 2  // 行注：导出当前能力
export const MESSAGE_TYPE_TEXT = 0  // 行注：导出当前能力
export const MESSAGE_TYPE_IMAGE = 1  // 行注：导出当前能力
export const MESSAGE_TYPE_FILE = 2  // 行注：导出当前能力
export const MESSAGE_TYPE_SYSTEM = 3  // 行注：导出当前能力
export const MESSAGE_STATUS_RECALLED = 1  // 行注：导出当前能力
export const GROUP_ROLE_MEMBER = 0  // 行注：导出当前能力
export const GROUP_ROLE_ADMIN = 1  // 行注：导出当前能力
export const GROUP_ROLE_OWNER = 2  // 行注：导出当前能力

export interface FriendItem {  // 行注：导出当前能力
  friendId: string | number  // 行注：设置 friendId 配置项
  friendNickname: string  // 行注：设置 friendNickname 配置项
  friendAvatar?: string  // 行注：补充当前表达式
  friendUsername: string  // 行注：设置 friendUsername 配置项
}  // 行注：结束当前代码块

export interface ChatSession {  // 行注：导出当前能力
  id?: string | number  // 行注：补充当前表达式
  userId?: string | number  // 行注：补充当前表达式
  targetId: string | number  // 行注：设置 targetId 配置项
  sessionType: number  // 行注：设置 sessionType 配置项
  targetNickname: string  // 行注：设置 targetNickname 配置项
  targetUsername?: string  // 行注：补充当前表达式
  targetAvatar?: string  // 行注：补充当前表达式
  lastMessage?: string  // 行注：补充当前表达式
  lastMessageTime?: string  // 行注：补充当前表达式
  unreadCount: number  // 行注：设置 unreadCount 配置项
  memberCount?: number  // 行注：补充当前表达式
  myRole?: number  // 行注：补充当前表达式
  groupRemark?: string  // 行注：补充当前表达式
  notice?: string  // 行注：补充当前表达式
  noticeUnread?: boolean  // 行注：补充当前表达式
  muted?: boolean  // 行注：补充当前表达式
  muteTime?: string  // 行注：补充当前表达式
  notificationMuted?: boolean  // 行注：补充当前表达式
  targetOnline?: boolean  // 行注：补充当前表达式
  isDraft?: boolean  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export interface GroupMember {  // 行注：导出当前能力
  userId: string | number  // 行注：设置 userId 配置项
  username: string  // 行注：设置 username 配置项
  nickname: string  // 行注：设置 nickname 配置项
  avatar?: string  // 行注：补充当前表达式
  role: number  // 行注：设置 role 配置项
  muted?: boolean  // 行注：补充当前表达式
  muteTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export interface GroupDetail {  // 行注：导出当前能力
  id: string | number  // 行注：设置 id 配置项
  groupName: string  // 行注：设置 groupName 配置项
  groupAvatar?: string  // 行注：补充当前表达式
  groupRemark?: string  // 行注：补充当前表达式
  notice?: string  // 行注：补充当前表达式
  noticeUpdateTime?: string  // 行注：补充当前表达式
  noticeReadTime?: string  // 行注：补充当前表达式
  noticeUnread?: boolean  // 行注：补充当前表达式
  ownerId: string | number  // 行注：设置 ownerId 配置项
  maxMembers: number  // 行注：设置 maxMembers 配置项
  memberCount: number  // 行注：设置 memberCount 配置项
  myRole: number  // 行注：设置 myRole 配置项
  muted?: boolean  // 行注：补充当前表达式
  muteTime?: string  // 行注：补充当前表达式
  notificationMuted?: boolean  // 行注：补充当前表达式
  members: GroupMember[]  // 行注：设置 members 配置项
}  // 行注：结束当前代码块

export interface GroupMediaItem {  // 行注：导出当前能力
  id: string | number  // 行注：设置 id 配置项
  fromUserId?: string | number  // 行注：补充当前表达式
  fromNickname?: string  // 行注：补充当前表达式
  fromAvatar?: string  // 行注：补充当前表达式
  content: string  // 行注：设置 content 配置项
  accessUrl?: string  // 行注：补充当前表达式
  msgType: number  // 行注：设置 msgType 配置项
  fileName?: string  // 行注：补充当前表达式
  fileSize?: number  // 行注：补充当前表达式
  fileType?: string  // 行注：补充当前表达式
  createTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export interface GroupRequestItem {  // 行注：导出当前能力
  id: number | string  // 行注：设置 id 配置项
  groupId: string | number  // 行注：设置 groupId 配置项
  groupName: string  // 行注：设置 groupName 配置项
  groupAvatar?: string  // 行注：补充当前表达式
  fromNickname?: string  // 行注：补充当前表达式
  fromUsername?: string  // 行注：补充当前表达式
  message?: string  // 行注：补充当前表达式
  requestType: number  // 行注：设置 requestType 配置项
  createTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export type GroupRoleFilter = 'all' | 'owner' | 'admin' | 'member' | 'muted'  // 行注：导出当前能力

export interface GroupRoleFilterOption {  // 行注：导出当前能力
  label: string  // 行注：设置 label 配置项
  value: GroupRoleFilter  // 行注：设置 value 配置项
}  // 行注：结束当前代码块

export interface GroupPreferenceDraftState {  // 行注：导出当前能力
  groupRemark: string  // 行注：设置 groupRemark 配置项
  notificationMuted: boolean  // 行注：设置 notificationMuted 配置项
}  // 行注：结束当前代码块

export interface DisplayMessage {  // 行注：导出当前能力
  id: string | number  // 行注：设置 id 配置项
  localId: string  // 行注：设置 localId 配置项
  clientMessageId?: string  // 行注：补充当前表达式
  isMe: boolean  // 行注：设置 isMe 配置项
  isSystem: boolean  // 行注：设置 isSystem 配置项
  name: string  // 行注：设置 name 配置项
  fromAvatar?: string  // 行注：补充当前表达式
  content: string  // 行注：设置 content 配置项
  msgType: number  // 行注：设置 msgType 配置项
  status: number  // 行注：设置 status 配置项
  readTime?: string  // 行注：补充当前表达式
  createTime: string  // 行注：设置 createTime 配置项
  time: string  // 行注：设置 time 配置项
  readStatus: string  // 行注：设置 readStatus 配置项
  deliveryStatus: 'sending' | 'sent' | 'failed'  // 行注：设置 deliveryStatus 配置项
  fileName?: string  // 行注：补充当前表达式
  fileSize?: number  // 行注：补充当前表达式
  sessionType: number  // 行注：设置 sessionType 配置项
  targetId: string  // 行注：设置 targetId 配置项
  mentionAll: boolean  // 行注：设置 mentionAll 配置项
  mentionUserIds: string[]  // 行注：设置 mentionUserIds 配置项
  mentionDisplayNames: string[]  // 行注：设置 mentionDisplayNames 配置项
  mentionedMe: boolean  // 行注：设置 mentionedMe 配置项
  retryFile?: File  // 行注：补充当前表达式
  uploadedFileId?: string | number  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export interface MentionCandidate {  // 行注：导出当前能力
  key: string  // 行注：设置 key 配置项
  label: string  // 行注：设置 label 配置项
  meta: string  // 行注：设置 meta 配置项
  insertToken: string  // 行注：设置 insertToken 配置项
  mentionUserId?: string  // 行注：补充当前表达式
  isAll: boolean  // 行注：设置 isAll 配置项
}  // 行注：结束当前代码块
