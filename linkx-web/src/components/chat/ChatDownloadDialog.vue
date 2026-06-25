<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="download-overlay" @click.self="$emit('close')">
    <!-- 行注：渲染容器 -->
    <div class="download-dialog">
      <!-- 行注：渲染容器 -->
      <div class="download-header">
        <!-- 行注：渲染图标容器 -->
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--linkx-primary)" stroke-width="1.5">
          <!-- 行注：补充图标路径 -->
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <!-- 行注：补充图标折线 -->
          <polyline points="14 2 14 8 20 8" />
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：渲染容器 -->
        <div class="download-filename">{{ fileName }}</div>
        <!-- 行注：渲染容器 -->
        <div class="download-size">{{ fileSize || '未知大小' }}</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-if="progress < 100" class="download-progress">
        <!-- 行注：渲染容器 -->
        <div class="progress-bar">
          <!-- 行注：渲染容器 -->
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="progress-text">{{ progress }}%</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-else class="download-actions">
        <!-- 行注：展示“打开”文案 -->
        <button class="download-btn open" @click="$emit('open')">打开</button>
        <!-- 行注：展示“保存”文案 -->
        <button class="download-btn save" @click="$emit('save')">保存</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：展示“取消”文案 -->
      <button class="download-cancel" @click="$emit('close')">取消</button>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * ChatDownloadDialog 组件，负责当前界面片段的展示与交互。
 */
defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  fileName: string  // 行注：设置 fileName 配置项
  fileSize: string  // 行注：设置 fileSize 配置项
  progress: number  // 行注：设置 progress 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  close: []  // 行注：设置 close 配置项
  open: []  // 行注：设置 open 配置项
  save: []  // 行注：设置 save 配置项
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.download-overlay {  /* 行注：定义 .download-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  z-index: 1000;  /* 行注：设置 z-index 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
}  /* 行注：结束当前样式块 */

.download-dialog {  /* 行注：定义 .download-dialog 样式 */
  width: 360px;  /* 行注：设置 width 样式 */
  max-width: calc(100vw - 24px);  /* 行注：设置 max-width 样式 */
  max-height: calc(100vh - 24px);  /* 行注：设置 max-height 样式 */
  padding: 32px;  /* 行注：设置 padding 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.download-header {  /* 行注：定义 .download-header 样式 */
  margin-bottom: 24px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.download-filename {  /* 行注：定义 .download-filename 样式 */
  margin-top: 12px;  /* 行注：设置 margin-top 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  word-break: break-all;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.download-size {  /* 行注：定义 .download-size 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.download-progress {  /* 行注：定义 .download-progress 样式 */
  margin-bottom: 24px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.progress-bar {  /* 行注：定义 .progress-bar 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 6px;  /* 行注：设置 height 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border-radius: 3px;  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.progress-fill {  /* 行注：定义 .progress-fill 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  border-radius: 3px;  /* 行注：设置 border-radius 样式 */
  transition: width 0.3s ease;  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.progress-text {  /* 行注：定义 .progress-text 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.download-actions {  /* 行注：定义 .download-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.download-btn,  /* 行注：补充 .download-btn 选择器 */
.download-cancel {  /* 行注：定义 .download-cancel 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.download-btn {  /* 行注：定义 .download-btn 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.download-btn.open {  /* 行注：定义 .download-btn.open 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.download-btn.open:hover {  /* 行注：定义 .download-btn.open:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.download-btn.save,  /* 行注：补充 .download-btn.save 选择器 */
.download-cancel {  /* 行注：定义 .download-cancel 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.download-cancel {  /* 行注：定义 .download-cancel 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */
</style>
