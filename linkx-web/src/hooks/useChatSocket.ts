/**
 * 封装聊天 WebSocket 连接、心跳、重连与命令请求响应。
 */
import { onUnmounted, ref } from 'vue'  // 行注：引入 onUnmounted, ref 能力
import { WS_BASE_URL } from '../config/env'  // 行注：引入 WS_BASE_URL 能力

interface SocketOptions {  // 行注：开始当前逻辑块
  token: () => string  // 行注：传入 token 回调
  createTicket: () => Promise<string>  // 行注：传入 createTicket 回调
  onMessage: (payload: any) => void  // 行注：设置 onMessage 配置项
  onOpen?: () => void  // 行注：执行当前调用逻辑
  onClose?: () => void  // 行注：执行当前调用逻辑
}  // 行注：结束当前代码块

interface PendingRequest {  // 行注：开始当前逻辑块
  resolve: (value: any) => void  // 行注：设置 resolve 配置项
  reject: (reason?: unknown) => void  // 行注：设置 reject 配置项
  timer: ReturnType<typeof setTimeout>  // 行注：设置 timer 配置项
}  // 行注：结束当前代码块

interface Waiter {  // 行注：开始当前逻辑块
  resolve: () => void  // 行注：传入 resolve 回调
  reject: (reason?: unknown) => void  // 行注：设置 reject 配置项
}  // 行注：结束当前代码块

export function useChatSocket(options: SocketOptions) {  // 行注：导出当前能力
  const socket = ref<WebSocket | null>(null)  // 行注：初始化 socket 变量
  const connected = ref(false)  // 行注：初始化 connected 响应式状态
  const connecting = ref(false)  // 行注：初始化 connecting 响应式状态
  const reconnectAttempt = ref(0)  // 行注：初始化 reconnectAttempt 响应式状态
  const reconnectDelays = [1000, 2000, 5000, 10000, 30000]  // 行注：初始化 reconnectDelays 变量
  const pendingRequests = new Map<string, PendingRequest>()  // 行注：初始化 pendingRequests 集合
  const openWaiters = new Set<Waiter>()  // 行注：初始化 openWaiters 方法

  let reconnectTimer: ReturnType<typeof setTimeout> | null = null  // 行注：初始化 reconnectTimer 变量
  let heartbeatTimer: ReturnType<typeof setTimeout> | null = null  // 行注：初始化 heartbeatTimer 变量
  let heartbeatTimeoutTimer: ReturnType<typeof setTimeout> | null = null  // 行注：初始化 heartbeatTimeoutTimer 变量
  let reconnectAttempts = 0  // 行注：初始化 reconnectAttempts 变量
  let manualClose = false  // 行注：初始化 manualClose 变量
  let requestSequence = 0  // 行注：初始化 requestSequence 变量

  // 切换连接状态前先清掉旧的重连定时器，避免并发重复重连。
  function clearReconnectTimer() {  // 行注：定义 clearReconnectTimer 方法
    if (reconnectTimer) {  // 行注：判断当前条件是否成立
      clearTimeout(reconnectTimer)  // 行注：调用 clearTimeout 方法
      reconnectTimer = null  // 行注：更新 reconnectTimer 值
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  // 每次收到心跳响应或连接关闭时都要重置心跳相关定时器。
  function clearHeartbeatTimers() {  // 行注：定义 clearHeartbeatTimers 方法
    if (heartbeatTimer) {  // 行注：判断当前条件是否成立
      clearTimeout(heartbeatTimer)  // 行注：调用 clearTimeout 方法
      heartbeatTimer = null  // 行注：更新 heartbeatTimer 值
    }  // 行注：结束当前代码块
    if (heartbeatTimeoutTimer) {  // 行注：判断当前条件是否成立
      clearTimeout(heartbeatTimeoutTimer)  // 行注：调用 clearTimeout 方法
      heartbeatTimeoutTimer = null  // 行注：更新 heartbeatTimeoutTimer 值
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  // 断线后按退避时间表自动重连，人工主动断开时不再触发。
  function scheduleReconnect() {  // 行注：定义 scheduleReconnect 方法
    clearReconnectTimer()  // 行注：调用 clearReconnectTimer 方法
    if (manualClose) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const nextDelay = reconnectDelays[Math.min(reconnectAttempts, reconnectDelays.length - 1)]  // 行注：初始化 nextDelay 变量
    reconnectTimer = setTimeout(() => {  // 行注：执行当前调用逻辑
      reconnectAttempts += 1  // 行注：补充当前表达式
      reconnectAttempt.value = reconnectAttempts  // 行注：更新 reconnectAttempt 状态
      connect()  // 行注：调用 connect 方法
    }, nextDelay)  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  function createSocketError(message: string, payload?: any) {  // 行注：定义 createSocketError 方法
    const error: any = new Error(message)  // 行注：初始化 error 变量
    if (payload) {  // 行注：判断当前条件是否成立
      error.code = payload.code  // 行注：更新 error.code 值
      error.response = {  // 行注：更新 error.response 值
        data: payload  // 行注：设置 data 配置项
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
    return error  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  function resolveOpenWaiters() {  // 行注：定义 resolveOpenWaiters 方法
    for (const waiter of openWaiters) {  // 行注：遍历当前数据集合
      waiter.resolve()  // 行注：调用 resolve 方法
    }  // 行注：结束当前代码块
    openWaiters.clear()  // 行注：调用 clear 方法
  }  // 行注：结束当前代码块

  function rejectOpenWaiters(error: Error) {  // 行注：定义 rejectOpenWaiters 方法
    for (const waiter of openWaiters) {  // 行注：遍历当前数据集合
      waiter.reject(error)  // 行注：调用 reject 方法
    }  // 行注：结束当前代码块
    openWaiters.clear()  // 行注：调用 clear 方法
  }  // 行注：结束当前代码块

  function rejectPendingRequests(error: Error) {  // 行注：定义 rejectPendingRequests 方法
    for (const [requestId, pendingRequest] of pendingRequests.entries()) {  // 行注：遍历当前数据集合
      clearTimeout(pendingRequest.timer)  // 行注：调用 clearTimeout 方法
      pendingRequest.reject(error)  // 行注：调用 reject 方法
      pendingRequests.delete(requestId)  // 行注：调用 delete 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  // 将服务端返回的命令执行结果回填到对应请求 Promise，形成 request-response 模式。
  function handleCommandResult(result: any) {  // 行注：定义 handleCommandResult 方法
    const requestId = result?.requestId ? String(result.requestId) : ''  // 行注：初始化 requestId 状态
    if (!requestId) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    const pendingRequest = pendingRequests.get(requestId)  // 行注：初始化 pendingRequest 变量
    if (!pendingRequest) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    clearTimeout(pendingRequest.timer)  // 行注：调用 clearTimeout 方法
    pendingRequests.delete(requestId)  // 行注：调用 delete 方法
    if (result.success) {  // 行注：判断当前条件是否成立
      pendingRequest.resolve({  // 行注：开始当前逻辑块
        code: result.code ?? 200,  // 行注：设置 code 配置项
        message: result.message || 'success',  // 行注：设置 message 配置项
        data: result.data  // 行注：设置 data 配置项
      })  // 行注：结束当前调用配置
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    pendingRequest.reject(createSocketError(result.message || '请求失败', result))  // 行注：调用 reject 方法
  }  // 行注：结束当前代码块

  // 对外发送命令前统一等待连接建立完成，避免业务层直接操作底层 readyState。
  function waitUntilConnected() {  // 行注：定义 waitUntilConnected 方法
    if (connected.value && socket.value?.readyState === WebSocket.OPEN) {  // 行注：判断当前条件是否成立
      return Promise.resolve()  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    connect()  // 行注：调用 connect 方法
    return new Promise<void>((resolve, reject) => {  // 行注：返回当前结果
      const waiter: Waiter = { resolve, reject }  // 行注：初始化 waiter 变量
      openWaiters.add(waiter)  // 行注：调用 add 方法
      setTimeout(() => {  // 行注：调用 setTimeout 方法
        if (!openWaiters.has(waiter)) {  // 行注：判断当前条件是否成立
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        openWaiters.delete(waiter)  // 行注：调用 delete 方法
        const err = createSocketError('WebSocket 连接超时')
        err.code = 'SOCKET_TIMEOUT'
        reject(err)
      }, 10000)  // 行注：补充当前表达式
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  // 定期发送 PING 并等待 PONG，超时后主动关闭连接以触发重连。
  function scheduleHeartbeat() {  // 行注：定义 scheduleHeartbeat 方法
    clearHeartbeatTimers()  // 行注：调用 clearHeartbeatTimers 方法
    heartbeatTimer = setTimeout(() => {  // 行注：执行当前调用逻辑
      const currentSocket = socket.value  // 行注：初始化 currentSocket 变量
      if (!currentSocket || currentSocket.readyState !== WebSocket.OPEN) {  // 行注：判断当前条件是否成立
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      try {  // 行注：尝试执行可能失败的逻辑
        currentSocket.send('PING')  // 行注：调用 send 方法
      } catch (error) {  // 行注：捕获并处理异常
        currentSocket.close()  // 行注：调用 close 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      heartbeatTimeoutTimer = setTimeout(() => {  // 行注：执行当前调用逻辑
        if (socket.value === currentSocket && currentSocket.readyState === WebSocket.OPEN) {  // 行注：判断当前条件是否成立
          currentSocket.close()  // 行注：调用 close 方法
        }  // 行注：结束当前代码块
      }, 10000)  // 行注：补充当前表达式
    }, 20000)  // 行注：补充当前表达式
  }  // 行注：结束当前代码块

  // 建立 WebSocket 连接前先向后端申请短期 ticket，避免直接在 URL 中暴露长期 token。
  async function connect() {  // 行注：定义异步 connect 方法
    const token = options.token()  // 行注：初始化 token 变量
    if (!token || connecting.value || connected.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    clearReconnectTimer()  // 行注：调用 clearReconnectTimer 方法
    connecting.value = true  // 行注：更新 connecting 状态
    manualClose = false  // 行注：更新 manualClose 值

    let ws: WebSocket  // 行注：初始化 ws 变量
    try {  // 行注：尝试执行可能失败的逻辑
      const ticket = await options.createTicket()  // 行注：接收 ticket 异步结果
      if (!ticket) {  // 行注：判断当前条件是否成立
        throw createSocketError('WebSocket 握手票据为空')  // 行注：抛出异常并终止当前流程
      }  // 行注：结束当前代码块
      if (manualClose) {  // 行注：判断当前条件是否成立
        connecting.value = false  // 行注：更新 connecting 状态
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      const wsUrl = `${WS_BASE_URL}/ws/chat?ticket=${encodeURIComponent(ticket)}`  // 行注：初始化 wsUrl 变量
      ws = new WebSocket(wsUrl)  // 行注：更新 ws 值
      socket.value = ws  // 行注：更新 socket 状态
    } catch (error: any) {  // 行注：捕获并处理异常
      connecting.value = false  // 行注：更新 connecting 状态
      socket.value = null  // 行注：更新 socket 状态
      const connectError = error instanceof Error ? error : createSocketError(error?.message || 'WebSocket 连接失败')  // 行注：初始化 connectError 方法
      rejectOpenWaiters(connectError)  // 行注：调用 rejectOpenWaiters 方法
      rejectPendingRequests(connectError)  // 行注：调用 rejectPendingRequests 方法
      options.onClose?.()  // 行注：执行当前调用逻辑
      scheduleReconnect()  // 行注：调用 scheduleReconnect 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    ws.onopen = () => {  // 行注：执行当前调用逻辑
      connecting.value = false  // 行注：更新 connecting 状态
      connected.value = true  // 行注：更新 connected 状态
      reconnectAttempts = 0  // 行注：更新 reconnectAttempts 值
      reconnectAttempt.value = 0  // 行注：更新 reconnectAttempt 状态
      resolveOpenWaiters()  // 行注：调用 resolveOpenWaiters 方法
      scheduleHeartbeat()  // 行注：调用 scheduleHeartbeat 方法
      options.onOpen?.()  // 行注：执行当前调用逻辑
    }  // 行注：结束当前代码块

    ws.onmessage = event => {  // 行注：开始当前逻辑块
      if (event.data === 'PONG') {  // 行注：判断当前条件是否成立
        clearHeartbeatTimers()  // 行注：调用 clearHeartbeatTimers 方法
        scheduleHeartbeat()  // 行注：调用 scheduleHeartbeat 方法
        return  // 行注：返回当前结果
      }  // 行注：结束当前代码块
      try {  // 行注：尝试执行可能失败的逻辑
        const payload = JSON.parse(event.data)  // 行注：初始化 payload 变量
        if (payload?.type === 'COMMAND_RESULT') {  // 行注：判断当前条件是否成立
          handleCommandResult(payload.data)  // 行注：调用 handleCommandResult 方法
          return  // 行注：返回当前结果
        }  // 行注：结束当前代码块
        scheduleHeartbeat()  // 行注：调用 scheduleHeartbeat 方法
        options.onMessage(payload)  // 行注：调用 onMessage 方法
      } catch (error) {  // 行注：捕获并处理异常
        console.error('parse websocket message error:', error)  // 行注：输出错误日志
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    ws.onerror = () => undefined  // 行注：执行当前调用逻辑

    ws.onclose = event => {  // 行注：开始当前逻辑块
      connecting.value = false  // 行注：更新 connecting 状态
      connected.value = false  // 行注：更新 connected 状态
      clearHeartbeatTimers()  // 行注：调用 clearHeartbeatTimers 方法
      socket.value = null  // 行注：更新 socket 状态
      const closeError = createSocketError(event.reason || 'WebSocket 连接已关闭')  // 行注：初始化 closeError 方法
      rejectOpenWaiters(closeError)  // 行注：调用 rejectOpenWaiters 方法
      rejectPendingRequests(closeError)  // 行注：调用 rejectPendingRequests 方法
      options.onClose?.()  // 行注：执行当前调用逻辑
      scheduleReconnect()  // 行注：调用 scheduleReconnect 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  // 每个上行命令都带 requestId，并在本地维护超时控制与结果回调。
  async function sendRequest(action: string, data: Record<string, unknown> = {}) {  // 行注：定义异步 sendRequest 方法
    await waitUntilConnected()  // 行注：调用 waitUntilConnected 方法
    const currentSocket = socket.value  // 行注：初始化 currentSocket 变量
    if (!currentSocket || currentSocket.readyState !== WebSocket.OPEN) {  // 行注：判断当前条件是否成立
      throw createSocketError('WebSocket 未连接')  // 行注：抛出异常并终止当前流程
    }  // 行注：结束当前代码块

    requestSequence += 1  // 行注：补充当前表达式
    const requestId = `${Date.now()}-${requestSequence}`  // 行注：记录 requestId 时间戳
    const requestPayload = JSON.stringify({  // 行注：开始解构当前返回值
      requestId,  // 行注：传入 requestId 参数
      action,  // 行注：传入 action 参数
      data  // 行注：传入 data 参数
    })  // 行注：结束当前调用配置

    return new Promise<any>((resolve, reject) => {  // 行注：返回当前结果
      const timer = setTimeout(() => {  // 行注：开始解构当前返回值
        pendingRequests.delete(requestId)  // 行注：调用 delete 方法
        reject(createSocketError(`${action} 请求超时`))  // 行注：调用 reject 方法
      }, 10000)  // 行注：补充当前表达式

      pendingRequests.set(requestId, { resolve, reject, timer })  // 行注：调用 set 方法

      try {  // 行注：尝试执行可能失败的逻辑
        currentSocket.send(requestPayload)  // 行注：调用 send 方法
      } catch (error) {  // 行注：捕获并处理异常
        clearTimeout(timer)  // 行注：调用 clearTimeout 方法
        pendingRequests.delete(requestId)  // 行注：调用 delete 方法
        reject(error)  // 行注：调用 reject 方法
      }  // 行注：结束当前代码块
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块

  // 主动断开时同时清理连接状态、待处理请求和所有定时器。
  function disconnect() {  // 行注：定义 disconnect 方法
    manualClose = true  // 行注：更新 manualClose 值
    clearReconnectTimer()  // 行注：调用 clearReconnectTimer 方法
    clearHeartbeatTimers()  // 行注：调用 clearHeartbeatTimers 方法
    connecting.value = false  // 行注：更新 connecting 状态
    connected.value = false  // 行注：更新 connected 状态
    reconnectAttempt.value = 0  // 行注：更新 reconnectAttempt 状态
    if (socket.value) {  // 行注：判断当前条件是否成立
      socket.value.close()  // 行注：调用 close 方法
      socket.value = null  // 行注：更新 socket 状态
    }  // 行注：结束当前代码块
    const disconnectError = createSocketError('WebSocket 已断开')  // 行注：初始化 disconnectError 方法
    rejectOpenWaiters(disconnectError)  // 行注：调用 rejectOpenWaiters 方法
    rejectPendingRequests(disconnectError)  // 行注：调用 rejectPendingRequests 方法
  }  // 行注：结束当前代码块

  onUnmounted(() => {  // 行注：注册组件卸载时的清理逻辑
    disconnect()  // 行注：调用 disconnect 方法
  })  // 行注：结束当前调用配置

  return {  // 行注：返回当前结果
    socket,  // 行注：补充 socket 配置项
    connected,  // 行注：补充当前配置项
    connecting,  // 行注：补充当前配置项
    reconnectAttempt,  // 行注：补充当前配置项
    connect,  // 行注：补充当前配置项
    disconnect,  // 行注：补充当前配置项
    sendRequest  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
