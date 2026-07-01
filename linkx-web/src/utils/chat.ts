/**
 * 提供聊天会话键、消息展示和时间格式化等通用方法。
 */
import { getDateTimeTimestamp, parseDateTime } from './datetime'  // 行注：引入 getDateTimeTimestamp, parseDateTime 能力
import {  // 行注：引入 { 模块
  GROUP_ROLE_ADMIN,  // 行注：补充 GROUP_ROLE_ADMIN 配置项
  GROUP_ROLE_OWNER,  // 行注：补充当前配置项
  MESSAGE_STATUS_RECALLED,  // 行注：补充当前配置项
  MESSAGE_TYPE_FILE,  // 行注：补充当前配置项
  MESSAGE_TYPE_IMAGE,  // 行注：补充当前配置项
  MESSAGE_TYPE_SYSTEM,  // 行注：补充当前配置项
  MESSAGE_TYPE_TEXT,  // 行注：补充当前配置项
  SESSION_TYPE_GROUP,  // 行注：补充当前配置项
  SESSION_TYPE_SINGLE,  // 行注：补充当前配置项
  type ChatSession,  // 行注：补充当前配置项
  type DisplayMessage,  // 行注：补充当前表达式
  type GroupMember
} from '../types/chat'  // 行注：补充当前表达式

export function buildSessionKey(targetId: string | number, sessionType: number) {  // 行注：导出当前能力
  return `${sessionType}-${String(targetId)}`  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function normalizeSession(session: any): ChatSession {  // 行注：导出当前能力
  return {  // 行注：返回当前结果
    ...session,  // 行注：补充当前配置项
    sessionType: Number(session.sessionType || SESSION_TYPE_SINGLE),  // 行注：设置 sessionType 配置项
    unreadCount: Number(session.unreadCount || 0),  // 行注：设置 unreadCount 配置项
    memberCount: session.memberCount != null ? Number(session.memberCount) : undefined,  // 行注：设置 memberCount 配置项
    myRole: session.myRole != null ? Number(session.myRole) : undefined,  // 行注：设置 myRole 配置项
    noticeUnread: Boolean(session.noticeUnread),  // 行注：设置 noticeUnread 配置项
    muted: Boolean(session.muted),  // 行注：设置 muted 配置项
    notificationMuted: Boolean(session.notificationMuted),  // 行注：设置 notificationMuted 配置项
    targetOnline: Boolean(session.targetOnline),  // 行注：设置 targetOnline 配置项
    sessionRemark: session.sessionRemark != null ? String(session.sessionRemark) : undefined,
    pinned: Boolean(session.pinned)
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function createLocalMessageId(prefix = 'local') {  // 行注：导出当前能力
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getMessageAnchorKey(messageItem: Pick<DisplayMessage, 'id' | 'localId' | 'clientMessageId'>) {  // 行注：导出当前能力
  return String(messageItem.clientMessageId || messageItem.id || messageItem.localId)  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function compareDisplayMessages(left: DisplayMessage, right: DisplayMessage) {  // 行注：导出当前能力
  const leftTime = getDateTimeTimestamp(left.createTime)  // 行注：初始化 leftTime 变量
  const rightTime = getDateTimeTimestamp(right.createTime)  // 行注：初始化 rightTime 变量
  if (leftTime !== rightTime) {  // 行注：判断当前条件是否成立
    return leftTime - rightTime  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return String(left.id).localeCompare(String(right.id))  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function resolveMessageTargetId(item: any, isMe: boolean, sessionType: number) {  // 行注：导出当前能力
  if (sessionType === SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
    return String(item.toUserId)  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return String(isMe ? item.toUserId : item.fromUserId)  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function escapeRegExp(value: string) {  // 行注：导出当前能力
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function escapeAttributeSelector(value: string) {  // 行注：导出当前能力
  if (typeof CSS !== 'undefined' && typeof CSS.escape === 'function') {  // 行注：判断当前条件是否成立
    return CSS.escape(value)  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function formatTime(time?: string) {  // 行注：导出当前能力
  if (!time) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const date = parseDateTime(time)  // 行注：初始化 date 变量
  if (!date) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const now = new Date()  // 行注：初始化 now 变量
  const diff = now.getTime() - date.getTime()  // 行注：初始化 diff 变量
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))  // 行注：初始化 days 变量
  if (days === 0) {  // 行注：判断当前条件是否成立
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (days === 1) {  // 行注：判断当前条件是否成立
    return '昨天'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (days < 7) {  // 行注：判断当前条件是否成立
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']  // 行注：初始化 weekdays 变量
    return `周${weekdays[date.getDay()]}`  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function formatDateTime(time?: string) {  // 行注：导出当前能力
  if (!time) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const date = parseDateTime(time)  // 行注：初始化 date 变量
  if (!date) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return date.toLocaleString('zh-CN', {  // 行注：返回当前结果
    year: 'numeric',  // 行注：设置 year 配置项
    month: '2-digit',  // 行注：设置 month 配置项
    day: '2-digit',  // 行注：设置 day 配置项
    hour: '2-digit',  // 行注：设置 hour 配置项
    minute: '2-digit'  // 行注：设置 minute 配置项
  }).replace(/\//g, '-')  // 行注：调用 replace 方法
}  // 行注：结束当前代码块

export function formatSize(bytes?: number) {  // 行注：导出当前能力
  if (!bytes) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (bytes < 1024) {  // 行注：判断当前条件是否成立
    return `${bytes} B`  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (bytes < 1024 * 1024) {  // 行注：判断当前条件是否成立
    return `${(bytes / 1024).toFixed(1)} KB`  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function roleText(role?: number) {  // 行注：导出当前能力
  if (role === GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
    return '群主'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
    return '管理员'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return '成员'  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function roleClass(role?: number) {  // 行注：导出当前能力
  if (role === GROUP_ROLE_OWNER) {  // 行注：判断当前条件是否成立
    return 'owner'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (role === GROUP_ROLE_ADMIN) {  // 行注：判断当前条件是否成立
    return 'admin'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return 'member'  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getFileName(url: string) {  // 行注：导出当前能力
  if (!url) {  // 行注：判断当前条件是否成立
    return '文件'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const segments = url.split('/')  // 行注：初始化 segments 变量
  const name = segments[segments.length - 1]  // 行注：初始化 name 变量
  const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i  // 行注：初始化 uuidPattern 变量
  return uuidPattern.test(name) ? '文件' : name  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getMessagePreview(msg: Pick<DisplayMessage, 'content' | 'msgType' | 'status' | 'isSystem'>) {  // 行注：导出当前能力
  if (msg.status === MESSAGE_STATUS_RECALLED) {  // 行注：判断当前条件是否成立
    return '[消息已撤回]'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (msg.isSystem || msg.msgType === MESSAGE_TYPE_TEXT) {  // 行注：判断当前条件是否成立
    return msg.content  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (msg.msgType === MESSAGE_TYPE_IMAGE) {  // 行注：判断当前条件是否成立
    return '[图片]'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (msg.msgType === MESSAGE_TYPE_FILE) {  // 行注：判断当前条件是否成立
    return '[文件]'  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (msg.msgType === MESSAGE_TYPE_SYSTEM) {  // 行注：判断当前条件是否成立
    return msg.content  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return '[消息]'  // 行注：返回当前结果
}  // 行注：结束当前代码块

/** 解析群成员在群聊中的展示名；preferCard 为 true 时优先使用群名片。 */
export function resolveGroupMemberDisplayName(
  member: Pick<GroupMember, 'nickname' | 'username' | 'memberCardName'> | null | undefined,
  preferCard = true
) {
  if (!member) return ''
  if (preferCard && member.memberCardName?.trim()) {
    return member.memberCardName.trim()
  }
  return member.nickname?.trim() || member.username?.trim() || member.memberCardName?.trim() || ''
}

/** 解析群聊消息发送者展示名。 */
export function resolveGroupMessageSenderName(
  message: Pick<DisplayMessage, 'name' | 'fromUserId'>,
  members: GroupMember[],
  preferCard = true
) {
  const member = message.fromUserId
    ? members.find(item => String(item.userId) === String(message.fromUserId))
    : undefined
  const fromMember = resolveGroupMemberDisplayName(member, preferCard)
  if (fromMember) return fromMember
  const fromMessage = message.name?.trim()
  if (fromMessage) return fromMessage
  return message.fromUserId ? '成员' : ''
}
