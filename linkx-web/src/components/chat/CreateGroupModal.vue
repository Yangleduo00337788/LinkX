<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <!-- 行注：渲染容器 -->
    <div class="modal-card create-group-modal">
      <!-- 行注：渲染容器 -->
      <div class="modal-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“新建群聊”文案 -->
          <div class="modal-title">新建群聊</div>
          <!-- 行注：展示“选择好友并填写群信息”文案 -->
          <div class="modal-subtitle">选择好友并填写群信息</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“x”文案 -->
        <button class="modal-close" @click="$emit('close')">x</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：展示“群头像”文案 -->
        <label class="form-label">群头像</label>
        <!-- 行注：渲染容器 -->
        <div class="group-avatar-selector" @click="$emit('trigger-avatar-upload')">
          <!-- 行注：渲染图片 -->
          <ProtectedImage v-if="avatarPreview" :src="avatarPreview" class="group-avatar-preview" />
          <!-- 行注：渲染文本节点 -->
          <span v-else>{{ groupName?.charAt(0) || '群' }}</span>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：展示“群名称”文案 -->
        <label class="form-label">群名称</label>
        <!-- 行注：渲染输入框 -->
        <input
          :value="groupName"
          class="modal-input"
          placeholder="请输入群名称"
          maxlength="100"
          @input="handleGroupNameInput"
        />
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：展示“群公告”文案 -->
        <label class="form-label">群公告</label>
        <!-- 行注：渲染文本域 -->
        <textarea
          :value="notice"
          class="modal-textarea"
          rows="3"
          placeholder="选填，创建后可在群设置继续编辑"
          @input="handleNoticeInput"
        ></textarea>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="form-section">
        <!-- 行注：渲染容器 -->
        <div class="section-title-row">
          <!-- 行注：展示“选择成员”文案 -->
          <label class="form-label">选择成员</label>
          <!-- 行注：展示“已选 {{ memberIds.le”文案 -->
          <span class="section-count">已选 {{ memberIds.length }} 人</span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="friends.length > 0" class="member-picker-list">
          <!-- 行注：渲染字段标签 -->
          <label v-for="friend in friends" :key="friend.friendId" class="member-picker-item">
            <!-- 行注：渲染输入框 -->
            <input
              :checked="isSelected(friend.friendId)"
              type="checkbox"
              @change="toggleMemberSelection(friend.friendId, ($event.target as HTMLInputElement).checked)"
            />
            <!-- 行注：渲染容器 -->
            <div class="member-picker-avatar">
              <!-- 行注：渲染图片 -->
              <ProtectedImage v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
              <!-- 行注：渲染文本节点 -->
              <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="member-picker-info">
              <!-- 行注：渲染文本节点 -->
              <span class="member-picker-name">{{ friend.friendNickname }}</span>
              <!-- 行注：展示“@{{ friend.friendU”文案 -->
              <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束字段标签 -->
          </label>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“暂无好友，请先添加好友再创建群聊。”文案 -->
        <div v-else class="panel-placeholder">暂无好友，请先添加好友再创建群聊。</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="modal-actions">
        <!-- 行注：展示“取消”文案 -->
        <button class="secondary-btn" @click="$emit('close')">取消</button>
        <!-- 行注：渲染按钮 -->
        <button class="primary-btn" :disabled="creatingGroup" @click="$emit('submit')">
          <!-- 行注：渲染动态文本 -->
          {{ creatingGroup ? '创建中...' : '创建群聊' }}
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
 * CreateGroupModal 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import type { FriendItem } from '../../types/chat'

const props = defineProps<{  // 行注：开始解构当前返回值
  visible: boolean  // 行注：设置 visible 配置项
  groupName: string  // 行注：设置 groupName 配置项
  notice: string  // 行注：设置 notice 配置项
  memberIds: Array<string | number>  // 行注：设置 memberIds 配置项
  avatarPreview: string  // 行注：设置 avatarPreview 配置项
  friends: FriendItem[]  // 行注：设置 friends 配置项
  creatingGroup: boolean  // 行注：设置 creatingGroup 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'trigger-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:groupName', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:notice', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:memberIds', value: Array<string | number>): void  // 行注：执行当前调用逻辑
  (event: 'submit'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑

function handleGroupNameInput(event: Event) {  // 行注：定义 handleGroupNameInput 方法
  const target = event.target as HTMLInputElement  // 行注：初始化 target 变量
  emit('update:groupName', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function handleNoticeInput(event: Event) {  // 行注：定义 handleNoticeInput 方法
  const target = event.target as HTMLTextAreaElement  // 行注：初始化 target 变量
  emit('update:notice', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function isSelected(friendId: string | number) {  // 行注：定义 isSelected 方法
  return props.memberIds.some(item => String(item) === String(friendId))  // 行注：返回当前结果
}  // 行注：结束当前代码块

function toggleMemberSelection(friendId: string | number, checked: boolean) {  // 行注：定义 toggleMemberSelection 方法
  const nextSelection = checked  // 行注：初始化 nextSelection 变量
    ? [...props.memberIds, friendId]  // 行注：补充当前表达式
    : props.memberIds.filter(item => String(item) !== String(friendId))  // 行注：调用 filter 方法
  emit('update:memberIds', nextSelection)  // 行注：调用 emit 方法
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
  width: min(640px, 100%);  /* 行注：设置 width 样式 */
  max-width: calc(100vw - 24px);  /* 行注：设置 max-width 样式 */
  max-height: min(720px, calc(100vh - 56px));  /* 行注：设置 max-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.modal-header,  /* 行注：补充 .modal-header 选择器 */
.section-title-row,  /* 行注：补充 .section-title-row 选择器 */
.modal-actions,  /* 行注：补充 .modal-actions 选择器 */
.member-picker-item {  /* 行注：定义 .member-picker-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.modal-header,  /* 行注：补充 .modal-header 选择器 */
.section-title-row,  /* 行注：补充 .section-title-row 选择器 */
.modal-actions {  /* 行注：定义 .modal-actions 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.modal-header {  /* 行注：定义 .modal-header 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.modal-title {  /* 行注：定义 .modal-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.modal-subtitle,  /* 行注：补充 .modal-subtitle 选择器 */
.section-count,  /* 行注：补充 .section-count 选择器 */
.member-picker-meta,  /* 行注：补充 .member-picker-meta 选择器 */
.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.modal-subtitle,  /* 行注：补充 .modal-subtitle 选择器 */
.section-count,  /* 行注：补充 .section-count 选择器 */
.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.form-section {  /* 行注：定义 .form-section 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  margin-bottom: 14px;  /* 行注：设置 margin-bottom 样式 */
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

.group-avatar-selector {  /* 行注：定义 .group-avatar-selector 样式 */
  width: 60px;  /* 行注：设置 width 样式 */
  height: 60px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 24px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-avatar-preview,  /* 行注：补充 .group-avatar-preview 选择器 */
.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.modal-input,  /* 行注：补充 .modal-input 选择器 */
.modal-textarea {  /* 行注：定义 .modal-textarea 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  padding: 10px 12px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  resize: vertical;  /* 行注：设置 resize 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  font-family: inherit;  /* 行注：设置 font-family 样式 */
}  /* 行注：结束当前样式块 */

.modal-input:focus,  /* 行注：补充 .modal-input:focus 选择器 */
.modal-textarea:focus {  /* 行注：定义 .modal-textarea:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-list {  /* 行注：定义 .member-picker-list 样式 */
  max-height: 220px;  /* 行注：设置 max-height 样式 */
  overflow: auto;  /* 行注：设置 overflow 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-item {  /* 行注：定义 .member-picker-item 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 10px 12px;  /* 行注：设置 padding 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-item:not(:last-child) {  /* 行注：定义 .member-picker-item:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.member-picker-avatar {  /* 行注：定义 .member-picker-avatar 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
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

.panel-placeholder {  /* 行注：定义 .panel-placeholder 样式 */
  padding: 18px 14px;  /* 行注：设置 padding 样式 */
  border: 1px dashed var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.modal-actions {  /* 行注：定义 .modal-actions 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
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
