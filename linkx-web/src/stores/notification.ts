import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '../api/client'

export const useNotificationStore = defineStore('notification', () => {
  const unreadCount = ref(0)

  async function refreshUnread() {
    try {
      const res: any = await notificationApi.unreadCount()
      const n = res.data?.data?.unreadCount ?? res.data?.unreadCount ?? 0
      unreadCount.value = Number(n) || 0
    } catch {
      /* ignore */
    }
  }

  function setUnreadFromWs(count: number) {
    if (Number.isFinite(count) && count >= 0) {
      unreadCount.value = count
    }
  }

  return { unreadCount, refreshUnread, setUnreadFromWs }
})