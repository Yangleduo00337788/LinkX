/**
 * 从系统通知等入口跳转到指定聊天会话。
 */
import type { Router } from 'vue-router'

export interface ChatNavigationTarget {
  targetId: string
  sessionType?: number
  messageId?: string
}

export async function navigateToChatSession(router: Router, target: ChatNavigationTarget) {
  if (!target.targetId) {
    return
  }
  const query: Record<string, string> = {}
  if (target.sessionType === 2) {
    query.sessionType = '2'
  }
  if (target.messageId) {
    query.messageId = target.messageId
  }
  await router.push({
    path: `/chat/${target.targetId}`,
    query
  })
}