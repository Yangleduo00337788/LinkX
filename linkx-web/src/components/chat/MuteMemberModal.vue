<template>
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <div class="modal-card mute-member-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">设置禁言</div>
          <div class="modal-subtitle">为该成员设置禁言时长</div>
        </div>
        <button class="modal-close" @click="$emit('close')">x</button>
      </div>

      <div class="form-section">
        <label class="form-label">成员</label>
        <div class="readonly-field">
          {{ member?.nickname || member?.username || '未选择成员' }}
        </div>
      </div>

      <div class="form-section">
        <label class="form-label">禁言时长（分钟）</label>
        <input
          :value="muteMinutesInput"
          type="number"
          min="1"
          max="43200"
          step="1"
          class="modal-input"
          placeholder="请输入 1 - 43200 之间的整数"
          @input="handleInput"
          @keyup.enter="$emit('submit')"
        />
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" :disabled="mutingMember" @click="$emit('close')">取消</button>
        <button class="primary-btn" :disabled="mutingMember" @click="$emit('submit')">
          {{ mutingMember ? '提交中...' : '确认禁言' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { GroupMember } from '../../types/chat'

defineProps<{
  visible: boolean
  member: GroupMember | null
  muteMinutesInput: string
  mutingMember: boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'update:muteMinutesInput', value: string): void
  (event: 'submit'): void
}>()

function handleInput(event: Event) {
  const target = event.target as HTMLInputElement
  emit('update:muteMinutesInput', target.value || '')
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
  width: min(420px, 100%);
  max-width: calc(100vw - 24px);
  display: flex;
  flex-direction: column;
  padding: 24px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  background: var(--linkx-bg-card);
  box-shadow: var(--linkx-shadow-lg);
}

.modal-header,
.modal-actions {
  display: flex;
  align-items: center;
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

.modal-subtitle {
  color: var(--linkx-text-muted);
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 18px;
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

.readonly-field,
.modal-input {
  width: 100%;
  min-height: 46px;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
}

.readonly-field {
  display: flex;
  align-items: center;
  line-height: 1.5;
}

.modal-input {
  outline: none;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
}

.modal-input:focus {
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
