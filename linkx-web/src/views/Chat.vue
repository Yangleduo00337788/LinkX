<template>
  <div class="content-area">
    <!-- Session List -->
    <div class="session-panel">
      <div class="panel-header">消息</div>
      <div class="session-list">
        <div v-for="s in sessions" :key="s.targetId"
             class="session-item" :class="{ active: currentTargetId === s.targetId }"
             @click="selectSession(s)">
          <div class="session-avatar">{{ s.targetNickname?.charAt(0) }}</div>
          <div class="session-info">
            <div class="session-name">{{ s.targetNickname }}</div>
            <div class="session-preview">{{ s.lastMessage }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chat Area -->
    <div class="chat-panel">
      <div class="panel-header">{{ currentTargetName || '选择一个会话' }}</div>

      <div class="chat-messages" ref="messagesRef">
        <div v-for="(msg, i) in messages" :key="i"
             class="message-row" :class="{ self: msg.isMe }">
          <div class="msg-avatar" :style="{ background: msg.isMe ? '#07c160' : '#e0e0e0' }">
            {{ msg.isMe ? '我' : msg.name?.charAt(0) }}
          </div>
          <div>
            <div v-if="!msg.isMe" style="font-size: 12px; color: #999; margin-bottom: 2px;">{{ msg.name }}</div>
            <div class="msg-bubble" :class="{ self: msg.isMe }">{{ msg.content }}</div>
            <div class="msg-meta" :class="{ self: msg.isMe }">
              <span>{{ msg.time }}</span>
              <span v-if="msg.isMe" class="msg-read">{{ msg.readStatus }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="chat-input-bar">
        <input v-model="inputMessage" placeholder="输入消息..." @keyup.enter="handleSend" />
        <button class="chat-send-btn" @click="handleSend" :disabled="!currentTargetId">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import { chatApi } from '../api/client'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()
const sessions = ref<any[]>([])
const messages = ref<any[]>([])
const currentTargetId = ref<number | null>(null)
const currentTargetName = ref('')
const inputMessage = ref('')
const messagesRef = ref<HTMLElement>()
let refreshTimer: any = null

onMounted(async () => {
  await loadSessions()
  if (route.params.targetId) {
    const id = Number(route.params.targetId)
    const s = sessions.value.find(s => s.targetId === id)
    if (s) selectSession(s)
  }
  refreshTimer = setInterval(() => {
    loadSessions()
    if (currentTargetId.value) loadMessages(currentTargetId.value)
  }, 3000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

async function loadSessions() {
  try {
    const res: any = await chatApi.getSessions()
    sessions.value = res.data || []
  } catch (e) {}
}

async function selectSession(s: any) {
  currentTargetId.value = s.targetId
  currentTargetName.value = s.targetNickname
  await loadMessages(s.targetId)
}

async function loadMessages(targetId: number) {
  try {
    const res: any = await chatApi.getHistory(targetId)
    const myId = userStore.userId
    messages.value = (res.data || []).map((m: any) => ({
      isMe: m.fromUserId === myId,
      name: m.fromNickname,
      content: m.content,
      time: m.createTime?.substring(11, 16) || '',
      readStatus: m.status === 1 ? '已读' : '已送达'
    }))
    await nextTick()
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
    chatApi.markRead(targetId)
  } catch (e) {}
}

async function handleSend() {
  if (!currentTargetId.value || !inputMessage.value.trim()) return
  try {
    await chatApi.send(currentTargetId.value, inputMessage.value.trim())
    inputMessage.value = ''
    await loadMessages(currentTargetId.value)
    await loadSessions()
  } catch (e) {}
}
</script>
