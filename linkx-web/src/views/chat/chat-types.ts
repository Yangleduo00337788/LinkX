export const SESSION_TYPE_SINGLE = 1
export const SESSION_TYPE_GROUP = 2
export const MESSAGE_TYPE_TEXT = 0
export const MESSAGE_TYPE_IMAGE = 1
export const MESSAGE_TYPE_FILE = 2
export const MESSAGE_TYPE_SYSTEM = 3
export const MESSAGE_STATUS_RECALLED = 1
export const GROUP_ROLE_MEMBER = 0
export const GROUP_ROLE_ADMIN = 1
export const GROUP_ROLE_OWNER = 2

export interface FriendItem {
  friendId: string | number
  friendNickname: string
  friendAvatar?: string
  friendUsername: string
}

export interface ChatSession {
  id?: string | number
  userId?: string | number
  targetId: string | number
  sessionType: number
  targetNickname: string
  targetUsername?: string
  targetAvatar?: string
  lastMessage?: string
  lastMessageTime?: string
  unreadCount: number
  memberCount?: number
  myRole?: number
  groupRemark?: string
  notice?: string
  noticeUnread?: boolean
  muted?: boolean
  muteTime?: string
  notificationMuted?: boolean
  targetOnline?: boolean
  isDraft?: boolean
}

export interface GroupMember {
  userId: string | number
  username: string
  nickname: string
  avatar?: string
  role: number
  muted?: boolean
  muteTime?: string
}

export interface GroupDetail {
  id: string | number
  groupName: string
  groupAvatar?: string
  groupRemark?: string
  notice?: string
  noticeUpdateTime?: string
  noticeReadTime?: string
  noticeUnread?: boolean
  ownerId: string | number
  maxMembers: number
  memberCount: number
  myRole: number
  muted?: boolean
  muteTime?: string
  notificationMuted?: boolean
  members: GroupMember[]
}

export interface DisplayMessage {
  id: string | number
  localId: string
  clientMessageId?: string
  isMe: boolean
  isSystem: boolean
  name: string
  fromAvatar?: string
  content: string
  msgType: number
  status: number
  readTime?: string
  createTime: string
  time: string
  readStatus: string
  deliveryStatus: 'sending' | 'sent' | 'failed'
  fileName?: string
  fileSize?: number
  sessionType: number
  targetId: string
  mentionAll: boolean
  mentionUserIds: string[]
  mentionDisplayNames: string[]
  mentionedMe: boolean
  retryFile?: File
  uploadedFileId?: string | number
}

export interface MentionCandidate {
  key: string
  label: string
  meta: string
  insertToken: string
  mentionUserId?: string
  isAll: boolean
}
