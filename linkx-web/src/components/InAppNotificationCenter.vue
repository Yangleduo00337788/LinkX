<template>
  <Teleport to="body">
    <div v-if="items.length" class="in-app-notification-center" aria-live="polite" aria-atomic="false">
      <button
        v-for="item in items"
        :key="item.id"
        type="button"
        class="in-app-notification-card"
        @click="handleOpen(item)"
      >
        <div class="in-app-notification-accent" :class="{ attention: item.attention }"></div>
        <div class="in-app-notification-body">
          <div class="in-app-notification-header">
            <span class="in-app-notification-title">{{ item.title }}</span>
            <span class="in-app-notification-time">{{ item.timeText }}</span>
          </div>
          <div class="in-app-notification-content">{{ item.body }}</div>
        </div>
        <span
          class="in-app-notification-close"
          title="关闭"
          @click.stop="removeItem(item.id)"
        >
          ×
        </span>
      </button>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { onInAppNotification, type InAppNotificationPayload } from '../utils/notify'

interface NotificationViewModel extends InAppNotificationPayload {
  timeText: string
}

const router = useRouter()
const items = ref<NotificationViewModel[]>([])
const timerMap = new Map<string, number>()
let stopListening: (() => void) | null = null

function formatNow() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date())
}

function clearTimer(id: string) {
  const timer = timerMap.get(id)
  if (timer) {
    window.clearTimeout(timer)
    timerMap.delete(id)
  }
}

function removeItem(id: string) {
  clearTimer(id)
  items.value = items.value.filter(item => item.id !== id)
}

function removeItemsByMessageIds(messageIds: string[]) {
  const targetIds = new Set(messageIds)
  const removableItems = items.value.filter(item => item.messageId && targetIds.has(item.messageId))
  for (const item of removableItems) {
    clearTimer(item.id)
  }
  items.value = items.value.filter(item => !item.messageId || !targetIds.has(item.messageId))
}

function scheduleDismiss(id: string) {
  clearTimer(id)
  const timer = window.setTimeout(() => {
    removeItem(id)
  }, 5200)
  timerMap.set(id, timer)
}

async function handleOpen(item: NotificationViewModel) {
  removeItem(item.id)
  if (!item.targetId) {
    return
  }
  const query: Record<string, string> = {}
  if (item.sessionType === 2) {
    query.sessionType = '2'
  }
  if (item.messageId) {
    query.messageId = item.messageId
  }
  await router.push({
    path: `/chat/${item.targetId}`,
    query
  })
}

function pushItem(payload: InAppNotificationPayload) {
  const nextItem: NotificationViewModel = {
    ...payload,
    title: payload.title || '新消息',
    body: payload.body || '你有一条新提醒',
    timeText: formatNow()
  }
  items.value = [nextItem, ...items.value.filter(item => item.id !== payload.id)].slice(0, 3)
  scheduleDismiss(payload.id)
}

onMounted(() => {
  stopListening = onInAppNotification(event => {
    if (event.type === 'removeByMessageIds') {
      removeItemsByMessageIds(event.messageIds)
      return
    }
    pushItem(event.payload)
  })
})

onBeforeUnmount(() => {
  stopListening?.()
  for (const id of timerMap.keys()) {
    clearTimer(id)
  }
})
</script>

<style scoped>
.in-app-notification-center {
  position: fixed;
  right: 18px;
  bottom: 18px;
  z-index: 2200;
  display: flex;
  flex-direction: column-reverse;
  gap: 10px;
  pointer-events: none;
}

.in-app-notification-card {
  width: min(320px, calc(100vw - 24px));
  border: 1px solid var(--linkx-border);
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, black 8%);
  border-radius: 14px;
  box-shadow: var(--linkx-shadow-lg);
  display: flex;
  align-items: stretch;
  gap: 12px;
  padding: 0;
  color: var(--linkx-text);
  overflow: hidden;
  cursor: pointer;
  pointer-events: auto;
  backdrop-filter: blur(16px);
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.in-app-notification-card:hover {
  transform: translateY(-1px);
  border-color: var(--linkx-primary);
}

.in-app-notification-accent {
  width: 4px;
  background: var(--linkx-primary);
  flex-shrink: 0;
}

.in-app-notification-accent.attention {
  background: #f6ad55;
}

.in-app-notification-body {
  flex: 1;
  min-width: 0;
  padding: 12px 0 12px 12px;
}

.in-app-notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.in-app-notification-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--linkx-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.in-app-notification-time {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--linkx-text-muted);
}

.in-app-notification-content {
  font-size: 13px;
  line-height: 1.45;
  color: var(--linkx-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-align: left;
  word-break: break-word;
}

.in-app-notification-close {
  width: 36px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
  font-size: 18px;
  flex-shrink: 0;
}

.in-app-notification-close:hover {
  color: var(--linkx-text);
}

@media (max-width: 760px) {
  .in-app-notification-center {
    left: 12px;
    right: 12px;
    bottom: calc(84px + env(safe-area-inset-bottom, 0px));
  }

  .in-app-notification-card {
    width: 100%;
  }
}
</style>
