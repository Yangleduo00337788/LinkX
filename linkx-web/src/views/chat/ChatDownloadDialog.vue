<template>
  <div v-if="visible" class="download-overlay" @click.self="$emit('close')">
    <div class="download-dialog">
      <div class="download-header">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--linkx-primary)" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <div class="download-filename">{{ fileName }}</div>
        <div class="download-size">{{ fileSize || '未知大小' }}</div>
      </div>
      <div v-if="progress < 100" class="download-progress">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="progress-text">{{ progress }}%</div>
      </div>
      <div v-else class="download-actions">
        <button class="download-btn open" @click="$emit('open')">打开</button>
        <button class="download-btn save" @click="$emit('save')">保存</button>
      </div>
      <button class="download-cancel" @click="$emit('close')">取消</button>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  fileName: string
  fileSize: string
  progress: number
}>()

defineEmits<{
  close: []
  open: []
  save: []
}>()
</script>

<style scoped>
.download-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.download-dialog {
  width: 360px;
  max-width: calc(100vw - 24px);
  max-height: calc(100vh - 24px);
  padding: 32px;
  text-align: center;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-lg);
}

.download-header {
  margin-bottom: 24px;
}

.download-filename {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text);
  word-break: break-all;
}

.download-size {
  margin-top: 8px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.download-progress {
  margin-bottom: 24px;
}

.progress-bar {
  width: 100%;
  height: 6px;
  overflow: hidden;
  background: var(--linkx-bg);
  border-radius: 3px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  margin-top: 8px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.download-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 16px;
}

.download-btn,
.download-cancel {
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.download-btn {
  flex: 1;
  padding: 12px;
}

.download-btn.open {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.download-btn.open:hover {
  background: var(--linkx-primary-hover);
}

.download-btn.save,
.download-cancel {
  background: transparent;
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text);
}

.download-cancel {
  width: 100%;
  height: 40px;
  margin-top: 4px;
}
</style>
