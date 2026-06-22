<template>
  <div v-if="requests.length > 0" class="request-list">
    <div
      v-for="request in requests"
      :key="request.id"
      class="request-card"
    >
      <div class="request-avatar">
        <img v-if="request.fromAvatar" :src="request.fromAvatar" class="avatar-img" />
        <span v-else>{{ request.fromNickname?.charAt(0) }}</span>
      </div>
      <div class="request-info">
        <div class="request-name">{{ request.fromNickname }}</div>
        <div class="request-message">{{ request.message || '请求加你为好友' }}</div>
        <div class="request-time">{{ request.createTime?.substring(0, 10) }}</div>
      </div>
      <div class="request-actions">
        <button class="action-btn accept" @click="$emit('accept', request.id)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </button>
        <button class="action-btn reject" @click="$emit('reject', request.id)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
  <div v-else class="empty-state">
    <div class="empty-icon">
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <rect x="2" y="4" width="20" height="16" rx="2"/>
        <path d="M22 7l-10 7L2 7"/>
      </svg>
    </div>
    <div class="empty-title">暂无好友申请</div>
    <div class="empty-subtitle">新的好友申请会在这里显示</div>
  </div>
</template>

<script setup lang="ts">
interface FriendRequestItem {
  id: number
  fromAvatar?: string
  fromNickname?: string
  message?: string
  createTime?: string
}

defineProps<{
  requests: FriendRequestItem[]
}>()

defineEmits<{
  (event: 'accept', requestId: number): void
  (event: 'reject', requestId: number): void
}>()
</script>

<style scoped>
.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.request-card {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 16px;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  transition: var(--linkx-transition);
}

.request-card:hover {
  border-color: var(--linkx-primary);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
}

.request-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #ffaa00 0%, #ffcc00 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(255, 170, 0, 0.3);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.request-info {
  flex: 1;
  min-width: 0;
}

.request-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  margin-bottom: 4px;
}

.request-message {
  font-size: 13px;
  color: var(--linkx-text-secondary);
  margin-bottom: 4px;
}

.request-time {
  font-size: 11px;
  color: var(--linkx-text-muted);
}

.request-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.action-btn.accept {
  background: rgba(0, 214, 143, 0.1);
  color: var(--linkx-primary);
}

.action-btn.accept:hover {
  background: var(--linkx-primary);
  color: white;
}

.action-btn.reject {
  background: rgba(255, 61, 113, 0.1);
  color: var(--linkx-error);
}

.action-btn.reject:hover {
  background: var(--linkx-error);
  color: white;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 8px;
  padding: 40px 20px;
  color: var(--linkx-text-muted);
}

.empty-icon {
  opacity: 0.3;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.empty-subtitle {
  font-size: 13px;
}

@media (max-width: 820px) {
  .request-card {
    gap: 12px;
  }
}

@media (max-width: 560px) {
  .request-card {
    padding: 12px 10px;
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .request-actions {
    width: 100%;
  }
}
</style>
