<template>
  <Teleport to="body">
    <div v-if="visible" class="gms-overlay" @click.self="close">
      <div class="gms-panel" role="dialog" aria-label="全局消息搜索">
        <header class="gms-header">
          <input
            ref="inputRef"
            v-model="keyword"
            type="search"
            placeholder="搜索聊天记录…"
            @keyup.enter="search"
          />
          <button type="button" class="gms-close" aria-label="关闭" @click="close">×</button>
        </header>
        <div class="gms-filters">
          <label>
            <input v-model="sessionTypeFilter" type="radio" :value="0" name="st" />
            全部
          </label>
          <label>
            <input v-model="sessionTypeFilter" type="radio" :value="1" name="st" />
            单聊
          </label>
          <label>
            <input v-model="sessionTypeFilter" type="radio" :value="2" name="st" />
            群聊
          </label>
          <button type="button" class="gms-go" :disabled="loading || !keyword.trim()" @click="search">
            {{ loading ? '搜索中…' : '搜索' }}
          </button>
        </div>
        <div v-if="loading" class="gms-state">搜索中…</div>
        <p v-else-if="searched && !rows.length" class="gms-state">无匹配消息</p>
        <ul v-else class="gms-list">
          <li
            v-for="row in rows"
            :key="row.id"
            class="gms-item"
            @click="pick(row)"
          >
            <span class="gms-session">{{ sessionLabel(row) }}</span>
            <p class="gms-preview">{{ row.content || '—' }}</p>
            <time class="gms-time">{{ formatTime(row.createTime) }}</time>
          </li>
        </ul>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { chatApi } from '../api/client'
import { mapSearchHits, type MessageSearchHitView } from '../utils/messageSearchHit'

export type GlobalSearchHit = MessageSearchHitView

const props = defineProps<{ visible: boolean }>()
const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void
  (e: 'select', hit: GlobalSearchHit): void
}>()

const message = useMessage()
const inputRef = ref<HTMLInputElement | null>(null)
const keyword = ref('')
const sessionTypeFilter = ref(0)
const rows = ref<GlobalSearchHit[]>([])
const loading = ref(false)
const searched = ref(false)

function close() {
  emit('update:visible', false)
}

function unwrapList(res: unknown): GlobalSearchHit[] {
  return mapSearchHits(res)
}

function sessionLabel(row: GlobalSearchHit) {
  if (row.targetName) return row.targetName
  const st = row.sessionType === 2 ? '群' : '单聊'
  return `${st} #${row.targetId ?? '—'}`
}

function formatTime(t?: string) {
  if (!t) return ''
  return t.replace('T', ' ').slice(0, 16)
}

async function search() {
  const q = keyword.value.trim()
  if (!q) return
  loading.value = true
  searched.value = true
  try {
    const res = await chatApi.searchMessages(q, {
      sessionType: sessionTypeFilter.value > 0 ? sessionTypeFilter.value : undefined,
      size: 40
    })
    rows.value = unwrapList(res)
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '搜索失败')
    rows.value = []
  } finally {
    loading.value = false
  }
}

function pick(row: GlobalSearchHit) {
  emit('select', row)
  close()
}

watch(
  () => props.visible,
  async open => {
    if (open) {
      keyword.value = ''
      rows.value = []
      searched.value = false
      await nextTick()
      inputRef.value?.focus()
    }
  }
)
</script>

<style scoped>
.gms-overlay {
  position: fixed;
  inset: 0;
  z-index: 2200;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 48px 16px;
}
.gms-panel {
  width: min(520px, 100%);
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg-card);
  border-radius: 12px;
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
}
.gms-header {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-bottom: 1px solid var(--linkx-border);
}
.gms-header input {
  flex: 1;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid var(--linkx-border);
  font-size: 14px;
}
.gms-close {
  border: none;
  background: transparent;
  font-size: 22px;
  cursor: pointer;
  color: var(--linkx-text-muted);
}
.gms-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  font-size: 13px;
}
.gms-go {
  margin-left: auto;
  padding: 6px 14px;
  border-radius: 8px;
  border: none;
  background: var(--linkx-primary);
  color: #fff;
  cursor: pointer;
}
.gms-list {
  list-style: none;
  margin: 0;
  padding: 8px;
  overflow-y: auto;
  flex: 1;
}
.gms-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
}
.gms-item:hover {
  background: var(--linkx-bg-muted, rgba(0, 0, 0, 0.04));
}
.gms-session {
  font-size: 12px;
  font-weight: 600;
  color: var(--linkx-primary);
}
.gms-preview {
  margin: 4px 0;
  font-size: 14px;
  line-clamp: 2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.gms-time {
  font-size: 11px;
  color: var(--linkx-text-muted);
}
.gms-state {
  padding: 24px;
  text-align: center;
  color: var(--linkx-text-muted);
}
</style>