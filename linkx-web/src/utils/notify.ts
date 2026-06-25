/**
 * 统一处理系统通知与应用内提示。
 */
import { getElectronAPI } from './electron'  // 行注：引入 getElectronAPI 能力

type NotificationSoundType = 'message' | 'attention'  // 行注：补充当前表达式
const IN_APP_NOTIFICATION_EVENT = 'linkx-in-app-notification'  // 行注：初始化 IN_APP_NOTIFICATION_EVENT 变量

let audioContext: AudioContext | null = null  // 行注：初始化 audioContext 变量
let soundThrottleTimer = 0  // 行注：初始化 soundThrottleTimer 变量

export interface InAppNotificationPayload {  // 行注：导出当前能力
  id: string  // 行注：设置 id 配置项
  title: string  // 行注：设置 title 配置项
  body: string  // 行注：设置 body 配置项
  icon?: string  // 行注：补充当前表达式
  targetId?: string  // 行注：补充当前表达式
  sessionType?: number  // 行注：补充当前表达式
  messageId?: string  // 行注：补充当前表达式
  attention?: boolean  // 行注：补充当前表达式
}  // 行注：结束当前代码块

type InAppNotificationEvent =  // 行注：补充当前表达式
  | {  // 行注：开始当前逻辑块
    type: 'push'  // 行注：设置 type 配置项
    payload: InAppNotificationPayload  // 行注：设置 payload 配置项
  }  // 行注：结束当前代码块
  | {  // 行注：开始当前逻辑块
    type: 'removeByMessageIds'  // 行注：设置 type 配置项
    messageIds: string[]  // 行注：设置 messageIds 配置项
  }  // 行注：结束当前代码块

function emitInAppNotification(event: InAppNotificationEvent) {  // 行注：定义 emitInAppNotification 方法
  if (typeof window === 'undefined') {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  window.dispatchEvent(new CustomEvent(IN_APP_NOTIFICATION_EVENT, {  // 行注：开始当前逻辑块
    detail: event  // 行注：设置 detail 配置项
  }))  // 行注：补充当前表达式
}  // 行注：结束当前代码块

function buildNotificationId() {  // 行注：定义 buildNotificationId 方法
  return `notify-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`  // 行注：返回当前结果
}  // 行注：结束当前代码块

export async function showNotification(  // 行注：导出当前能力
  title: string,  // 行注：设置 title 配置项
  body: string,  // 行注：设置 body 配置项
  icon?: string,  // 行注：补充当前配置项
  payload: Omit<InAppNotificationPayload, 'id' | 'title' | 'body' | 'icon'> = {}  // 行注：设置 payload 配置项
): Promise<boolean> {  // 行注：开始当前逻辑块
  emitInAppNotification({  // 行注：开始当前逻辑块
    type: 'push',  // 行注：设置 type 配置项
    payload: {  // 行注：设置 payload 配置项
      id: buildNotificationId(),  // 行注：设置 id 配置项
      title,  // 行注：传入 title 参数
      body,  // 行注：传入 body 参数
      icon,  // 行注：传入 icon 参数
      ...payload  // 行注：补充当前表达式
    }  // 行注：结束当前代码块
  })  // 行注：结束当前调用配置
  const api = getElectronAPI()  // 行注：初始化 api 实例
  if (api) {  // 行注：判断当前条件是否成立
    return await api.showNotification(title, body, icon)  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  if ('Notification' in window) {  // 行注：判断当前条件是否成立
    if (Notification.permission === 'granted') {  // 行注：判断当前条件是否成立
      new Notification(title, { body, icon })  // 行注：执行当前调用逻辑
      return true  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    if (Notification.permission !== 'denied') {  // 行注：判断当前条件是否成立
      const permission = await Notification.requestPermission()  // 行注：接收 permission 异步结果
      if (permission === 'granted') {  // 行注：判断当前条件是否成立
        new Notification(title, { body, icon })  // 行注：执行当前调用逻辑
        return true  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return false  // 行注：返回当前结果
}  // 行注：结束当前代码块

function getAudioContext(): AudioContext | null {  // 行注：定义 getAudioContext 方法
  if (typeof window === 'undefined') {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const AudioContextConstructor = window.AudioContext || (window as typeof window & {  // 行注：开始解构当前返回值
    webkitAudioContext?: typeof AudioContext  // 行注：补充当前表达式
  }).webkitAudioContext  // 行注：补充当前表达式
  if (!AudioContextConstructor) {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (!audioContext) {  // 行注：判断当前条件是否成立
    audioContext = new AudioContextConstructor()  // 行注：更新 audioContext 值
  }  // 行注：结束当前代码块
  return audioContext  // 行注：返回当前结果
}  // 行注：结束当前代码块

function createTone(  // 行注：定义 createTone 方法
  context: AudioContext,  // 行注：设置 context 配置项
  frequency: number,  // 行注：设置 frequency 配置项
  startAt: number,  // 行注：设置 startAt 配置项
  duration: number,  // 行注：设置 duration 配置项
  volume: number  // 行注：设置 volume 配置项
) {  // 行注：开始当前逻辑块
  const oscillator = context.createOscillator()  // 行注：初始化 oscillator 变量
  const gainNode = context.createGain()  // 行注：初始化 gainNode 变量
  oscillator.type = 'sine'  // 行注：更新 oscillator.type 值
  oscillator.frequency.setValueAtTime(frequency, startAt)  // 行注：调用 setValueAtTime 方法
  gainNode.gain.setValueAtTime(0.0001, startAt)  // 行注：调用 setValueAtTime 方法
  gainNode.gain.exponentialRampToValueAtTime(volume, startAt + 0.02)  // 行注：调用 exponentialRampToValueAtTime 方法
  gainNode.gain.exponentialRampToValueAtTime(0.0001, startAt + duration)  // 行注：调用 exponentialRampToValueAtTime 方法
  oscillator.connect(gainNode)  // 行注：调用 connect 方法
  gainNode.connect(context.destination)  // 行注：调用 connect 方法
  oscillator.start(startAt)  // 行注：调用 start 方法
  oscillator.stop(startAt + duration + 0.03)  // 行注：调用 stop 方法
}  // 行注：结束当前代码块

export async function playNotificationSound(type: NotificationSoundType = 'message'): Promise<boolean> {  // 行注：导出当前能力
  const api = getElectronAPI()  // 行注：初始化 api 实例
  if (api?.playNotificationSound) {  // 行注：判断当前条件是否成立
    return await api.playNotificationSound(type)  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const now = Date.now()  // 行注：记录 now 时间戳
  if (now - soundThrottleTimer < 280) {  // 行注：判断当前条件是否成立
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  soundThrottleTimer = now  // 行注：更新 soundThrottleTimer 值

  const context = getAudioContext()  // 行注：初始化 context 变量
  if (!context) {  // 行注：判断当前条件是否成立
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  try {  // 行注：尝试执行可能失败的逻辑
    if (context.state === 'suspended') {  // 行注：判断当前条件是否成立
      await context.resume()  // 行注：调用 resume 方法
    }  // 行注：结束当前代码块
    const startAt = context.currentTime + 0.01  // 行注：初始化 startAt 方法
    if (type === 'attention') {  // 行注：判断当前条件是否成立
      createTone(context, 740, startAt, 0.12, 0.04)  // 行注：调用 createTone 方法
      createTone(context, 988, startAt + 0.16, 0.18, 0.05)  // 行注：调用 createTone 方法
      return true  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    createTone(context, 660, startAt, 0.1, 0.035)  // 行注：调用 createTone 方法
    createTone(context, 784, startAt + 0.12, 0.14, 0.04)  // 行注：调用 createTone 方法
    return true  // 行注：返回当前结果
  } catch (error) {  // 行注：捕获并处理异常
    console.warn('playNotificationSound failed:', error)  // 行注：调用 warn 方法
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function onNewMessage(callback: (senderName: string, content: string) => void): () => void {  // 行注：导出当前能力
  const handler = (event: CustomEvent) => {  // 行注：定义 handler 方法
    const { senderName, content } = event.detail  // 行注：声明当前变量
    callback(senderName, content)  // 行注：调用 callback 方法
  }  // 行注：结束当前代码块

  window.addEventListener('new-message', handler as EventListener)  // 行注：注册new-message事件监听
  return () => window.removeEventListener('new-message', handler as EventListener)  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function emitNewMessage(senderName: string, content: string): void {  // 行注：导出当前能力
  window.dispatchEvent(new CustomEvent('new-message', {  // 行注：开始当前逻辑块
    detail: { senderName, content }  // 行注：设置 detail 配置项
  }))  // 行注：补充当前表达式
}  // 行注：结束当前代码块

export function removeInAppNotificationsByMessageIds(messageIds: Array<string | number | null | undefined>) {  // 行注：导出当前能力
  const normalizedIds = messageIds  // 行注：初始化 normalizedIds 变量
    .map(messageId => messageId == null ? '' : String(messageId))  // 行注：调用 map 方法
    .filter(messageId => messageId.length > 0)  // 行注：调用 filter 方法

  if (normalizedIds.length === 0) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  emitInAppNotification({  // 行注：开始当前逻辑块
    type: 'removeByMessageIds',  // 行注：设置 type 配置项
    messageIds: normalizedIds  // 行注：设置 messageIds 配置项
  })  // 行注：结束当前调用配置
}  // 行注：结束当前代码块

export function onInAppNotification(callback: (event: InAppNotificationEvent) => void): () => void {  // 行注：导出当前能力
  const handler = (event: Event) => {  // 行注：定义 handler 方法
    const customEvent = event as CustomEvent<InAppNotificationEvent>  // 行注：初始化 customEvent 变量
    callback(customEvent.detail)  // 行注：调用 callback 方法
  }  // 行注：结束当前代码块
  window.addEventListener(IN_APP_NOTIFICATION_EVENT, handler)  // 行注：注册当前事件监听
  return () => window.removeEventListener(IN_APP_NOTIFICATION_EVENT, handler)  // 行注：返回当前结果
}  // 行注：结束当前代码块
