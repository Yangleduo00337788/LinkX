/**
 * 处理消息输入框、表情、提及与上传入口等输入行为。
 */
import { computed, nextTick, ref, type ComponentPublicInstance, type ComputedRef, type Ref } from 'vue'  // 行注：引入 computed, nextTick, ref, type ComponentPublicInstance, type ComputedRef, type Ref 能力
import type { GroupDetail, GroupMember, MentionCandidate } from '../types/chat'  // 行注：引入 type { GroupDetail, GroupMember, MentionCandidate } 模块
import { escapeRegExp } from '../utils/chat'  // 行注：引入 escapeRegExp 能力

interface UseChatComposerOptions {  // 行注：开始当前逻辑块
  currentMuted: ComputedRef<boolean>  // 行注：设置 currentMuted 配置项
  isGroupSession: ComputedRef<boolean>  // 行注：设置 isGroupSession 配置项
  canMentionAll: ComputedRef<boolean>  // 行注：设置 canMentionAll 配置项
  groupDetail: Ref<GroupDetail | null>  // 行注：设置 groupDetail 配置项
  getCurrentUserId: () => string | number  // 行注：传入 getCurrentUserId 回调
  onSend: () => Promise<void> | void  // 行注：传入 onSend 回调
}  // 行注：结束当前代码块

export function useChatComposer(options: UseChatComposerOptions) {  // 行注：导出当前能力
  const inputMessage = ref('')  // 行注：初始化 inputMessage 响应式状态
  const showEmojiPicker = ref(false)  // 行注：初始化 showEmojiPicker 响应式状态
  const showMentionMenu = ref(false)  // 行注：初始化 showMentionMenu 响应式状态
  const mentionQuery = ref('')  // 行注：初始化 mentionQuery 响应式状态
  const mentionTriggerIndex = ref(-1)  // 行注：初始化 mentionTriggerIndex 响应式状态
  const mentionHighlightedIndex = ref(0)  // 行注：初始化 mentionHighlightedIndex 响应式状态
  const textareaRef = ref<HTMLTextAreaElement>()  // 行注：初始化 textareaRef 状态
  const fileInputRef = ref<HTMLInputElement>()  // 行注：初始化 fileInputRef 状态
  const imageInputRef = ref<HTMLInputElement>()  // 行注：初始化 imageInputRef 状态
  const emojiRef = ref<HTMLElement>()  // 行注：初始化 emojiRef 状态
  const mentionMenuRef = ref<HTMLElement>()  // 行注：初始化 mentionMenuRef 状态

  function setTextareaRef(element: Element | ComponentPublicInstance | null) {  // 行注：定义 setTextareaRef 方法
    textareaRef.value = element instanceof HTMLTextAreaElement ? element : undefined  // 行注：更新 textareaRef 状态
  }  // 行注：结束当前代码块

  function setEmojiRef(element: Element | ComponentPublicInstance | null) {  // 行注：定义 setEmojiRef 方法
    emojiRef.value = element instanceof HTMLElement ? element : undefined  // 行注：更新 emojiRef 状态
  }  // 行注：结束当前代码块

  function setMentionMenuRef(element: Element | ComponentPublicInstance | null) {  // 行注：定义 setMentionMenuRef 方法
    mentionMenuRef.value = element instanceof HTMLElement ? element : undefined  // 行注：更新 mentionMenuRef 状态
  }  // 行注：结束当前代码块

  function autoResize() {  // 行注：定义 autoResize 方法
    if (!textareaRef.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    textareaRef.value.style.height = 'auto'  // 行注：更新 textareaRef.value.style.height 值
    textareaRef.value.style.height = `${Math.min(textareaRef.value.scrollHeight, 120)}px`  // 行注：更新 textareaRef.value.style.height 值
  }  // 行注：结束当前代码块

  function getMemberMentionToken(member: GroupMember) {  // 行注：定义 getMemberMentionToken 方法
    const nickname = member.nickname?.trim()  // 行注：初始化 nickname 变量
    if (nickname) {  // 行注：判断当前条件是否成立
      const duplicateCount = (options.groupDetail.value?.members || []).filter(item => item.nickname?.trim() === nickname).length  // 行注：初始化 duplicateCount 变量
      if (duplicateCount === 1) {  // 行注：判断当前条件是否成立
        return nickname  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    return member.username?.trim() || nickname || `成员${member.userId}`  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  const mentionMenuBaseOptions = computed<MentionCandidate[]>(() => {  // 行注：开始解构当前返回值
    if (!options.isGroupSession.value) {  // 行注：判断当前条件是否成立
      return []  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextOptions: MentionCandidate[] = []  // 行注：初始化 nextOptions 变量
    if (options.canMentionAll.value) {  // 行注：判断当前条件是否成立
      nextOptions.push({  // 行注：开始当前逻辑块
        key: 'all',  // 行注：设置 key 配置项
        label: '所有人',  // 行注：设置 label 配置项
        meta: '仅群主和管理员可用',  // 行注：设置 meta 配置项
        insertToken: '所有人',  // 行注：设置 insertToken 配置项
        isAll: true  // 行注：设置 isAll 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    for (const member of options.groupDetail.value?.members || []) {  // 行注：遍历当前数据集合
      if (String(member.userId) === String(options.getCurrentUserId())) {  // 行注：判断当前条件是否成立
        continue  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
      const displayName = member.nickname || member.username || `成员${member.userId}`  // 行注：初始化 displayName 变量
      nextOptions.push({  // 行注：开始当前逻辑块
        key: `user-${member.userId}`,  // 行注：设置 key 配置项
        label: displayName,  // 行注：设置 label 配置项
        meta: `@${member.username}`,  // 行注：设置 meta 配置项
        insertToken: getMemberMentionToken(member),  // 行注：设置 insertToken 配置项
        mentionUserId: String(member.userId),  // 行注：设置 mentionUserId 配置项
        isAll: false  // 行注：设置 isAll 配置项
      })  // 行注：结束当前调用配置
    }  // 行注：结束当前代码块
    return nextOptions  // 行注：返回当前结果
  })  // 行注：结束当前调用配置

  const mentionCandidates = computed(() => {  // 行注：开始解构当前返回值
    const keyword = mentionQuery.value.trim().toLowerCase()  // 行注：初始化 keyword 状态
    if (!keyword) {  // 行注：判断当前条件是否成立
      return mentionMenuBaseOptions.value  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return mentionMenuBaseOptions.value.filter(candidate => {  // 行注：返回当前结果
      const text = `${candidate.label} ${candidate.meta} ${candidate.insertToken}`.toLowerCase()  // 行注：初始化 text 变量
      return text.includes(keyword)  // 行注：返回当前结果
    })  // 行注：结束当前调用配置
  })  // 行注：结束当前调用配置

  function closeMentionMenu() {  // 行注：定义 closeMentionMenu 方法
    showMentionMenu.value = false  // 行注：更新 showMentionMenu 状态
    mentionQuery.value = ''  // 行注：更新 mentionQuery 状态
    mentionTriggerIndex.value = -1  // 行注：更新 mentionTriggerIndex 状态
    mentionHighlightedIndex.value = 0  // 行注：更新 mentionHighlightedIndex 状态
  }  // 行注：结束当前代码块

  function getMentionTrigger(text: string, cursor: number) {  // 行注：定义 getMentionTrigger 方法
    const beforeCursor = text.slice(0, cursor)  // 行注：初始化 beforeCursor 变量
    const atIndex = beforeCursor.lastIndexOf('@')  // 行注：初始化 atIndex 变量
    if (atIndex === -1) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const beforeChar = atIndex > 0 ? beforeCursor[atIndex - 1] : ''  // 行注：初始化 beforeChar 变量
    if (beforeChar && !/\s/.test(beforeChar)) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const query = beforeCursor.slice(atIndex + 1)  // 行注：初始化 query 变量
    if (/[\s@]/.test(query)) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return {  // 行注：返回当前结果
      atIndex,  // 行注：补充 atIndex 配置项
      query  // 行注：补充当前表达式
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function syncMentionMenu() {  // 行注：定义 syncMentionMenu 方法
    if (!options.isGroupSession.value || !textareaRef.value || options.currentMuted.value) {  // 行注：判断当前条件是否成立
      closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length  // 行注：初始化 cursor 变量
    const trigger = getMentionTrigger(inputMessage.value, cursor)  // 行注：初始化 trigger 方法
    if (!trigger) {  // 行注：判断当前条件是否成立
      closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    mentionTriggerIndex.value = trigger.atIndex  // 行注：更新 mentionTriggerIndex 状态
    mentionQuery.value = trigger.query  // 行注：更新 mentionQuery 状态
    mentionHighlightedIndex.value = 0  // 行注：更新 mentionHighlightedIndex 状态
    showMentionMenu.value = true  // 行注：更新 showMentionMenu 状态
  }  // 行注：结束当前代码块

  function handleInputChange() {  // 行注：定义 handleInputChange 方法
    autoResize()  // 行注：调用 autoResize 方法
    syncMentionMenu()  // 行注：调用 syncMentionMenu 方法
  }  // 行注：结束当前代码块

  function selectMentionCandidate(candidate: MentionCandidate) {  // 行注：定义 selectMentionCandidate 方法
    if (!textareaRef.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const cursor = textareaRef.value.selectionStart ?? inputMessage.value.length  // 行注：初始化 cursor 变量
    const replaceStart = mentionTriggerIndex.value >= 0 ? mentionTriggerIndex.value : cursor  // 行注：初始化 replaceStart 变量
    const before = inputMessage.value.slice(0, replaceStart)  // 行注：初始化 before 变量
    const after = inputMessage.value.slice(cursor)  // 行注：初始化 after 变量
    inputMessage.value = `${before}@${candidate.insertToken} ${after}`  // 行注：更新 inputMessage 状态
    closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
    nextTick(() => {  // 行注：调用 nextTick 方法
      if (!textareaRef.value) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const nextCursor = before.length + candidate.insertToken.length + 2  // 行注：初始化 nextCursor 变量
      textareaRef.value.focus()  // 行注：调用 focus 方法
      textareaRef.value.setSelectionRange(nextCursor, nextCursor)  // 行注：调用 setSelectionRange 方法
      autoResize()  // 行注：调用 autoResize 方法
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  function handleInputKeydown(event: KeyboardEvent) {  // 行注：定义 handleInputKeydown 方法
    if (showMentionMenu.value) {  // 行注：判断当前条件是否成立
      if (event.key === 'ArrowDown') {  // 行注：判断当前条件是否成立
        event.preventDefault()  // 行注：调用 preventDefault 方法
        if (mentionCandidates.value.length > 0) {  // 行注：判断当前条件是否成立
          mentionHighlightedIndex.value = (mentionHighlightedIndex.value + 1) % mentionCandidates.value.length  // 行注：更新 mentionHighlightedIndex 状态
        }  // 行注：结束当前代码块
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (event.key === 'ArrowUp') {  // 行注：判断当前条件是否成立
        event.preventDefault()  // 行注：调用 preventDefault 方法
        if (mentionCandidates.value.length > 0) {  // 行注：判断当前条件是否成立
          mentionHighlightedIndex.value = (mentionHighlightedIndex.value - 1 + mentionCandidates.value.length) % mentionCandidates.value.length  // 行注：更新 mentionHighlightedIndex 状态
        }  // 行注：结束当前代码块
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (event.key === 'Enter' && !event.shiftKey && mentionCandidates.value.length > 0) {  // 行注：判断当前条件是否成立
        event.preventDefault()  // 行注：调用 preventDefault 方法
        selectMentionCandidate(mentionCandidates.value[mentionHighlightedIndex.value] || mentionCandidates.value[0])  // 行注：调用 selectMentionCandidate 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      if (event.key === 'Escape') {  // 行注：判断当前条件是否成立
        event.preventDefault()  // 行注：调用 preventDefault 方法
        closeMentionMenu()  // 行注：调用 closeMentionMenu 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    if (event.key === 'Enter' && !event.shiftKey) {  // 行注：判断当前条件是否成立
      event.preventDefault()  // 行注：调用 preventDefault 方法
      void options.onSend()  // 行注：调用 onSend 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function insertEmoji(emoji: string) {  // 行注：定义 insertEmoji 方法
    inputMessage.value += emoji  // 行注：补充当前表达式
    showEmojiPicker.value = false  // 行注：更新 showEmojiPicker 状态
    textareaRef.value?.focus()  // 行注：调用 focus 方法
    nextTick(handleInputChange)  // 行注：调用 nextTick 方法
  }  // 行注：结束当前代码块

  function triggerFileUpload() {  // 行注：定义 triggerFileUpload 方法
    if (!options.currentMuted.value) {  // 行注：判断当前条件是否成立
      fileInputRef.value?.click()  // 行注：调用 click 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function triggerImageUpload() {  // 行注：定义 triggerImageUpload 方法
    if (!options.currentMuted.value) {  // 行注：判断当前条件是否成立
      imageInputRef.value?.click()  // 行注：调用 click 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function hasMentionToken(content: string, token: string, ignoreCase = false) {  // 行注：定义 hasMentionToken 方法
    if (!content || !token) {  // 行注：判断当前条件是否成立
      return false  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const flags = ignoreCase ? 'i' : ''  // 行注：初始化 flags 变量
    const pattern = new RegExp(`(^|[\\s])@${escapeRegExp(token)}(?=$|[\\s,锛岋拷?锟?锟?锟?锟?])`, flags)  // 行注：初始化 pattern 变量
    return pattern.test(content)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function resolveOutgoingMentions(content: string) {  // 行注：定义 resolveOutgoingMentions 方法
    if (!options.isGroupSession.value) {  // 行注：判断当前条件是否成立
      return {  // 行注：返回当前结果
        mentionAll: false,  // 行注：设置 mentionAll 配置项
        mentionUserIds: [] as string[],  // 行注：设置 mentionUserIds 配置项
        mentionDisplayNames: [] as string[]  // 行注：设置 mentionDisplayNames 配置项
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    const mentionUserIds: string[] = []  // 行注：初始化 mentionUserIds 变量
    const mentionDisplayNames: string[] = []  // 行注：初始化 mentionDisplayNames 变量
    for (const member of options.groupDetail.value?.members || []) {  // 行注：遍历当前数据集合
      if (String(member.userId) === String(options.getCurrentUserId())) {  // 行注：判断当前条件是否成立
        continue  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
      if (!hasMentionToken(content, getMemberMentionToken(member))) {  // 行注：判断当前条件是否成立
        continue  // 行注：补充当前表达式
      }  // 行注：结束当前代码块
      mentionUserIds.push(String(member.userId))  // 行注：跳转到目标路由
      mentionDisplayNames.push(member.nickname || member.username || `成员${member.userId}`)  // 行注：跳转到目标路由
    }  // 行注：结束当前代码块
    return {  // 行注：返回当前结果
      mentionAll: hasMentionToken(content, '所有人') || hasMentionToken(content, 'all', true),  // 行注：设置 mentionAll 配置项
      mentionUserIds,  // 行注：补充当前配置项
      mentionDisplayNames  // 行注：补充当前表达式
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    inputMessage,  // 行注：补充 inputMessage 配置项
    showEmojiPicker,  // 行注：补充当前配置项
    showMentionMenu,  // 行注：补充当前配置项
    mentionHighlightedIndex,  // 行注：补充当前配置项
    mentionCandidates,  // 行注：补充当前配置项
    textareaRef,  // 行注：补充当前配置项
    fileInputRef,  // 行注：补充当前配置项
    imageInputRef,  // 行注：补充当前配置项
    emojiRef,  // 行注：补充当前配置项
    mentionMenuRef,  // 行注：补充当前配置项
    setTextareaRef,  // 行注：补充当前配置项
    setEmojiRef,  // 行注：补充当前配置项
    setMentionMenuRef,  // 行注：补充当前配置项
    autoResize,  // 行注：补充当前配置项
    closeMentionMenu,  // 行注：补充当前配置项
    syncMentionMenu,  // 行注：补充当前配置项
    handleInputChange,  // 行注：补充当前配置项
    selectMentionCandidate,  // 行注：补充当前配置项
    handleInputKeydown,  // 行注：补充当前配置项
    insertEmoji,  // 行注：补充当前配置项
    triggerFileUpload,  // 行注：补充当前配置项
    triggerImageUpload,  // 行注：补充当前配置项
    resolveOutgoingMentions  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
