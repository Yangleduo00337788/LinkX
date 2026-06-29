/**
 * 根据事件类型分发聊天实时消息。
 */
interface ChatRealtimePayload {  // 行注：开始当前逻辑块
  type?: string  // 行注：补充当前表达式
  data?: Record<string, any>  // 行注：补充当前表达式
}  // 行注：结束当前代码块

interface ChatRealtimeHandlers {  // 行注：开始当前逻辑块
  onConnected?: () => void  // 行注：执行当前调用逻辑
  onGroupDetail?: (detail: any) => void  // 行注：执行当前调用逻辑
  onGroupRemoved?: (payload: any) => void  // 行注：执行当前调用逻辑
  onMessage?: (message: any) => void  // 行注：执行当前调用逻辑
  onSession?: (session: any) => void  // 行注：执行当前调用逻辑
  onReadReceipt?: (payload: any) => void  // 行注：执行当前调用逻辑
  onOnlineStatus?: (payload: any) => void  // 行注：执行当前调用逻辑
  onMessageRecalled?: (message: any) => void  // 行注：执行当前调用逻辑
  onForceLogout?: (payload: { reason?: string }) => void
  onNotification?: (payload: { unreadCount?: number }) => void
}  // 行注：结束当前代码块

export function createChatRealtimeService(handlers: ChatRealtimeHandlers) {  // 行注：导出当前能力
  // 用事件类型到处理函数的映射表替代分散判断，便于后续扩展新事件。
  const routeMap: Record<string, (payload: ChatRealtimePayload) => void> = {  // 行注：开始解构当前返回值
    CONNECTED: () => {  // 行注：传入 CONNECTED 回调
      handlers.onConnected?.()  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    GROUP_DETAIL: payload => {  // 行注：设置 GROUP_DETAIL 配置项
      handlers.onGroupDetail?.(payload.data?.detail)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    GROUP_REMOVED: payload => {  // 行注：设置 GROUP_REMOVED 配置项
      handlers.onGroupRemoved?.(payload.data)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    MESSAGE: payload => {  // 行注：设置 MESSAGE 配置项
      handlers.onMessage?.(payload.data?.message)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    SESSION: payload => {  // 行注：设置 SESSION 配置项
      handlers.onSession?.(payload.data?.session)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    READ_RECEIPT: payload => {  // 行注：设置 READ_RECEIPT 配置项
      handlers.onReadReceipt?.(payload.data)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    ONLINE_STATUS: payload => {  // 行注：设置 ONLINE_STATUS 配置项
      handlers.onOnlineStatus?.(payload.data)  // 行注：执行当前调用逻辑
    },  // 行注：补充当前配置项
    MESSAGE_RECALLED: payload => {  // 行注：设置 MESSAGE_RECALLED 配置项
      handlers.onMessageRecalled?.(payload.data?.message)  // 行注：执行当前调用逻辑
    },
    FORCE_LOGOUT: payload => {
      handlers.onForceLogout?.(payload.data ?? {})
    },
    NOTIFICATION: payload => {
      handlers.onNotification?.(payload.data ?? {})
    }
  }  // 行注：结束当前代码块

  // 按服务端下发的事件类型分发到不同处理器，避免页面层直接写大量 if/else。
  function handleEvent(payload: ChatRealtimePayload | null | undefined) {  // 行注：定义 handleEvent 方法
    if (!payload?.type) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    routeMap[payload.type]?.(payload)  // 行注：执行当前调用逻辑
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    handleEvent  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
