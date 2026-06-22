<template>
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <div class="modal-card add-members-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">邀请成员</div>
          <div class="modal-subtitle">向好友发送入群邀请，待对方确认后加入</div>
        </div>
        <button class="modal-close" @click="$emit('close')">x</button>
      </div>

      <div v-if="availableFriends.length > 0" class="member-picker-list compact">
        <label v-for="friend in availableFriends" :key="friend.friendId" class="member-picker-item">
          <input
            :checked="isSelected(friend.friendId)"
            :disabled="addingMembers"
            type="checkbox"
            @change="toggleMemberSelection(friend.friendId, ($event.target as HTMLInputElement).checked)"
          />
          <div class="member-picker-avatar">
            <img v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
            <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
          </div>
          <div class="member-picker-info">
            <span class="member-picker-name">{{ friend.friendNickname }}</span>
            <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
          </div>
        </label>
      </div>
      <div v-else class="panel-placeholder">所有好友都已在群内，暂无可邀请成员。</div>

      <div class="form-section">
        <label class="form-label">邀请说明</label>
        <textarea
          :value="inviteMessage"
          class="modal-textarea"
          rows="3"
          maxlength="255"
          placeholder="选填，邀请说明会出现在对方的群通知中"
          @input="handleInviteMessageInput"
        ></textarea>
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" :disabled="addingMembers" @click="$emit('close')">取消</button>
        <button class="primary-btn" :disabled="addingMembers || !hasSelection" @click="$emit('submit')">
          {{ addingMembers ? '邀请中...' : '确认邀请' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { FriendItem } from '../../types/chat'

const props = defineProps<{
  visible: boolean
  availableFriends: FriendItem[]
  selectedMemberIds: Array<string | number>
  inviteMessage: string
  addingMembers: boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:selectedMemberIds', value: Array<string | number>): void
  (event: 'update:inviteMessage', value: string): void
  (event: 'submit'): void
}>()

const hasSelection = computed(() => props.selectedMemberIds.length > 0)

function isSelected(friendId: string | number) {
  return props.selectedMemberIds.some(item => String(item) === String(friendId))
}

function toggleMemberSelection(friendId: string | number, checked: boolean) {
  const nextSelection = checked
    ? [...props.selectedMemberIds, friendId]
    : props.selectedMemberIds.filter(item => String(item) !== String(friendId))
  emit('update:selectedMemberIds', nextSelection)
}

function handleInviteMessageInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:inviteMessage', target.value || '')
}
</script>

<style scoped>
.overlay-panel {
  position: fixed;
  inset: 0;
  z-index: 2200;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 56px 24px 24px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-card {
  width: min(560px, 100%);
  max-width: calc(100vw - 24px);
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  background: var(--linkx-bg-card);
  box-shadow: var(--linkx-shadow-lg);
}

.modal-header,
.modal-actions,
.member-picker-item {
  display: flex;
  align-items: center;
}

.modal-header,
.modal-actions {
  justify-content: space-between;
}

.modal-header {
  margin-bottom: 20px;
}

.modal-title {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.modal-subtitle,
.member-picker-meta,
.panel-placeholder {
  color: var(--linkx-text-muted);
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 18px 0;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.modal-close:hover,
.secondary-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.member-picker-list {
  overflow: auto;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
}

.member-picker-list.compact {
  max-height: 360px;
}

.member-picker-item {
  gap: 12px;
  padding: 12px 14px;
  cursor: pointer;
}

.member-picker-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.member-picker-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--linkx-radius-sm);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-weight: 700;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.member-picker-info {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.member-picker-name {
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 700;
}

.member-picker-meta {
  font-size: 12px;
}

.panel-placeholder {
  padding: 18px 14px;
  border: 1px dashed var(--linkx-border);
  border-radius: var(--linkx-radius);
  font-size: 13px;
}

.modal-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  outline: none;
  resize: vertical;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
}

.modal-textarea:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.modal-actions {
  gap: 12px;
}

.secondary-btn,
.primary-btn {
  height: 40px;
  padding: 0 18px;
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.secondary-btn {
  border: 1px solid var(--linkx-border);
  background: transparent;
  color: var(--linkx-text);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .overlay-panel {
    padding: 20px 10px;
  }

  .modal-card {
    width: calc(100vw - 20px);
    padding: 20px 16px;
  }

  .modal-actions {
    flex-wrap: wrap;
  }

  .secondary-btn,
  .primary-btn {
    width: 100%;
  }
}
</style>
