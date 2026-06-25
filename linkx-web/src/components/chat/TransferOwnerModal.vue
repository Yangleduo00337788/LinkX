<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <!-- 行注：渲染容器 -->
    <div class="modal-card add-members-modal">
      <!-- 行注：渲染容器 -->
      <div class="modal-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“转让群主”文案 -->
          <div class="modal-title">转让群主</div>
          <!-- 行注：展示“选择一位成员成为新群主”文案 -->
          <div class="modal-subtitle">选择一位成员成为新群主</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“x”文案 -->
        <button class="modal-close" @click="$emit('close')">x</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-if="members.length > 0" class="member-picker-list compact">
        <!-- 行注：渲染字段标签 -->
        <label v-for="member in members" :key="member.userId" class="member-picker-item">
          <!-- 行注：渲染输入框 -->
          <input
            :checked="String(selectedOwnerId) === String(member.userId)"
            :disabled="transferringOwner"
            type="radio"
            name="transferOwner"
            @change="emit('update:selectedOwnerId', member.userId)"
          />
          <!-- 行注：渲染容器 -->
          <div class="member-picker-avatar">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="member.avatar" :src="member.avatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="member-picker-info">
            <!-- 行注：渲染文本节点 -->
            <span class="member-picker-name">{{ member.nickname || member.username }}</span>
            <!-- 行注：展示“@{{ member.usernam”文案 -->
            <span class="member-picker-meta">@{{ member.username }} · {{ roleText(member.role) }}</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束字段标签 -->
        </label>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：展示“群内没有其他成员可转让。”文案 -->
      <div v-else class="panel-placeholder">群内没有其他成员可转让。</div>
      <!-- 行注：渲染容器 -->
      <div class="modal-actions">
        <!-- 行注：展示“取消”文案 -->
        <button class="secondary-btn" :disabled="transferringOwner" @click="$emit('close')">取消</button>
        <!-- 行注：渲染按钮 -->
        <button class="primary-btn" :disabled="!selectedOwnerId || transferringOwner" @click="$emit('submit')">
          <!-- 行注：渲染动态文本 -->
          {{ transferringOwner ? '转让中...' : '确认转让' }}
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
 * TransferOwnerModal 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupMember } from '../../types/chat'
import { roleText } from '../../utils/chat'  // 行注：引入 roleText 能力

defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  members: GroupMember[]  // 行注：设置 members 配置项
  selectedOwnerId: string | number | null  // 行注：设置 selectedOwnerId 配置项
  transferringOwner: boolean  // 行注：设置 transferringOwner 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'update:selectedOwnerId', value: string | number | null): void  // 行注：执行当前调用逻辑
  (event: 'submit'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
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
  width: min(560px, 100%);  /* 行注：设置 width 样式 */
  max-width: calc(100vw - 24px);  /* 行注：设置 max-width 样式 */
  max-height: calc(100vh - 48px);  /* 行注：设置 max-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.modal-header,  /* 行注：补充 .modal-header 选择器 */
.modal-actions,  /* 行注：补充 .modal-actions 选择器 */
.member-picker-item {  /* 行注：定义 .member-picker-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.modal-header,  /* 行注：补充 .modal-header 选择器 */
.modal-actions {  /* 行注：定义 .modal-actions 样式 */
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

.modal-subtitle,  /* 行注：补充 .modal-subtitle 选择器 */
.member-picker-meta,  /* 行注：补充 .member-picker-meta 选择器 */
.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
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

.member-picker-list {  /* 行注：定义 .member-picker-list 样式 */
  overflow: auto;  /* 行注：设置 overflow 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-list.compact {  /* 行注：定义 .member-picker-list.compact 样式 */
  max-height: 360px;  /* 行注：设置 max-height 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-item {  /* 行注：定义 .member-picker-item 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-item:not(:last-child) {  /* 行注：定义 .member-picker-item:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-avatar {  /* 行注：定义 .member-picker-avatar 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-info {  /* 行注：定义 .member-picker-info 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-name {  /* 行注：定义 .member-picker-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-meta {  /* 行注：定义 .member-picker-meta 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  padding: 18px 14px;  /* 行注：设置 padding 样式 */
  border: 1px dashed var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.modal-actions {  /* 行注：定义 .modal-actions 样式 */
  margin-top: 18px;  /* 行注：设置 margin-top 样式 */
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
