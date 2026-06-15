<template>
  <div v-if="visible" class="confirm-dialog-overlay" @click.self="handleCancel">
    <div class="confirm-dialog-card">
      <div class="confirm-dialog-header">
        <div>
          <div class="confirm-dialog-title">{{ title }}</div>
          <div v-if="subtitle" class="confirm-dialog-subtitle">{{ subtitle }}</div>
        </div>
        <button class="confirm-dialog-close" @click="handleCancel">×</button>
      </div>

      <div class="confirm-dialog-body">
        <div class="confirm-dialog-description">{{ description }}</div>
      </div>

      <div class="confirm-dialog-actions">
        <button class="confirm-dialog-secondary-btn" @click="handleCancel">{{ cancelText }}</button>
        <button class="confirm-dialog-primary-btn" :class="{ danger: confirmType === 'danger' }" @click="handleConfirm">
          {{ confirmText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
type ConfirmType = 'primary' | 'danger'

const props = withDefaults(defineProps<{
  visible: boolean
  title: string
  subtitle?: string
  description: string
  cancelText?: string
  confirmText?: string
  confirmType?: ConfirmType
}>(), {
  subtitle: '',
  cancelText: '取消',
  confirmText: '确认',
  confirmType: 'primary'
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  cancel: []
  confirm: []
}>()

function handleCancel() {
  emit('update:visible', false)
  emit('cancel')
}

function handleConfirm() {
  emit('update:visible', false)
  emit('confirm')
}
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 56px 24px 24px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 2200;
}

.confirm-dialog-card {
  width: min(420px, 100%);
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-lg);
  padding: 24px;
}

.confirm-dialog-header,
.confirm-dialog-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.confirm-dialog-header {
  margin-bottom: 20px;
}

.confirm-dialog-title {
  font-size: 18px;
  color: var(--linkx-text);
  font-weight: 700;
}

.confirm-dialog-subtitle,
.confirm-dialog-description {
  color: var(--linkx-text-muted);
}

.confirm-dialog-subtitle {
  margin-top: 4px;
  font-size: 13px;
}

.confirm-dialog-body {
  margin-bottom: 18px;
}

.confirm-dialog-description {
  font-size: 13px;
  line-height: 1.6;
}

.confirm-dialog-close {
  width: 32px;
  height: 32px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-md);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.confirm-dialog-primary-btn,
.confirm-dialog-secondary-btn {
  height: 40px;
  padding: 0 18px;
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.confirm-dialog-secondary-btn {
  background: transparent;
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text);
}

.confirm-dialog-primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.confirm-dialog-primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.confirm-dialog-primary-btn.danger {
  background: #ff6b6b;
}

.confirm-dialog-primary-btn.danger:hover {
  background: #ff5f5f;
}

@media (max-width: 640px) {
  .confirm-dialog-overlay {
    padding: 56px 16px 16px;
  }

  .confirm-dialog-actions {
    flex-direction: column;
    gap: 12px;
  }

  .confirm-dialog-secondary-btn,
  .confirm-dialog-primary-btn {
    width: 100%;
  }
}
</style>
