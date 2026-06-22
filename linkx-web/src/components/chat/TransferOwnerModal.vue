<template>
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <div class="modal-card add-members-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">转让群主</div>
          <div class="modal-subtitle">选择一位成员成为新群主</div>
        </div>
        <button class="modal-close" @click="$emit('close')">x</button>
      </div>

      <div v-if="members.length > 0" class="member-picker-list compact">
        <label v-for="member in members" :key="member.userId" class="member-picker-item">
          <input
            :checked="String(selectedOwnerId) === String(member.userId)"
            :disabled="transferringOwner"
            type="radio"
            name="transferOwner"
            @change="emit('update:selectedOwnerId', member.userId)"
          />
          <div class="member-picker-avatar">
            <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
            <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
          </div>
          <div class="member-picker-info">
            <span class="member-picker-name">{{ member.nickname || member.username }}</span>
            <span class="member-picker-meta">@{{ member.username }} · {{ roleText(member.role) }}</span>
          </div>
        </label>
      </div>
      <div v-else class="panel-placeholder">群内没有其他成员可转让。</div>

      <div class="modal-actions">
        <button class="secondary-btn" :disabled="transferringOwner" @click="$emit('close')">取消</button>
        <button class="primary-btn" :disabled="!selectedOwnerId || transferringOwner" @click="$emit('submit')">
          {{ transferringOwner ? '转让中...' : '确认转让' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GroupMember } from '../../types/chat'
import { roleText } from '../../utils/chat'

defineProps<{
  visible: boolean
  members: GroupMember[]
  selectedOwnerId: string | number | null
  transferringOwner: boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:selectedOwnerId', value: string | number | null): void
  (event: 'submit'): void
}>()
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
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
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

.modal-actions {
  margin-top: 18px;
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
