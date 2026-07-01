<template>
  <div ref="rootRef" class="chat-plus-menu">
    <button type="button" class="plus-btn" title="更多" aria-label="更多" @click="open = !open">
      <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="12" y1="5" x2="12" y2="19" />
        <line x1="5" y1="12" x2="19" y2="12" />
      </svg>
    </button>
    <div v-if="open" class="plus-dropdown">
      <button type="button" class="plus-item" @click="onCreateGroup">发起群聊</button>
      <button type="button" class="plus-item" @click="onAddFriend">添加朋友</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const emit = defineEmits<{
  (e: 'create-group'): void
  (e: 'add-friend'): void
}>()

const open = ref(false)
const rootRef = ref<HTMLElement | null>(null)

function close() {
  open.value = false
}

function onCreateGroup() {
  close()
  emit('create-group')
}

function onAddFriend() {
  close()
  emit('add-friend')
}

function onDocClick(e: MouseEvent) {
  if (!rootRef.value?.contains(e.target as Node)) {
    close()
  }
}

onMounted(() => document.addEventListener('click', onDocClick))
onBeforeUnmount(() => document.removeEventListener('click', onDocClick))
</script>

<style scoped>
.chat-plus-menu {
  position: relative;
  flex-shrink: 0;
}

.plus-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--linkx-text-muted);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.plus-btn:hover {
  background: var(--linkx-bg-hover, rgba(0, 0, 0, 0.06));
  color: var(--linkx-text);
}

.plus-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 6px;
  min-width: 140px;
  padding: 6px 0;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  z-index: 50;
}

.plus-item {
  display: block;
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: none;
  text-align: left;
  font-size: 14px;
  color: var(--linkx-text);
  cursor: pointer;
}

.plus-item:hover {
  background: var(--linkx-bg-hover, rgba(0, 0, 0, 0.06));
}
</style>