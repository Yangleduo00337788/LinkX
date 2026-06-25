<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <!-- 行注：渲染容器 -->
    <div class="modal-card mute-member-modal">
      <!-- 行注：渲染容器 -->
      <div class="modal-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“设置禁言”文案 -->
          <div class="modal-title">设置禁言</div>
          <!-- 行注：展示“为该成员设置禁言时长”文案 -->
          <div class="modal-subtitle">为该成员设置禁言时长</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“x”文案 -->
        <button class="modal-close" @click="$emit('close')">x</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：展示“成员”文案 -->
        <label class="form-label">成员</label>
        <!-- 行注：渲染容器 -->
        <div class="readonly-field">
          <!-- 行注：渲染动态文本 -->
          {{ member?.nickname || member?.username || '未选择成员' }}
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：展示“禁言时长（分钟）”文案 -->
        <label class="form-label">禁言时长（分钟）</label>
        <!-- 行注：渲染输入框 -->
        <input
          :value="muteMinutesInput"
          type="number"
          min="1"
          max="43200"
          step="1"
          class="modal-input"
          placeholder="请输入 1 - 43200 之间的整数"
          @input="handleInput"
          @keyup.enter="$emit('submit')"
        />
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="modal-actions">
        <!-- 行注：展示“取消”文案 -->
        <button class="secondary-btn" :disabled="mutingMember" @click="$emit('close')">取消</button>
        <!-- 行注：渲染按钮 -->
        <button class="primary-btn" :disabled="mutingMember" @click="$emit('submit')">
          <!-- 行注：渲染动态文本 -->
          {{ mutingMember ? '提交中...' : '确认禁言' }}
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
 * MuteMemberModal 组件，负责当前界面片段的展示与交互。
 */
import type { GroupMember } from '../../types/chat'  // 行注：引入 type { GroupMember } 模块

defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  member: GroupMember | null  // 行注：设置 member 配置项
  muteMinutesInput: string  // 行注：设置 muteMinutesInput 配置项
  mutingMember: boolean  // 行注：设置 mutingMember 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'update:muteMinutesInput', value: string): void  // 行注：执行当前调用逻辑
  (event: 'submit'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑

function handleInput(event: Event) {  // 行注：定义 handleInput 方法
  const target = event.target as HTMLInputElement  // 行注：初始化 target 变量
  emit('update:muteMinutesInput', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.overlay-panel {  /* 行注：定义 .overlay-panel 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  z-index: 2200;  /* 行注：设置 z-index 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding: 56px 24px 24px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
}  /* 行注：结束当前样式块 */

.modal-card {  /* 行注：定义 .modal-card 样式 */
  width: min(420px, 100%);  /* 行注：设置 width 样式 */
  max-width: calc(100vw - 24px);  /* 行注：设置 max-width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.modal-header,  /* 行注：补充 .modal-header 选择器 */
.modal-actions {  /* 行注：定义 .modal-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.modal-header {  /* 行注：定义 .modal-header 样式 */
  margin-bottom: 20px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.modal-title {  /* 行注：定义 .modal-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.modal-subtitle {  /* 行注：定义 .modal-subtitle 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.form-section {  /* 行注：定义 .form-section 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-bottom: 18px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.form-label {  /* 行注：定义 .form-label 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.modal-close {  /* 行注：定义 .modal-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.modal-close:hover,  /* 行注：补充 .modal-close:hover 选择器 */
.secondary-btn:hover {  /* 行注：定义 .secondary-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.readonly-field,  /* 行注：补充 .readonly-field 选择器 */
.modal-input {  /* 行注：定义 .modal-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  min-height: 46px;  /* 行注：设置 min-height 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.readonly-field {  /* 行注：定义 .readonly-field 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  line-height: 1.5;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.modal-input {  /* 行注：定义 .modal-input 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  font-family: inherit;  /* 行注：设置 font-family 样式 */
}  /* 行注：结束当前样式块 */

.modal-input:focus {  /* 行注：定义 .modal-input:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.modal-actions {  /* 行注：定义 .modal-actions 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
.primary-btn {  /* 行注：定义 .primary-btn 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn {  /* 行注：定义 .primary-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn:hover {  /* 行注：定义 .primary-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn:disabled,  /* 行注：补充 .primary-btn:disabled 选择器 */
.secondary-btn:disabled {  /* 行注：定义 .secondary-btn:disabled 样式 */
  opacity: 0.6;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 640px) {  /* 行注：声明响应式样式区块 */
  .overlay-panel {  /* 行注：定义 .overlay-panel 样式 */
    padding: 20px 10px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .modal-card {  /* 行注：定义 .modal-card 样式 */
    width: calc(100vw - 20px);  /* 行注：设置 width 样式 */
    padding: 20px 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .modal-actions {  /* 行注：定义 .modal-actions 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
  .primary-btn {  /* 行注：定义 .primary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
