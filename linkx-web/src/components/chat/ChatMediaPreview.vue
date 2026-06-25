<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染 Teleport 组件 -->
  <Teleport to="body">
    <!-- 行注：渲染容器 -->
    <div v-if="visible" class="image-preview-overlay" @click.self="$emit('close')">
      <!-- 行注：渲染按钮 -->
      <button class="image-preview-close" type="button" @click="$emit('close')">
        <!-- 行注：渲染图标容器 -->
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标线段 -->
          <line x1="18" y1="6" x2="6" y2="18" />
          <!-- 行注：补充图标线段 -->
          <line x1="6" y1="6" x2="18" y2="18" />
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染容器 -->
      <div class="image-preview-stage">
        <!-- 行注：渲染容器 -->
        <div class="image-preview-container" @click.stop>
          <!-- 行注：渲染图片 -->
          <img :src="imageUrl" class="image-preview-img" />
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束Teleport 节点 -->
  </Teleport>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * ChatMediaPreview 组件，负责当前界面片段的展示与交互。
 */
defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  imageUrl: string  // 行注：设置 imageUrl 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  close: []  // 行注：设置 close 配置项
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.image-preview-overlay {  /* 行注：定义 .image-preview-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  z-index: 3000;  /* 行注：设置 z-index 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(0, 0, 0, 0.9);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.image-preview-stage {  /* 行注：定义 .image-preview-stage 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding: 32px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.image-preview-container {  /* 行注：定义 .image-preview-container 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  max-width: 100%;  /* 行注：设置 max-width 样式 */
  max-height: 100%;  /* 行注：设置 max-height 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.image-preview-img {  /* 行注：定义 .image-preview-img 样式 */
  display: block;  /* 行注：设置 display 样式 */
  max-width: min(100%, 1600px);  /* 行注：设置 max-width 样式 */
  max-height: calc(100vh - 64px);  /* 行注：设置 max-height 样式 */
  object-fit: contain;  /* 行注：设置 object-fit 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.image-preview-close {  /* 行注：定义 .image-preview-close 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  top: 20px;  /* 行注：设置 top 样式 */
  right: 20px;  /* 行注：设置 right 样式 */
  z-index: 3001;  /* 行注：设置 z-index 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  background: rgba(255, 255, 255, 0.1);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */
</style>
