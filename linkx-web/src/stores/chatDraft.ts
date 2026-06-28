/**
 * 内存中的会话草稿，与 chatDraftSync 配合持久化到服务端。
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { buildSessionKey } from '../utils/chat'
import { loadDraftsFromServer } from '../utils/chatDraftSync'

export const useChatDraftStore = defineStore('chatDraft', () => {
  const draftsByKey = ref<Record<string, string>>({})

  function getDraft(targetId: string | number, sessionType: number): string {
    return draftsByKey.value[buildSessionKey(targetId, sessionType)] || ''
  }

  function setDraft(targetId: string | number, sessionType: number, content: string) {
    const key = buildSessionKey(targetId, sessionType)
    const trimmed = content.trim()
    if (!trimmed) {
      const next = { ...draftsByKey.value }
      delete next[key]
      draftsByKey.value = next
      return
    }
    draftsByKey.value = { ...draftsByKey.value, [key]: trimmed }
  }

  function clearDraft(targetId: string | number, sessionType: number) {
    setDraft(targetId, sessionType, '')
  }

  async function hydrateFromServer() {
    draftsByKey.value = await loadDraftsFromServer()
  }

  function reset() {
    draftsByKey.value = {}
  }

  return {
    draftsByKey,
    getDraft,
    setDraft,
    clearDraft,
    hydrateFromServer,
    reset
  }
})