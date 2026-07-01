/**
 * WebSocket 发送失败时降级到 REST（与 ChatController 对齐）。
 */
import { chatApi } from '../api/client'

/** 仅传输层失败时降级 REST；业务错误（禁言、非好友等）不重复走 REST */
export function isSocketTransportError(error: unknown): boolean {
  if (!error || typeof error !== 'object') return false
  const e = error as { message?: string; code?: string }
  if (e.code === 'SOCKET_NOT_CONNECTED' || e.code === 'SOCKET_TIMEOUT') {
    return true
  }
  const msg = String(e.message || '').toLowerCase()
  return (
    msg.includes('websocket') ||
    msg.includes('socket') ||
    msg.includes('连接超时') ||
    msg.includes('timeout') ||
    msg.includes('未连接')
  )
}

export async function sendTextViaRest(payload: {
  toUserId: string | number
  content: string
  sessionType: number
  clientMessageId?: string
  mentionAll?: boolean
  mentionUserIds?: Array<string | number>
}) {
  const res: any = await chatApi.sendMessage({
    toUserId: payload.toUserId,
    content: payload.content,
    msgType: 1,
    sessionType: payload.sessionType,
    clientMessageId: payload.clientMessageId,
    mentionAll: payload.mentionAll,
    mentionUserIds: payload.mentionUserIds
  })
  return res?.data
}

export async function sendFileViaRest(payload: {
  toUserId: string | number
  fileId: number
  msgType: number
  sessionType: number
  clientMessageId?: string
}) {
  const res: any = await chatApi.sendFileMessage(payload)
  return res?.data
}