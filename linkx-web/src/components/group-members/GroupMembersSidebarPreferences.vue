<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card preference-card">
    <!-- 行注：渲染容器 -->
    <div class="panel-title-row">
      <!-- 行注：渲染容器 -->
      <div>
        <!-- 行注：展示“我的群偏好”文案 -->
        <div class="panel-title">我的群偏好</div>
        <!-- 行注：展示“只对当前账号生效，支持群备注和消息免”文案 -->
        <div class="panel-note">只对当前账号生效，支持群备注和消息免打扰。</div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="preference-form">
      <!-- 行注：展示“群备注”文案 -->
      <label class="field-label" for="groupRemarkInput">群备注</label>
      <!-- 行注：渲染输入框 -->
      <input
        id="groupRemarkInput"
        :value="groupPreferenceDraft.groupRemark"
        type="text"
        class="text-input filled-input"
        maxlength="100"
        placeholder="给这个群设置一个你自己的备注名"
        @input="$emit('update:group-remark', ($event.target as HTMLInputElement).value || '')"
      />
      <!-- 行注：渲染字段标签 -->
      <label class="preference-switch">
        <!-- 行注：渲染输入框 -->
        <input
          :checked="groupPreferenceDraft.notificationMuted"
          type="checkbox"
          @change="$emit('update:group-notification-muted', ($event.target as HTMLInputElement).checked)"
        />
        <!-- 行注：展示“群免打扰”文案 -->
        <span>群免打扰</span>
        <!-- 行注：展示“开启后该群新消息不再弹出桌面通知”文案 -->
        <small>开启后该群新消息不再弹出桌面通知</small>
      <!-- 行注：结束字段标签 -->
      </label>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="profile-action-row">
      <!-- 行注：渲染按钮 -->
      <button
        class="secondary-btn"
        :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
        @click="$emit('restore-group-preferences')"
      >
        <!-- 行注：展示“还原偏好”文案 -->
        还原偏好
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button
        class="primary-btn"
        :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
        @click="$emit('submit-group-preferences')"
      >
        <!-- 行注：渲染动态文本 -->
        {{ savingGroupPreferences ? '保存中...' : '保存偏好' }}
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
 * GroupMembersSidebarPreferences 组件，负责当前界面片段的展示与交互。
 */
import type { GroupPreferenceDraftState } from '../../types/chat'  // 行注：引入 type { GroupPreferenceDraftState } 模块

defineProps<{  // 行注：开始当前逻辑块
  groupPreferenceDraft: GroupPreferenceDraftState  // 行注：设置 groupPreferenceDraft 配置项
  isGroupPreferenceChanged: boolean  // 行注：设置 isGroupPreferenceChanged 配置项
  savingGroupPreferences: boolean  // 行注：设置 savingGroupPreferences 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'update:group-remark', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:group-notification-muted', value: boolean): void  // 行注：执行当前调用逻辑
  (event: 'restore-group-preferences'): void  // 行注：执行当前调用逻辑
  (event: 'submit-group-preferences'): void  // 行注：执行当前调用逻辑
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

.preference-card {  /* 行注：定义 .preference-card 样式 */
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

.preference-form {  /* 行注：定义 .preference-form 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.field-label {  /* 行注：定义 .field-label 样式 */
  display: block;  /* 行注：设置 display 样式 */
  margin-bottom: 8px;  /* 行注：设置 margin-bottom 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.text-input {  /* 行注：定义 .text-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.filled-input {  /* 行注：定义 .filled-input 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.preference-switch {  /* 行注：定义 .preference-switch 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: auto 1fr;  /* 行注：设置 grid-template-columns 样式 */
  gap: 6px 10px;  /* 行注：设置 gap 样式 */
  align-items: start;  /* 行注：设置 align-items 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);  /* 行注：设置 border 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preference-switch input {  /* 行注：定义 .preference-switch input 样式 */
  margin-top: 3px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

.preference-switch span,  /* 行注：补充 .preference-switch span 选择器 */
.preference-switch small {  /* 行注：定义 .preference-switch small 样式 */
  grid-column: 2;  /* 行注：设置 grid-column 样式 */
}  /* 行注：结束当前样式块 */

.preference-switch small {  /* 行注：定义 .preference-switch small 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
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
