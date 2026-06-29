/**
 * 会话置顶、免打扰、清空记录、举报会话等（与 sessionApi / chatApi 对齐）。
 */
import type { Ref } from 'vue'
import { chatApi, friendApi, reportApi, sessionApi, groupApi } from '../api/client'
import { SESSION_TYPE_GROUP, type ChatSession } from '../types/chat'
import { buildSessionKey } from '../utils/chat'

interface FeedbackApi {
  success: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

export function useChatSessionActions(options: {
  message: FeedbackApi
  currentTargetId: Ref<string | null>
  currentSessionType: Ref<number>
  currentSession: Ref<ChatSession | null>
  isGroupSession: Ref<boolean>
  upsertSession: (session: ChatSession) => ChatSession
  loadSessions: () => Promise<void>
  refreshCurrentSession: () => Promise<void>
  messages: Ref<unknown[]>
  openConfirmDialog: (opts: {
    title: string
    subtitle?: string
    description?: string
    confirmText?: string
    cancelText?: string
  }) => Promise<boolean>
}) {
  async function patchSession(partial: {
    pinned?: boolean
    notificationMuted?: boolean
    sessionRemark?: string
  }) {
    const targetId = options.currentTargetId.value
    if (!targetId) return
    const sessionType = options.currentSessionType.value
    await sessionApi.updatePreferences(targetId, sessionType, partial)
    const cur = options.currentSession.value
    if (cur) {
      options.upsertSession({
        ...cur,
        pinned: partial.pinned ?? cur.pinned,
        notificationMuted: partial.notificationMuted ?? cur.notificationMuted,
        sessionRemark: partial.sessionRemark !== undefined ? partial.sessionRemark : cur.sessionRemark
      })
    }
    await options.loadSessions()
  }

  async function toggleSessionPinned() {
    const cur = options.currentSession.value
    if (!cur) return
    try {
      await patchSession({ pinned: !cur.pinned })
      options.message.success(cur.pinned ? '已取消置顶' : '已置顶聊天')
    } catch (e: any) {
      options.message.error(e?.message || '操作失败')
    }
  }

  async function toggleNotificationMuted() {
    const cur = options.currentSession.value
    if (!cur) return
    const next = !Boolean(cur.notificationMuted)
    try {
      if (options.isGroupSession.value) {
        await groupApi.updatePreferences(options.currentTargetId.value!, { notificationMuted: next })
      } else {
        await patchSession({ notificationMuted: next })
      }
      options.message.success(next ? '已开启免打扰' : '已关闭免打扰')
      await options.refreshCurrentSession()
    } catch (e: any) {
      options.message.error(e?.message || '操作失败')
    }
  }

  async function saveGroupRemark(remark: string) {
    if (!options.isGroupSession.value || !options.currentTargetId.value) return
    try {
      await groupApi.updatePreferences(options.currentTargetId.value, { groupRemark: remark })
      options.message.success('群备注已保存')
      await options.loadSessions()
      await options.refreshCurrentSession()
    } catch (e: any) {
      options.message.error(e?.message || '保存失败')
    }
  }

  /** 好友备注（sys_friend.remark），仅单聊 */
  async function saveFriendRemark(remark: string) {
    const friendId = options.currentTargetId.value
    if (!friendId || options.isGroupSession.value) return
    try {
      await friendApi.updateRemark(friendId, remark.trim())
      options.message.success('好友备注已保存')
      await options.loadSessions()
      await options.refreshCurrentSession()
    } catch (e: any) {
      options.message.error(e?.message || '保存失败')
    }
  }

  /** 会话备注（im_session.session_remark），仅影响会话列表展示 */
  async function saveSessionRemark(remark: string) {
    if (options.isGroupSession.value) return
    try {
      await patchSession({ sessionRemark: remark.trim() })
      options.message.success('会话备注已保存')
    } catch (e: any) {
      options.message.error(e?.message || '保存失败')
    }
  }

  async function clearChatHistory() {
    const targetId = options.currentTargetId.value
    if (!targetId) return
    const ok = await options.openConfirmDialog({
      title: '清空聊天记录？',
      description: '仅清空你本机的会话展示与未读，不会删除对方消息记录。',
      confirmText: '清空',
      cancelText: '取消'
    })
    if (!ok) return
    try {
      await chatApi.clearChatHistory(targetId, options.currentSessionType.value)
      options.messages.value = []
      await options.loadSessions()
      await options.refreshCurrentSession()
      options.message.success('聊天记录已清空')
    } catch (e: any) {
      options.message.error(e?.message || '清空失败')
    }
  }

  async function reportCurrentChat(reasonDetail?: string) {
    const targetId = options.currentTargetId.value
    if (!targetId) return
    const targetType = options.isGroupSession.value ? 'GROUP' : 'USER'
    try {
      await reportApi.submit({
        targetType,
        targetId: String(targetId),
        reasonCategory: 'CHAT',
        reasonDetail: reasonDetail?.trim() || undefined
      })
      options.message.success('举报已提交')
      return true
    } catch (e: any) {
      options.message.error(e?.message || '提交失败')
      return false
    }
  }

  function currentPinned() {
    return Boolean(options.currentSession.value?.pinned)
  }

  function currentNotificationMuted() {
    return Boolean(options.currentSession.value?.notificationMuted)
  }

  return {
    toggleSessionPinned,
    toggleNotificationMuted,
    saveGroupRemark,
    saveFriendRemark,
    saveSessionRemark,
    clearChatHistory,
    reportCurrentChat,
    currentPinned,
    currentNotificationMuted,
    buildSessionKey
  }
}