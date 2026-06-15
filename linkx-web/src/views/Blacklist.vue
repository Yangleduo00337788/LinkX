<template>
  <div class="content-area">
    <div class="blacklist-panel">
      <div class="panel-header">
        <span class="header-title">黑名单</span>
        <button class="refresh-btn" @click="loadBlacklist" title="刷新">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
        </button>
      </div>

      <div class="blacklist-list">
        <div
          v-for="user in blacklist"
          :key="user.id"
          class="blacklist-item"
        >
          <div class="user-avatar">
            <img v-if="user.avatar" :src="user.avatar" class="avatar-img" />
            <span v-else>{{ user.nickname?.charAt(0) }}</span>
          </div>
          <div class="user-info">
            <div class="user-name">{{ user.nickname }}</div>
            <div class="user-username">@{{ user.username }}</div>
          </div>
          <button class="remove-btn" @click="handleRemove(user.id)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
            移除
          </button>
        </div>

        <div v-if="blacklist.length === 0 && !loading" class="empty-state">
          <div class="empty-icon">
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <circle cx="12" cy="12" r="10"/>
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
            </svg>
          </div>
          <div class="empty-title">暂无黑名单用户</div>
          <div class="empty-subtitle">被拉黑的用户会显示在这里</div>
        </div>
      </div>
    </div>

    <div class="right-panel">
      <div class="info-card">
        <div class="info-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="16" x2="12" y2="12"/>
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          </svg>
        </div>
        <h3>黑名单说明</h3>
        <ul>
          <li>拉黑后对方无法给你发消息</li>
          <li>拉黑后对方无法看到你的在线状态</li>
          <li>你可以随时将对方从黑名单移除</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { blacklistApi } from '../api/client'
import { useMessage } from 'naive-ui'

const message = useMessage()
const blacklist = ref<any[]>([])
const loading = ref(false)

onMounted(() => {
  loadBlacklist()
})

async function loadBlacklist() {
  loading.value = true
  try {
    const res: any = await blacklistApi.list()
    blacklist.value = res.data || []
  } catch (e) {
    console.error('loadBlacklist error:', e)
  } finally {
    loading.value = false
  }
}

async function handleRemove(userId: number) {
  try {
    await blacklistApi.remove(userId)
    message.success('已移除')
    await loadBlacklist()
  } catch (e: any) {
    message.error(e.response?.data?.message || '操作失败')
  }
}
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.blacklist-panel {
  width: 360px;
  background: var(--linkx-bg-card);
  border-right: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  min-width: 300px;
}

.panel-header {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--linkx-text);
  letter-spacing: 0.3px;
}

.refresh-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.refresh-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-primary);
}

.blacklist-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.blacklist-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  border-radius: var(--linkx-radius);
  transition: var(--linkx-transition-fast);
}

.blacklist-item:hover {
  background: var(--linkx-bg-hover);
}

.user-avatar {
  width: 44px;
  height: 44px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  margin-bottom: 2px;
}

.user-username {
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.remove-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: transparent;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.remove-btn:hover {
  border-color: var(--linkx-error);
  color: var(--linkx-error);
  background: rgba(255, 61, 113, 0.05);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--linkx-text-muted);
  gap: 12px;
}

.empty-icon {
  opacity: 0.3;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
}

.empty-subtitle {
  font-size: 13px;
}

.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-width: 0;
}

.info-card {
  max-width: 320px;
  text-align: center;
}

.info-icon {
  color: var(--linkx-text-muted);
  margin-bottom: 16px;
  opacity: 0.5;
}

.info-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 16px;
}

.info-card ul {
  list-style: none;
  padding: 0;
  text-align: left;
}

.info-card li {
  font-size: 14px;
  color: var(--linkx-text-secondary);
  padding: 8px 0;
  border-bottom: 1px solid var(--linkx-border);
}

.info-card li:last-child {
  border-bottom: none;
}

.info-card li::before {
  content: '•';
  color: var(--linkx-primary);
  margin-right: 8px;
}

@media (max-width: 960px) {
  .blacklist-panel {
    width: 300px;
    min-width: 260px;
  }

  .right-panel {
    padding: 24px;
  }
}

@media (max-width: 760px) {
  .content-area {
    flex-direction: column;
  }

  .blacklist-panel {
    width: 100%;
    min-width: 0;
    max-height: 46%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .right-panel {
    align-items: stretch;
  }

  .info-card {
    max-width: none;
    text-align: left;
  }
}

@media (max-width: 560px) {
  .blacklist-item {
    align-items: flex-start;
    flex-wrap: wrap;
    padding: 12px 10px;
  }

  .remove-btn {
    width: 100%;
    justify-content: center;
  }

  .right-panel {
    padding: 16px 12px;
  }
}
</style>
