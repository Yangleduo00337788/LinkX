<template>
  <div v-if="groupRequests.length > 0" class="request-list">
    <div
      v-for="request in groupRequests"
      :key="request.id"
      class="request-card group-request-card"
    >
      <div class="request-avatar group-request-avatar">
        <img v-if="request.groupAvatar" :src="request.groupAvatar" class="avatar-img" />
        <span v-else>{{ request.groupName?.charAt(0) || '群' }}</span>
      </div>
      <div class="request-info">
        <div class="request-title-row">
          <div class="request-name">{{ request.groupName }}</div>
          <span class="request-type-tag" :class="groupRequestTagClass(request.requestType)">
            {{ groupRequestTypeText(request.requestType) }}
          </span>
        </div>
        <div class="request-message">
          {{ buildGroupRequestMessage(request) }}
        </div>
        <div class="request-time">{{ formatRequestTime(request.createTime) }}</div>
      </div>
      <div class="request-actions">
        <button
          class="action-btn accept"
          :disabled="isRequestActionLoading(request.id)"
          @click="$emit('accept', request.id)"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
        </button>
        <button
          class="action-btn reject"
          :disabled="isRequestActionLoading(request.id)"
          @click="$emit('reject', request.id)"
        >
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
        <path d="M12 5v14"/>
        <path d="M5 12h14"/>
        <rect x="3" y="3" width="18" height="18" rx="4"/>
      </svg>
    </div>
    <div class="empty-title">暂无群通知</div>
    <div class="empty-subtitle">入群申请和邀请会在这里显示</div>
  </div>
</template>

<script setup lang="ts">
import type { GroupRequestItem } from '../../types/chat'

defineProps<{
  groupRequests: GroupRequestItem[]
  groupRequestTypeText: (requestType: number) => string
  groupRequestTagClass: (requestType: number) => string
  buildGroupRequestMessage: (request: GroupRequestItem) => string
  formatRequestTime: (time?: string) => string
  isRequestActionLoading: (requestId: string | number) => boolean
}>()

defineEmits<{
  (event: 'accept', requestId: string | number): void
  (event: 'reject', requestId: string | number): void
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

.group-request-card {
  align-items: flex-start;
}

.request-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  overflow: hidden;
}

.group-request-avatar {
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.28);
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

.request-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.request-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
}

.request-type-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: var(--linkx-radius-full);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  border: 1px solid transparent;
}

.request-type-tag.join {
  color: #ffb020;
  background: rgba(255, 176, 32, 0.12);
  border-color: rgba(255, 176, 32, 0.22);
}

.request-type-tag.invite {
  color: var(--linkx-primary);
  background: rgba(0, 214, 143, 0.12);
  border-color: rgba(0, 214, 143, 0.22);
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

.action-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
  box-shadow: none;
}

.action-btn.accept {
  background: rgba(0, 214, 143, 0.1);
  color: var(--linkx-primary);
}

.action-btn.accept:hover:not(:disabled) {
  background: var(--linkx-primary);
  color: white;
}

.action-btn.reject {
  background: rgba(255, 61, 113, 0.1);
  color: var(--linkx-error);
}

.action-btn.reject:hover:not(:disabled) {
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
