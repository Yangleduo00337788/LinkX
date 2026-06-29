<template>
  <div class="content-area">
    <div class="reports-panel">
      <div class="panel-header">
        <div class="header-text">
          <span class="header-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              <line x1="12" y1="9" x2="12" y2="13" />
              <line x1="12" y1="17" x2="12.01" y2="17" />
            </svg>
          </span>
          <div class="header-titles">
            <span class="header-title">我的举报</span>
            <span class="header-sub">查看处理进度与结果（含站内通知）</span>
          </div>
        </div>
        <button class="refresh-btn" type="button" title="刷新" @click="load(1)">刷新</button>
      </div>
      <div class="reports-list">
        <div v-if="loading" class="state-box">加载中…</div>
        <div v-else-if="rows.length === 0" class="state-box muted">暂无举报记录</div>
        <div v-for="r in rows" :key="r.id" class="report-item">
          <div class="report-head">
            <span class="report-id">#{{ r.id }}</span>
            <span class="status" :class="statusClass(r.status)">{{ statusLabel(r.status) }}</span>
          </div>
          <div class="report-meta">
            <span>{{ r.targetType }} · {{ r.targetId }}</span>
            <span class="time">{{ formatTime(r.createTime) }}</span>
          </div>
          <p class="reason"><strong>{{ r.reasonCategory }}</strong>{{ r.reasonDetail ? `：${r.reasonDetail}` : '' }}</p>
          <div v-if="r.resolutionNote || r.handledTime" class="resolution">
            <span v-if="r.resolution">处理：{{ r.resolution }}</span>
            <p v-if="r.resolutionNote">{{ r.resolutionNote }}</p>
            <span v-if="r.handledTime" class="handled">处理时间 {{ formatTime(r.handledTime) }}</span>
          </div>
        </div>
        <button
          v-if="hasMore && !loading"
          type="button"
          class="load-more"
          @click="load(page + 1)"
        >
          加载更多
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { reportApi } from '../api/client'

interface Row {
  id: number
  targetType: string
  targetId: string
  reasonCategory: string
  reasonDetail?: string
  status: number
  resolution?: string
  resolutionNote?: string
  handledTime?: string
  createTime?: string
}

const loading = ref(false)
const rows = ref<Row[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 20
const hasMore = ref(false)

function statusLabel(s: number) {
  const map: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已处理', 3: '已驳回' }
  return map[s] ?? String(s)
}

function statusClass(s: number) {
  if (s === 2) return 'ok'
  if (s === 3) return 'dismiss'
  if (s === 0) return 'pending'
  return 'progress'
}

function formatTime(t?: string) {
  if (!t) return '—'
  return t.replace('T', ' ').substring(0, 19)
}

async function load(p: number) {
  loading.value = true
  try {
    const res: any = await reportApi.mine(p, pageSize)
    const data = res.data?.data ?? res.data
    const records: Row[] = data?.records ?? []
    total.value = data?.total ?? 0
    page.value = p
    if (p === 1) rows.value = records
    else rows.value = [...rows.value, ...records]
    hasMore.value = rows.value.length < total.value
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))
</script>

<style scoped>
.reports-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--lx-surface, #fff);
  border-radius: 12px;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--lx-border, #eee);
}
.header-text {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-titles {
  display: flex;
  flex-direction: column;
}
.header-title {
  font-weight: 600;
}
.header-sub {
  font-size: 12px;
  color: var(--lx-text-muted, #888);
}
.refresh-btn {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--lx-border, #ddd);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}
.reports-list {
  padding: 16px 20px;
  overflow: auto;
  flex: 1;
}
.report-item {
  padding: 14px 16px;
  border: 1px solid var(--lx-border, #eee);
  border-radius: 10px;
  margin-bottom: 12px;
}
.report-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}
.report-id {
  font-weight: 600;
}
.status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
}
.status.pending {
  background: #fff8e1;
  color: #f57c00;
}
.status.progress {
  background: #e3f2fd;
  color: #1976d2;
}
.status.ok {
  background: #e8f5e9;
  color: #388e3c;
}
.status.dismiss {
  background: #f5f5f5;
  color: #757575;
}
.report-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--lx-text-muted, #888);
  margin-bottom: 8px;
}
.reason {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}
.resolution {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--lx-border, #eee);
  font-size: 13px;
  color: var(--lx-text-muted, #666);
}
.resolution p {
  margin: 6px 0 0;
}
.handled {
  display: block;
  margin-top: 6px;
  font-size: 12px;
}
.state-box {
  text-align: center;
  padding: 32px;
  color: var(--lx-text-muted, #888);
}
.load-more {
  width: 100%;
  padding: 10px;
  margin-top: 8px;
  border: 1px dashed var(--lx-border, #ccc);
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
}
</style>