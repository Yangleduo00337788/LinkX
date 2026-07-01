<template>
  <Teleport to="body">
    <div v-if="visible" class="notif-overlay" @click.self="close">
      <aside class="notif-panel" role="dialog" aria-labelledby="notif-title">
        <header class="notif-header">
          <h2 id="notif-title">系统通知</h2>
          <div class="notif-header-actions">
            <button
              v-if="hasUnread"
              type="button"
              class="notif-mark-all"
              :disabled="markingAll"
              @click="markAllRead"
            >
              {{ markingAll ? '处理中…' : '全部已读' }}
            </button>
            <button type="button" class="notif-close" aria-label="关闭" @click="close">×</button>
          </div>
        </header>
        <div v-if="loading" class="notif-state">加载中…</div>
        <div v-else-if="!items.length" class="notif-state">暂无通知</div>
        <ul v-else class="notif-list">
          <li
            v-for="row in items"
            :key="row.id"
            class="notif-item"
            :class="{ unread: row.readFlag === 0 }"
          >
            <div class="notif-item-head">
              <span class="notif-item-title">{{ row.title || '通知' }}</span>
              <time class="notif-item-time">{{ formatTime(row.createTime) }}</time>
            </div>
            <p class="notif-item-body">{{ row.content || '—' }}</p>
            <button
              v-if="row.readFlag === 0"
              type="button"
              class="notif-mark-read"
              :disabled="markingId === row.id"
              @click="markRead(row)"
            >
              {{ markingId === row.id ? '处理中…' : '标为已读' }}
            </button>
          </li>
        </ul>
        <footer v-if="total > items.length" class="notif-footer">
          <button type="button" class="notif-more" :disabled="loadingMore" @click="loadMore">
            {{ loadingMore ? '加载中…' : '加载更多' }}
          </button>
        </footer>
      </aside>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { notificationApi } from '../api/client'
import { useNotificationStore } from '../stores/notification'

export interface UserNotificationRow {
  id: number | string
  title?: string
  content?: string
  readFlag?: number
  createTime?: string
}

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{ (e: 'update:visible', v: boolean): void }>()

const message = useMessage()
const notificationStore = useNotificationStore()
const items = ref<UserNotificationRow[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const page = ref(1)
const total = ref(0)
const markingId = ref<number | string | null>(null)
const markingAll = ref(false)
const pageSize = 20

const hasUnread = computed(() => items.value.some(row => row.readFlag === 0))

function close() {
  emit('update:visible', false)
}

function unwrapPage(res: unknown): { records: UserNotificationRow[]; total: number } {
  const root = res as { data?: { records?: UserNotificationRow[]; total?: number } | UserNotificationRow[] }
  const data = root?.data
  if (data && typeof data === 'object' && 'records' in data && Array.isArray(data.records)) {
    return { records: data.records, total: Number(data.total ?? data.records.length) }
  }
  if (Array.isArray(data)) {
    return { records: data, total: data.length }
  }
  return { records: [], total: 0 }
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

async function fetchPage(p: number, append: boolean) {
  const res = await notificationApi.list(p, pageSize)
  const { records, total: t } = unwrapPage(res)
  total.value = t
  items.value = append ? [...items.value, ...records] : records
  page.value = p
}

async function loadInitial() {
  loading.value = true
  try {
    await fetchPage(1, false)
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '加载通知失败')
    items.value = []
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    await fetchPage(page.value + 1, true)
  } catch {
    message.error('加载更多失败')
  } finally {
    loadingMore.value = false
  }
}

async function markRead(row: UserNotificationRow) {
  markingId.value = row.id
  try {
    await notificationApi.markRead(row.id)
    row.readFlag = 1
    void notificationStore.refreshUnread()
  } catch {
    message.error('标记已读失败')
  } finally {
    markingId.value = null
  }
}

async function markAllRead() {
  markingAll.value = true
  try {
    await notificationApi.markAllRead()
    for (const row of items.value) {
      row.readFlag = 1
    }
    void notificationStore.refreshUnread()
    message.success('已全部标为已读')
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '操作失败')
  } finally {
    markingAll.value = false
  }
}

watch(
  () => props.visible,
  open => {
    if (open) void loadInitial()
  }
)
</script>

<style scoped>
.notif-overlay {
  position: fixed;
  inset: 0;
  z-index: 2100;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: flex-end;
}
.notif-panel {
  width: min(400px, 100vw);
  height: 100%;
  background: var(--linkx-bg-card);
  border-left: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  box-shadow: var(--linkx-shadow-lg);
}
.notif-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid var(--linkx-border);
}
.notif-header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.notif-mark-all {
  border: none;
  background: var(--linkx-bg-muted, rgba(0, 0, 0, 0.06));
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  color: var(--linkx-text);
}
.notif-mark-all:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.notif-header h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
}
.notif-close {
  border: none;
  background: transparent;
  font-size: 24px;
  line-height: 1;
  cursor: pointer;
  color: var(--linkx-text-muted);
}
.notif-state {
  padding: 32px 18px;
  text-align: center;
  color: var(--linkx-text-muted);
  font-size: 14px;
}
.notif-list {
  list-style: none;
  margin: 0;
  padding: 8px 0;
  overflow-y: auto;
  flex: 1;
}
.notif-item {
  padding: 14px 18px;
  border-bottom: 1px solid var(--linkx-border);
}
.notif-item.unread {
  background: color-mix(in srgb, var(--linkx-primary-glow) 40%, transparent);
}
.notif-item-head {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.notif-item-title {
  font-weight: 600;
  font-size: 14px;
  color: var(--linkx-text);
}
.notif-item-time {
  font-size: 11px;
  color: var(--linkx-text-muted);
  flex-shrink: 0;
}
.notif-item-body {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
  line-height: 1.45;
  white-space: pre-wrap;
  word-break: break-word;
}
.notif-mark-read {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  cursor: pointer;
  color: var(--linkx-primary);
}
.notif-footer {
  padding: 12px 18px;
  border-top: 1px solid var(--linkx-border);
}
.notif-more {
  width: 100%;
  padding: 10px;
  border-radius: 10px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  cursor: pointer;
  font-size: 13px;
}
</style>