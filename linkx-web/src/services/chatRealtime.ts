interface ChatRealtimePayload {
  type?: string
  data?: Record<string, any>
}

interface ChatRealtimeHandlers {
  onConnected?: () => void
  onGroupDetail?: (detail: any) => void
  onGroupRemoved?: (payload: any) => void
  onMessage?: (message: any) => void
  onSession?: (session: any) => void
  onReadReceipt?: (payload: any) => void
  onOnlineStatus?: (payload: any) => void
  onMessageRecalled?: (message: any) => void
}

export function createChatRealtimeService(handlers: ChatRealtimeHandlers) {
  const routeMap: Record<string, (payload: ChatRealtimePayload) => void> = {
    CONNECTED: () => {
      handlers.onConnected?.()
    },
    GROUP_DETAIL: payload => {
      handlers.onGroupDetail?.(payload.data?.detail)
    },
    GROUP_REMOVED: payload => {
      handlers.onGroupRemoved?.(payload.data)
    },
    MESSAGE: payload => {
      handlers.onMessage?.(payload.data?.message)
    },
    SESSION: payload => {
      handlers.onSession?.(payload.data?.session)
    },
    READ_RECEIPT: payload => {
      handlers.onReadReceipt?.(payload.data)
    },
    ONLINE_STATUS: payload => {
      handlers.onOnlineStatus?.(payload.data)
    },
    MESSAGE_RECALLED: payload => {
      handlers.onMessageRecalled?.(payload.data?.message)
    }
  }

  function handleEvent(payload: ChatRealtimePayload | null | undefined) {
    if (!payload?.type) {
      return
    }
    routeMap[payload.type]?.(payload)
  }

  return {
    handleEvent
  }
}
