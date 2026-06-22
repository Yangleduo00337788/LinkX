import { defineStore } from 'pinia'
import { ref } from 'vue'
import { SESSION_TYPE_SINGLE, type ChatSession, type DisplayMessage, type FriendItem } from '../types/chat'

export const useChatStore = defineStore('chat', () => {
  const sessions = ref<ChatSession[]>([])
  const messages = ref<DisplayMessage[]>([])
  const friends = ref<FriendItem[]>([])
  const currentTargetId = ref<string | null>(null)
  const currentSessionType = ref<number>(SESSION_TYPE_SINGLE)
  const searchText = ref('')

  function resetConversation() {
    currentTargetId.value = null
    currentSessionType.value = SESSION_TYPE_SINGLE
    messages.value = []
  }

  function resetChatState() {
    sessions.value = []
    messages.value = []
    friends.value = []
    currentTargetId.value = null
    currentSessionType.value = SESSION_TYPE_SINGLE
    searchText.value = ''
  }

  return {
    sessions,
    messages,
    friends,
    currentTargetId,
    currentSessionType,
    searchText,
    resetConversation,
    resetChatState
  }
})
