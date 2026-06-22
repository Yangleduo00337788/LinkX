<template>
  <Teleport to="body">
    <div v-if="visible && selectedFriend" class="user-info-overlay" @click.self="$emit('close')">
      <div class="user-info-popup">
        <button class="user-info-close" @click="$emit('close')">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
        <div class="user-info-avatar">
          <img v-if="selectedFriend.friendAvatar" :src="selectedFriend.friendAvatar" class="user-info-avatar-img" />
          <span v-else>{{ selectedFriend.friendNickname?.charAt(0) }}</span>
        </div>
        <div class="user-info-name">{{ selectedFriend.friendNickname }}</div>
        <div class="user-info-username">@{{ selectedFriend.friendUsername }}</div>
        <div class="user-info-details">
          <div class="user-info-detail-item">
            <span class="detail-label">昵称</span>
            <span class="detail-value">{{ selectedFriend.friendNickname }}</span>
          </div>
          <div class="user-info-detail-item">
            <span class="detail-label">用户名</span>
            <span class="detail-value">{{ selectedFriend.friendUsername }}</span>
          </div>
        </div>
        <button class="user-info-send-btn" @click="$emit('send-message', selectedFriend.friendId)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          发送消息
        </button>
        <button class="user-info-blacklist-btn" @click="$emit('blacklist', selectedFriend.friendId)">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
          </svg>
          拉黑
        </button>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
interface FriendListItem {
  friendId: string | number
  friendAvatar?: string
  friendNickname?: string
  friendUsername?: string
}

defineProps<{
  visible: boolean
  selectedFriend: FriendListItem | null
}>()

defineEmits<{
  (event: 'close'): void
  (event: 'send-message', friendId: string | number): void
  (event: 'blacklist', friendId: string | number): void
}>()
</script>

<style scoped>
.user-info-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.user-info-popup {
  width: 300px;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius-lg);
  padding: 24px;
  text-align: center;
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
  position: relative;
}

.user-info-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg-hover);
  border: none;
  border-radius: 50%;
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.user-info-close:hover {
  background: var(--linkx-error);
  color: white;
}

.user-info-avatar {
  width: 72px;
  height: 72px;
  border-radius: var(--linkx-radius-lg);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  font-size: 28px;
  font-weight: 700;
  color: white;
  overflow: hidden;
}

.user-info-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-info-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 2px;
}

.user-info-username {
  font-size: 13px;
  color: var(--linkx-text-secondary);
  margin-bottom: 16px;
}

.user-info-details {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  overflow: hidden;
  margin-bottom: 16px;
}

.user-info-detail-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
}

.user-info-detail-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.detail-label {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.detail-value {
  font-size: 13px;
  color: var(--linkx-text);
}

.user-info-send-btn,
.user-info-blacklist-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.user-info-send-btn {
  background: var(--linkx-primary);
  border: none;
  color: white;
}

.user-info-send-btn:hover {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
  transform: translateY(-1px);
}

.user-info-blacklist-btn {
  background: transparent;
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text-secondary);
  margin-top: 8px;
}

.user-info-blacklist-btn:hover {
  border-color: var(--linkx-error);
  color: var(--linkx-error);
  background: rgba(255, 61, 113, 0.05);
}

@media (max-width: 560px) {
  .user-info-popup {
    width: calc(100vw - 20px);
    max-height: calc(100vh - 20px);
    padding: 20px 16px;
    overflow-y: auto;
  }

  .user-info-detail-item {
    align-items: flex-start;
    gap: 12px;
  }

  .detail-value {
    text-align: right;
    word-break: break-word;
  }
}
</style>
