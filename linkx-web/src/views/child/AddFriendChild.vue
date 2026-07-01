<template>
  <ChildWindowFrame :width="360" :height="560">
    <div class="add-friend-shell">
      <header class="add-friend-head">
        <span class="add-friend-title">添加朋友</span>
        <button type="button" class="add-friend-close" aria-label="关闭" @click="close">×</button>
      </header>
      <div class="add-friend-search-row">
        <div class="add-friend-input-wrap">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#999" stroke-width="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            v-model="keyword"
            type="text"
            class="add-friend-input"
            placeholder="搜索用户名或手机号"
            @keyup.enter="search"
          />
        </div>
        <button type="button" class="add-friend-search-btn" :disabled="searching" @click="search">
          {{ searching ? '…' : '搜索' }}
        </button>
      </div>
      <div class="add-friend-results">
        <div v-if="searching" class="add-friend-hint">搜索中…</div>
        <div v-else-if="searched && !results.length" class="add-friend-hint">未找到用户</div>
        <div v-for="user in results" :key="String(user.id)" class="add-friend-user">
          <div class="add-friend-avatar">
            <ProtectedImage v-if="user.avatar" :src="user.avatar" class="avatar-img" />
            <span v-else>{{ (user.nickname || user.username)?.charAt(0) }}</span>
          </div>
          <div class="add-friend-meta">
            <div class="add-friend-name">{{ user.nickname || user.username }}</div>
            <div class="add-friend-sub">@{{ user.username }}</div>
          </div>
          <button
            type="button"
            class="add-friend-action"
            :disabled="requestingId === user.id"
            @click="sendRequest(user)"
          >
            {{ requestingId === user.id ? '…' : '添加' }}
          </button>
        </div>
      </div>
    </div>
  </ChildWindowFrame>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { friendApi, userApi } from '../../api/client'
import ProtectedImage from '../../components/ProtectedImage.vue'
import { closeLinkxChildWindow } from '../../utils/childWindow'
import ChildWindowFrame from './ChildWindowFrame.vue'

interface SearchUser {
  id: string | number
  username: string
  nickname?: string
  avatar?: string
}

const message = useMessage()
const keyword = ref('')
const results = ref<SearchUser[]>([])
const searching = ref(false)
const searched = ref(false)
const requestingId = ref<string | number | null>(null)

onMounted(() => {
  document.body.classList.add('child-window-page')
})

async function close() {
  await closeLinkxChildWindow()
  window.close()
}

async function search() {
  const k = keyword.value.trim()
  if (!k) {
    message.warning('请输入搜索关键词')
    return
  }
  searching.value = true
  searched.value = true
  try {
    const res: any = await userApi.search(k)
    results.value = Array.isArray(res.data) ? res.data : res.data?.records || []
  } catch {
    results.value = []
  } finally {
    searching.value = false
  }
}

async function sendRequest(user: SearchUser) {
  requestingId.value = user.id
  try {
    await friendApi.sendRequest(user.id, '')
    message.success('好友申请已发送')
  } catch (e: any) {
    message.error(e?.response?.data?.message || '发送失败')
  } finally {
    requestingId.value = null
  }
}
</script>

<style scoped>
.add-friend-shell {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f7f7f7;
  color: #1a1a1a;
}

.add-friend-head {
  position: relative;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #e8e8e8;
  background: #f7f7f7;
}

.add-friend-title {
  font-size: 15px;
  font-weight: 600;
}

.add-friend-close {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: none;
  font-size: 22px;
  color: #666;
  cursor: pointer;
  line-height: 1;
}

.add-friend-search-row {
  display: flex;
  gap: 8px;
  padding: 12px;
}

.add-friend-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 36px;
  padding: 0 10px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 4px;
}

.add-friend-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 13px;
}

.add-friend-search-btn {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 4px;
  background: #07c160;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.add-friend-search-btn:disabled {
  opacity: 0.6;
}

.add-friend-results {
  flex: 1;
  overflow-y: auto;
  background: #fff;
}

.add-friend-hint {
  padding: 24px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.add-friend-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.add-friend-avatar {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
}

.add-friend-avatar .avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.add-friend-meta {
  flex: 1;
  min-width: 0;
}

.add-friend-name {
  font-size: 14px;
  font-weight: 500;
}

.add-friend-sub {
  font-size: 12px;
  color: #888;
  margin-top: 2px;
}

.add-friend-action {
  height: 32px;
  padding: 0 14px;
  border: 1px solid #07c160;
  border-radius: 4px;
  background: #fff;
  color: #07c160;
  font-size: 13px;
  cursor: pointer;
}
</style>