<template>
  <div v-if="searchResults.length > 0" class="result-list">
    <div
      v-for="user in searchResults"
      :key="user.id"
      class="result-card"
    >
      <div class="result-avatar">
        <img v-if="user.avatar" :src="user.avatar" class="avatar-img" />
        <span v-else>{{ user.nickname?.charAt(0) }}</span>
      </div>
      <div class="result-info">
        <div class="result-name">{{ user.nickname }}</div>
        <div class="result-username">@{{ user.username }}</div>
      </div>
      <button class="add-friend-btn" @click="$emit('add-friend', user.id)">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
        添加好友
      </button>
    </div>
  </div>
  <div v-else class="empty-state">
    <div class="empty-icon">
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="11" cy="11" r="8"/>
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
    </div>
    <div class="empty-title">{{ searchKeyword ? '未找到用户' : '搜索用户' }}</div>
    <div class="empty-subtitle">{{ searchKeyword ? '尝试其他关键词' : '输入用户名搜索' }}</div>
  </div>
</template>

<script setup lang="ts">
interface SearchUserItem {
  id: string | number
  avatar?: string
  nickname?: string
  username?: string
}

defineProps<{
  searchKeyword: string
  searchResults: SearchUserItem[]
}>()

defineEmits<{
  (event: 'add-friend', userId: string | number): void
}>()
</script>

<style scoped>
.result-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-card {
  display: flex;
  align-items: center;
  padding: 16px;
  gap: 16px;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  transition: var(--linkx-transition);
}

.result-card:hover {
  border-color: var(--linkx-primary);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
}

.result-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 2px 8px var(--linkx-primary-glow);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.result-info {
  flex: 1;
  min-width: 0;
}

.result-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  margin-bottom: 4px;
}

.result-username {
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.add-friend-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.add-friend-btn:hover {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
  transform: translateY(-1px);
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
  .result-card {
    gap: 12px;
  }
}

@media (max-width: 560px) {
  .result-card {
    padding: 12px 10px;
    align-items: flex-start;
    flex-wrap: wrap;
  }

  .add-friend-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
