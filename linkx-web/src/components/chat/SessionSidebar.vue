<template>
  <div class="session-panel">
    <div class="panel-header">
      <span class="header-title">消息</span>
      <button class="create-group-btn" @click="$emit('create-group')">建群</button>
    </div>

    <div class="session-search">
      <div class="search-input-wrapper">
        <n-icon :component="SearchOutline" class="search-icon" />
        <input
          :value="searchText"
          type="text"
          placeholder="搜索会话..."
          class="search-input"
          @input="handleSearchInput"
        />
        <div v-if="searchText" class="search-clear" @click="$emit('update:searchText', '')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.2" />
            <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
        </div>
      </div>
    </div>

    <div class="session-list">
      <div
        v-for="session in filteredSessions"
        :key="buildSessionKey(session.targetId, session.sessionType)"
        class="session-item"
        :class="{
          active: isCurrentSession(session),
          flash: flashSessionKey === buildSessionKey(session.targetId, session.sessionType)
        }"
        @click="$emit('select-session', session)"
      >
        <div class="session-avatar" :class="{ 'has-unread': session.unreadCount > 0, group: session.sessionType === SESSION_TYPE_GROUP }">
          <img v-if="session.targetAvatar" :src="session.targetAvatar" class="avatar-img" />
          <span v-else class="avatar-text">{{ session.targetNickname?.charAt(0) || (session.sessionType === SESSION_TYPE_GROUP ? '群' : '聊') }}</span>
          <div class="session-type-badge" :class="{ group: session.sessionType === SESSION_TYPE_GROUP }">
            {{ session.sessionType === SESSION_TYPE_GROUP ? '群' : '单' }}
          </div>
          <div v-if="session.sessionType === SESSION_TYPE_SINGLE" class="online-indicator" :class="{ active: session.targetOnline }"></div>
          <div v-if="session.unreadCount > 0" class="unread-badge">
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          </div>
        </div>
        <div class="session-info">
          <div class="session-header">
            <div class="session-title-row">
              <span class="session-name">{{ session.targetNickname }}</span>
              <span v-if="session.sessionType === SESSION_TYPE_GROUP" class="session-tag">{{ session.memberCount || 0 }}人</span>
              <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.noticeUnread" class="session-tag notice">新公告</span>
            </div>
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          </div>
          <div class="session-preview-row">
            <span class="session-preview">{{ session.lastMessage || (session.sessionType === SESSION_TYPE_GROUP ? '群聊已创建' : '暂无消息') }}</span>
            <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.muted" class="session-muted">已禁言</span>
          </div>
        </div>
      </div>

      <div v-if="sessions.length === 0 && !loadingSessions" class="empty-state">
        <div class="empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
        </div>
        <div class="empty-title">暂无会话</div>
        <div class="empty-subtitle">创建群聊或找好友开始聊天</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NIcon } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { SESSION_TYPE_GROUP, SESSION_TYPE_SINGLE, type ChatSession } from '../../types/chat'
import { buildSessionKey, formatTime } from '../../utils/chat'

const props = defineProps<{
  sessions: ChatSession[]
  loadingSessions: boolean
  currentTargetId: string | null
  currentSessionType: number
  searchText: string
  flashSessionKey: string | null
}>()

const emit = defineEmits<{
  (event: 'create-group'): void
  (event: 'select-session', session: ChatSession): void
  (event: 'update:searchText', value: string): void
}>()

const filteredSessions = computed(() => {
  if (!props.searchText.trim()) {
    return props.sessions
  }
  const keyword = props.searchText.trim().toLowerCase()
  return props.sessions.filter(session =>
    session.targetNickname?.toLowerCase().includes(keyword)
    || session.lastMessage?.toLowerCase().includes(keyword)
  )
})

function isCurrentSession(session: ChatSession) {
  if (!props.currentTargetId) {
    return false
  }
  return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(props.currentTargetId, props.currentSessionType)
}

function handleSearchInput(event: Event) {
  const target = event.target as HTMLInputElement
  const value = target.value || ''
  emit('update:searchText', value)
}
</script>

<style scoped>
.session-panel {
  width: 320px;
  background: var(--linkx-bg-card);
  border-right: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  min-width: 280px;
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
}

.create-group-btn {
  height: 32px;
  padding: 0 14px;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: var(--linkx-primary);
  color: white;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.create-group-btn:hover {
  background: var(--linkx-primary-hover);
}

.session-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--linkx-border);
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--linkx-text-muted);
  font-size: 16px;
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 36px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 13px;
  transition: var(--linkx-transition);
  outline: none;
}

.search-input::placeholder,
.session-time,
.session-muted {
  color: var(--linkx-text-muted);
}

.search-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.search-clear {
  position: absolute;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
  cursor: pointer;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: var(--linkx-radius);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.session-item:hover {
  background: var(--linkx-bg-hover);
}

.session-item.active {
  background: var(--linkx-bg-active);
}

.session-item.flash {
  animation: flashHighlight 2s ease-out;
}

@keyframes flashHighlight {
  0%,
  100% {
    background: transparent;
  }
  15%,
  45%,
  75% {
    background: var(--linkx-primary-glow);
  }
}

.session-avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  flex-shrink: 0;
  overflow: hidden;
  color: white;
  font-size: 18px;
  font-weight: 700;
}

.session-avatar.group {
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.session-type-badge,
.session-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-full);
  font-size: 11px;
  font-weight: 700;
}

.session-type-badge {
  position: absolute;
  left: -2px;
  bottom: -2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: rgba(0, 0, 0, 0.56);
  color: white;
}

.session-type-badge.group {
  background: rgba(77, 107, 255, 0.18);
  color: #90a7ff;
}

.session-tag {
  padding: 2px 8px;
  background: rgba(77, 107, 255, 0.12);
  color: #90a7ff;
}

.session-tag.notice {
  background: rgba(0, 214, 143, 0.14);
  color: var(--linkx-primary);
}

.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: var(--linkx-error);
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: var(--linkx-radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.online-indicator {
  position: absolute;
  right: -2px;
  bottom: 2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 2px solid var(--linkx-bg-card);
  background: #64748b;
}

.online-indicator.active {
  background: var(--linkx-primary);
}

.session-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.session-title-row,
.session-preview-row {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.session-name {
  color: var(--linkx-text);
  font-weight: 700;
  font-size: 14px;
}

.session-preview,
.empty-subtitle {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.session-preview {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-state {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--linkx-text-muted);
}

.empty-icon {
  opacity: 0.2;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}
</style>
