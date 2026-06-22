import { computed, nextTick, ref, type ComponentPublicInstance, type ComputedRef, type Ref } from 'vue'
import type { GroupDetail, GroupMember, MentionCandidate } from '../types/chat'
import { escapeRegExp } from '../utils/chat'

interface UseChatComposerOptions {
  currentMuted: ComputedRef<boolean>
  isGroupSession: ComputedRef<boolean>
  canMentionAll: ComputedRef<boolean>
  groupDetail: Ref<GroupDetail | null>
  getCurrentUserId: () => string | number
  onSend: () => Promise<void> | void
}

export function useChatComposer(options: UseChatComposerOptions) {
  const inputMessage = ref('')
  const showEmojiPicker = ref(false)
  const showMentionMenu = ref(false)
  const mentionQuery = ref('')
  const mentionTriggerIndex = ref(-1)
  const mentionHighlightedIndex = ref(0)
  const textareaRef = ref<HTMLTextAreaElement>()
  const fileInputRef = ref<HTMLInputElement>()
  const imageInputRef = ref<HTMLInputElement>()
  const emojiRef = ref<HTMLElement>()
  const mentionMenuRef = ref<HTMLElement>()

  function setTextareaRef(element: Element | ComponentPublicInstance | null) {
    textareaRef.value = element instanceof HTMLTextAreaElement ? element : undefined
  }

  function setEmojiRef(element: Element | ComponentPublicInstance | null) {
    emojiRef.value = element instanceof HTMLElement ? element : undefined
  }

  function setMentionMenuRef(element: Element | ComponentPublicInstance | null) {
    mentionMenuRef.value = element instanceof HTMLElement ? element : undefined
  }

  function autoResize() {
    if (!textareaRef.value) {
      return
    }
    textareaRef.value.style.height = 'auto'
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 120)}px`
  }

  function getMemberMentionToken(member: GroupMember) {
    const nickname = member.nickname?.trim()
    if (nickname) {
      const duplicateCount = (options.groupDetail.value?.members || []).filter(item => item.nickname?.trim() === nickname).length
      if (duplicateCount === 1) {
        return nickname
      }
    }
    return member.username?.trim() || nickname || `成员${member.userId}`
  }

  const mentionMenuBaseOptions = computed<MentionCandidate[]>(() => {
    if (!options.isGroupSession.value) {
      return []
    }
    const nextOptions: MentionCandidate[] = []
    if (options.canMentionAll.value) {
      nextOptions.push({
        key: 'all',
        label: '所有人',
        meta: '仅群主和管理员可用',
        insertToken: '所有人',
        isAll: true
      })
    }
    for (const member of options.groupDetail.value?.members || []) {
      if (String(member.userId) === String(options.getCurrentUserId())) {
        continue
      }
      const displayName = member.nickname || member.username || `成员${member.userId}`
      nextOptions.push({
        key: `user-${member.userId}`,
        label: displayName,
        meta: `@${member.username}`,
        insertToken: getMemberMentionToken(member),
        mentionUserId: String(member.userId),
        isAll: false
      })
    }
    return nextOptions
  })

  const mentionCandidates = computed(() => {
    const keyword = mentionQuery.value.trim().toLowerCase()
    if (!keyword) {
      return mentionMenuBaseOptions.value
    }
    return mentionMenuBaseOptions.value.filter(candidate => {
      const text = `${candidate.label} ${candidate.meta} ${candidate.insertToken}`.toLowerCase()
      return text.includes(keyword)
    })
  })

  function closeMentionMenu() {
    showMentionMenu.value = false
    mentionQuery.value = ''
    mentionTriggerIndex.value = -1
    mentionHighlightedIndex.value = 0
  }

  function getMentionTrigger(text: string, cursor: number) {
    const beforeCursor = text.slice(0, cursor)
    const atIndex = beforeCursor.lastIndexOf('@')
    if (atIndex === -1) {
      return null
    }
    const beforeChar = atIndex > 0 ? beforeCursor[atIndex - 1] : ''
    if (beforeChar && !/\s/.test(beforeChar)) {
      return null
    }
    const query = beforeCursor.slice(atIndex + 1)
    if (/[\s@]/.test(query)) {
      return null
    }
    return {
      atIndex,
      query
    }
  }

  function syncMentionMenu() {
    if (!options.isGroupSession.value || !textareaRef.value || options.currentMuted.value) {
      closeMentionMenu()
      return
    }
    const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length
    const trigger = getMentionTrigger(inputMessage.value, cursor)
    if (!trigger) {
      closeMentionMenu()
      return
    }
    mentionTriggerIndex.value = trigger.atIndex
    mentionQuery.value = trigger.query
    mentionHighlightedIndex.value = 0
    showMentionMenu.value = true
  }

  function handleInputChange() {
    autoResize()
    syncMentionMenu()
  }

  function selectMentionCandidate(candidate: MentionCandidate) {
    if (!textareaRef.value) {
      return
    }
    const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length
    const replaceStart = mentionTriggerIndex.value >= 0 ? mentionTriggerIndex.value : cursor
    const before = inputMessage.value.slice(0, replaceStart)
    const after = inputMessage.value.slice(cursor)
    inputMessage.value = `${before}@${candidate.insertToken} ${after}`
    closeMentionMenu()
    nextTick(() => {
      if (!textareaRef.value) {
        return
      }
      const nextCursor = before.length + candidate.insertToken.length + 2
      textareaRef.value.focus()
      textareaRef.value.setSelectionRange(nextCursor, nextCursor)
      autoResize()
    })
  }

  function handleInputKeydown(event: KeyboardEvent) {
    if (showMentionMenu.value) {
      if (event.key === 'ArrowDown') {
        event.preventDefault()
        if (mentionCandidates.value.length > 0) {
          mentionHighlightedIndex.value = (mentionHighlightedIndex.value + 1) % mentionCandidates.value.length
        }
        return
      }
      if (event.key === 'ArrowUp') {
        event.preventDefault()
        if (mentionCandidates.value.length > 0) {
          mentionHighlightedIndex.value = (mentionHighlightedIndex.value - 1 + mentionCandidates.value.length) % mentionCandidates.value.length
        }
        return
      }
      if (event.key === 'Enter' && !event.shiftKey && mentionCandidates.value.length > 0) {
        event.preventDefault()
        selectMentionCandidate(mentionCandidates.value[mentionHighlightedIndex.value] || mentionCandidates.value[0])
        return
      }
      if (event.key === 'Escape') {
        event.preventDefault()
        closeMentionMenu()
        return
      }
    }
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void options.onSend()
    }
  }

  function insertEmoji(emoji: string) {
    inputMessage.value += emoji
    showEmojiPicker.value = false
    textareaRef.value?.focus()
    nextTick(handleInputChange)
  }

  function triggerFileUpload() {
    if (!options.currentMuted.value) {
      fileInputRef.value?.click()
    }
  }

  function triggerImageUpload() {
    if (!options.currentMuted.value) {
      imageInputRef.value?.click()
    }
  }

  function hasMentionToken(content: string, token: string, ignoreCase = false) {
    if (!content || !token) {
      return false
    }
    const flags = ignoreCase ? 'i' : ''
    const pattern = new RegExp(`(^|[\\s])@${escapeRegExp(token)}(?=$|[\\s,锛岋拷?锟?锟?锟?锟?])`, flags)
    return pattern.test(content)
  }

  function resolveOutgoingMentions(content: string) {
    if (!options.isGroupSession.value) {
      return {
        mentionAll: false,
        mentionUserIds: [] as string[],
        mentionDisplayNames: [] as string[]
      }
    }
    const mentionUserIds: string[] = []
    const mentionDisplayNames: string[] = []
    for (const member of options.groupDetail.value?.members || []) {
      if (String(member.userId) === String(options.getCurrentUserId())) {
        continue
      }
      if (!hasMentionToken(content, getMemberMentionToken(member))) {
        continue
      }
      mentionUserIds.push(String(member.userId))
      mentionDisplayNames.push(member.nickname || member.username || `成员${member.userId}`)
    }
    return {
      mentionAll: hasMentionToken(content, '所有人') || hasMentionToken(content, 'all', true),
      mentionUserIds,
      mentionDisplayNames
    }
  }

  return {
    inputMessage,
    showEmojiPicker,
    showMentionMenu,
    mentionHighlightedIndex,
    mentionCandidates,
    textareaRef,
    fileInputRef,
    imageInputRef,
    emojiRef,
    mentionMenuRef,
    setTextareaRef,
    setEmojiRef,
    setMentionMenuRef,
    autoResize,
    closeMentionMenu,
    syncMentionMenu,
    handleInputChange,
    selectMentionCandidate,
    handleInputKeydown,
    insertEmoji,
    triggerFileUpload,
    triggerImageUpload,
    resolveOutgoingMentions
  }
}
