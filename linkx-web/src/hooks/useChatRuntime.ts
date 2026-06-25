/**
 * 协调聊天页初始化、会话同步、实时事件处理与滚动行为。
 */
import { computed, nextTick, ref, type ComputedRef, type Ref } from 'vue'  // 行注：引入 computed, nextTick, ref, type ComputedRef, type Ref 能力
import type { RouteLocationNormalizedLoaded, Router } from 'vue-router'  // 行注：引入 type { RouteLocationNormalizedLoaded, Router } 模块
import { chatApi, friendApi, groupApi, userApi } from '../api/client'  // 行注：引入 chatApi, friendApi, groupApi, userApi 能力
import { createChatRealtimeService } from '../services/chatRealtime'  // 行注：引入 createChatRealtimeService 能力
import type { ChatSession, DisplayMessage, FriendItem, GroupDetail } from '../types/chat'  // 行注：引入 type { ChatSession, DisplayMessage, FriendItem, GroupDetail } 模块
import { getCachedFileAccessUrl, resolveFileAccessUrl } from '../utils/file-access'  // 行注：引入 getCachedFileAccessUrl, resolveFileAccessUrl 能力
import { getDateTimeTimestamp } from '../utils/datetime'  // 行注：引入 getDateTimeTimestamp 能力
import { playNotificationSound, removeInAppNotificationsByMessageIds, showNotification } from '../utils/notify'  // 行注：引入 playNotificationSound, removeInAppNotificationsByMessageIds, showNotification 能力
import { useChatSocket } from './useChatSocket'  // 行注：引入 useChatSocket 能力
import {  // 行注：引入 { 模块
  MESSAGE_STATUS_RECALLED,  // 行注：补充 MESSAGE_STATUS_RECALLED 配置项
  MESSAGE_TYPE_FILE,  // 行注：补充当前配置项
  MESSAGE_TYPE_IMAGE,  // 行注：补充当前配置项
  MESSAGE_TYPE_SYSTEM,  // 行注：补充当前配置项
  MESSAGE_TYPE_TEXT,  // 行注：补充当前配置项
  SESSION_TYPE_GROUP,  // 行注：补充当前配置项
  SESSION_TYPE_SINGLE  // 行注：补充当前表达式
} from '../types/chat'  // 行注：补充当前表达式
import {  // 行注：引入 { 模块
  buildSessionKey,  // 行注：补充 buildSessionKey 配置项
  compareDisplayMessages,  // 行注：补充当前配置项
  createLocalMessageId,  // 行注：补充当前配置项
  escapeAttributeSelector,  // 行注：补充当前配置项
  getMessageAnchorKey,  // 行注：补充当前配置项
  getMessagePreview,  // 行注：补充当前配置项
  normalizeSession,  // 行注：补充当前配置项
  resolveMessageTargetId  // 行注：补充当前表达式
} from '../utils/chat'  // 行注：补充当前表达式

interface FeedbackApi {  // 行注：开始当前逻辑块
  success: (content: string) => void  // 行注：设置 success 配置项
  warning: (content: string) => void  // 行注：设置 warning 配置项
  error: (content: string) => void  // 行注：设置 error 配置项
}  // 行注：结束当前代码块

interface UseChatRuntimeOptions {  // 行注：开始当前逻辑块
  route: RouteLocationNormalizedLoaded  // 行注：设置 route 配置项
  router: Router  // 行注：设置 router 配置项
  message: FeedbackApi  // 行注：设置 message 配置项
  sessions: Ref<ChatSession[]>  // 行注：设置 sessions 配置项
  messages: Ref<DisplayMessage[]>  // 行注：设置 messages 配置项
  friends: Ref<FriendItem[]>  // 行注：设置 friends 配置项
  currentTargetId: Ref<string | null>  // 行注：设置 currentTargetId 配置项
  currentSessionType: Ref<number>  // 行注：设置 currentSessionType 配置项
  groupDetail: Ref<GroupDetail | null>  // 行注：设置 groupDetail 配置项
  showGroupDrawer: Ref<boolean>  // 行注：设置 showGroupDrawer 配置项
  noticeDraft: Ref<string>  // 行注：设置 noticeDraft 配置项
  isGroupSession: ComputedRef<boolean>  // 行注：设置 isGroupSession 配置项
  currentSessionName: ComputedRef<string>  // 行注：设置 currentSessionName 配置项
  currentNotificationMuted: ComputedRef<boolean>  // 行注：设置 currentNotificationMuted 配置项
  isGroupProfileChanged: ComputedRef<boolean>  // 行注：设置 isGroupProfileChanged 配置项
  isGroupNoticeChanged: ComputedRef<boolean>  // 行注：设置 isGroupNoticeChanged 配置项
  messagesRef: Ref<HTMLElement | undefined>  // 行注：设置 messagesRef 配置项
  loadingSessions?: Ref<boolean>  // 行注：补充当前表达式
  loadingMessages?: Ref<boolean>  // 行注：补充当前表达式
  showMenu?: Ref<boolean>  // 行注：补充当前表达式
  syncGroupProfileDraft: (detail?: GroupDetail | null) => void  // 行注：设置 syncGroupProfileDraft 配置项
  closeGroupDrawer: (options?: { force?: boolean }) => Promise<boolean> | boolean  // 行注：设置 closeGroupDrawer 配置项
  getToken: () => string  // 行注：传入 getToken 回调
  getCurrentUserId: () => string | number  // 行注：传入 getCurrentUserId 回调
  getCurrentUserNickname: () => string  // 行注：传入 getCurrentUserNickname 回调
  getCurrentUserAvatar: () => string  // 行注：传入 getCurrentUserAvatar 回调
  onResetConversationState?: () => void  // 行注：执行当前调用逻辑
}  // 行注：结束当前代码块

// 聊天页面的大部分运行时协作都从这里统一编排，减少页面组件本身的复杂度。
export function useChatRuntime(options: UseChatRuntimeOptions) {  // 行注：导出当前能力
  const initialized = ref(false)  // 行注：初始化 initialized 响应式状态
  const syncingSocketState = ref(false)  // 行注：初始化 syncingSocketState 响应式状态
  const activeJumpMessageKey = ref('')  // 行注：初始化 activeJumpMessageKey 响应式状态
  const flashSessionKey = ref<string | null>(null)  // 行注：初始化 flashSessionKey 变量
  const mentionBannerQueue = ref<string[]>([])  // 行注：初始化 mentionBannerQueue 变量
  const showNoticeReminder = ref(false)  // 行注：初始化 showNoticeReminder 响应式状态
  const acknowledgingNoticeReminder = ref(false)  // 行注：初始化 acknowledgingNoticeReminder 响应式状态
  const resolvedMessageFileUrls = ref<Record<string, string>>({})  // 行注：初始化 resolvedMessageFileUrls 方法

  let activeJumpHighlightTimer: ReturnType<typeof setTimeout> | null = null  // 行注：初始化 activeJumpHighlightTimer 变量
  let wasAtBottom = true  // 行注：初始化 wasAtBottom 变量
  let allowInitialHomeSessionAutoSelect = true  // 行注：初始化 allowInitialHomeSessionAutoSelect 变量

  function sortSessions() {  // 行注：定义 sortSessions 方法
    options.sessions.value = [...options.sessions.value].sort((left, right) => {  // 行注：调用 sort 方法
      const leftTime = getDateTimeTimestamp(left.lastMessageTime)  // 行注：初始化 leftTime 变量
      const rightTime = getDateTimeTimestamp(right.lastMessageTime)  // 行注：初始化 rightTime 变量
      if (leftTime !== rightTime) {  // 行注：判断当前条件是否成立
        return rightTime - leftTime  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      return String(right.id || '').localeCompare(String(left.id || ''))  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function isFriendTarget(targetId: string | number) {  // 行注：定义 isFriendTarget 方法
    return options.friends.value.some(friend => String(friend.friendId) === String(targetId))  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function shouldPreserveDraftSession(session: ChatSession) {  // 行注：定义 shouldPreserveDraftSession 方法
    if (Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
      return true  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return isFriendTarget(session.targetId)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function isCurrentSession(session: ChatSession) {  // 行注：定义 isCurrentSession 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function upsertSession(nextSession: ChatSession) {  // 行注：定义 upsertSession 方法
    const normalizedSession = normalizeSession(nextSession)  // 行注：初始化 normalizedSession 变量
    const sessionKey = buildSessionKey(normalizedSession.targetId, normalizedSession.sessionType)  // 行注：初始化 sessionKey 变量
    const sessionIndex = options.sessions.value.findIndex(session => buildSessionKey(session.targetId, session.sessionType) === sessionKey)  // 行注：初始化 sessionIndex 变量
    if (sessionIndex === -1) {  // 行注：判断当前条件是否成立
      options.sessions.value = [normalizedSession, ...options.sessions.value]  // 行注：更新 options.sessions 状态
      sortSessions()  // 行注：调用 sortSessions 方法
      return normalizedSession  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const mergedSession = {  // 行注：开始解构当前返回值
      ...options.sessions.value[sessionIndex],  // 行注：补充当前配置项
      ...normalizedSession  // 行注：补充当前表达式
    }  // 行注：结束当前代码块
    options.sessions.value.splice(sessionIndex, 1, mergedSession)  // 行注：调用 splice 方法
    sortSessions()  // 行注：调用 sortSessions 方法
    return mergedSession  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function updateCurrentSessionFromStore(nextSession: ChatSession) {  // 行注：定义 updateCurrentSessionFromStore 方法
    if (!options.currentTargetId.value || !isCurrentSession(nextSession)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.isGroupSession.value && options.groupDetail.value) {  // 行注：判断当前条件是否成立
      options.groupDetail.value = {  // 行注：更新 options.groupDetail 状态
        ...options.groupDetail.value,  // 行注：补充当前配置项
        groupAvatar: nextSession.targetAvatar || options.groupDetail.value.groupAvatar,  // 行注：设置 groupAvatar 配置项
        groupRemark: nextSession.groupRemark ?? options.groupDetail.value.groupRemark,  // 行注：设置 groupRemark 配置项
        notice: nextSession.notice ?? options.groupDetail.value.notice,  // 行注：设置 notice 配置项
        noticeUnread: nextSession.noticeUnread ?? options.groupDetail.value.noticeUnread,  // 行注：设置 noticeUnread 配置项
        memberCount: nextSession.memberCount ?? options.groupDetail.value.memberCount,  // 行注：设置 memberCount 配置项
        myRole: nextSession.myRole ?? options.groupDetail.value.myRole,  // 行注：设置 myRole 配置项
        muted: nextSession.muted ?? options.groupDetail.value.muted,  // 行注：设置 muted 配置项
        muteTime: nextSession.muteTime ?? options.groupDetail.value.muteTime,  // 行注：设置 muteTime 配置项
        notificationMuted: nextSession.notificationMuted ?? options.groupDetail.value.notificationMuted  // 行注：设置 notificationMuted 配置项
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function flashSession(targetId: string | number, sessionType: number) {  // 行注：定义 flashSession 方法
    const sessionKey = buildSessionKey(targetId, sessionType)  // 行注：初始化 sessionKey 变量
    flashSessionKey.value = sessionKey  // 行注：更新 flashSessionKey 状态
    setTimeout(() => {  // 行注：调用 setTimeout 方法
      if (flashSessionKey.value === sessionKey) {  // 行注：判断当前条件是否成立
        flashSessionKey.value = null  // 行注：更新 flashSessionKey 状态
      }  // 行注：结束当前代码块
    }, 2000)  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  function getRouteMessageId() {  // 行注：定义 getRouteMessageId 方法
    const rawMessageId = options.route.query.messageId  // 行注：初始化 rawMessageId 状态
    if (Array.isArray(rawMessageId)) {  // 行注：判断当前条件是否成立
      return rawMessageId[0] ? String(rawMessageId[0]) : ''  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return rawMessageId ? String(rawMessageId) : ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function clearActiveJumpHighlight() {  // 行注：定义 clearActiveJumpHighlight 方法
    if (activeJumpHighlightTimer) {  // 行注：判断当前条件是否成立
      clearTimeout(activeJumpHighlightTimer)  // 行注：调用 clearTimeout 方法
      activeJumpHighlightTimer = null  // 行注：更新 activeJumpHighlightTimer 值
    }  // 行注：结束当前代码块
    activeJumpMessageKey.value = ''  // 行注：更新 activeJumpMessageKey 状态
  }  // 行注：结束当前代码块

  function setActiveJumpHighlight(messageItem: DisplayMessage | null | undefined) {  // 行注：定义 setActiveJumpHighlight 方法
    if (!messageItem) {  // 行注：判断当前条件是否成立
      clearActiveJumpHighlight()  // 行注：调用 clearActiveJumpHighlight 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    clearActiveJumpHighlight()  // 行注：调用 clearActiveJumpHighlight 方法
    activeJumpMessageKey.value = getMessageAnchorKey(messageItem)  // 行注：更新 activeJumpMessageKey 状态
    activeJumpHighlightTimer = setTimeout(() => {  // 行注：执行当前调用逻辑
      activeJumpMessageKey.value = ''  // 行注：更新 activeJumpMessageKey 状态
      activeJumpHighlightTimer = null  // 行注：更新 activeJumpHighlightTimer 值
    }, 2600)  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  function findMessageByRouteMessageId(messageId: string) {  // 行注：定义 findMessageByRouteMessageId 方法
    if (!messageId) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return options.messages.value.find(messageItem => String(messageItem.id) === messageId || getMessageAnchorKey(messageItem) === messageId) || null  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getMessageTextSegments(content: string) {  // 行注：定义 getMessageTextSegments 方法
    const segments: Array<{ text: string; mention: boolean }> = []  // 行注：初始化 segments 变量
    const pattern = /@[^\s,，。！？；：、]+/g  // 行注：初始化 pattern 变量
    let lastIndex = 0  // 行注：初始化 lastIndex 变量
    for (const match of content.matchAll(pattern)) {  // 行注：遍历当前数据集合
      const index = match.index ?? 0  // 行注：初始化 index 变量
      if (index > lastIndex) {  // 行注：判断当前条件是否成立
        segments.push({  // 行注：开始当前逻辑块
          text: content.slice(lastIndex, index),  // 行注：设置 text 配置项
          mention: false  // 行注：设置 mention 配置项
        })  // 行注：结束当前调用配置
      }  // 行注：结束当前代码块
      segments.push({  // 行注：开始当前逻辑块
        text: match[0],  // 行注：设置 text 配置项
        mention: true  // 行注：设置 mention 配置项
      })  // 行注：结束当前调用配置
      lastIndex = index + match[0].length  // 行注：更新 lastIndex 值
    }  // 行注：结束当前代码块
    if (lastIndex < content.length) {  // 行注：判断当前条件是否成立
      segments.push({  // 行注：开始当前逻辑块
        text: content.slice(lastIndex),  // 行注：设置 text 配置项
        mention: false  // 行注：设置 mention 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    if (segments.length === 0) {  // 行注：判断当前条件是否成立
      segments.push({  // 行注：开始当前逻辑块
        text: content,  // 行注：设置 text 配置项
        mention: false  // 行注：设置 mention 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    return segments  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function clearMentionBannerQueue() {  // 行注：定义 clearMentionBannerQueue 方法
    mentionBannerQueue.value = []  // 行注：更新 mentionBannerQueue 状态
  }  // 行注：结束当前代码块

  function setMentionBannerQueue(messageItems: DisplayMessage[]) {  // 行注：定义 setMentionBannerQueue 方法
    mentionBannerQueue.value = messageItems  // 行注：更新 mentionBannerQueue 状态
      .filter(messageItem => messageItem.mentionedMe && !messageItem.isMe && !messageItem.isSystem)  // 行注：调用 filter 方法
      .map(messageItem => getMessageAnchorKey(messageItem))  // 行注：调用 map 方法
  }  // 行注：结束当前代码块

  function appendMentionBanner(messageItem: DisplayMessage | null | undefined) {  // 行注：定义 appendMentionBanner 方法
    if (!messageItem?.mentionedMe || messageItem.isMe || messageItem.isSystem) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextKey = getMessageAnchorKey(messageItem)  // 行注：初始化 nextKey 变量
    mentionBannerQueue.value = [nextKey, ...mentionBannerQueue.value.filter(key => key !== nextKey)]  // 行注：调用 filter 方法
  }  // 行注：结束当前代码块

  function consumeMentionBanner(messageItem?: DisplayMessage | null) {  // 行注：定义 consumeMentionBanner 方法
    if (!messageItem) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const key = getMessageAnchorKey(messageItem)  // 行注：初始化 key 变量
    mentionBannerQueue.value = mentionBannerQueue.value.filter(item => item !== key)  // 行注：调用 filter 方法
  }  // 行注：结束当前代码块

  const resolvedMentionBannerKeys = computed(() => {  // 行注：开始解构当前返回值
    const existingKeys = new Set(options.messages.value.map(messageItem => getMessageAnchorKey(messageItem)))  // 行注：初始化 existingKeys 变量
    const seen = new Set<string>()  // 行注：初始化 seen 变量
    return mentionBannerQueue.value.filter(key => {  // 行注：返回当前结果
      if (!existingKeys.has(key) || seen.has(key)) {  // 行注：判断当前条件是否成立
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      seen.add(key)  // 行注：调用 add 方法
      return true  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  })  // 行注：结束当前调用配置

  const activeMentionBannerMessage = computed(() => {  // 行注：开始解构当前返回值
    const activeKey = resolvedMentionBannerKeys.value[0]  // 行注：初始化 activeKey 变量
    if (!activeKey) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return options.messages.value.find(messageItem => getMessageAnchorKey(messageItem) === activeKey) || null  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const showMentionBanner = computed(() => {  // 行注：开始解构当前返回值
    return options.isGroupSession.value && Boolean(activeMentionBannerMessage.value)  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const mentionBannerText = computed(() => {  // 行注：开始解构当前返回值
    const messageItem = activeMentionBannerMessage.value  // 行注：初始化 messageItem 变量
    if (!messageItem) {  // 行注：判断当前条件是否成立
      return ''  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const senderName = messageItem.name || '有成员'  // 行注：初始化 senderName 变量
    const preview = getMessagePreview(messageItem)  // 行注：初始化 preview 变量
    return `${senderName}提醒你留意这条消息：${preview}`  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const mentionBannerActionText = computed(() => {  // 行注：开始解构当前返回值
    const remainingCount = Math.max(0, resolvedMentionBannerKeys.value.length - 1)  // 行注：初始化 remainingCount 变量
    return remainingCount > 0 ? `定位后还有 ${remainingCount} 条` : '点击定位'  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  function dismissMentionBanner() {  // 行注：定义 dismissMentionBanner 方法
    consumeMentionBanner(activeMentionBannerMessage.value)  // 行注：调用 consumeMentionBanner 方法
  }  // 行注：结束当前代码块

  function scrollToMessage(messageItem: DisplayMessage | null | undefined, behavior: ScrollBehavior = 'smooth') {  // 行注：定义 scrollToMessage 方法
    if (!options.messagesRef.value || !messageItem) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const selector = `[data-message-key="${escapeAttributeSelector(getMessageAnchorKey(messageItem))}"]`  // 行注：初始化 selector 变量
    const target = options.messagesRef.value.querySelector<HTMLElement>(selector)  // 行注：初始化 target 变量
    if (!target) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    target.scrollIntoView({  // 行注：开始当前逻辑块
      behavior,  // 行注：传入 behavior 参数
      block: 'center'  // 行注：设置 block 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  async function clearRouteMessageQuery() {  // 行注：定义异步 clearRouteMessageQuery 方法
    if (!getRouteMessageId()) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextQuery = { ...options.route.query }  // 行注：初始化 nextQuery 变量
    delete nextQuery.messageId  // 行注：补充当前表达式
    await options.router.replace({  // 行注：开始当前逻辑块
      path: options.route.path,  // 行注：设置 path 配置项
      query: nextQuery  // 行注：设置 query 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function scrollToActiveMentionMessage(behavior: ScrollBehavior = 'smooth') {  // 行注：定义 scrollToActiveMentionMessage 方法
    if (!activeMentionBannerMessage.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    scrollToMessage(activeMentionBannerMessage.value, behavior)  // 行注：调用 scrollToMessage 方法
  }  // 行注：结束当前代码块

  function handleMentionBannerClick() {  // 行注：定义 handleMentionBannerClick 方法
    const messageItem = activeMentionBannerMessage.value  // 行注：初始化 messageItem 变量
    if (!messageItem) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    scrollToMessage(messageItem)  // 行注：调用 scrollToMessage 方法
    consumeMentionBanner(messageItem)  // 行注：调用 consumeMentionBanner 方法
  }  // 行注：结束当前代码块

  function resolveUnreadMentionMessages(messageList: DisplayMessage[], unreadCount: number) {  // 行注：定义 resolveUnreadMentionMessages 方法
    if (!Number.isFinite(unreadCount) || unreadCount <= 0) {  // 行注：判断当前条件是否成立
      return []  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const incomingMessages = messageList.filter(messageItem => !messageItem.isMe && !messageItem.isSystem)  // 行注：初始化 incomingMessages 变量
    if (incomingMessages.length === 0) {  // 行注：判断当前条件是否成立
      return []  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const unreadMessages = incomingMessages.slice(-Math.max(0, Math.trunc(unreadCount)))  // 行注：初始化 unreadMessages 变量
    return unreadMessages.filter(messageItem => messageItem.mentionedMe).reverse()  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getNotificationTitle(messageItem: DisplayMessage) {  // 行注：定义 getNotificationTitle 方法
    if (messageItem.isSystem) {  // 行注：判断当前条件是否成立
      return '群系统通知'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const sessionName = options.sessions.value.find(session =>  // 行注：初始化 sessionName 变量
      buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(messageItem.targetId, messageItem.sessionType)  // 行注：调用 buildSessionKey 方法
    )?.targetNickname  // 行注：补充当前表达式
    if (messageItem.mentionedMe) {  // 行注：判断当前条件是否成立
      return `${sessionName || options.currentSessionName.value || '群聊'} · 特别提醒`  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return sessionName || messageItem.name || '新消息'  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function shouldShowDesktopNotification(messageItem: DisplayMessage) {  // 行注：定义 shouldShowDesktopNotification 方法
    if (messageItem.isMe) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (messageItem.sessionType !== SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
      return true  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const targetId = String(messageItem.targetId)  // 行注：初始化 targetId 状态
    if (options.currentTargetId.value && options.currentSessionType.value === SESSION_TYPE_GROUP && String(options.currentTargetId.value) === targetId) {  // 行注：判断当前条件是否成立
      return !options.currentNotificationMuted.value  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const session = options.sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(targetId, SESSION_TYPE_GROUP))  // 行注：初始化 session 变量
    return !Boolean(session?.notificationMuted)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function getNotificationSoundType(messageItem: DisplayMessage) {  // 行注：定义 getNotificationSoundType 方法
    if (messageItem.isSystem || messageItem.mentionedMe) {  // 行注：判断当前条件是否成立
      return 'attention' as const  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return 'message' as const  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function notifyIncomingMessage(messageItem: DisplayMessage) {  // 行注：定义 notifyIncomingMessage 方法
    if (!shouldShowDesktopNotification(messageItem)) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const targetId = String(messageItem.targetId || '')  // 行注：初始化 targetId 状态
    const sessionType = Number(messageItem.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 sessionType 变量
    void showNotification(getNotificationTitle(messageItem), getMessagePreview(messageItem), undefined, {  // 行注：调用 showNotification 方法
      targetId,  // 行注：补充 targetId 配置项
      sessionType,  // 行注：补充当前配置项
      messageId: String(messageItem.id || ''),  // 行注：设置 messageId 配置项
      attention: Boolean(messageItem.isSystem || messageItem.mentionedMe)  // 行注：设置 attention 配置项
    })  // 行注：结束当前调用配置
    void playNotificationSound(getNotificationSoundType(messageItem))  // 行注：调用 playNotificationSound 方法
  }  // 行注：结束当前代码块

  function releaseMessageResource(messageItem?: DisplayMessage | null) {  // 行注：定义 releaseMessageResource 方法
    if (!messageItem?.content?.startsWith('blob:')) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    URL.revokeObjectURL(messageItem.content)  // 行注：调用 revokeObjectURL 方法
  }  // 行注：结束当前代码块

  function cleanupMessageResources(messageList = options.messages.value) {  // 行注：定义 cleanupMessageResources 方法
    for (const messageItem of messageList) {  // 行注：遍历当前数据集合
      releaseMessageResource(messageItem)  // 行注：调用 releaseMessageResource 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function shouldResolveRemoteMessageFile(messageItem?: DisplayMessage | null) {  // 行注：定义 shouldResolveRemoteMessageFile 方法
    if (!messageItem) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (messageItem.msgType !== MESSAGE_TYPE_IMAGE && messageItem.msgType !== MESSAGE_TYPE_FILE) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return Boolean(messageItem.content) && !messageItem.content.startsWith('blob:')  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function ensureMessageFileAccessUrl(rawUrl: string) {  // 行注：定义异步 ensureMessageFileAccessUrl 方法
    if (!rawUrl || rawUrl.startsWith('blob:')) {  // 行注：判断当前条件是否成立
      return rawUrl  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)  // 行注：初始化 cachedAccessUrl 变量
    if (cachedAccessUrl) {  // 行注：判断当前条件是否成立
      if (resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {  // 行注：判断当前条件是否成立
        resolvedMessageFileUrls.value = {  // 行注：更新 resolvedMessageFileUrls 状态
          ...resolvedMessageFileUrls.value,  // 行注：补充当前配置项
          [rawUrl]: cachedAccessUrl  // 行注：补充当前表达式
        }  // 行注：结束当前代码块
      }  // 行注：结束当前代码块
      return cachedAccessUrl  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const accessUrl = await resolveFileAccessUrl(rawUrl)  // 行注：接收 accessUrl 异步结果
    if (accessUrl) {  // 行注：判断当前条件是否成立
      resolvedMessageFileUrls.value = {  // 行注：更新 resolvedMessageFileUrls 状态
        ...resolvedMessageFileUrls.value,  // 行注：补充当前配置项
        [rawUrl]: accessUrl  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    return accessUrl  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function preloadMessageFileUrls(messageList: DisplayMessage[]) {  // 行注：定义异步 preloadMessageFileUrls 方法
    const targets = Array.from(new Set(  // 行注：初始化 targets 变量
      messageList  // 行注：补充当前表达式
        .filter(shouldResolveRemoteMessageFile)  // 行注：调用 filter 方法
        .map(messageItem => messageItem.content)  // 行注：调用 map 方法
        .filter(Boolean)  // 行注：调用 filter 方法
    ))  // 行注：补充当前表达式
    await Promise.all(targets.map(rawUrl => ensureMessageFileAccessUrl(rawUrl).catch(() => '')))  // 行注：并行执行多项异步任务
  }  // 行注：结束当前代码块

  function getResolvedMessageFileUrl(messageItem: DisplayMessage) {  // 行注：定义 getResolvedMessageFileUrl 方法
    if (!shouldResolveRemoteMessageFile(messageItem)) {  // 行注：判断当前条件是否成立
      return messageItem.content  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const rawUrl = messageItem.content  // 行注：初始化 rawUrl 变量
    const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)  // 行注：初始化 cachedAccessUrl 变量
    if (cachedAccessUrl && resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {  // 行注：判断当前条件是否成立
      resolvedMessageFileUrls.value = {  // 行注：更新 resolvedMessageFileUrls 状态
        ...resolvedMessageFileUrls.value,  // 行注：补充当前配置项
        [rawUrl]: cachedAccessUrl  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    return cachedAccessUrl || ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function resetCurrentConversationState() {  // 行注：定义 resetCurrentConversationState 方法
    cleanupMessageResources()  // 行注：调用 cleanupMessageResources 方法
    resolvedMessageFileUrls.value = {}  // 行注：更新 resolvedMessageFileUrls 状态
    options.onResetConversationState?.()  // 行注：执行当前调用逻辑
  }  // 行注：结束当前代码块

  function toDisplayMessage(item: any): DisplayMessage {  // 行注：定义 toDisplayMessage 方法
    const readTime = item.readTime || ''  // 行注：初始化 readTime 变量
    const isRecalled = Number(item.status ?? 0) === MESSAGE_STATUS_RECALLED  // 行注：初始化 isRecalled 状态
    const msgType = Number(item.msgType ?? MESSAGE_TYPE_TEXT)  // 行注：初始化 msgType 变量
    const sessionType = Number(item.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 sessionType 变量
    const isSystem = msgType === MESSAGE_TYPE_SYSTEM  // 行注：初始化 isSystem 状态
    const isMe = !isSystem && String(item.fromUserId) === String(options.getCurrentUserId())  // 行注：初始化 isMe 状态
    const mentionUserIds = Array.isArray(item.mentionUserIds) ? item.mentionUserIds.map((id: string | number) => String(id)) : []  // 行注：初始化 mentionUserIds 变量
    const mentionAll = Boolean(item.mentionAll)  // 行注：初始化 mentionAll 变量
    const mentionDisplayNames = Array.isArray(item.mentionDisplayNames)  // 行注：初始化 mentionDisplayNames 变量
      ? item.mentionDisplayNames.filter((value: unknown): value is string => typeof value === 'string' && value.trim().length > 0)  // 行注：调用 filter 方法
      : []  // 行注：补充当前表达式
    return {  // 行注：返回当前结果
      id: item.id,  // 行注：设置 id 配置项
      localId: item.clientMessageId || String(item.id || createLocalMessageId('server')),  // 行注：设置 localId 配置项
      clientMessageId: item.clientMessageId || '',  // 行注：设置 clientMessageId 配置项
      isMe,  // 行注：补充当前配置项
      isSystem,  // 行注：补充当前配置项
      name: item.fromNickname,  // 行注：设置 name 配置项
      fromAvatar: item.fromAvatar || '',  // 行注：设置 fromAvatar 配置项
      content: item.content,  // 行注：设置 content 配置项
      msgType,  // 行注：补充当前配置项
      status: Number(item.status ?? 0),  // 行注：设置 status 配置项
      readTime,  // 行注：补充当前配置项
      createTime: item.createTime,  // 行注：设置 createTime 配置项
      time: item.createTime?.substring(11, 16) || '',  // 行注：设置 time 配置项
      readStatus: isRecalled ? '已撤回' : (readTime ? '已读' : '未读'),  // 行注：设置 readStatus 配置项
      deliveryStatus: 'sent',  // 行注：设置 deliveryStatus 配置项
      fileName: item.fileName || '',  // 行注：设置 fileName 配置项
      fileSize: item.fileSize ? Number(item.fileSize) : undefined,  // 行注：设置 fileSize 配置项
      sessionType,  // 行注：补充当前配置项
      targetId: resolveMessageTargetId(item, isMe, sessionType),  // 行注：设置 targetId 配置项
      mentionAll,  // 行注：补充当前配置项
      mentionUserIds,  // 行注：补充当前配置项
      mentionDisplayNames,  // 行注：补充当前配置项
      mentionedMe: sessionType === SESSION_TYPE_GROUP && !isSystem && !isMe && (mentionAll || mentionUserIds.includes(String(options.getCurrentUserId()))),  // 行注：设置 mentionedMe 配置项
      uploadedFileId: undefined  // 行注：设置 uploadedFileId 配置项
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function upsertMessage(nextMessage: DisplayMessage) {  // 行注：定义 upsertMessage 方法
    const messageIndex = options.messages.value.findIndex(item => {  // 行注：开始解构当前返回值
      if (String(item.id) === String(nextMessage.id)) {  // 行注：判断当前条件是否成立
        return true  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (item.clientMessageId && nextMessage.clientMessageId && item.clientMessageId === nextMessage.clientMessageId) {  // 行注：判断当前条件是否成立
        return true  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      return item.localId === nextMessage.localId  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
    if (messageIndex === -1) {  // 行注：判断当前条件是否成立
      options.messages.value = [...options.messages.value, nextMessage]  // 行注：更新 options.messages 状态
    } else {  // 行注：开始当前逻辑块
      const previousMessage = options.messages.value[messageIndex]  // 行注：初始化 previousMessage 实例
      if (previousMessage.content !== nextMessage.content) {  // 行注：判断当前条件是否成立
        releaseMessageResource(previousMessage)  // 行注：调用 releaseMessageResource 方法
      }  // 行注：结束当前代码块
      options.messages.value.splice(messageIndex, 1, {  // 行注：开始当前逻辑块
        ...options.messages.value[messageIndex],  // 行注：补充当前配置项
        ...nextMessage  // 行注：补充当前表达式
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    options.messages.value.sort(compareDisplayMessages)  // 行注：调用 sort 方法
    if (shouldResolveRemoteMessageFile(nextMessage)) {  // 行注：判断当前条件是否成立
      void ensureMessageFileAccessUrl(nextMessage.content)  // 行注：调用 ensureMessageFileAccessUrl 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {  // 行注：定义 applyGroupDetail 方法
    options.groupDetail.value = detail  // 行注：更新 options.groupDetail 状态
    if (syncDraft || !options.isGroupNoticeChanged.value) {  // 行注：判断当前条件是否成立
      options.noticeDraft.value = detail?.notice || ''  // 行注：更新 options.noticeDraft 状态
    }  // 行注：结束当前代码块
    if (syncDraft || !options.isGroupProfileChanged.value) {  // 行注：判断当前条件是否成立
      options.syncGroupProfileDraft(detail)  // 行注：调用 syncGroupProfileDraft 方法
    }  // 行注：结束当前代码块
    if (!detail?.noticeUnread || !detail.notice?.trim()) {  // 行注：判断当前条件是否成立
      showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
    } else {  // 行注：开始当前逻辑块
      showNoticeReminder.value = true  // 行注：更新 showNoticeReminder 状态
    }  // 行注：结束当前代码块

    if (!detail) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const sessionKey = buildSessionKey(detail.id, SESSION_TYPE_GROUP)  // 行注：初始化 sessionKey 变量
    const session = options.sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === sessionKey)  // 行注：初始化 session 变量
    if (session) {  // 行注：判断当前条件是否成立
      session.targetNickname = detail.groupRemark || detail.groupName  // 行注：更新 session.targetNickname 值
      session.targetAvatar = detail.groupAvatar || ''  // 行注：更新 session.targetAvatar 值
      session.groupRemark = detail.groupRemark || ''  // 行注：更新 session.groupRemark 值
      session.memberCount = detail.memberCount  // 行注：更新 session.memberCount 值
      session.notice = detail.notice || ''  // 行注：更新 session.notice 值
      session.myRole = detail.myRole  // 行注：更新 session.myRole 值
      session.muted = detail.muted  // 行注：更新 session.muted 值
      session.muteTime = detail.muteTime  // 行注：更新 session.muteTime 值
      session.notificationMuted = Boolean(detail.notificationMuted)  // 行注：更新 session.notificationMuted 值
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function removeSessionByTarget(targetId: string | number, sessionType: number) {  // 行注：定义 removeSessionByTarget 方法
    const sessionKey = buildSessionKey(targetId, sessionType)  // 行注：初始化 sessionKey 变量
    options.sessions.value = options.sessions.value.filter(session => buildSessionKey(session.targetId, session.sessionType) !== sessionKey)  // 行注：调用 filter 方法
  }  // 行注：结束当前代码块

  function isUnavailableConversationError(error: any) {  // 行注：定义 isUnavailableConversationError 方法
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)  // 行注：初始化 code 变量
    return code === 403 || code === 404  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function closeUnavailableSingleSession(targetId: string | number, config: { notify?: boolean } = {}) {  // 行注：定义异步 closeUnavailableSingleSession 方法
    const normalizedTargetId = String(targetId)  // 行注：初始化 normalizedTargetId 状态
    removeSessionByTarget(normalizedTargetId, SESSION_TYPE_SINGLE)  // 行注：调用 removeSessionByTarget 方法

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''  // 行注：初始化 routeTargetId 状态
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 routeSessionType 变量
    const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_SINGLE  // 行注：初始化 routeMatches 变量
    const currentMatches = Boolean(options.currentTargetId.value)  // 行注：初始化 currentMatches 变量
      && options.currentSessionType.value === SESSION_TYPE_SINGLE  // 行注：补充当前表达式
      && String(options.currentTargetId.value) === normalizedTargetId  // 行注：执行当前调用逻辑

    if (currentMatches) {  // 行注：判断当前条件是否成立
      resetCurrentConversationState()  // 行注：调用 resetCurrentConversationState 方法
    }  // 行注：结束当前代码块

    if (routeMatches) {  // 行注：判断当前条件是否成立
      await options.router.replace('/chat')  // 行注：替换当前路由
    }  // 行注：结束当前代码块

    if ((routeMatches || currentMatches) && config.notify) {  // 行注：判断当前条件是否成立
      options.message.warning('当前单聊已不可用')  // 行注：提示警告信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function closeUnavailableGroupSession(  // 行注：定义异步 closeUnavailableGroupSession 方法
    targetId: string | number,  // 行注：设置 targetId 配置项
    config: { notify?: boolean; messageText?: string } = {}  // 行注：设置 config 配置项
  ) {  // 行注：开始当前逻辑块
    const normalizedTargetId = String(targetId)  // 行注：初始化 normalizedTargetId 状态
    removeSessionByTarget(normalizedTargetId, SESSION_TYPE_GROUP)  // 行注：调用 removeSessionByTarget 方法

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''  // 行注：初始化 routeTargetId 状态
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 routeSessionType 变量
    const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_GROUP  // 行注：初始化 routeMatches 变量
    const currentMatches = Boolean(options.currentTargetId.value)  // 行注：初始化 currentMatches 变量
      && options.currentSessionType.value === SESSION_TYPE_GROUP  // 行注：补充当前表达式
      && String(options.currentTargetId.value) === normalizedTargetId  // 行注：执行当前调用逻辑

    if (currentMatches) {  // 行注：判断当前条件是否成立
      await options.closeGroupDrawer({ force: true })  // 行注：调用 closeGroupDrawer 方法
      resetCurrentConversationState()  // 行注：调用 resetCurrentConversationState 方法
    }  // 行注：结束当前代码块

    if (routeMatches) {  // 行注：判断当前条件是否成立
      await options.router.replace('/chat')  // 行注：替换当前路由
    }  // 行注：结束当前代码块

    if ((routeMatches || currentMatches) && config.notify) {  // 行注：判断当前条件是否成立
      options.message.warning(config.messageText || '当前群聊已不可用')  // 行注：提示警告信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function loadFriends() {  // 行注：定义异步 loadFriends 方法
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await friendApi.getList()  // 行注：接收 response 异步结果
      options.friends.value = response.data || []  // 行注：更新 options.friends 状态
    } catch (error) {  // 行注：捕获并处理异常
      console.error('loadFriends error:', error)  // 行注：输出错误日志
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function markCurrentSessionRead(targetId = options.currentTargetId.value, sessionType = options.currentSessionType.value) {  // 行注：定义异步 markCurrentSessionRead 方法
    if (!targetId) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await sendChatCommand('MARK_READ', { targetId, sessionType }).catch(() => undefined)  // 行注：追加异常兜底处理
  }  // 行注：结束当前代码块

  async function loadMessages(  // 行注：定义异步 loadMessages 方法
    targetId: string,  // 行注：设置 targetId 配置项
    sessionType: number,  // 行注：设置 sessionType 配置项
    config: {  // 行注：设置 config 配置项
      unreadCountSnapshot?: number  // 行注：补充当前表达式
      preferMentionScroll?: boolean  // 行注：补充当前表达式
      targetMessageId?: string  // 行注：补充当前表达式
      silent?: boolean  // 行注：补充当前表达式
    } = {}  // 行注：补充当前表达式
  ) {  // 行注：开始当前逻辑块
    let skipPostLoadHandling = false  // 行注：初始化 skipPostLoadHandling 变量
    if (!config.silent && options.loadingMessages) {  // 行注：判断当前条件是否成立
      options.loadingMessages.value = true  // 行注：更新 options.loadingMessages 状态
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      const targetMessageId = config.targetMessageId ? String(config.targetMessageId) : ''  // 行注：初始化 targetMessageId 状态
      let nextMessages: DisplayMessage[] = []  // 行注：初始化 nextMessages 变量
      if (targetMessageId) {  // 行注：判断当前条件是否成立
        const aggregatedMessages = new Map<string, DisplayMessage>()  // 行注：初始化 aggregatedMessages 变量
        const pageSize = 100  // 行注：初始化 pageSize 变量
        const maxPages = 20  // 行注：初始化 maxPages 变量
        let locatedTargetMessage = false  // 行注：初始化 locatedTargetMessage 实例
        for (let page = 1; page <= maxPages; page += 1) {  // 行注：遍历当前数据集合
          const response: any = await sendChatCommand('GET_HISTORY', {  // 行注：开始解构当前返回值
            targetId,  // 行注：补充 targetId 配置项
            sessionType,  // 行注：补充当前配置项
            page,  // 行注：补充当前配置项
            size: pageSize  // 行注：设置 size 配置项
          })  // 行注：结束当前调用配置
          const pageRawMessages = response.data || []  // 行注：初始化 pageRawMessages 变量
          for (const item of pageRawMessages) {  // 行注：遍历当前数据集合
            const messageItem = toDisplayMessage(item)  // 行注：初始化 messageItem 变量
            aggregatedMessages.set(getMessageAnchorKey(messageItem), messageItem)  // 行注：调用 set 方法
            if (String(messageItem.id) === targetMessageId || getMessageAnchorKey(messageItem) === targetMessageId) {  // 行注：判断当前条件是否成立
              locatedTargetMessage = true  // 行注：更新 locatedTargetMessage 值
            }  // 行注：结束当前代码块
          }  // 行注：结束当前代码块
          if (locatedTargetMessage || pageRawMessages.length < pageSize) {  // 行注：判断当前条件是否成立
            break  // 行注：补充当前表达式
          }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
        nextMessages = Array.from(aggregatedMessages.values()).sort(compareDisplayMessages)  // 行注：更新 nextMessages 值
      } else {  // 行注：开始当前逻辑块
        const response: any = await sendChatCommand('GET_HISTORY', {  // 行注：开始解构当前返回值
          targetId,  // 行注：补充 targetId 配置项
          sessionType  // 行注：补充当前表达式
        })  // 行注：结束当前调用配置
        const rawMessages = response.data || []  // 行注：初始化 rawMessages 变量
        nextMessages = rawMessages.map((item: any) => toDisplayMessage(item))  // 行注：调用 map 方法
      }  // 行注：结束当前代码块
      const mentionTargets = config.preferMentionScroll  // 行注：初始化 mentionTargets 变量
        ? resolveUnreadMentionMessages(nextMessages, Number(config.unreadCountSnapshot || 0))  // 行注：执行当前调用逻辑
        : []  // 行注：补充当前表达式

      cleanupMessageResources(options.messages.value)  // 行注：调用 cleanupMessageResources 方法
      options.messages.value = nextMessages  // 行注：更新 options.messages 状态
      resolvedMessageFileUrls.value = {}  // 行注：更新 resolvedMessageFileUrls 状态
      await preloadMessageFileUrls(nextMessages)  // 行注：调用 preloadMessageFileUrls 方法
      if (mentionTargets.length > 0) {  // 行注：判断当前条件是否成立
        setMentionBannerQueue(mentionTargets)  // 行注：调用 setMentionBannerQueue 方法
      } else {  // 行注：开始当前逻辑块
        clearMentionBannerQueue()  // 行注：调用 clearMentionBannerQueue 方法
      }  // 行注：结束当前代码块
      await markCurrentSessionRead(targetId, sessionType)  // 行注：调用 markCurrentSessionRead 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('loadMessages error:', error)  // 行注：输出错误日志
      if (sessionType === SESSION_TYPE_GROUP && isUnavailableConversationError(error)) {  // 行注：判断当前条件是否成立
        skipPostLoadHandling = true  // 行注：更新 skipPostLoadHandling 值
        await closeUnavailableGroupSession(targetId, { notify: true })  // 行注：调用 closeUnavailableGroupSession 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      cleanupMessageResources(options.messages.value)  // 行注：调用 cleanupMessageResources 方法
      options.messages.value = []  // 行注：更新 options.messages 状态
      clearMentionBannerQueue()  // 行注：调用 clearMentionBannerQueue 方法
    } finally {  // 行注：执行收尾清理逻辑
      if (!config.silent && options.loadingMessages) {  // 行注：判断当前条件是否成立
        options.loadingMessages.value = false  // 行注：更新 options.loadingMessages 状态
      }  // 行注：结束当前代码块
      if (skipPostLoadHandling) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      wasAtBottom = true  // 行注：更新 wasAtBottom 值
      await nextTick()  // 行注：调用 nextTick 方法
      const targetMessageId = config.targetMessageId ? String(config.targetMessageId) : ''  // 行注：初始化 targetMessageId 状态
      const targetMessage = targetMessageId ? findMessageByRouteMessageId(targetMessageId) : null  // 行注：初始化 targetMessage 实例
      if (targetMessage) {  // 行注：判断当前条件是否成立
        scrollToMessage(targetMessage, 'auto')  // 行注：调用 scrollToMessage 方法
        setActiveJumpHighlight(targetMessage)  // 行注：调用 setActiveJumpHighlight 方法
        await clearRouteMessageQuery()  // 行注：调用 clearRouteMessageQuery 方法
      } else if (targetMessageId) {  // 行注：执行当前调用逻辑
        options.message.warning('未找到目标聊天内容，可能已超出当前加载范围')  // 行注：提示警告信息
        await clearRouteMessageQuery()  // 行注：调用 clearRouteMessageQuery 方法
        if (showMentionBanner.value && activeMentionBannerMessage.value) {  // 行注：判断当前条件是否成立
          scrollToActiveMentionMessage('auto')  // 行注：调用 scrollToActiveMentionMessage 方法
        } else {  // 行注：开始当前逻辑块
          scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
        }  // 行注：结束当前代码块
      } else if (showMentionBanner.value && activeMentionBannerMessage.value) {  // 行注：执行当前调用逻辑
        scrollToActiveMentionMessage('auto')  // 行注：调用 scrollToActiveMentionMessage 方法
      } else {  // 行注：开始当前逻辑块
        scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function loadGroupDetail(groupId: string | number, syncDraft = true) {  // 行注：定义异步 loadGroupDetail 方法
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await groupApi.detail(groupId)  // 行注：接收 response 异步结果
      const detail = response.data || null  // 行注：初始化 detail 变量
      applyGroupDetail(detail, syncDraft)  // 行注：调用 applyGroupDetail 方法
      return true  // 行注：返回当前结果
    } catch (error) {  // 行注：捕获并处理异常
      console.error('loadGroupDetail error:', error)  // 行注：输出错误日志
      if (isUnavailableConversationError(error)) {  // 行注：判断当前条件是否成立
        await closeUnavailableGroupSession(groupId, { notify: true })  // 行注：调用 closeUnavailableGroupSession 方法
        return false  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error('获取群详情失败')  // 行注：提示错误信息
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function selectSession(  // 行注：定义异步 selectSession 方法
    session: ChatSession,  // 行注：设置 session 配置项
    syncRoute = true,  // 行注：更新 syncRoute 值
    config: {  // 行注：设置 config 配置项
      targetMessageId?: string  // 行注：补充当前表达式
    } = {}  // 行注：补充当前表达式
  ) {  // 行注：开始当前逻辑块
    allowInitialHomeSessionAutoSelect = false  // 行注：更新 allowInitialHomeSessionAutoSelect 值
    const nextSessionKey = buildSessionKey(session.targetId, Number(session.sessionType || SESSION_TYPE_SINGLE))  // 行注：初始化 nextSessionKey 变量
    const currentSessionKey = options.currentTargetId.value  // 行注：初始化 currentSessionKey 变量
      ? buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)  // 行注：执行当前调用逻辑
      : null  // 行注：补充当前表达式
    if (options.showGroupDrawer.value && currentSessionKey && currentSessionKey !== nextSessionKey) {  // 行注：判断当前条件是否成立
      const closed = await options.closeGroupDrawer()  // 行注：接收 closed 异步结果
      if (!closed) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    options.currentTargetId.value = String(session.targetId)  // 行注：更新 options.currentTargetId 状态
    options.currentSessionType.value = Number(session.sessionType || SESSION_TYPE_SINGLE)  // 行注：更新 options.currentSessionType 状态
    if (options.showMenu) {  // 行注：判断当前条件是否成立
      options.showMenu.value = false  // 行注：更新 options.showMenu 状态
    }  // 行注：结束当前代码块
    wasAtBottom = true  // 行注：更新 wasAtBottom 值
    clearMentionBannerQueue()  // 行注：调用 clearMentionBannerQueue 方法
    clearActiveJumpHighlight()  // 行注：调用 clearActiveJumpHighlight 方法

    if (options.currentSessionType.value === SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
      const detailLoaded = await loadGroupDetail(session.targetId)  // 行注：接收 detailLoaded 异步结果
      if (!detailLoaded) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    } else {  // 行注：开始当前逻辑块
      applyGroupDetail(null)  // 行注：调用 applyGroupDetail 方法
      options.showGroupDrawer.value = false  // 行注：更新 options.showGroupDrawer 状态
    }  // 行注：结束当前代码块

    await loadMessages(String(session.targetId), options.currentSessionType.value, {  // 行注：调用 loadMessages 方法
      unreadCountSnapshot: Number(session.unreadCount || 0),  // 行注：设置 unreadCountSnapshot 配置项
      preferMentionScroll: Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP,  // 行注：设置 preferMentionScroll 配置项
      targetMessageId: config.targetMessageId || ''  // 行注：设置 targetMessageId 配置项
    })  // 行注：结束当前调用配置

    if (syncRoute) {  // 行注：判断当前条件是否成立
      await options.router.replace({  // 行注：开始当前逻辑块
        path: `/chat/${session.targetId}`,  // 行注：设置 path 配置项
        query: options.currentSessionType.value === SESSION_TYPE_GROUP ? { sessionType: String(SESSION_TYPE_GROUP) } : {}  // 行注：设置 query 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function initializeRouteSession() {  // 行注：定义异步 initializeRouteSession 方法
    if (!initialized.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''  // 行注：初始化 routeTargetId 状态
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 routeSessionType 变量
    const routeMessageId = getRouteMessageId()  // 行注：初始化 routeMessageId 状态

    if (!routeTargetId) {  // 行注：判断当前条件是否成立
      if (allowInitialHomeSessionAutoSelect && !options.currentTargetId.value && options.sessions.value.length > 0) {  // 行注：判断当前条件是否成立
        await selectSession(options.sessions.value[0], false)  // 行注：调用 selectSession 方法
      }  // 行注：结束当前代码块
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const existingSession = options.sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(routeTargetId, routeSessionType))  // 行注：初始化 existingSession 变量
    if (existingSession) {  // 行注：判断当前条件是否成立
      if (  // 行注：判断当前条件是否成立
        routeMessageId  // 行注：补充当前表达式
        || !options.currentTargetId.value  // 行注：补充当前表达式
        || buildSessionKey(options.currentTargetId.value, options.currentSessionType.value) !== buildSessionKey(routeTargetId, routeSessionType)  // 行注：执行当前调用逻辑
      ) {  // 行注：开始当前逻辑块
        await selectSession(existingSession, false, { targetMessageId: routeMessageId })  // 行注：调用 selectSession 方法
      }  // 行注：结束当前代码块
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    if (routeSessionType === SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
      try {  // 行注：尝试执行可能失败的逻辑
        const response: any = await groupApi.detail(routeTargetId)  // 行注：接收 response 异步结果
        const detail = response.data  // 行注：初始化 detail 变量
        if (!detail) {  // 行注：判断当前条件是否成立
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        const draftSession: ChatSession = {  // 行注：开始解构当前返回值
          targetId: routeTargetId,  // 行注：设置 targetId 配置项
          sessionType: SESSION_TYPE_GROUP,  // 行注：设置 sessionType 配置项
          targetNickname: detail.groupName,  // 行注：设置 targetNickname 配置项
          targetAvatar: detail.groupAvatar || '',  // 行注：设置 targetAvatar 配置项
          lastMessage: '',  // 行注：设置 lastMessage 配置项
          lastMessageTime: detail.createTime || '',  // 行注：设置 lastMessageTime 配置项
          unreadCount: 0,  // 行注：设置 unreadCount 配置项
          memberCount: detail.memberCount,  // 行注：设置 memberCount 配置项
          myRole: detail.myRole,  // 行注：设置 myRole 配置项
          notice: detail.notice || '',  // 行注：设置 notice 配置项
          noticeUnread: Boolean(detail.noticeUnread),  // 行注：设置 noticeUnread 配置项
          muted: detail.muted,  // 行注：设置 muted 配置项
          muteTime: detail.muteTime,  // 行注：设置 muteTime 配置项
          isDraft: true  // 行注：设置 isDraft 配置项
        }  // 行注：结束当前代码块
        options.sessions.value = [draftSession, ...options.sessions.value]  // 行注：更新 options.sessions 状态
        await selectSession(draftSession, false, { targetMessageId: routeMessageId })  // 行注：调用 selectSession 方法
      } catch (error: any) {  // 行注：捕获并处理异常
        if (isUnavailableConversationError(error)) {  // 行注：判断当前条件是否成立
          await closeUnavailableGroupSession(routeTargetId, { notify: true })  // 行注：调用 closeUnavailableGroupSession 方法
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        options.message.error('打开群聊失败')  // 行注：提示错误信息
      }  // 行注：结束当前代码块
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    try {  // 行注：尝试执行可能失败的逻辑
      if (!isFriendTarget(routeTargetId)) {  // 行注：判断当前条件是否成立
        await closeUnavailableSingleSession(routeTargetId, { notify: true })  // 行注：调用 closeUnavailableSingleSession 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const response: any = await userApi.getUser(routeTargetId)  // 行注：接收 response 异步结果
      const user = response.data  // 行注：初始化 user 变量
      if (!user) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const draftSession: ChatSession = {  // 行注：开始解构当前返回值
        targetId: routeTargetId,  // 行注：设置 targetId 配置项
        sessionType: SESSION_TYPE_SINGLE,  // 行注：设置 sessionType 配置项
        targetNickname: user.nickname,  // 行注：设置 targetNickname 配置项
        targetUsername: user.username,  // 行注：设置 targetUsername 配置项
        targetAvatar: user.avatar || '',  // 行注：设置 targetAvatar 配置项
        lastMessage: '',  // 行注：设置 lastMessage 配置项
        lastMessageTime: '',  // 行注：设置 lastMessageTime 配置项
        unreadCount: 0,  // 行注：设置 unreadCount 配置项
        isDraft: true  // 行注：设置 isDraft 配置项
      }  // 行注：结束当前代码块
      options.sessions.value = [draftSession, ...options.sessions.value]  // 行注：更新 options.sessions 状态
      await selectSession(draftSession, false, { targetMessageId: routeMessageId })  // 行注：调用 selectSession 方法
    } catch (error: any) {  // 行注：捕获并处理异常
      if (isUnavailableConversationError(error)) {  // 行注：判断当前条件是否成立
        await closeUnavailableSingleSession(routeTargetId, { notify: true })  // 行注：调用 closeUnavailableSingleSession 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.message.error('打开会话失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function refreshCurrentSession() {  // 行注：定义异步 refreshCurrentSession 方法
    if (options.showMenu) {  // 行注：判断当前条件是否成立
      options.showMenu.value = false  // 行注：更新 options.showMenu 状态
    }  // 行注：结束当前代码块
    await loadFriends()  // 行注：调用 loadFriends 方法
    await loadSessions()  // 行注：调用 loadSessions 方法
    if (!options.currentTargetId.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (options.isGroupSession.value) {  // 行注：判断当前条件是否成立
      const detailLoaded = await loadGroupDetail(options.currentTargetId.value)  // 行注：接收 detailLoaded 异步结果
      if (!detailLoaded || !options.currentTargetId.value) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    await loadMessages(options.currentTargetId.value, options.currentSessionType.value)  // 行注：调用 loadMessages 方法
    options.message.success('已刷新')  // 行注：提示成功信息
  }  // 行注：结束当前代码块

  async function acknowledgeNoticeReminder() {  // 行注：定义异步 acknowledgeNoticeReminder 方法
    if (!options.currentTargetId.value || options.currentSessionType.value !== SESSION_TYPE_GROUP || !options.groupDetail.value) {  // 行注：判断当前条件是否成立
      showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    acknowledgingNoticeReminder.value = true  // 行注：更新 acknowledgingNoticeReminder 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await groupApi.markNoticeRead(options.currentTargetId.value)  // 行注：调用 markNoticeRead 方法
      const activeSession = options.sessions.value.find(session =>  // 行注：初始化 activeSession 变量
        buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(options.currentTargetId.value || '', SESSION_TYPE_GROUP)  // 行注：调用 buildSessionKey 方法
      )  // 行注：结束当前调用
      if (activeSession) {  // 行注：判断当前条件是否成立
        upsertSession({  // 行注：开始当前逻辑块
          ...activeSession,  // 行注：补充当前配置项
          noticeUnread: false  // 行注：设置 noticeUnread 配置项
        })  // 行注：结束当前调用配置
      }  // 行注：结束当前代码块
      applyGroupDetail({  // 行注：开始当前逻辑块
        ...options.groupDetail.value,  // 行注：补充当前配置项
        noticeUnread: false,  // 行注：设置 noticeUnread 配置项
        noticeReadTime: options.groupDetail.value.noticeUpdateTime || options.groupDetail.value.noticeReadTime  // 行注：设置 noticeReadTime 配置项
      }, false)  // 行注：补充当前表达式
      showNoticeReminder.value = false  // 行注：更新 showNoticeReminder 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      console.error('acknowledgeNoticeReminder error:', error)  // 行注：输出错误日志
      options.message.error(error.response?.data?.message || '确认群公告失败')  // 行注：提示错误信息
    } finally {  // 行注：执行收尾清理逻辑
      acknowledgingNoticeReminder.value = false  // 行注：更新 acknowledgingNoticeReminder 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function handleRealtimeMessage(rawMessage: any) {  // 行注：定义 handleRealtimeMessage 方法
    if (!rawMessage) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const messageItem = toDisplayMessage(rawMessage)  // 行注：初始化 messageItem 变量
    const messageSessionType = Number(rawMessage.sessionType || SESSION_TYPE_SINGLE)  // 行注：初始化 messageSessionType 变量
    const messageTargetId = messageSessionType === SESSION_TYPE_GROUP  // 行注：初始化 messageTargetId 状态
      ? String(rawMessage.toUserId)  // 行注：执行当前调用逻辑
      : String(messageItem.isMe ? rawMessage.toUserId : rawMessage.fromUserId)  // 行注：执行当前调用逻辑
    const sameCurrentSession = options.currentTargetId.value  // 行注：初始化 sameCurrentSession 变量
      && buildSessionKey(messageTargetId, messageSessionType) === buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)  // 行注：执行当前调用逻辑

    if (sameCurrentSession) {  // 行注：判断当前条件是否成立
      const shouldStickBottom = wasAtBottom  // 行注：初始化 shouldStickBottom 状态
      upsertMessage(messageItem)  // 行注：调用 upsertMessage 方法
      if (!messageItem.isMe && messageItem.mentionedMe) {  // 行注：判断当前条件是否成立
        appendMentionBanner(messageItem)  // 行注：调用 appendMentionBanner 方法
      }  // 行注：结束当前代码块
      nextTick(() => {  // 行注：调用 nextTick 方法
        if (options.messagesRef.value && shouldStickBottom) {  // 行注：判断当前条件是否成立
          options.messagesRef.value.scrollTop = options.messagesRef.value.scrollHeight  // 行注：更新 options.messagesRef.value.scrollTop 值
        }  // 行注：结束当前代码块
      })  // 行注：结束当前调用配置
      if (!messageItem.isMe) {  // 行注：判断当前条件是否成立
        notifyIncomingMessage(messageItem)  // 行注：调用 notifyIncomingMessage 方法
        void markCurrentSessionRead(messageTargetId, messageSessionType)  // 行注：调用 markCurrentSessionRead 方法
      }  // 行注：结束当前代码块
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    if (!messageItem.isMe) {  // 行注：判断当前条件是否成立
      notifyIncomingMessage(messageItem)  // 行注：调用 notifyIncomingMessage 方法
      flashSession(messageTargetId, messageSessionType)  // 行注：调用 flashSession 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function handleRealtimeMessageRecalled(rawMessage: any) {  // 行注：定义 handleRealtimeMessageRecalled 方法
    if (!rawMessage) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const messageItem = toDisplayMessage(rawMessage)  // 行注：初始化 messageItem 变量
    upsertMessage(messageItem)  // 行注：调用 upsertMessage 方法
    consumeMentionBanner(messageItem)  // 行注：调用 consumeMentionBanner 方法
    removeInAppNotificationsByMessageIds([messageItem.id])  // 行注：调用 removeInAppNotificationsByMessageIds 方法
  }  // 行注：结束当前代码块

  function handleRealtimeSession(rawSession: any) {  // 行注：定义 handleRealtimeSession 方法
    if (!rawSession) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const previousSession = options.sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(rawSession.targetId, Number(rawSession.sessionType || SESSION_TYPE_SINGLE)))  // 行注：初始化 previousSession 变量
    const session = upsertSession(rawSession)  // 行注：初始化 session 变量
    updateCurrentSessionFromStore(session)  // 行注：调用 updateCurrentSessionFromStore 方法
    if (previousSession && previousSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {  // 行注：判断当前条件是否成立
      flashSession(session.targetId, session.sessionType)  // 行注：调用 flashSession 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function applyReadReceiptToMessage(item: DisplayMessage, readTime: string) {  // 行注：定义 applyReadReceiptToMessage 方法
    if (item.status === MESSAGE_STATUS_RECALLED) {  // 行注：判断当前条件是否成立
      return item  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return {  // 行注：返回当前结果
      ...item,  // 行注：补充当前配置项
      readTime: readTime || item.readTime,  // 行注：设置 readTime 配置项
      readStatus: '已读'  // 行注：设置 readStatus 配置项
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function handleRealtimeReadReceipt(payload: any) {  // 行注：定义 handleRealtimeReadReceipt 方法
    if (!payload) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const sessionType = Number(payload.sessionType ?? SESSION_TYPE_SINGLE)  // 行注：初始化 sessionType 变量
    const readTime = payload.readTime ? String(payload.readTime) : ''  // 行注：初始化 readTime 变量
    const readTimestamp = getDateTimeTimestamp(readTime)  // 行注：初始化 readTimestamp 变量

    if (sessionType === SESSION_TYPE_SINGLE) {  // 行注：判断当前条件是否成立
      const messageIds = new Set((payload.messageIds || []).map((id: string | number) => String(id)))  // 行注：初始化 messageIds 变量
      if (messageIds.size === 0) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.messages.value = options.messages.value.map(item => {  // 行注：开始当前逻辑块
        if (!messageIds.has(String(item.id))) {  // 行注：判断当前条件是否成立
          return item  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        return applyReadReceiptToMessage(item, readTime)  // 行注：返回当前结果
      })  // 行注：结束当前调用配置
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    if (sessionType !== SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const groupId = payload.targetId != null ? String(payload.targetId) : ''  // 行注：初始化 groupId 状态
    const readerUserId = payload.readerUserId != null ? String(payload.readerUserId) : ''  // 行注：初始化 readerUserId 状态
    if (!groupId || !readerUserId) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const viewingGroup =  // 行注：初始化 viewingGroup 变量
      options.currentSessionType.value === SESSION_TYPE_GROUP &&  // 行注：补充当前表达式
      options.currentTargetId.value != null &&  // 行注：补充当前表达式
      String(options.currentTargetId.value) === groupId  // 行注：调用 String 方法

    if (!viewingGroup) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    const messageIds = new Set((payload.messageIds || []).map((id: string | number) => String(id)))  // 行注：初始化 messageIds 变量
    options.messages.value = options.messages.value.map(item => {  // 行注：开始当前逻辑块
      if (!item.isMe || Number(item.sessionType) !== SESSION_TYPE_GROUP || String(item.targetId) !== groupId) {  // 行注：判断当前条件是否成立
        return item  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (messageIds.size > 0) {  // 行注：判断当前条件是否成立
        if (!messageIds.has(String(item.id))) {  // 行注：判断当前条件是否成立
          return item  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        return applyReadReceiptToMessage(item, readTime)  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (!readTimestamp) {  // 行注：判断当前条件是否成立
        return item  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const messageTimestamp = getDateTimeTimestamp(item.createTime)  // 行注：初始化 messageTimestamp 变量
      if (messageTimestamp > 0 && messageTimestamp <= readTimestamp) {  // 行注：判断当前条件是否成立
        return applyReadReceiptToMessage(item, readTime)  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      return item  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function handleRealtimeOnlineStatus(payload: any) {  // 行注：定义 handleRealtimeOnlineStatus 方法
    if (!payload?.userId) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const targetUserId = String(payload.userId)  // 行注：初始化 targetUserId 状态
    options.sessions.value = options.sessions.value.map(session => {  // 行注：开始当前逻辑块
      if (session.sessionType !== SESSION_TYPE_SINGLE || String(session.targetId) !== targetUserId) {  // 行注：判断当前条件是否成立
        return session  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      return {  // 行注：返回当前结果
        ...session,  // 行注：补充当前配置项
        targetOnline: Boolean(payload.online)  // 行注：设置 targetOnline 配置项
      }  // 行注：结束当前代码块
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  async function handleRealtimeGroupRemoved(payload: any) {  // 行注：定义异步 handleRealtimeGroupRemoved 方法
    const groupId = payload?.groupId ? String(payload.groupId) : ''  // 行注：初始化 groupId 状态
    if (!groupId) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    await closeUnavailableGroupSession(groupId, {  // 行注：开始当前逻辑块
      notify: true,  // 行注：设置 notify 配置项
      messageText: '你已不在该群聊中'  // 行注：设置 messageText 配置项
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function handleRealtimeGroupDetail(detail: GroupDetail | null) {  // 行注：定义 handleRealtimeGroupDetail 方法
    if (!detail?.id) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const existingSession = options.sessions.value.find(session =>  // 行注：初始化 existingSession 变量
      buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(detail.id, SESSION_TYPE_GROUP)  // 行注：调用 buildSessionKey 方法
    )  // 行注：结束当前调用

    upsertSession({  // 行注：开始当前逻辑块
      targetId: detail.id,  // 行注：设置 targetId 配置项
      sessionType: SESSION_TYPE_GROUP,  // 行注：设置 sessionType 配置项
      targetNickname: detail.groupRemark || detail.groupName,  // 行注：设置 targetNickname 配置项
      targetAvatar: detail.groupAvatar || '',  // 行注：设置 targetAvatar 配置项
      lastMessage: existingSession?.lastMessage || '',  // 行注：设置 lastMessage 配置项
      lastMessageTime: existingSession?.lastMessageTime || '',  // 行注：设置 lastMessageTime 配置项
      unreadCount: existingSession?.unreadCount || 0,  // 行注：设置 unreadCount 配置项
      memberCount: detail.memberCount,  // 行注：设置 memberCount 配置项
      myRole: detail.myRole,  // 行注：设置 myRole 配置项
      groupRemark: detail.groupRemark || '',  // 行注：设置 groupRemark 配置项
      notice: detail.notice || '',  // 行注：设置 notice 配置项
      noticeUnread: Boolean(detail.noticeUnread),  // 行注：设置 noticeUnread 配置项
      muted: detail.muted,  // 行注：设置 muted 配置项
      muteTime: detail.muteTime,  // 行注：设置 muteTime 配置项
      notificationMuted: Boolean(detail.notificationMuted)  // 行注：设置 notificationMuted 配置项
    })  // 行注：结束当前调用配置

    if (options.currentTargetId.value && options.currentSessionType.value === SESSION_TYPE_GROUP && String(options.currentTargetId.value) === String(detail.id)) {  // 行注：判断当前条件是否成立
      applyGroupDetail(detail, false)  // 行注：调用 applyGroupDetail 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  const realtimeService = createChatRealtimeService({  // 行注：开始解构当前返回值
    onGroupDetail: handleRealtimeGroupDetail,  // 行注：设置 onGroupDetail 配置项
    onGroupRemoved: payload => {  // 行注：设置 onGroupRemoved 配置项
      void handleRealtimeGroupRemoved(payload)  // 行注：调用 handleRealtimeGroupRemoved 方法
    },  // 行注：补充当前配置项
    onMessage: handleRealtimeMessage,  // 行注：设置 onMessage 配置项
    onSession: handleRealtimeSession,  // 行注：设置 onSession 配置项
    onReadReceipt: handleRealtimeReadReceipt,  // 行注：设置 onReadReceipt 配置项
    onOnlineStatus: handleRealtimeOnlineStatus,  // 行注：设置 onOnlineStatus 配置项
    onMessageRecalled: handleRealtimeMessageRecalled  // 行注：设置 onMessageRecalled 配置项
  })  // 行注：结束当前调用配置

  async function syncSocketState() {  // 行注：定义异步 syncSocketState 方法
    if (!initialized.value || syncingSocketState.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    syncingSocketState.value = true  // 行注：更新 syncingSocketState 状态
    try {  // 行注：尝试执行可能失败的逻辑
      await loadFriends()  // 行注：调用 loadFriends 方法
      await loadSessions({ silent: true })  // 行注：调用 loadSessions 方法
      if (options.currentTargetId.value) {  // 行注：判断当前条件是否成立
        const syncTasks: Array<Promise<unknown>> = []  // 行注：初始化 syncTasks 方法
        syncTasks.push(loadMessages(options.currentTargetId.value, options.currentSessionType.value, { silent: true }))  // 行注：跳转到目标路由
        if (options.currentSessionType.value === SESSION_TYPE_GROUP) {  // 行注：判断当前条件是否成立
          syncTasks.push(loadGroupDetail(options.currentTargetId.value, false))  // 行注：跳转到目标路由
        }  // 行注：结束当前代码块
        await Promise.all(syncTasks)  // 行注：并行执行多项异步任务
      }  // 行注：结束当前代码块
    } finally {  // 行注：执行收尾清理逻辑
      syncingSocketState.value = false  // 行注：更新 syncingSocketState 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  const {  // 行注：开始解构当前返回值
    connected: wsConnected,  // 行注：设置 connected 配置项
    reconnectAttempt: wsReconnectAttempt,  // 行注：设置 reconnectAttempt 配置项
    connect: connectChatSocket,  // 行注：设置 connect 配置项
    disconnect: disconnectChatSocket,  // 行注：设置 disconnect 配置项
    sendRequest: sendChatSocketRequest  // 行注：设置 sendRequest 配置项
  } = useChatSocket({  // 行注：从 useChatSocket 中解构所需能力
    token: options.getToken,  // 行注：设置 token 配置项
    createTicket: async () => {  // 行注：设置 createTicket 配置项
      const res: any = await chatApi.createWsTicket()  // 行注：接收 res 异步结果
      return String(res.data?.ticket || '')  // 行注：返回当前结果
    },  // 行注：补充当前配置项
    onMessage: realtimeService.handleEvent,  // 行注：设置 onMessage 配置项
    onOpen: () => {  // 行注：传入 onOpen 回调
      void syncSocketState()  // 行注：调用 syncSocketState 方法
    }  // 行注：结束当前代码块
  })  // 行注：结束当前调用配置

  function sendChatCommand(action: string, data: Record<string, unknown> = {}) {  // 行注：定义 sendChatCommand 方法
    return sendChatSocketRequest(action, data)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  async function loadSessions(config: { silent?: boolean } = {}) {  // 行注：定义异步 loadSessions 方法
    if (!config.silent && options.loadingSessions) {  // 行注：判断当前条件是否成立
      options.loadingSessions.value = true  // 行注：更新 options.loadingSessions 状态
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      const response: any = await sendChatCommand('GET_SESSIONS')  // 行注：接收 response 异步结果
      const nextSessions = (response.data || []).map((item: any) => normalizeSession(item))  // 行注：初始化 nextSessions 变量

      if (options.sessions.value.length > 0) {  // 行注：判断当前条件是否成立
        const sessionMap = new Map(options.sessions.value.map(session => [buildSessionKey(session.targetId, session.sessionType), session]))  // 行注：初始化 sessionMap 变量
        for (const session of nextSessions) {  // 行注：遍历当前数据集合
          const oldSession = sessionMap.get(buildSessionKey(session.targetId, session.sessionType))  // 行注：初始化 oldSession 变量
          if (oldSession && oldSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {  // 行注：判断当前条件是否成立
            flashSession(session.targetId, session.sessionType)  // 行注：调用 flashSession 方法
          }  // 行注：结束当前代码块
        }  // 行注：结束当前代码块
      }  // 行注：结束当前代码块

      const draftSessions = options.sessions.value.filter(existing =>  // 行注：初始化 draftSessions 变量
        existing.isDraft  // 行注：补充当前表达式
        && shouldPreserveDraftSession(existing)  // 行注：执行当前调用逻辑
        && !nextSessions.some((item: ChatSession) => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(existing.targetId, existing.sessionType))  // 行注：调用 some 方法
      )  // 行注：结束当前调用
      options.sessions.value = [...draftSessions, ...nextSessions]  // 行注：更新 options.sessions 状态
      sortSessions()  // 行注：调用 sortSessions 方法

      const activeSingleTargetId = options.currentSessionType.value === SESSION_TYPE_SINGLE ? options.currentTargetId.value : null  // 行注：初始化 activeSingleTargetId 状态
      if (  // 行注：判断当前条件是否成立
        activeSingleTargetId  // 行注：补充当前表达式
        && !options.sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeSingleTargetId, options.currentSessionType.value))  // 行注：调用 some 方法
        && !isFriendTarget(activeSingleTargetId)  // 行注：执行当前调用逻辑
      ) {  // 行注：开始当前逻辑块
        await closeUnavailableSingleSession(activeSingleTargetId, { notify: true })  // 行注：调用 closeUnavailableSingleSession 方法
      }  // 行注：结束当前代码块

      const activeGroupTargetId = options.currentSessionType.value === SESSION_TYPE_GROUP ? options.currentTargetId.value : null  // 行注：初始化 activeGroupTargetId 状态
      if (  // 行注：判断当前条件是否成立
        activeGroupTargetId  // 行注：补充当前表达式
        && !options.sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeGroupTargetId, SESSION_TYPE_GROUP))  // 行注：调用 some 方法
      ) {  // 行注：开始当前逻辑块
        await closeUnavailableGroupSession(activeGroupTargetId, { notify: true })  // 行注：调用 closeUnavailableGroupSession 方法
      }  // 行注：结束当前代码块
    } catch (error) {  // 行注：捕获并处理异常
      console.error('loadSessions error:', error)  // 行注：输出错误日志
    } finally {  // 行注：执行收尾清理逻辑
      if (!config.silent && options.loadingSessions) {  // 行注：判断当前条件是否成立
        options.loadingSessions.value = false  // 行注：更新 options.loadingSessions 状态
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function checkIfAtBottom() {  // 行注：定义 checkIfAtBottom 方法
    if (!options.messagesRef.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const element = options.messagesRef.value  // 行注：初始化 element 变量
    wasAtBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 50  // 行注：更新 wasAtBottom 值
  }  // 行注：结束当前代码块

  function scrollMessagesToBottom(force = false) {  // 行注：定义 scrollMessagesToBottom 方法
    nextTick(() => {  // 行注：调用 nextTick 方法
      if (!options.messagesRef.value) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (!force && !wasAtBottom) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      options.messagesRef.value.scrollTop = options.messagesRef.value.scrollHeight  // 行注：更新 options.messagesRef.value.scrollTop 值
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function handleMessageMediaLoad() {  // 行注：定义 handleMessageMediaLoad 方法
    if (!wasAtBottom) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    scrollMessagesToBottom(true)  // 行注：调用 scrollMessagesToBottom 方法
  }  // 行注：结束当前代码块

  function setInitialized(value: boolean) {  // 行注：定义 setInitialized 方法
    initialized.value = value  // 行注：更新 initialized 状态
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    initialized,  // 行注：补充 initialized 配置项
    syncingSocketState,  // 行注：补充当前配置项
    activeJumpMessageKey,  // 行注：补充当前配置项
    flashSessionKey,  // 行注：补充当前配置项
    mentionBannerQueue,  // 行注：补充当前配置项
    showNoticeReminder,  // 行注：补充当前配置项
    acknowledgingNoticeReminder,  // 行注：补充当前配置项
    resolvedMessageFileUrls,  // 行注：补充当前配置项
    wsConnected,  // 行注：补充当前配置项
    wsReconnectAttempt,  // 行注：补充当前配置项
    showMentionBanner,  // 行注：补充当前配置项
    mentionBannerText,  // 行注：补充当前配置项
    mentionBannerActionText,  // 行注：补充当前配置项
    activeMentionBannerMessage,  // 行注：补充当前配置项
    connectChatSocket,  // 行注：补充当前配置项
    disconnectChatSocket,  // 行注：补充当前配置项
    sendChatCommand,  // 行注：补充当前配置项
    loadFriends,  // 行注：补充当前配置项
    loadSessions,  // 行注：补充当前配置项
    loadGroupDetail,  // 行注：补充当前配置项
    selectSession,  // 行注：补充当前配置项
    initializeRouteSession,  // 行注：补充当前配置项
    refreshCurrentSession,  // 行注：补充当前配置项
    acknowledgeNoticeReminder,  // 行注：补充当前配置项
    dismissMentionBanner,  // 行注：补充当前配置项
    handleMentionBannerClick,  // 行注：补充当前配置项
    clearActiveJumpHighlight,  // 行注：补充当前配置项
    cleanupMessageResources,  // 行注：补充当前配置项
    getMessageTextSegments,  // 行注：补充当前配置项
    ensureMessageFileAccessUrl,  // 行注：补充当前配置项
    getResolvedMessageFileUrl,  // 行注：补充当前配置项
    toDisplayMessage,  // 行注：补充当前配置项
    upsertSession,  // 行注：补充当前配置项
    upsertMessage,  // 行注：补充当前配置项
    applyGroupDetail,  // 行注：补充当前配置项
    removeSessionByTarget,  // 行注：补充当前配置项
    resetCurrentConversationState,  // 行注：补充当前配置项
    checkIfAtBottom,  // 行注：补充当前配置项
    scrollMessagesToBottom,  // 行注：补充当前配置项
    handleMessageMediaLoad,  // 行注：补充当前配置项
    setInitialized  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
