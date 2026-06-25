<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card profile-card">
    <!-- 行注：渲染容器 -->
    <div class="panel-title-row">
      <!-- 行注：渲染容器 -->
      <div>
        <!-- 行注：展示“群资料”文案 -->
        <div class="panel-title">群资料</div>
        <!-- 行注：渲染容器 -->
        <div class="panel-note">{{ canEditGroupProfile ? '支持直接编辑群名称、头像与公告' : '仅群主和管理员可编辑群资料' }}</div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="profile-editor">
      <!-- 行注：渲染按钮 -->
      <button
        class="profile-avatar-editor"
        :class="{ editable: canEditGroupProfile }"
        :disabled="!canEditGroupProfile || updatingGroupProfile"
        @click="$emit('trigger-group-avatar-upload')"
      >
        <!-- 行注：渲染图片 -->
        <ProtectedImage
          v-if="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
          :src="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
          class="avatar-img"
        />
        <!-- 行注：渲染文本节点 -->
        <span v-else>{{ groupProfileDraft.groupName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
        <!-- 行注：展示“更换头像”文案 -->
        <span v-if="canEditGroupProfile" class="profile-avatar-mask">更换头像</span>
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染容器 -->
      <div class="profile-fields">
        <!-- 行注：展示“群名称”文案 -->
        <label class="field-label" for="groupNameInput">群名称</label>
        <!-- 行注：渲染输入框 -->
        <input
          id="groupNameInput"
          :value="groupProfileDraft.groupName"
          type="text"
          class="text-input filled-input"
          maxlength="30"
          :disabled="!canEditGroupProfile || updatingGroupProfile"
          placeholder="请输入群名称"
          @input="$emit('update:group-profile-name', ($event.target as HTMLInputElement).value || '')"
        />
        <!-- 行注：展示“群公告”文案 -->
        <label class="field-label" for="groupNoticeInput">群公告</label>
        <!-- 行注：渲染文本域 -->
        <textarea
          id="groupNoticeInput"
          :value="noticeDraft"
          class="text-area"
          rows="4"
          maxlength="255"
          :disabled="!canEditNotice || updatingNotice"
          :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
          @input="$emit('update:notice-draft', ($event.target as HTMLTextAreaElement).value || '')"
        ></textarea>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="profile-action-row">
      <!-- 行注：渲染按钮 -->
      <button
        class="secondary-btn"
        :disabled="(!isGroupProfileChanged && !isGroupNoticeChanged) || updatingGroupProfile || updatingNotice"
        @click="$emit('discard-group-profile-drafts')"
      >
        <!-- 行注：展示“还原修改”文案 -->
        还原修改
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button
        v-if="canEditNotice"
        class="secondary-btn"
        :disabled="!isGroupNoticeChanged || updatingNotice"
        @click="$emit('submit-update-notice')"
      >
        <!-- 行注：渲染动态文本 -->
        {{ updatingNotice ? '保存公告中...' : '保存公告' }}
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button
        v-if="canEditGroupProfile"
        class="primary-btn"
        :disabled="!isGroupProfileChanged || updatingGroupProfile"
        @click="$emit('submit-update-group-profile')"
      >
        <!-- 行注：渲染动态文本 -->
        {{ updatingGroupProfile ? '保存资料中...' : '保存资料' }}
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersSidebarProfile 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupDetail } from '../../types/chat'
import type { GroupProfileDraftState } from '../../utils/group-draft'

defineProps<{  // 行注：开始当前逻辑块
  groupDetail: GroupDetail  // 行注：设置 groupDetail 配置项
  canEditGroupProfile: boolean  // 行注：设置 canEditGroupProfile 配置项
  canEditNotice: boolean  // 行注：设置 canEditNotice 配置项
  updatingGroupProfile: boolean  // 行注：设置 updatingGroupProfile 配置项
  updatingNotice: boolean  // 行注：设置 updatingNotice 配置项
  groupProfileDraft: Pick<GroupProfileDraftState, 'groupName' | 'avatarPreview'>  // 行注：设置 groupProfileDraft 配置项
  noticeDraft: string  // 行注：设置 noticeDraft 配置项
  isGroupProfileChanged: boolean  // 行注：设置 isGroupProfileChanged 配置项
  isGroupNoticeChanged: boolean  // 行注：设置 isGroupNoticeChanged 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'trigger-group-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:group-profile-name', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:notice-draft', value: string): void  // 行注：执行当前调用逻辑
  (event: 'discard-group-profile-drafts'): void  // 行注：执行当前调用逻辑
  (event: 'submit-update-notice'): void  // 行注：执行当前调用逻辑
  (event: 'submit-update-group-profile'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.panel-card {  /* 行注：定义 .panel-card 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  padding: 22px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.profile-card {  /* 行注：定义 .profile-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 18px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.panel-title {  /* 行注：定义 .panel-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 17px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.panel-title-row,  /* 行注：补充 .panel-title-row 选择器 */
.profile-action-row {  /* 行注：定义 .profile-action-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.panel-title-row {  /* 行注：定义 .panel-title-row 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.panel-note {  /* 行注：定义 .panel-note 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.profile-editor {  /* 行注：定义 .profile-editor 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: 96px minmax(0, 1fr);  /* 行注：设置 grid-template-columns 样式 */
  align-items: start;  /* 行注：设置 align-items 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar-editor {  /* 行注：定义 .profile-avatar-editor 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  width: 96px;  /* 行注：设置 width 样式 */
  height: 96px;  /* 行注：设置 height 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: 28px;  /* 行注：设置 border-radius 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 30px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  cursor: default;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar-editor.editable {  /* 行注：定义 .profile-avatar-editor.editable 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar-mask {  /* 行注：定义 .profile-avatar-mask 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: auto 0 0;  /* 行注：设置 inset 样式 */
  padding: 8px 10px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 0, 0, 0.48);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.profile-fields {  /* 行注：定义 .profile-fields 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
}  /* 行注：结束当前样式块 */

.field-label {  /* 行注：定义 .field-label 样式 */
  display: block;  /* 行注：设置 display 样式 */
  margin-bottom: 8px;  /* 行注：设置 margin-bottom 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.field-label + .filled-input,  /* 行注：补充 .field-label + .filled-input 选择器 */
.field-label + .text-area {  /* 行注：定义 .field-label + .text-area 样式 */
  margin-bottom: 14px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.text-input,  /* 行注：补充 .text-input 选择器 */
.text-area {  /* 行注：定义 .text-area 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.filled-input {  /* 行注：定义 .filled-input 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.text-input {  /* 行注：定义 .text-input 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.text-area {  /* 行注：定义 .text-area 样式 */
  min-height: 92px;  /* 行注：设置 min-height 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  resize: vertical;  /* 行注：设置 resize 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.profile-action-row {  /* 行注：定义 .profile-action-row 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
.primary-btn {  /* 行注：定义 .primary-btn 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn:hover {  /* 行注：定义 .secondary-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn {  /* 行注：定义 .primary-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn:hover {  /* 行注：定义 .primary-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .profile-editor {  /* 行注：定义 .profile-editor 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .profile-action-row {  /* 行注：定义 .profile-action-row 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
  .primary-btn {  /* 行注：定义 .primary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
