<template>
  <div class="child-frame" :style="frameStyle">
    <slot />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  width: number
  height: number
}>()

const frameStyle = computed(() => ({
  width: `${props.width}px`,
  height: `${props.height}px`,
  maxWidth: '100vw',
  maxHeight: '100vh'
}))
</script>

<style scoped>
.child-frame {
  margin: 0 auto;
  overflow: hidden;
  background: #f5f5f5;
  box-sizing: border-box;
}

:global(body.child-window-page) {
  margin: 0;
  background: #e8e8e8;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
}
</style>