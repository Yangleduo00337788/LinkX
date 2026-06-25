<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="confirm-dialog-overlay" @click.self="handleCancel">
    <!-- 行注：渲染容器 -->
    <div class="confirm-dialog-card">
      <!-- 行注：渲染容器 -->
      <div class="confirm-dialog-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：渲染容器 -->
          <div class="confirm-dialog-title">{{ title }}</div>
          <!-- 行注：渲染容器 -->
          <div v-if="subtitle" class="confirm-dialog-subtitle">{{ subtitle }}</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“×”文案 -->
        <button class="confirm-dialog-close" @click="handleCancel">×</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="confirm-dialog-body">
        <!-- 行注：渲染容器 -->
        <div class="confirm-dialog-description">{{ description }}</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="confirm-dialog-actions">
        <!-- 行注：渲染按钮 -->
        <button class="confirm-dialog-secondary-btn" @click="handleCancel">{{ cancelText }}</button>
        <!-- 行注：渲染按钮 -->
        <button class="confirm-dialog-primary-btn" :class="{ danger: confirmType === 'danger' }" @click="handleConfirm">
          <!-- 行注：渲染动态文本 -->
          {{ confirmText }}
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * ConfirmDialog 组件，负责当前界面片段的展示与交互。
 */
type ConfirmType = 'primary' | 'danger'  // 行注：补充当前表达式

const props = withDefaults(defineProps<{  // 行注：开始解构当前返回值
  visible: boolean  // 行注：设置 visible 配置项
  title: string  // 行注：设置 title 配置项
  subtitle?: string  // 行注：补充当前表达式
  description: string  // 行注：设置 description 配置项
  cancelText?: string  // 行注：补充当前表达式
  confirmText?: string  // 行注：补充当前表达式
  confirmType?: ConfirmType  // 行注：补充当前表达式
}>(), {  // 行注：执行当前调用逻辑
  subtitle: '',  // 行注：设置 subtitle 配置项
  cancelText: '取消',  // 行注：设置 cancelText 配置项
  confirmText: '确认',  // 行注：设置 confirmText 配置项
  confirmType: 'primary'  // 行注：设置 confirmType 配置项
})  // 行注：结束当前调用配置

const emit = defineEmits<{  // 行注：开始解构当前返回值
  'update:visible': [value: boolean]  // 行注：补充当前表达式
  cancel: []  // 行注：设置 cancel 配置项
  confirm: []  // 行注：设置 confirm 配置项
}>()  // 行注：执行当前调用逻辑

function handleCancel() {  // 行注：定义 handleCancel 方法
  emit('update:visible', false)  // 行注：调用 emit 方法
  emit('cancel')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function handleConfirm() {  // 行注：定义 handleConfirm 方法
  emit('update:visible', false)  // 行注：调用 emit 方法
  emit('confirm')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.confirm-dialog-overlay {  /* 行注：定义 .confirm-dialog-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding: 56px 24px 24px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
  z-index: 2200;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-card {  /* 行注：定义 .confirm-dialog-card 样式 */
  width: min(420px, 100%);  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-header,  /* 行注：补充 .confirm-dialog-header 选择器 */
.confirm-dialog-actions {  /* 行注：定义 .confirm-dialog-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-header {  /* 行注：定义 .confirm-dialog-header 样式 */
  margin-bottom: 20px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-title {  /* 行注：定义 .confirm-dialog-title 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-subtitle,  /* 行注：补充 .confirm-dialog-subtitle 选择器 */
.confirm-dialog-description {  /* 行注：定义 .confirm-dialog-description 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-subtitle {  /* 行注：定义 .confirm-dialog-subtitle 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-body {  /* 行注：定义 .confirm-dialog-body 样式 */
  margin-bottom: 18px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-description {  /* 行注：定义 .confirm-dialog-description 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-close {  /* 行注：定义 .confirm-dialog-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-md);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  line-height: 1.2;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-primary-btn,  /* 行注：补充 .confirm-dialog-primary-btn 选择器 */
.confirm-dialog-secondary-btn {  /* 行注：定义 .confirm-dialog-secondary-btn 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-secondary-btn {  /* 行注：定义 .confirm-dialog-secondary-btn 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-primary-btn {  /* 行注：定义 .confirm-dialog-primary-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-primary-btn:hover {  /* 行注：定义 .confirm-dialog-primary-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-primary-btn.danger {  /* 行注：定义 .confirm-dialog-primary-btn.danger 样式 */
  background: #ff6b6b;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.confirm-dialog-primary-btn.danger:hover {  /* 行注：定义 .confirm-dialog-primary-btn.danger:hover 样式 */
  background: #ff5f5f;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 640px) {  /* 行注：声明响应式样式区块 */
  .confirm-dialog-overlay {  /* 行注：定义 .confirm-dialog-overlay 样式 */
    padding: 56px 16px 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .confirm-dialog-actions {  /* 行注：定义 .confirm-dialog-actions 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */

  .confirm-dialog-secondary-btn,  /* 行注：补充 .confirm-dialog-secondary-btn 选择器 */
  .confirm-dialog-primary-btn {  /* 行注：定义 .confirm-dialog-primary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
