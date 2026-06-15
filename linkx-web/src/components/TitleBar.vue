<template>
  <div class="titlebar" :class="{ 'is-maximized': isMaximized }">
    <div class="titlebar-drag">
      <div class="titlebar-brand">
        <img :src="logoImg" class="brand-icon-img" alt="LinkX" />
        <span class="brand-text">LinkX</span>
      </div>
    </div>

    <div class="titlebar-controls">
      <button
        class="titlebar-btn minimize"
        @click="minimize"
        title="最小化"
      >
        <svg width="10" height="1" viewBox="0 0 10 1">
          <rect fill="currentColor" width="10" height="1"/>
        </svg>
      </button>

      <button
        class="titlebar-btn maximize"
        @click="maximize"
        :title="isMaximized ? '还原' : '最大化'"
      >
        <svg v-if="!isMaximized" width="10" height="10" viewBox="0 0 10 10">
          <rect fill="none" stroke="currentColor" x="0.5" y="0.5" width="9" height="9" rx="1"/>
        </svg>
        <svg v-else width="10" height="10" viewBox="0 0 10 10">
          <rect fill="none" stroke="currentColor" x="2" y="0" width="8" height="8" rx="1"/>
          <rect fill="var(--linkx-bg-card)" stroke="currentColor" x="0" y="2" width="8" height="8" rx="1"/>
        </svg>
      </button>

      <button
        class="titlebar-btn close"
        @click="close"
        title="关闭"
      >
        <svg width="10" height="10" viewBox="0 0 10 10">
          <path fill="currentColor" d="M1 0L0 1L4 5L0 9L1 10L5 6L9 10L10 9L6 5L10 1L9 0L5 4L1 0Z"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import logoImg from '../assets/tray.png'

const isMaximized = ref(false)

const electronAPI = (window as any).electronAPI

onMounted(async () => {
  if (electronAPI) {
    isMaximized.value = await electronAPI.isMaximized()
    electronAPI.onWindowMaximize((maximized: boolean) => {
      isMaximized.value = maximized
    })
  }
})

async function minimize() {
  await electronAPI?.minimizeWindow()
}

async function maximize() {
  await electronAPI?.maximizeWindow()
}

async function close() {
  await electronAPI?.closeWindow()
}
</script>

<style scoped>
.titlebar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 32px;
  background: var(--linkx-bg);
  border-bottom: 1px solid var(--linkx-border);
  user-select: none;
  position: relative;
  z-index: 100;
}

.titlebar::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--linkx-primary-glow), transparent);
  opacity: 0.5;
}

.titlebar-drag {
  flex: 1;
  -webkit-app-region: drag;
  display: flex;
  align-items: center;
  padding-left: 12px;
  height: 100%;
}

.titlebar-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.brand-icon-img {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  object-fit: contain;
}

.brand-text {
  font-size: 13px;
  font-weight: 700;
  color: var(--linkx-text);
  letter-spacing: 0.5px;
}

.titlebar-controls {
  display: flex;
  -webkit-app-region: no-drag;
  height: 100%;
}

.titlebar-btn {
  width: 46px;
  height: 100%;
  border: none;
  background: transparent;
  color: var(--linkx-text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: var(--linkx-transition-fast);
  position: relative;
}

.titlebar-btn::before {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 4px;
  background: transparent;
  transition: background 0.15s ease;
}

.titlebar-btn:hover::before {
  background: rgba(255, 255, 255, 0.08);
}

.titlebar-btn:active::before {
  background: rgba(255, 255, 255, 0.04);
}

.titlebar-btn.close:hover::before {
  background: var(--linkx-error);
}

.titlebar-btn.close:hover {
  color: white;
}

.titlebar-btn svg {
  position: relative;
  z-index: 1;
}
</style>
