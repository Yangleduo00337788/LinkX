<template>
  <div v-if="visible" class="group-notice-dialog-overlay" @click.self="handleClose">
    <div class="group-notice-dialog-card">
      <div class="group-notice-dialog-header">
        <div>
          <div class="group-notice-dialog-title">群公告提醒</div>
          <div class="group-notice-dialog-subtitle">{{ groupName || '当前群聊' }}</div>
        </div>
        <button class="group-notice-dialog-close" :disabled="loading" @click="handleClose">×</button>
      </div>

      <div class="group-notice-dialog-meta">
        <span v-if="updateTimeText">发布时间：{{ updateTimeText }}</span>
        <span v-else>你有一条新的群公告待查看</span>
      </div>

      <div class="group-notice-dialog-body">
        {{ notice?.trim() || '暂无群公告内容' }}
      </div>

      <div class="group-notice-dialog-actions">
        <button class="group-notice-dialog-primary-btn" :disabled="loading" @click="handleAcknowledge">
          {{ loading ? '我知道了...' : '我知道了' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  visible: boolean
  groupName?: string
  notice?: string
  updateTimeText?: string
  loading?: boolean
}>(), {
  groupName: '',
  notice: '',
  updateTimeText: '',
  loading: false
})

const emit = defineEmits<{
  'update:visible': [value: boolean]
  close: []
  acknowledge: []
}>()

function handleClose() {
  if (props.loading) {
    return
  }
  emit('update:visible', false)
  emit('close')
}

function handleAcknowledge() {
  if (props.loading) {
    return
  }
  emit('acknowledge')
}
</script>

<style scoped>
.group-notice-dialog-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 56px 24px 24px;
  background: rgba(0, 0, 0, 0.54);
  backdrop-filter: blur(4px);
  z-index: 2250;
}

.group-notice-dialog-card {
  width: min(460px, 100%);
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-lg);
  padding: 24px;
}

.group-notice-dialog-header,
.group-notice-dialog-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.group-notice-dialog-title {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.group-notice-dialog-subtitle,
.group-notice-dialog-meta {
  color: var(--linkx-text-muted);
}

.group-notice-dialog-subtitle {
  margin-top: 4px;
  font-size: 13px;
}

.group-notice-dialog-close {
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

.group-notice-dialog-meta {
  margin-top: 16px;
  font-size: 12px;
}

.group-notice-dialog-body {
  margin-top: 14px;
  padding: 16px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 74%, transparent);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 84%, transparent);
  color: var(--linkx-text);
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.group-notice-dialog-actions {
  margin-top: 20px;
  justify-content: flex-end;
}

.group-notice-dialog-primary-btn {
  min-width: 120px;
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: var(--linkx-radius);
  background: var(--linkx-primary);
  color: white;
  font-size: 14px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.group-notice-dialog-primary-btn:hover {
  background: var(--linkx-primary-hover);
}

@media (max-width: 640px) {
  .group-notice-dialog-overlay {
    padding: 56px 16px 16px;
  }

  .group-notice-dialog-actions {
    justify-content: stretch;
  }

  .group-notice-dialog-primary-btn {
    width: 100%;
  }
}
</style>
