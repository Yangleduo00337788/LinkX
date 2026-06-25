/**
 * 桌面端全局能力：系统通知跳转、托盘菜单动作。
 */
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getElectronAPI } from '../utils/electron'
import { navigateToChatSession } from '../utils/chatNavigation'

export function useDesktopIntegration() {
  const router = useRouter()

  onMounted(() => {
    const api = getElectronAPI()
    if (!api) {
      return
    }

    api.onNotificationNavigate(data => {
      if (!data?.targetId) {
        return
      }
      void navigateToChatSession(router, {
        targetId: String(data.targetId),
        sessionType: data.sessionType,
        messageId: data.messageId
      })
    })

    api.onTrayAction(data => {
      if (data?.action === 'open-chat') {
        void router.push('/chat')
      }
    })
  })

  onUnmounted(() => {
    const api = getElectronAPI()
    api?.removeNotificationNavigateListener?.()
    api?.removeTrayActionListener?.()
  })
}