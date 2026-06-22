import { getDateTimeTimestamp, parseDateTime } from './datetime'
import {
  GROUP_ROLE_ADMIN,
  GROUP_ROLE_OWNER,
  MESSAGE_STATUS_RECALLED,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  MESSAGE_TYPE_SYSTEM,
  MESSAGE_TYPE_TEXT,
  SESSION_TYPE_GROUP,
  SESSION_TYPE_SINGLE,
  type ChatSession,
  type DisplayMessage
} from '../types/chat'

export function buildSessionKey(targetId: string | number, sessionType: number) {
  return `${sessionType}-${String(targetId)}`
}

export function normalizeSession(session: any): ChatSession {
  return {
    ...session,
    sessionType: Number(session.sessionType || SESSION_TYPE_SINGLE),
    unreadCount: Number(session.unreadCount || 0),
    memberCount: session.memberCount != null ? Number(session.memberCount) : undefined,
    myRole: session.myRole != null ? Number(session.myRole) : undefined,
    noticeUnread: Boolean(session.noticeUnread),
    muted: Boolean(session.muted),
    notificationMuted: Boolean(session.notificationMuted),
    targetOnline: Boolean(session.targetOnline)
  }
}

export function createLocalMessageId(prefix = 'local') {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`
}

export function getMessageAnchorKey(messageItem: Pick<DisplayMessage, 'id' | 'localId' | 'clientMessageId'>) {
  return String(messageItem.clientMessageId || messageItem.id || messageItem.localId)
}

export function compareDisplayMessages(left: DisplayMessage, right: DisplayMessage) {
  const leftTime = getDateTimeTimestamp(left.createTime)
  const rightTime = getDateTimeTimestamp(right.createTime)
  if (leftTime !== rightTime) {
    return leftTime - rightTime
  }
  return String(left.id).localeCompare(String(right.id))
}

export function resolveMessageTargetId(item: any, isMe: boolean, sessionType: number) {
  if (sessionType === SESSION_TYPE_GROUP) {
    return String(item.toUserId)
  }
  return String(isMe ? item.toUserId : item.fromUserId)
}

export function escapeRegExp(value: string) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

export function escapeAttributeSelector(value: string) {
  if (typeof CSS !== 'undefined' && typeof CSS.escape === 'function') {
    return CSS.escape(value)
  }
  return value.replace(/\\/g, '\\\\').replace(/"/g, '\\"')
}

export function formatTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = parseDateTime(time)
  if (!date) {
    return ''
  }
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  if (days === 0) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  if (days === 1) {
    return '昨天'
  }
  if (days < 7) {
    const weekdays = ['日', '一', '二', '三', '四', '五', '六']
    return `周${weekdays[date.getDay()]}`
  }
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' })
}

export function formatDateTime(time?: string) {
  if (!time) {
    return ''
  }
  const date = parseDateTime(time)
  if (!date) {
    return ''
  }
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).replace(/\//g, '-')
}

export function formatSize(bytes?: number) {
  if (!bytes) {
    return ''
  }
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function roleText(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return '群主'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return '管理员'
  }
  return '成员'
}

export function roleClass(role?: number) {
  if (role === GROUP_ROLE_OWNER) {
    return 'owner'
  }
  if (role === GROUP_ROLE_ADMIN) {
    return 'admin'
  }
  return 'member'
}

export function getFileName(url: string) {
  if (!url) {
    return '文件'
  }
  const segments = url.split('/')
  const name = segments[segments.length - 1]
  const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/i
  return uuidPattern.test(name) ? '文件' : name
}

export function getMessagePreview(msg: Pick<DisplayMessage, 'content' | 'msgType' | 'status' | 'isSystem'>) {
  if (msg.status === MESSAGE_STATUS_RECALLED) {
    return '[消息已撤回]'
  }
  if (msg.isSystem || msg.msgType === MESSAGE_TYPE_TEXT) {
    return msg.content
  }
  if (msg.msgType === MESSAGE_TYPE_IMAGE) {
    return '[图片]'
  }
  if (msg.msgType === MESSAGE_TYPE_FILE) {
    return '[文件]'
  }
  if (msg.msgType === MESSAGE_TYPE_SYSTEM) {
    return msg.content
  }
  return '[消息]'
}
