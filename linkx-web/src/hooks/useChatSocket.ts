import { onUnmounted, ref } from 'vue'
import { WS_BASE_URL } from '../api/client'

interface SocketOptions {
  token: () => string
  onMessage: (payload: any) => void
  onOpen?: () => void
  onClose?: () => void
}

interface PendingRequest {
  resolve: (value: any) => void
  reject: (reason?: unknown) => void
  timer: ReturnType<typeof setTimeout>
}

interface Waiter {
  resolve: () => void
  reject: (reason?: unknown) => void
}

export function useChatSocket(options: SocketOptions) {
  const socket = ref<WebSocket | null>(null)
  const connected = ref(false)
  const connecting = ref(false)
  const reconnectDelays = [1000, 2000, 5000, 10000, 30000]
  const pendingRequests = new Map<string, PendingRequest>()
  const openWaiters = new Set<Waiter>()

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null
  let reconnectAttempts = 0
  let manualClose = false
  let requestSequence = 0

  function clearReconnectTimer() {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  function scheduleReconnect() {
    clearReconnectTimer()
    if (manualClose) {
      return
    }
    const nextDelay = reconnectDelays[Math.min(reconnectAttempts, reconnectDelays.length - 1)]
    reconnectTimer = setTimeout(() => {
      reconnectAttempts += 1
      connect()
    }, nextDelay)
  }

  function createSocketError(message: string, payload?: any) {
    const error: any = new Error(message)
    if (payload) {
      error.code = payload.code
      error.response = {
        data: payload
      }
    }
    return error
  }

  function resolveOpenWaiters() {
    for (const waiter of openWaiters) {
      waiter.resolve()
    }
    openWaiters.clear()
  }

  function rejectOpenWaiters(error: Error) {
    for (const waiter of openWaiters) {
      waiter.reject(error)
    }
    openWaiters.clear()
  }

  function rejectPendingRequests(error: Error) {
    for (const [requestId, pendingRequest] of pendingRequests.entries()) {
      clearTimeout(pendingRequest.timer)
      pendingRequest.reject(error)
      pendingRequests.delete(requestId)
    }
  }

  function handleCommandResult(result: any) {
    const requestId = result?.requestId ? String(result.requestId) : ''
    if (!requestId) {
      return
    }
    const pendingRequest = pendingRequests.get(requestId)
    if (!pendingRequest) {
      return
    }
    clearTimeout(pendingRequest.timer)
    pendingRequests.delete(requestId)
    if (result.success) {
      pendingRequest.resolve({
        code: result.code ?? 200,
        message: result.message || 'success',
        data: result.data
      })
      return
    }
    pendingRequest.reject(createSocketError(result.message || '请求失败', result))
  }

  function waitUntilConnected() {
    if (connected.value && socket.value?.readyState === WebSocket.OPEN) {
      return Promise.resolve()
    }
    connect()
    return new Promise<void>((resolve, reject) => {
      const waiter: Waiter = { resolve, reject }
      openWaiters.add(waiter)
      setTimeout(() => {
        if (!openWaiters.has(waiter)) {
          return
        }
        openWaiters.delete(waiter)
        reject(createSocketError('WebSocket 连接超时'))
      }, 10000)
    })
  }

  function connect() {
    const token = options.token()
    if (!token || connecting.value || connected.value) {
      return
    }

    clearReconnectTimer()
    connecting.value = true
    manualClose = false

    const wsUrl = `${WS_BASE_URL}/ws/chat?token=${encodeURIComponent(token)}`
    const ws = new WebSocket(wsUrl)
    socket.value = ws

    ws.onopen = () => {
      connecting.value = false
      connected.value = true
      reconnectAttempts = 0
      resolveOpenWaiters()
      options.onOpen?.()
    }

    ws.onmessage = event => {
      try {
        const payload = JSON.parse(event.data)
        if (payload?.type === 'COMMAND_RESULT') {
          handleCommandResult(payload.data)
          return
        }
        options.onMessage(payload)
      } catch (error) {
        console.error('parse websocket message error:', error)
      }
    }

    ws.onerror = () => undefined

    ws.onclose = event => {
      connecting.value = false
      connected.value = false
      socket.value = null
      const closeError = createSocketError(event.reason || 'WebSocket 连接已关闭')
      rejectOpenWaiters(closeError)
      rejectPendingRequests(closeError)
      options.onClose?.()
      scheduleReconnect()
    }
  }

  async function sendRequest(action: string, data: Record<string, unknown> = {}) {
    await waitUntilConnected()
    const currentSocket = socket.value
    if (!currentSocket || currentSocket.readyState !== WebSocket.OPEN) {
      throw createSocketError('WebSocket 未连接')
    }

    requestSequence += 1
    const requestId = `${Date.now()}-${requestSequence}`
    const requestPayload = JSON.stringify({
      requestId,
      action,
      data
    })

    return new Promise<any>((resolve, reject) => {
      const timer = setTimeout(() => {
        pendingRequests.delete(requestId)
        reject(createSocketError(`${action} 请求超时`))
      }, 10000)

      pendingRequests.set(requestId, { resolve, reject, timer })

      try {
        currentSocket.send(requestPayload)
      } catch (error) {
        clearTimeout(timer)
        pendingRequests.delete(requestId)
        reject(error)
      }
    })
  }

  function disconnect() {
    manualClose = true
    clearReconnectTimer()
    connecting.value = false
    connected.value = false
    if (socket.value) {
      socket.value.close()
      socket.value = null
    }
    const disconnectError = createSocketError('WebSocket 已断开')
    rejectOpenWaiters(disconnectError)
    rejectPendingRequests(disconnectError)
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    socket,
    connected,
    connecting,
    connect,
    disconnect,
    sendRequest
  }
}
