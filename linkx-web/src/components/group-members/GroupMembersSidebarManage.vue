<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card manage-card">
    <!-- 行注：展示“群管理”文案 -->
    <div class="panel-title">群管理</div>
    <!-- 行注：展示“把常用群操作集中到这里处理，减少在聊”文案 -->
    <div class="manage-subtitle">把常用群操作集中到这里处理，减少在聊天页和成员页之间来回切换。</div>
    <!-- 行注：渲染容器 -->
    <div class="manage-summary-grid">
      <!-- 行注：渲染容器 -->
      <div class="manage-summary-item">
        <!-- 行注：展示“我的权限”文案 -->
        <span class="manage-summary-label">我的权限</span>
        <!-- 行注：渲染strong 节点 -->
        <strong>{{ roleText(currentGroupRole) }}</strong>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="manage-summary-item">
        <!-- 行注：展示“可邀请好友”文案 -->
        <span class="manage-summary-label">可邀请好友</span>
        <!-- 行注：渲染strong 节点 -->
        <strong>{{ availableFriendsCount }}</strong>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="manage-action-list">
      <!-- 行注：渲染按钮 -->
      <button v-if="canManageMembers" class="secondary-btn action-btn" @click="$emit('open-add-members-modal')">
        <!-- 行注：展示“邀请成员”文案 -->
        邀请成员
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button v-if="isGroupOwner" class="secondary-btn action-btn" @click="$emit('open-transfer-owner-modal')">
        <!-- 行注：展示“转让群主”文案 -->
        转让群主
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button v-if="!isGroupOwner" class="secondary-btn action-btn warn" @click="$emit('leave-group')">
        <!-- 行注：展示“退出群聊”文案 -->
        退出群聊
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button v-if="canDissolveGroup" class="secondary-btn action-btn danger" @click="$emit('dissolve-group')">
        <!-- 行注：展示“解散群聊”文案 -->
        解散群聊
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="manage-tip">
      <!-- 行注：展示“你可以直接邀请好友或处理群管理动作。”文案 -->
      <span v-if="canManageMembers">你可以直接邀请好友或处理群管理动作。</span>
      <!-- 行注：展示“当前仅支持浏览成员信息，如需更多操作”文案 -->
      <span v-else>当前仅支持浏览成员信息，如需更多操作请联系群管理员。</span>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersSidebarManage 组件，负责当前界面片段的展示与交互。
 */
import { roleText } from '../../utils/chat'  // 行注：引入 roleText 能力

defineProps<{  // 行注：开始当前逻辑块
  currentGroupRole: number  // 行注：设置 currentGroupRole 配置项
  availableFriendsCount: number  // 行注：设置 availableFriendsCount 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  isGroupOwner: boolean  // 行注：设置 isGroupOwner 配置项
  canDissolveGroup: boolean  // 行注：设置 canDissolveGroup 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'open-add-members-modal'): void  // 行注：执行当前调用逻辑
  (event: 'open-transfer-owner-modal'): void  // 行注：执行当前调用逻辑
  (event: 'leave-group'): void  // 行注：执行当前调用逻辑
  (event: 'dissolve-group'): void  // 行注：执行当前调用逻辑
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

.panel-title {  /* 行注：定义 .panel-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 17px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.manage-card {  /* 行注：定义 .manage-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.manage-subtitle {  /* 行注：定义 .manage-subtitle 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.7;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.manage-summary-grid {  /* 行注：定义 .manage-summary-grid 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(2, minmax(0, 1fr));  /* 行注：设置 grid-template-columns 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.manage-summary-item {  /* 行注：定义 .manage-summary-item 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.manage-summary-label {  /* 行注：定义 .manage-summary-label 样式 */
  display: block;  /* 行注：设置 display 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.manage-summary-item strong {  /* 行注：定义 .manage-summary-item strong 样式 */
  display: block;  /* 行注：设置 display 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.manage-action-list {  /* 行注：定义 .manage-action-list 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn:hover {  /* 行注：定义 .secondary-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.action-btn {  /* 行注：定义 .action-btn 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.warn {  /* 行注：定义 .action-btn.warn 样式 */
  color: #d88914;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.danger {  /* 行注：定义 .action-btn.danger 样式 */
  color: #ff6b6b;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.manage-tip {  /* 行注：定义 .manage-tip 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.7;  /* 行注：设置 line-height 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  border-radius: 14px;  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, var(--linkx-bg-hover) 68%, transparent);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .manage-summary-grid {  /* 行注：定义 .manage-summary-grid 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
