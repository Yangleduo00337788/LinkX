<template>
  <DesktopToolWindow
    :visible="visible"
    title="添加朋友"
    :width="360"
    :height="560"
    @close="$emit('close')"
  >
    <div class="af-root">
      <div class="af-search-row">
        <div class="af-input-wrap">
          <svg class="af-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索用户名或者手机号"
            class="af-input"
            @keyup.enter="search"
          />
        </div>
        <button type="button" class="af-search-btn" :disabled="searching || !keyword.trim()" @click="search">
          {{ searching ? '…' : '搜索' }}
        </button>
      </div>
      <div class="af-results">
        <div v-if="searched && !results.length" class="af-hint">未找到用户</div>
        <div v-for="u in results" :key="u.id" class="af-user">
          <div class="af-avatar">
            <ProtectedImage v-if="u.avatar" :src="u.avatar" class="avatar-img" />
            <span v-else>{{ (u.nickname || u.username)?.charAt(0) }}</span>
          </div>
          <div class="af-meta">
            <div class="af-nick">{{ u.nickname || u.username }}</div>
            <div class="af-user-id">@{{ u.username }}</div>
          </div>
          <button
            type="button"
            class="af-add-btn"
            :disabled="requestingId === u.id"
            @click="$emit('send-request', u.id)"
          >
            {{ requestingId === u.id ? '发送中' : '添加' }}
          </button>
        </div>
      </div>
      <div class="af-footer">
        <button type="button" class="af-cancel-btn" @click="$emit('close')">取消</button>
      </div>
    </div>
  </DesktopToolWindow>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import ProtectedImage from '../ProtectedImage.vue'
import DesktopToolWindow from './DesktopToolWindow.vue'
import { userApi } from '../../api/client'

export interface SearchUserRow {
  id: string | number
  username: string
  nickname?: string
  avatar?: string
}

const props = defineProps<{
  visible: boolean
  requestingId?: string | number | null
}>()

defineEmits<{
  (e: 'close'): void
  (e: 'send-request', userId: string | number): void
}>()

const keyword = ref('')
const searching = ref(false)
const searched = ref(false)
const results = ref<SearchUserRow[]>([])

watch(
  () => props.visible,
  v => {
    if (!v) {
      keyword.value = ''
      results.value = []
      searched.value = false
    }
  }
)

async function search() {
  const k = keyword.value.trim()
  if (!k) return
  searching.value = true
  searched.value = true
  try {
    const res: any = await userApi.search(k)
    const data = res.data
    results.value = Array.isArray(data) ? data : data ? [data] : []
  } catch {
    results.value = []
  } finally {
    searching.value = false
  }
}
</script>

<style scoped>
.af-root {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  box-sizing: border-box;
}

.af-search-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.af-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  background: #fff;
}

.af-icon {
  color: #999;
  flex-shrink: 0;
}

.af-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
}

.af-search-btn {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background: #07c160;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  flex-shrink: 0;
}

.af-search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.af-results {
  flex: 1;
  margin-top: 16px;
  overflow-y: auto;
}

.af-hint {
  text-align: center;
  color: #999;
  font-size: 13px;
  padding: 24px;
}

.af-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.af-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.af-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.af-meta {
  flex: 1;
  min-width: 0;
}

.af-nick {
  font-size: 14px;
  color: #333;
}

.af-user-id {
  font-size: 12px;
  color: #999;
}

.af-add-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  background: #07c160;
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

.af-add-btn:disabled {
  opacity: 0.6;
}

.af-footer {
  margin-top: auto;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.af-cancel-btn {
  height: 36px;
  padding: 0 18px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fff;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}

.af-cancel-btn:hover {
  background: #fafafa;
}
</style>