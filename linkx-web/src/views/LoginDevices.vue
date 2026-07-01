<template>
  <div class="content-area">
    <div class="devices-panel">
      <header class="panel-header">
        <button type="button" class="back" @click="router.push({ name: 'Settings' })">← 设置</button>
        <h1>登录设备</h1>
        <p class="sub">管理已登录本账号的设备；踢下线后该设备需重新登录</p>
      </header>
      <div v-if="loading" class="state">加载中…</div>
      <ul v-else-if="sessions.length" class="session-list">
        <li v-for="row in sessions" :key="row.sessionId" class="session-row">
          <div class="session-main">
            <span class="device">{{ row.deviceLabel || '设备' }}</span>
            <span v-if="row.current" class="badge">当前设备</span>
            <p class="meta">
              <span v-if="row.clientIp">IP {{ row.clientIp }}</span>
              <span v-if="row.issuedAt"> · {{ formatTime(row.issuedAt) }}</span>
            </p>
          </div>
          <button
            v-if="!row.current"
            type="button"
            class="kick"
            :disabled="revokingId === row.sessionId"
            @click="revokeOne(row.sessionId)"
          >
            {{ revokingId === row.sessionId ? '处理中…' : '踢下线' }}
          </button>
        </li>
      </ul>
      <p v-else class="state">暂无其他登录会话</p>
      <footer class="footer-actions">
        <button type="button" class="secondary" :disabled="loading || revokingAll" @click="revokeOthers">
          {{ revokingAll ? '处理中…' : '踢掉其他所有设备' }}
        </button>
        <button type="button" class="ghost" @click="load">刷新</button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { authApi } from '../api/client'
import { useUserStore } from '../stores/user'

interface SessionRow {
  sessionId: string
  deviceLabel?: string
  clientIp?: string
  issuedAt?: string
  current?: boolean
}

const router = useRouter()
const message = useMessage()
const userStore = useUserStore()
const sessions = ref<SessionRow[]>([])
const loading = ref(false)
const revokingId = ref<string | null>(null)
const revokingAll = ref(false)

function currentSid() {
  return userStore.sessionId || localStorage.getItem('sessionId') || undefined
}

function unwrapList(res: unknown): SessionRow[] {
  const root = res as { data?: SessionRow[] }
  return Array.isArray(root?.data) ? root.data : []
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

async function load() {
  loading.value = true
  try {
    const res = await authApi.listSessions(currentSid())
    sessions.value = unwrapList(res)
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function revokeOne(sessionId: string) {
  revokingId.value = sessionId
  try {
    await authApi.revokeSession(sessionId, currentSid())
    message.success('已踢下线')
    await load()
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '操作失败')
  } finally {
    revokingId.value = null
  }
}

async function revokeOthers() {
  revokingAll.value = true
  try {
    await authApi.revokeOtherSessions(currentSid())
    message.success('已踢掉其他设备')
    await load()
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '操作失败')
  } finally {
    revokingAll.value = false
  }
}

onMounted(() => void load())
</script>

<style scoped>
.devices-panel {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px;
}
.panel-header h1 {
  margin: 8px 0 4px;
  font-size: 20px;
}
.sub {
  margin: 0;
  font-size: 13px;
  color: var(--linkx-text-muted);
}
.back {
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--linkx-primary);
  font-size: 14px;
  padding: 0;
}
.session-list {
  list-style: none;
  margin: 20px 0;
  padding: 0;
}
.session-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--linkx-border);
  border-radius: 10px;
  margin-bottom: 10px;
  background: var(--linkx-bg-card);
}
.device {
  font-weight: 600;
}
.badge {
  margin-left: 8px;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--linkx-primary);
  color: #fff;
}
.meta {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--linkx-text-muted);
}
.kick {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid var(--linkx-border);
  background: transparent;
  cursor: pointer;
}
.footer-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}
.secondary,
.ghost {
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
}
.secondary {
  border: none;
  background: var(--linkx-primary);
  color: #fff;
}
.ghost {
  border: 1px solid var(--linkx-border);
  background: transparent;
}
.state {
  padding: 24px;
  text-align: center;
  color: var(--linkx-text-muted);
}
</style>