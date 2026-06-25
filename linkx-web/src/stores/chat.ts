/**
 * 维护聊天页面中的会话、消息、联系人和当前上下文状态。
 */
import { defineStore } from 'pinia'  // 行注：引入 defineStore 能力
import { ref } from 'vue'  // 行注：引入 ref 能力
import { SESSION_TYPE_SINGLE, type ChatSession, type DisplayMessage, type FriendItem } from '../types/chat'  // 行注：引入 SESSION_TYPE_SINGLE, type ChatSession, type DisplayMessage, type FriendItem 能力

export const useChatStore = defineStore('chat', () => {  // 行注：导出当前能力
  const sessions = ref<ChatSession[]>([])  // 行注：初始化 sessions 变量
  const messages = ref<DisplayMessage[]>([])  // 行注：初始化 messages 变量
  const friends = ref<FriendItem[]>([])  // 行注：初始化 friends 集合
  const currentTargetId = ref<string | null>(null)  // 行注：初始化 currentTargetId 状态
  const currentSessionType = ref<number>(SESSION_TYPE_SINGLE)  // 行注：初始化 currentSessionType 变量
  const searchText = ref('')  // 行注：初始化 searchText 响应式状态

  function resetConversation() {  // 行注：定义 resetConversation 方法
    currentTargetId.value = null  // 行注：更新 currentTargetId 状态
    currentSessionType.value = SESSION_TYPE_SINGLE  // 行注：更新 currentSessionType 状态
    messages.value = []  // 行注：更新 messages 状态
  }  // 行注：结束当前代码块

  function resetChatState() {  // 行注：定义 resetChatState 方法
    sessions.value = []  // 行注：更新 sessions 状态
    messages.value = []  // 行注：更新 messages 状态
    friends.value = []  // 行注：更新 friends 状态
    currentTargetId.value = null  // 行注：更新 currentTargetId 状态
    currentSessionType.value = SESSION_TYPE_SINGLE  // 行注：更新 currentSessionType 状态
    searchText.value = ''  // 行注：更新 searchText 状态
  }  // 行注：结束当前代码块

  return {  // 行注：返回当前结果
    sessions,  // 行注：补充 sessions 配置项
    messages,  // 行注：补充当前配置项
    friends,  // 行注：补充当前配置项
    currentTargetId,  // 行注：补充当前配置项
    currentSessionType,  // 行注：补充当前配置项
    searchText,  // 行注：补充当前配置项
    resetConversation,  // 行注：补充当前配置项
    resetChatState  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置
