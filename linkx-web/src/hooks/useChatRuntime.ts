import { computed, nextTick, ref, type ComputedRef, type Ref } from 'vue'
import type { RouteLocationNormalizedLoaded, Router } from 'vue-router'
import { chatApi, friendApi, groupApi, userApi } from '../api/client'
import { createChatRealtimeService } from '../services/chatRealtime'
import type { ChatSession, DisplayMessage, FriendItem, GroupDetail } from '../types/chat'
import { getCachedFileAccessUrl, resolveFileAccessUrl } from '../utils/file-access'
import { getDateTimeTimestamp } from '../utils/datetime'
import { playNotificationSound, removeInAppNotificationsByMessageIds, showNotification } from '../utils/notify'
import { useChatSocket } from './useChatSocket'
import {
  MESSAGE_STATUS_RECALLED,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  MESSAGE_TYPE_SYSTEM,
  MESSAGE_TYPE_TEXT,
  SESSION_TYPE_GROUP,
  SESSION_TYPE_SINGLE
} from '../types/chat'
import {
  buildSessionKey,
  compareDisplayMessages,
  createLocalMessageId,
  escapeAttributeSelector,
  getMessageAnchorKey,
  getMessagePreview,
  normalizeSession,
  resolveMessageTargetId
} from '../utils/chat'

interface FeedbackApi {
  success: (content: string) => void
  warning: (content: string) => void
  error: (content: string) => void
}

interface UseChatRuntimeOptions {
  route: RouteLocationNormalizedLoaded
  router: Router
  message: FeedbackApi
  sessions: Ref<ChatSession[]>
  messages: Ref<DisplayMessage[]>
  friends: Ref<FriendItem[]>
  currentTargetId: Ref<string | null>
  currentSessionType: Ref<number>
  groupDetail: Ref<GroupDetail | null>
  showGroupDrawer: Ref<boolean>
  noticeDraft: Ref<string>
  isGroupSession: ComputedRef<boolean>
  currentSessionName: ComputedRef<string>
  currentNotificationMuted: ComputedRef<boolean>
  isGroupProfileChanged: ComputedRef<boolean>
  isGroupNoticeChanged: ComputedRef<boolean>
  messagesRef: Ref<HTMLElement | undefined>
  loadingSessions?: Ref<boolean>
  loadingMessages?: Ref<boolean>
  showMenu?: Ref<boolean>
  syncGroupProfileDraft: (detail?: GroupDetail | null) => void
  closeGroupDrawer: (options?: { force?: boolean }) => Promise<boolean> | boolean
  getToken: () => string
  getCurrentUserId: () => string | number
  getCurrentUserNickname: () => string
  getCurrentUserAvatar: () => string
  onResetConversationState?: () => void
}

export function useChatRuntime(options: UseChatRuntimeOptions) {
  const initialized = ref(false)
  const syncingSocketState = ref(false)
  const activeJumpMessageKey = ref('')
  const flashSessionKey = ref<string | null>(null)
  const mentionBannerQueue = ref<string[]>([])
  const showNoticeReminder = ref(false)
  const acknowledgingNoticeReminder = ref(false)
  const resolvedMessageFileUrls = ref<Record<string, string>>({})

  let activeJumpHighlightTimer: ReturnType<typeof setTimeout> | null = null
  let wasAtBottom = true
  let allowInitialHomeSessionAutoSelect = true

  function sortSessions() {
    options.sessions.value = [...options.sessions.value].sort((left, right) => {
      const leftTime = getDateTimeTimestamp(left.lastMessageTime)
      const rightTime = getDateTimeTimestamp(right.lastMessageTime)
      if (leftTime !== rightTime) {
        return rightTime - leftTime
      }
      return String(right.id || '').localeCompare(String(left.id || ''))
    })
  }

  function isFriendTarget(targetId: string | number) {
    return options.friends.value.some(friend => String(friend.friendId) === String(targetId))
  }

  function shouldPreserveDraftSession(session: ChatSession) {
    if (Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP) {
      return true
    }
    return isFriendTarget(session.targetId)
  }

  function isCurrentSession(session: ChatSession) {
    if (!options.currentTargetId.value) {
      return false
    }
    return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)
  }

  function upsertSession(nextSession: ChatSession) {
    const normalizedSession = normalizeSession(nextSession)
    const sessionKey = buildSessionKey(normalizedSession.targetId, normalizedSession.sessionType)
    const sessionIndex = options.sessions.value.findIndex(session => buildSessionKey(session.targetId, session.sessionType) === sessionKey)
    if (sessionIndex === -1) {
      options.sessions.value = [normalizedSession, ...options.sessions.value]
      sortSessions()
      return normalizedSession
    }

    const mergedSession = {
      ...options.sessions.value[sessionIndex],
      ...normalizedSession
    }
    options.sessions.value.splice(sessionIndex, 1, mergedSession)
    sortSessions()
    return mergedSession
  }

  function updateCurrentSessionFromStore(nextSession: ChatSession) {
    if (!options.currentTargetId.value || !isCurrentSession(nextSession)) {
      return
    }
    if (options.isGroupSession.value && options.groupDetail.value) {
      options.groupDetail.value = {
        ...options.groupDetail.value,
        groupAvatar: nextSession.targetAvatar || options.groupDetail.value.groupAvatar,
        groupRemark: nextSession.groupRemark ?? options.groupDetail.value.groupRemark,
        notice: nextSession.notice ?? options.groupDetail.value.notice,
        noticeUnread: nextSession.noticeUnread ?? options.groupDetail.value.noticeUnread,
        memberCount: nextSession.memberCount ?? options.groupDetail.value.memberCount,
        myRole: nextSession.myRole ?? options.groupDetail.value.myRole,
        muted: nextSession.muted ?? options.groupDetail.value.muted,
        muteTime: nextSession.muteTime ?? options.groupDetail.value.muteTime,
        notificationMuted: nextSession.notificationMuted ?? options.groupDetail.value.notificationMuted
      }
    }
  }

  function flashSession(targetId: string | number, sessionType: number) {
    const sessionKey = buildSessionKey(targetId, sessionType)
    flashSessionKey.value = sessionKey
    setTimeout(() => {
      if (flashSessionKey.value === sessionKey) {
        flashSessionKey.value = null
      }
    }, 2000)
  }

  function getRouteMessageId() {
    const rawMessageId = options.route.query.messageId
    if (Array.isArray(rawMessageId)) {
      return rawMessageId[0] ? String(rawMessageId[0]) : ''
    }
    return rawMessageId ? String(rawMessageId) : ''
  }

  function clearActiveJumpHighlight() {
    if (activeJumpHighlightTimer) {
      clearTimeout(activeJumpHighlightTimer)
      activeJumpHighlightTimer = null
    }
    activeJumpMessageKey.value = ''
  }

  function setActiveJumpHighlight(messageItem: DisplayMessage | null | undefined) {
    if (!messageItem) {
      clearActiveJumpHighlight()
      return
    }
    clearActiveJumpHighlight()
    activeJumpMessageKey.value = getMessageAnchorKey(messageItem)
    activeJumpHighlightTimer = setTimeout(() => {
      activeJumpMessageKey.value = ''
      activeJumpHighlightTimer = null
    }, 2600)
  }

  function findMessageByRouteMessageId(messageId: string) {
    if (!messageId) {
      return null
    }
    return options.messages.value.find(messageItem => String(messageItem.id) === messageId || getMessageAnchorKey(messageItem) === messageId) || null
  }

  function getMessageTextSegments(content: string) {
    const segments: Array<{ text: string; mention: boolean }> = []
    const pattern = /@[^\s,，。！？；：、]+/g
    let lastIndex = 0
    for (const match of content.matchAll(pattern)) {
      const index = match.index ?? 0
      if (index > lastIndex) {
        segments.push({
          text: content.slice(lastIndex, index),
          mention: false
        })
      }
      segments.push({
        text: match[0],
        mention: true
      })
      lastIndex = index + match[0].length
    }
    if (lastIndex < content.length) {
      segments.push({
        text: content.slice(lastIndex),
        mention: false
      })
    }
    if (segments.length === 0) {
      segments.push({
        text: content,
        mention: false
      })
    }
    return segments
  }

  function clearMentionBannerQueue() {
    mentionBannerQueue.value = []
  }

  function setMentionBannerQueue(messageItems: DisplayMessage[]) {
    mentionBannerQueue.value = messageItems
      .filter(messageItem => messageItem.mentionedMe && !messageItem.isMe && !messageItem.isSystem)
      .map(messageItem => getMessageAnchorKey(messageItem))
  }

  function appendMentionBanner(messageItem: DisplayMessage | null | undefined) {
    if (!messageItem?.mentionedMe || messageItem.isMe || messageItem.isSystem) {
      return
    }
    const nextKey = getMessageAnchorKey(messageItem)
    mentionBannerQueue.value = [nextKey, ...mentionBannerQueue.value.filter(key => key !== nextKey)]
  }

  function consumeMentionBanner(messageItem?: DisplayMessage | null) {
    if (!messageItem) {
      return
    }
    const key = getMessageAnchorKey(messageItem)
    mentionBannerQueue.value = mentionBannerQueue.value.filter(item => item !== key)
  }

  const resolvedMentionBannerKeys = computed(() => {
    const existingKeys = new Set(options.messages.value.map(messageItem => getMessageAnchorKey(messageItem)))
    const seen = new Set<string>()
    return mentionBannerQueue.value.filter(key => {
      if (!existingKeys.has(key) || seen.has(key)) {
        return false
      }
      seen.add(key)
      return true
    })
  })

  const activeMentionBannerMessage = computed(() => {
    const activeKey = resolvedMentionBannerKeys.value[0]
    if (!activeKey) {
      return null
    }
    return options.messages.value.find(messageItem => getMessageAnchorKey(messageItem) === activeKey) || null
  })

  const showMentionBanner = computed(() => {
    return options.isGroupSession.value && Boolean(activeMentionBannerMessage.value)
  })

  const mentionBannerText = computed(() => {
    const messageItem = activeMentionBannerMessage.value
    if (!messageItem) {
      return ''
    }
    const senderName = messageItem.name || '有成员'
    const preview = getMessagePreview(messageItem)
    return `${senderName}提醒你留意这条消息：${preview}`
  })

  const mentionBannerActionText = computed(() => {
    const remainingCount = Math.max(0, resolvedMentionBannerKeys.value.length - 1)
    return remainingCount > 0 ? `定位后还有 ${remainingCount} 条` : '点击定位'
  })

  function dismissMentionBanner() {
    consumeMentionBanner(activeMentionBannerMessage.value)
  }

  function scrollToMessage(messageItem: DisplayMessage | null | undefined, behavior: ScrollBehavior = 'smooth') {
    if (!options.messagesRef.value || !messageItem) {
      return
    }
    const selector = `[data-message-key="${escapeAttributeSelector(getMessageAnchorKey(messageItem))}"]`
    const target = options.messagesRef.value.querySelector<HTMLElement>(selector)
    if (!target) {
      return
    }
    target.scrollIntoView({
      behavior,
      block: 'center'
    })
  }

  async function clearRouteMessageQuery() {
    if (!getRouteMessageId()) {
      return
    }
    const nextQuery = { ...options.route.query }
    delete nextQuery.messageId
    await options.router.replace({
      path: options.route.path,
      query: nextQuery
    })
  }

  function scrollToActiveMentionMessage(behavior: ScrollBehavior = 'smooth') {
    if (!activeMentionBannerMessage.value) {
      return
    }
    scrollToMessage(activeMentionBannerMessage.value, behavior)
  }

  function handleMentionBannerClick() {
    const messageItem = activeMentionBannerMessage.value
    if (!messageItem) {
      return
    }
    scrollToMessage(messageItem)
    consumeMentionBanner(messageItem)
  }

  function resolveUnreadMentionMessages(messageList: DisplayMessage[], unreadCount: number) {
    if (!Number.isFinite(unreadCount) || unreadCount <= 0) {
      return []
    }
    const incomingMessages = messageList.filter(messageItem => !messageItem.isMe && !messageItem.isSystem)
    if (incomingMessages.length === 0) {
      return []
    }
    const unreadMessages = incomingMessages.slice(-Math.max(0, Math.trunc(unreadCount)))
    return unreadMessages.filter(messageItem => messageItem.mentionedMe).reverse()
  }

  function getNotificationTitle(messageItem: DisplayMessage) {
    if (messageItem.isSystem) {
      return '群系统通知'
    }
    const sessionName = options.sessions.value.find(session =>
      buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(messageItem.targetId, messageItem.sessionType)
    )?.targetNickname
    if (messageItem.mentionedMe) {
      return `${sessionName || options.currentSessionName.value || '群聊'} · 特别提醒`
    }
    return sessionName || messageItem.name || '新消息'
  }

  function shouldShowDesktopNotification(messageItem: DisplayMessage) {
    if (messageItem.isMe) {
      return false
    }
    if (messageItem.sessionType !== SESSION_TYPE_GROUP) {
      return true
    }
    const targetId = String(messageItem.targetId)
    if (options.currentTargetId.value && options.currentSessionType.value === SESSION_TYPE_GROUP && String(options.currentTargetId.value) === targetId) {
      return !options.currentNotificationMuted.value
    }
    const session = options.sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(targetId, SESSION_TYPE_GROUP))
    return !Boolean(session?.notificationMuted)
  }

  function getNotificationSoundType(messageItem: DisplayMessage) {
    if (messageItem.isSystem || messageItem.mentionedMe) {
      return 'attention' as const
    }
    return 'message' as const
  }

  function notifyIncomingMessage(messageItem: DisplayMessage) {
    if (!shouldShowDesktopNotification(messageItem)) {
      return
    }
    const targetId = String(messageItem.targetId || '')
    const sessionType = Number(messageItem.sessionType || SESSION_TYPE_SINGLE)
    void showNotification(getNotificationTitle(messageItem), getMessagePreview(messageItem), undefined, {
      targetId,
      sessionType,
      messageId: String(messageItem.id || ''),
      attention: Boolean(messageItem.isSystem || messageItem.mentionedMe)
    })
    void playNotificationSound(getNotificationSoundType(messageItem))
  }

  function releaseMessageResource(messageItem?: DisplayMessage | null) {
    if (!messageItem?.content?.startsWith('blob:')) {
      return
    }
    URL.revokeObjectURL(messageItem.content)
  }

  function cleanupMessageResources(messageList = options.messages.value) {
    for (const messageItem of messageList) {
      releaseMessageResource(messageItem)
    }
  }

  function shouldResolveRemoteMessageFile(messageItem?: DisplayMessage | null) {
    if (!messageItem) {
      return false
    }
    if (messageItem.msgType !== MESSAGE_TYPE_IMAGE && messageItem.msgType !== MESSAGE_TYPE_FILE) {
      return false
    }
    return Boolean(messageItem.content) && !messageItem.content.startsWith('blob:')
  }

  async function ensureMessageFileAccessUrl(rawUrl: string) {
    if (!rawUrl || rawUrl.startsWith('blob:')) {
      return rawUrl
    }
    const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)
    if (cachedAccessUrl) {
      if (resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {
        resolvedMessageFileUrls.value = {
          ...resolvedMessageFileUrls.value,
          [rawUrl]: cachedAccessUrl
        }
      }
      return cachedAccessUrl
    }
    const accessUrl = await resolveFileAccessUrl(rawUrl)
    if (accessUrl) {
      resolvedMessageFileUrls.value = {
        ...resolvedMessageFileUrls.value,
        [rawUrl]: accessUrl
      }
    }
    return accessUrl
  }

  async function preloadMessageFileUrls(messageList: DisplayMessage[]) {
    const targets = Array.from(new Set(
      messageList
        .filter(shouldResolveRemoteMessageFile)
        .map(messageItem => messageItem.content)
        .filter(Boolean)
    ))
    await Promise.all(targets.map(rawUrl => ensureMessageFileAccessUrl(rawUrl).catch(() => '')))
  }

  function getResolvedMessageFileUrl(messageItem: DisplayMessage) {
    if (!shouldResolveRemoteMessageFile(messageItem)) {
      return messageItem.content
    }
    const rawUrl = messageItem.content
    const cachedAccessUrl = getCachedFileAccessUrl(rawUrl)
    if (cachedAccessUrl && resolvedMessageFileUrls.value[rawUrl] !== cachedAccessUrl) {
      resolvedMessageFileUrls.value = {
        ...resolvedMessageFileUrls.value,
        [rawUrl]: cachedAccessUrl
      }
    }
    return cachedAccessUrl || ''
  }

  function resetCurrentConversationState() {
    cleanupMessageResources()
    resolvedMessageFileUrls.value = {}
    options.onResetConversationState?.()
  }

  function toDisplayMessage(item: any): DisplayMessage {
    const readTime = item.readTime || ''
    const isRecalled = Number(item.status ?? 0) === MESSAGE_STATUS_RECALLED
    const msgType = Number(item.msgType ?? MESSAGE_TYPE_TEXT)
    const sessionType = Number(item.sessionType || SESSION_TYPE_SINGLE)
    const isSystem = msgType === MESSAGE_TYPE_SYSTEM
    const isMe = !isSystem && String(item.fromUserId) === String(options.getCurrentUserId())
    const mentionUserIds = Array.isArray(item.mentionUserIds) ? item.mentionUserIds.map((id: string | number) => String(id)) : []
    const mentionAll = Boolean(item.mentionAll)
    const mentionDisplayNames = Array.isArray(item.mentionDisplayNames)
      ? item.mentionDisplayNames.filter((value: unknown): value is string => typeof value === 'string' && value.trim().length > 0)
      : []
    return {
      id: item.id,
      localId: item.clientMessageId || String(item.id || createLocalMessageId('server')),
      clientMessageId: item.clientMessageId || '',
      isMe,
      isSystem,
      name: item.fromNickname,
      fromAvatar: item.fromAvatar || '',
      content: item.content,
      msgType,
      status: Number(item.status ?? 0),
      readTime,
      createTime: item.createTime,
      time: item.createTime?.substring(11, 16) || '',
      readStatus: isRecalled ? '已撤回' : (readTime ? '已读' : '未读'),
      deliveryStatus: 'sent',
      fileName: item.fileName || '',
      fileSize: item.fileSize ? Number(item.fileSize) : undefined,
      sessionType,
      targetId: resolveMessageTargetId(item, isMe, sessionType),
      mentionAll,
      mentionUserIds,
      mentionDisplayNames,
      mentionedMe: sessionType === SESSION_TYPE_GROUP && !isSystem && !isMe && (mentionAll || mentionUserIds.includes(String(options.getCurrentUserId()))),
      uploadedFileId: undefined
    }
  }

  function upsertMessage(nextMessage: DisplayMessage) {
    const messageIndex = options.messages.value.findIndex(item => {
      if (String(item.id) === String(nextMessage.id)) {
        return true
      }
      if (item.clientMessageId && nextMessage.clientMessageId && item.clientMessageId === nextMessage.clientMessageId) {
        return true
      }
      return item.localId === nextMessage.localId
    })
    if (messageIndex === -1) {
      options.messages.value = [...options.messages.value, nextMessage]
    } else {
      const previousMessage = options.messages.value[messageIndex]
      if (previousMessage.content !== nextMessage.content) {
        releaseMessageResource(previousMessage)
      }
      options.messages.value.splice(messageIndex, 1, {
        ...options.messages.value[messageIndex],
        ...nextMessage
      })
    }
    options.messages.value.sort(compareDisplayMessages)
    if (shouldResolveRemoteMessageFile(nextMessage)) {
      void ensureMessageFileAccessUrl(nextMessage.content)
    }
  }

  function applyGroupDetail(detail: GroupDetail | null, syncDraft = true) {
    options.groupDetail.value = detail
    if (syncDraft || !options.isGroupNoticeChanged.value) {
      options.noticeDraft.value = detail?.notice || ''
    }
    if (syncDraft || !options.isGroupProfileChanged.value) {
      options.syncGroupProfileDraft(detail)
    }
    if (!detail?.noticeUnread || !detail.notice?.trim()) {
      showNoticeReminder.value = false
    } else {
      showNoticeReminder.value = true
    }

    if (!detail) {
      return
    }

    const sessionKey = buildSessionKey(detail.id, SESSION_TYPE_GROUP)
    const session = options.sessions.value.find(item => buildSessionKey(item.targetId, item.sessionType) === sessionKey)
    if (session) {
      session.targetNickname = detail.groupRemark || detail.groupName
      session.targetAvatar = detail.groupAvatar || ''
      session.groupRemark = detail.groupRemark || ''
      session.memberCount = detail.memberCount
      session.notice = detail.notice || ''
      session.myRole = detail.myRole
      session.muted = detail.muted
      session.muteTime = detail.muteTime
      session.notificationMuted = Boolean(detail.notificationMuted)
    }
  }

  function removeSessionByTarget(targetId: string | number, sessionType: number) {
    const sessionKey = buildSessionKey(targetId, sessionType)
    options.sessions.value = options.sessions.value.filter(session => buildSessionKey(session.targetId, session.sessionType) !== sessionKey)
  }

  function isUnavailableConversationError(error: any) {
    const code = Number(error?.response?.data?.code || error?.response?.status || 0)
    return code === 403 || code === 404
  }

  async function closeUnavailableSingleSession(targetId: string | number, config: { notify?: boolean } = {}) {
    const normalizedTargetId = String(targetId)
    removeSessionByTarget(normalizedTargetId, SESSION_TYPE_SINGLE)

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)
    const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_SINGLE
    const currentMatches = Boolean(options.currentTargetId.value)
      && options.currentSessionType.value === SESSION_TYPE_SINGLE
      && String(options.currentTargetId.value) === normalizedTargetId

    if (currentMatches) {
      resetCurrentConversationState()
    }

    if (routeMatches) {
      await options.router.replace('/chat')
    }

    if ((routeMatches || currentMatches) && config.notify) {
      options.message.warning('当前单聊已不可用')
    }
  }

  async function closeUnavailableGroupSession(
    targetId: string | number,
    config: { notify?: boolean; messageText?: string } = {}
  ) {
    const normalizedTargetId = String(targetId)
    removeSessionByTarget(normalizedTargetId, SESSION_TYPE_GROUP)

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)
    const routeMatches = routeTargetId === normalizedTargetId && routeSessionType === SESSION_TYPE_GROUP
    const currentMatches = Boolean(options.currentTargetId.value)
      && options.currentSessionType.value === SESSION_TYPE_GROUP
      && String(options.currentTargetId.value) === normalizedTargetId

    if (currentMatches) {
      await options.closeGroupDrawer({ force: true })
      resetCurrentConversationState()
    }

    if (routeMatches) {
      await options.router.replace('/chat')
    }

    if ((routeMatches || currentMatches) && config.notify) {
      options.message.warning(config.messageText || '当前群聊已不可用')
    }
  }

  async function loadFriends() {
    try {
      const response: any = await friendApi.getList()
      options.friends.value = response.data || []
    } catch (error) {
      console.error('loadFriends error:', error)
    }
  }

  async function markCurrentSessionRead(targetId = options.currentTargetId.value, sessionType = options.currentSessionType.value) {
    if (!targetId) {
      return
    }
    await sendChatCommand('MARK_READ', { targetId, sessionType }).catch(() => undefined)
  }

  async function loadMessages(
    targetId: string,
    sessionType: number,
    config: {
      unreadCountSnapshot?: number
      preferMentionScroll?: boolean
      targetMessageId?: string
      silent?: boolean
    } = {}
  ) {
    let skipPostLoadHandling = false
    if (!config.silent && options.loadingMessages) {
      options.loadingMessages.value = true
    }
    try {
      const targetMessageId = config.targetMessageId ? String(config.targetMessageId) : ''
      let nextMessages: DisplayMessage[] = []
      if (targetMessageId) {
        const aggregatedMessages = new Map<string, DisplayMessage>()
        const pageSize = 100
        const maxPages = 20
        let locatedTargetMessage = false
        for (let page = 1; page <= maxPages; page += 1) {
          const response: any = await sendChatCommand('GET_HISTORY', {
            targetId,
            sessionType,
            page,
            size: pageSize
          })
          const pageRawMessages = response.data || []
          for (const item of pageRawMessages) {
            const messageItem = toDisplayMessage(item)
            aggregatedMessages.set(getMessageAnchorKey(messageItem), messageItem)
            if (String(messageItem.id) === targetMessageId || getMessageAnchorKey(messageItem) === targetMessageId) {
              locatedTargetMessage = true
            }
          }
          if (locatedTargetMessage || pageRawMessages.length < pageSize) {
            break
          }
        }
        nextMessages = Array.from(aggregatedMessages.values()).sort(compareDisplayMessages)
      } else {
        const response: any = await sendChatCommand('GET_HISTORY', {
          targetId,
          sessionType
        })
        const rawMessages = response.data || []
        nextMessages = rawMessages.map((item: any) => toDisplayMessage(item))
      }
      const mentionTargets = config.preferMentionScroll
        ? resolveUnreadMentionMessages(nextMessages, Number(config.unreadCountSnapshot || 0))
        : []

      cleanupMessageResources(options.messages.value)
      options.messages.value = nextMessages
      resolvedMessageFileUrls.value = {}
      await preloadMessageFileUrls(nextMessages)
      if (mentionTargets.length > 0) {
        setMentionBannerQueue(mentionTargets)
      } else {
        clearMentionBannerQueue()
      }
      await markCurrentSessionRead(targetId, sessionType)
    } catch (error: any) {
      console.error('loadMessages error:', error)
      if (sessionType === SESSION_TYPE_GROUP && isUnavailableConversationError(error)) {
        skipPostLoadHandling = true
        await closeUnavailableGroupSession(targetId, { notify: true })
        return
      }
      cleanupMessageResources(options.messages.value)
      options.messages.value = []
      clearMentionBannerQueue()
    } finally {
      if (!config.silent && options.loadingMessages) {
        options.loadingMessages.value = false
      }
      if (skipPostLoadHandling) {
        return
      }
      wasAtBottom = true
      await nextTick()
      const targetMessageId = config.targetMessageId ? String(config.targetMessageId) : ''
      const targetMessage = targetMessageId ? findMessageByRouteMessageId(targetMessageId) : null
      if (targetMessage) {
        scrollToMessage(targetMessage, 'auto')
        setActiveJumpHighlight(targetMessage)
        await clearRouteMessageQuery()
      } else if (targetMessageId) {
        options.message.warning('未找到目标聊天内容，可能已超出当前加载范围')
        await clearRouteMessageQuery()
        if (showMentionBanner.value && activeMentionBannerMessage.value) {
          scrollToActiveMentionMessage('auto')
        } else {
          scrollMessagesToBottom(true)
        }
      } else if (showMentionBanner.value && activeMentionBannerMessage.value) {
        scrollToActiveMentionMessage('auto')
      } else {
        scrollMessagesToBottom(true)
      }
    }
  }

  async function loadGroupDetail(groupId: string | number, syncDraft = true) {
    try {
      const response: any = await groupApi.detail(groupId)
      const detail = response.data || null
      applyGroupDetail(detail, syncDraft)
      return true
    } catch (error) {
      console.error('loadGroupDetail error:', error)
      if (isUnavailableConversationError(error)) {
        await closeUnavailableGroupSession(groupId, { notify: true })
        return false
      }
      options.message.error('获取群详情失败')
      return false
    }
  }

  async function selectSession(
    session: ChatSession,
    syncRoute = true,
    config: {
      targetMessageId?: string
    } = {}
  ) {
    allowInitialHomeSessionAutoSelect = false
    const nextSessionKey = buildSessionKey(session.targetId, Number(session.sessionType || SESSION_TYPE_SINGLE))
    const currentSessionKey = options.currentTargetId.value
      ? buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)
      : null
    if (options.showGroupDrawer.value && currentSessionKey && currentSessionKey !== nextSessionKey) {
      const closed = await options.closeGroupDrawer()
      if (!closed) {
        return
      }
    }

    options.currentTargetId.value = String(session.targetId)
    options.currentSessionType.value = Number(session.sessionType || SESSION_TYPE_SINGLE)
    if (options.showMenu) {
      options.showMenu.value = false
    }
    wasAtBottom = true
    clearMentionBannerQueue()
    clearActiveJumpHighlight()

    if (options.currentSessionType.value === SESSION_TYPE_GROUP) {
      const detailLoaded = await loadGroupDetail(session.targetId)
      if (!detailLoaded) {
        return
      }
    } else {
      applyGroupDetail(null)
      options.showGroupDrawer.value = false
    }

    await loadMessages(String(session.targetId), options.currentSessionType.value, {
      unreadCountSnapshot: Number(session.unreadCount || 0),
      preferMentionScroll: Number(session.sessionType || SESSION_TYPE_SINGLE) === SESSION_TYPE_GROUP,
      targetMessageId: config.targetMessageId || ''
    })

    if (syncRoute) {
      await options.router.replace({
        path: `/chat/${session.targetId}`,
        query: options.currentSessionType.value === SESSION_TYPE_GROUP ? { sessionType: String(SESSION_TYPE_GROUP) } : {}
      })
    }
  }

  async function initializeRouteSession() {
    if (!initialized.value) {
      return
    }

    const routeTargetId = options.route.params.targetId ? String(options.route.params.targetId) : ''
    const routeSessionType = Number(options.route.query.sessionType || SESSION_TYPE_SINGLE)
    const routeMessageId = getRouteMessageId()

    if (!routeTargetId) {
      if (allowInitialHomeSessionAutoSelect && !options.currentTargetId.value && options.sessions.value.length > 0) {
        await selectSession(options.sessions.value[0], false)
      }
      return
    }

    const existingSession = options.sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(routeTargetId, routeSessionType))
    if (existingSession) {
      if (
        routeMessageId
        || !options.currentTargetId.value
        || buildSessionKey(options.currentTargetId.value, options.currentSessionType.value) !== buildSessionKey(routeTargetId, routeSessionType)
      ) {
        await selectSession(existingSession, false, { targetMessageId: routeMessageId })
      }
      return
    }

    if (routeSessionType === SESSION_TYPE_GROUP) {
      try {
        const response: any = await groupApi.detail(routeTargetId)
        const detail = response.data
        if (!detail) {
          return
        }
        const draftSession: ChatSession = {
          targetId: routeTargetId,
          sessionType: SESSION_TYPE_GROUP,
          targetNickname: detail.groupName,
          targetAvatar: detail.groupAvatar || '',
          lastMessage: '',
          lastMessageTime: detail.createTime || '',
          unreadCount: 0,
          memberCount: detail.memberCount,
          myRole: detail.myRole,
          notice: detail.notice || '',
          noticeUnread: Boolean(detail.noticeUnread),
          muted: detail.muted,
          muteTime: detail.muteTime,
          isDraft: true
        }
        options.sessions.value = [draftSession, ...options.sessions.value]
        await selectSession(draftSession, false, { targetMessageId: routeMessageId })
      } catch (error: any) {
        if (isUnavailableConversationError(error)) {
          await closeUnavailableGroupSession(routeTargetId, { notify: true })
          return
        }
        options.message.error('打开群聊失败')
      }
      return
    }

    try {
      if (!isFriendTarget(routeTargetId)) {
        await closeUnavailableSingleSession(routeTargetId, { notify: true })
        return
      }
      const response: any = await userApi.getUser(routeTargetId)
      const user = response.data
      if (!user) {
        return
      }
      const draftSession: ChatSession = {
        targetId: routeTargetId,
        sessionType: SESSION_TYPE_SINGLE,
        targetNickname: user.nickname,
        targetUsername: user.username,
        targetAvatar: user.avatar || '',
        lastMessage: '',
        lastMessageTime: '',
        unreadCount: 0,
        isDraft: true
      }
      options.sessions.value = [draftSession, ...options.sessions.value]
      await selectSession(draftSession, false, { targetMessageId: routeMessageId })
    } catch (error: any) {
      if (isUnavailableConversationError(error)) {
        await closeUnavailableSingleSession(routeTargetId, { notify: true })
        return
      }
      options.message.error('打开会话失败')
    }
  }

  async function refreshCurrentSession() {
    if (options.showMenu) {
      options.showMenu.value = false
    }
    await loadFriends()
    await loadSessions()
    if (!options.currentTargetId.value) {
      return
    }
    if (options.isGroupSession.value) {
      const detailLoaded = await loadGroupDetail(options.currentTargetId.value)
      if (!detailLoaded || !options.currentTargetId.value) {
        return
      }
    }
    await loadMessages(options.currentTargetId.value, options.currentSessionType.value)
    options.message.success('已刷新')
  }

  async function acknowledgeNoticeReminder() {
    if (!options.currentTargetId.value || options.currentSessionType.value !== SESSION_TYPE_GROUP || !options.groupDetail.value) {
      showNoticeReminder.value = false
      return
    }
    acknowledgingNoticeReminder.value = true
    try {
      await groupApi.markNoticeRead(options.currentTargetId.value)
      const activeSession = options.sessions.value.find(session =>
        buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(options.currentTargetId.value || '', SESSION_TYPE_GROUP)
      )
      if (activeSession) {
        upsertSession({
          ...activeSession,
          noticeUnread: false
        })
      }
      applyGroupDetail({
        ...options.groupDetail.value,
        noticeUnread: false,
        noticeReadTime: options.groupDetail.value.noticeUpdateTime || options.groupDetail.value.noticeReadTime
      }, false)
      showNoticeReminder.value = false
    } catch (error: any) {
      console.error('acknowledgeNoticeReminder error:', error)
      options.message.error(error.response?.data?.message || '确认群公告失败')
    } finally {
      acknowledgingNoticeReminder.value = false
    }
  }

  function handleRealtimeMessage(rawMessage: any) {
    if (!rawMessage) {
      return
    }
    const messageItem = toDisplayMessage(rawMessage)
    const messageSessionType = Number(rawMessage.sessionType || SESSION_TYPE_SINGLE)
    const messageTargetId = messageSessionType === SESSION_TYPE_GROUP
      ? String(rawMessage.toUserId)
      : String(messageItem.isMe ? rawMessage.toUserId : rawMessage.fromUserId)
    const sameCurrentSession = options.currentTargetId.value
      && buildSessionKey(messageTargetId, messageSessionType) === buildSessionKey(options.currentTargetId.value, options.currentSessionType.value)

    if (sameCurrentSession) {
      const shouldStickBottom = wasAtBottom
      upsertMessage(messageItem)
      if (!messageItem.isMe && messageItem.mentionedMe) {
        appendMentionBanner(messageItem)
      }
      nextTick(() => {
        if (options.messagesRef.value && shouldStickBottom) {
          options.messagesRef.value.scrollTop = options.messagesRef.value.scrollHeight
        }
      })
      if (!messageItem.isMe) {
        notifyIncomingMessage(messageItem)
        void markCurrentSessionRead(messageTargetId, messageSessionType)
      }
      return
    }

    if (!messageItem.isMe) {
      notifyIncomingMessage(messageItem)
      flashSession(messageTargetId, messageSessionType)
    }
  }

  function handleRealtimeMessageRecalled(rawMessage: any) {
    if (!rawMessage) {
      return
    }
    const messageItem = toDisplayMessage(rawMessage)
    upsertMessage(messageItem)
    consumeMentionBanner(messageItem)
    removeInAppNotificationsByMessageIds([messageItem.id])
  }

  function handleRealtimeSession(rawSession: any) {
    if (!rawSession) {
      return
    }
    const previousSession = options.sessions.value.find(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(rawSession.targetId, Number(rawSession.sessionType || SESSION_TYPE_SINGLE)))
    const session = upsertSession(rawSession)
    updateCurrentSessionFromStore(session)
    if (previousSession && previousSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {
      flashSession(session.targetId, session.sessionType)
    }
  }

  function applyReadReceiptToMessage(item: DisplayMessage, readTime: string) {
    if (item.status === MESSAGE_STATUS_RECALLED) {
      return item
    }
    return {
      ...item,
      readTime: readTime || item.readTime,
      readStatus: '已读'
    }
  }

  function handleRealtimeReadReceipt(payload: any) {
    if (!payload) {
      return
    }
    const sessionType = Number(payload.sessionType ?? SESSION_TYPE_SINGLE)
    const readTime = payload.readTime ? String(payload.readTime) : ''
    const readTimestamp = getDateTimeTimestamp(readTime)

    if (sessionType === SESSION_TYPE_SINGLE) {
      const messageIds = new Set((payload.messageIds || []).map((id: string | number) => String(id)))
      if (messageIds.size === 0) {
        return
      }
      options.messages.value = options.messages.value.map(item => {
        if (!messageIds.has(String(item.id))) {
          return item
        }
        return applyReadReceiptToMessage(item, readTime)
      })
      return
    }

    if (sessionType !== SESSION_TYPE_GROUP) {
      return
    }

    const groupId = payload.targetId != null ? String(payload.targetId) : ''
    const readerUserId = payload.readerUserId != null ? String(payload.readerUserId) : ''
    if (!groupId || !readerUserId) {
      return
    }

    const viewingGroup =
      options.currentSessionType.value === SESSION_TYPE_GROUP &&
      options.currentTargetId.value != null &&
      String(options.currentTargetId.value) === groupId

    if (!viewingGroup) {
      return
    }

    const messageIds = new Set((payload.messageIds || []).map((id: string | number) => String(id)))
    options.messages.value = options.messages.value.map(item => {
      if (!item.isMe || Number(item.sessionType) !== SESSION_TYPE_GROUP || String(item.targetId) !== groupId) {
        return item
      }
      if (messageIds.size > 0) {
        if (!messageIds.has(String(item.id))) {
          return item
        }
        return applyReadReceiptToMessage(item, readTime)
      }
      if (!readTimestamp) {
        return item
      }
      const messageTimestamp = getDateTimeTimestamp(item.createTime)
      if (messageTimestamp > 0 && messageTimestamp <= readTimestamp) {
        return applyReadReceiptToMessage(item, readTime)
      }
      return item
    })
  }

  function handleRealtimeOnlineStatus(payload: any) {
    if (!payload?.userId) {
      return
    }
    const targetUserId = String(payload.userId)
    options.sessions.value = options.sessions.value.map(session => {
      if (session.sessionType !== SESSION_TYPE_SINGLE || String(session.targetId) !== targetUserId) {
        return session
      }
      return {
        ...session,
        targetOnline: Boolean(payload.online)
      }
    })
  }

  async function handleRealtimeGroupRemoved(payload: any) {
    const groupId = payload?.groupId ? String(payload.groupId) : ''
    if (!groupId) {
      return
    }
    await closeUnavailableGroupSession(groupId, {
      notify: true,
      messageText: '你已不在该群聊中'
    })
  }

  function handleRealtimeGroupDetail(detail: GroupDetail | null) {
    if (!detail?.id) {
      return
    }
    const existingSession = options.sessions.value.find(session =>
      buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(detail.id, SESSION_TYPE_GROUP)
    )

    upsertSession({
      targetId: detail.id,
      sessionType: SESSION_TYPE_GROUP,
      targetNickname: detail.groupRemark || detail.groupName,
      targetAvatar: detail.groupAvatar || '',
      lastMessage: existingSession?.lastMessage || '',
      lastMessageTime: existingSession?.lastMessageTime || '',
      unreadCount: existingSession?.unreadCount || 0,
      memberCount: detail.memberCount,
      myRole: detail.myRole,
      groupRemark: detail.groupRemark || '',
      notice: detail.notice || '',
      noticeUnread: Boolean(detail.noticeUnread),
      muted: detail.muted,
      muteTime: detail.muteTime,
      notificationMuted: Boolean(detail.notificationMuted)
    })

    if (options.currentTargetId.value && options.currentSessionType.value === SESSION_TYPE_GROUP && String(options.currentTargetId.value) === String(detail.id)) {
      applyGroupDetail(detail, false)
    }
  }

  const realtimeService = createChatRealtimeService({
    onGroupDetail: handleRealtimeGroupDetail,
    onGroupRemoved: payload => {
      void handleRealtimeGroupRemoved(payload)
    },
    onMessage: handleRealtimeMessage,
    onSession: handleRealtimeSession,
    onReadReceipt: handleRealtimeReadReceipt,
    onOnlineStatus: handleRealtimeOnlineStatus,
    onMessageRecalled: handleRealtimeMessageRecalled
  })

  async function syncSocketState() {
    if (!initialized.value || syncingSocketState.value) {
      return
    }
    syncingSocketState.value = true
    try {
      await loadFriends()
      await loadSessions({ silent: true })
      if (options.currentTargetId.value) {
        const syncTasks: Array<Promise<unknown>> = []
        syncTasks.push(loadMessages(options.currentTargetId.value, options.currentSessionType.value, { silent: true }))
        if (options.currentSessionType.value === SESSION_TYPE_GROUP) {
          syncTasks.push(loadGroupDetail(options.currentTargetId.value, false))
        }
        await Promise.all(syncTasks)
      }
    } finally {
      syncingSocketState.value = false
    }
  }

  const {
    connected: wsConnected,
    reconnectAttempt: wsReconnectAttempt,
    connect: connectChatSocket,
    disconnect: disconnectChatSocket,
    sendRequest: sendChatSocketRequest
  } = useChatSocket({
    token: options.getToken,
    createTicket: async () => {
      const res: any = await chatApi.createWsTicket()
      return String(res.data?.ticket || '')
    },
    onMessage: realtimeService.handleEvent,
    onOpen: () => {
      void syncSocketState()
    }
  })

  function sendChatCommand(action: string, data: Record<string, unknown> = {}) {
    return sendChatSocketRequest(action, data)
  }

  async function loadSessions(config: { silent?: boolean } = {}) {
    if (!config.silent && options.loadingSessions) {
      options.loadingSessions.value = true
    }
    try {
      const response: any = await sendChatCommand('GET_SESSIONS')
      const nextSessions = (response.data || []).map((item: any) => normalizeSession(item))

      if (options.sessions.value.length > 0) {
        const sessionMap = new Map(options.sessions.value.map(session => [buildSessionKey(session.targetId, session.sessionType), session]))
        for (const session of nextSessions) {
          const oldSession = sessionMap.get(buildSessionKey(session.targetId, session.sessionType))
          if (oldSession && oldSession.lastMessage !== session.lastMessage && !isCurrentSession(session)) {
            flashSession(session.targetId, session.sessionType)
          }
        }
      }

      const draftSessions = options.sessions.value.filter(existing =>
        existing.isDraft
        && shouldPreserveDraftSession(existing)
        && !nextSessions.some((item: ChatSession) => buildSessionKey(item.targetId, item.sessionType) === buildSessionKey(existing.targetId, existing.sessionType))
      )
      options.sessions.value = [...draftSessions, ...nextSessions]
      sortSessions()

      const activeSingleTargetId = options.currentSessionType.value === SESSION_TYPE_SINGLE ? options.currentTargetId.value : null
      if (
        activeSingleTargetId
        && !options.sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeSingleTargetId, options.currentSessionType.value))
        && !isFriendTarget(activeSingleTargetId)
      ) {
        await closeUnavailableSingleSession(activeSingleTargetId, { notify: true })
      }

      const activeGroupTargetId = options.currentSessionType.value === SESSION_TYPE_GROUP ? options.currentTargetId.value : null
      if (
        activeGroupTargetId
        && !options.sessions.value.some(session => buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(activeGroupTargetId, SESSION_TYPE_GROUP))
      ) {
        await closeUnavailableGroupSession(activeGroupTargetId, { notify: true })
      }
    } catch (error) {
      console.error('loadSessions error:', error)
    } finally {
      if (!config.silent && options.loadingSessions) {
        options.loadingSessions.value = false
      }
    }
  }

  function checkIfAtBottom() {
    if (!options.messagesRef.value) {
      return
    }
    const element = options.messagesRef.value
    wasAtBottom = element.scrollHeight - element.scrollTop - element.clientHeight < 50
  }

  function scrollMessagesToBottom(force = false) {
    nextTick(() => {
      if (!options.messagesRef.value) {
        return
      }
      if (!force && !wasAtBottom) {
        return
      }
      options.messagesRef.value.scrollTop = options.messagesRef.value.scrollHeight
    })
  }

  function handleMessageMediaLoad() {
    if (!wasAtBottom) {
      return
    }
    scrollMessagesToBottom(true)
  }

  function setInitialized(value: boolean) {
    initialized.value = value
  }

  return {
    initialized,
    syncingSocketState,
    activeJumpMessageKey,
    flashSessionKey,
    mentionBannerQueue,
    showNoticeReminder,
    acknowledgingNoticeReminder,
    resolvedMessageFileUrls,
    wsConnected,
    wsReconnectAttempt,
    showMentionBanner,
    mentionBannerText,
    mentionBannerActionText,
    activeMentionBannerMessage,
    connectChatSocket,
    disconnectChatSocket,
    sendChatCommand,
    loadFriends,
    loadSessions,
    loadGroupDetail,
    selectSession,
    initializeRouteSession,
    refreshCurrentSession,
    acknowledgeNoticeReminder,
    dismissMentionBanner,
    handleMentionBannerClick,
    clearActiveJumpHighlight,
    cleanupMessageResources,
    getMessageTextSegments,
    ensureMessageFileAccessUrl,
    getResolvedMessageFileUrl,
    toDisplayMessage,
    upsertSession,
    upsertMessage,
    applyGroupDetail,
    removeSessionByTarget,
    resetCurrentConversationState,
    checkIfAtBottom,
    scrollMessagesToBottom,
    handleMessageMediaLoad,
    setInitialized
  }
}
