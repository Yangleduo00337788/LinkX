<template>
  <Teleport to="body">
    <div v-if="visible" class="image-preview-overlay" @click.self="$emit('close')">
      <button class="image-preview-close" type="button" @click="$emit('close')">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>
      <div class="image-preview-stage">
        <div class="image-preview-container" @click.stop>
          <img :src="imageUrl" class="image-preview-img" />
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean
  imageUrl: string
}>()

defineEmits<{
  close: []
}>()
</script>

<style scoped>
.image-preview-overlay {
  position: fixed;
  inset: 0;
  z-index: 3000;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.9);
}

.image-preview-stage {
  display: flex;
  width: 100%;
  height: 100%;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.image-preview-container {
  display: flex;
  max-width: 100%;
  max-height: 100%;
  align-items: center;
  justify-content: center;
}

.image-preview-img {
  display: block;
  max-width: min(100%, 1600px);
  max-height: calc(100vh - 64px);
  object-fit: contain;
  border-radius: var(--linkx-radius-sm);
}

.image-preview-close {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 3001;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  cursor: pointer;
}
</style>
