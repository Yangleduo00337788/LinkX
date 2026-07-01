<template>
  <Teleport to="body">
    <div v-if="visible" class="dtw-backdrop" @click.self="$emit('close')">
      <div
        class="dtw-window"
        :style="{ width: `${width}px`, height: `${height}px` }"
        role="dialog"
        :aria-label="title"
        @click.stop
      >
        <header v-if="showTitleBar" class="dtw-titlebar">
          <span class="dtw-title">{{ title }}</span>
          <button type="button" class="dtw-close" aria-label="关闭" @click="$emit('close')">×</button>
        </header>
        <div class="dtw-body" :class="{ 'no-title': !showTitleBar }">
          <slot />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  title: string
  width: number
  height: number
  showTitleBar?: boolean
}>()

defineEmits<{ (e: 'close'): void }>()
</script>

<style scoped>
.dtw-backdrop {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
  cursor: default;
}

.dtw-window {
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.dtw-titlebar {
  height: 40px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #ededed;
  border-bottom: 1px solid #dcdcdc;
  flex-shrink: 0;
}

.dtw-title {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.dtw-close {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  font-size: 18px;
  color: #666;
  cursor: pointer;
  border-radius: 4px;
}

.dtw-close:hover {
  background: rgba(0, 0, 0, 0.06);
}

.dtw-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.dtw-body.no-title {
  border-radius: 8px;
}
</style>