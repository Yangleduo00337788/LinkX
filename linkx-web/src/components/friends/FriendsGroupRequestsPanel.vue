<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="groupRequests.length > 0" class="request-list">
    <!-- 行注：渲染容器 -->
    <div
      v-for="request in groupRequests"
      :key="request.id"
      class="request-card group-request-card"
    >
      <!-- 行注：渲染容器 -->
      <div class="request-avatar group-request-avatar">
        <!-- 行注：渲染图片 -->
        <ProtectedImage v-if="request.groupAvatar" :src="request.groupAvatar" class="avatar-img" />
        <!-- 行注：渲染文本节点 -->
        <span v-else>{{ request.groupName?.charAt(0) || '群' }}</span>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="request-info">
        <!-- 行注：渲染容器 -->
        <div class="request-title-row">
          <!-- 行注：渲染容器 -->
          <div class="request-name">{{ request.groupName }}</div>
          <!-- 行注：渲染文本节点 -->
          <span class="request-type-tag" :class="groupRequestTagClass(request.requestType)">
            <!-- 行注：渲染动态文本 -->
            {{ groupRequestTypeText(request.requestType) }}
          <!-- 行注：结束文本节点 -->
          </span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="request-message">
          <!-- 行注：渲染动态文本 -->
          {{ buildGroupRequestMessage(request) }}
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="request-time">{{ formatRequestTime(request.createTime) }}</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="request-actions">
        <!-- 行注：渲染按钮 -->
        <button
          class="action-btn accept"
          :disabled="isRequestActionLoading(request.id)"
          @click="$emit('accept', request.id)"
        >
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <!-- 行注：补充图标折线 -->
            <polyline points="20 6 9 17 4 12"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button
          class="action-btn reject"
          :disabled="isRequestActionLoading(request.id)"
          @click="$emit('reject', request.id)"
        >
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <!-- 行注：补充图标线段 -->
            <line x1="18" y1="6" x2="6" y2="18"/>
            <!-- 行注：补充图标线段 -->
            <line x1="6" y1="6" x2="18" y2="18"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
  <!-- 行注：渲染容器 -->
  <div v-else class="empty-state">
    <!-- 行注：渲染容器 -->
    <div class="empty-icon">
      <!-- 行注：渲染图标容器 -->
      <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <!-- 行注：补充图标路径 -->
        <path d="M12 5v14"/>
        <!-- 行注：补充图标路径 -->
        <path d="M5 12h14"/>
        <!-- 行注：补充图标矩形路径 -->
        <rect x="3" y="3" width="18" height="18" rx="4"/>
      <!-- 行注：结束图标容器 -->
      </svg>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：展示“暂无群通知”文案 -->
    <div class="empty-title">暂无群通知</div>
    <!-- 行注：展示“入群申请和邀请会在这里显示”文案 -->
    <div class="empty-subtitle">入群申请和邀请会在这里显示</div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupRequestItem } from '../../types/chat'  // 行注：引入 type { GroupRequestItem } 模块

defineProps<{  // 行注：开始当前逻辑块
  groupRequests: GroupRequestItem[]  // 行注：设置 groupRequests 配置项
  groupRequestTypeText: (requestType: number) => string  // 行注：设置 groupRequestTypeText 配置项
  groupRequestTagClass: (requestType: number) => string  // 行注：设置 groupRequestTagClass 配置项
  buildGroupRequestMessage: (request: GroupRequestItem) => string  // 行注：设置 buildGroupRequestMessage 配置项
  formatRequestTime: (time?: string) => string  // 行注：设置 formatRequestTime 配置项
  isRequestActionLoading: (requestId: string | number) => boolean  // 行注：设置 isRequestActionLoading 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'accept', requestId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'reject', requestId: string | number): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.request-list {  /* 行注：定义 .request-list 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.request-card {  /* 行注：定义 .request-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 16px;  /* 行注：设置 padding 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.request-card:hover {  /* 行注：定义 .request-card:hover 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.group-request-card {  /* 行注：定义 .group-request-card 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.request-avatar {  /* 行注：定义 .request-avatar 样式 */
  width: 48px;  /* 行注：设置 width 样式 */
  height: 48px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-request-avatar {  /* 行注：定义 .group-request-avatar 样式 */
  background: linear-gradient(135deg, #00a6ff 0%, #0066ff 100%);  /* 行注：设置 background 样式 */
  box-shadow: 0 2px 8px rgba(0, 102, 255, 0.28);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.request-info {  /* 行注：定义 .request-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.request-title-row {  /* 行注：定义 .request-title-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.request-name {  /* 行注：定义 .request-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag {  /* 行注：定义 .request-type-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  height: 22px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  letter-spacing: 0.02em;  /* 行注：设置 letter-spacing 样式 */
  border: 1px solid transparent;  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag.join {  /* 行注：定义 .request-type-tag.join 样式 */
  color: #ffb020;  /* 行注：设置 color 样式 */
  background: rgba(255, 176, 32, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(255, 176, 32, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.request-type-tag.invite {  /* 行注：定义 .request-type-tag.invite 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  background: rgba(0, 214, 143, 0.12);  /* 行注：设置 background 样式 */
  border-color: rgba(0, 214, 143, 0.22);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.request-message {  /* 行注：定义 .request-message 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.request-time {  /* 行注：定义 .request-time 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.request-actions {  /* 行注：定义 .request-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.action-btn {  /* 行注：定义 .action-btn 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.action-btn:disabled {  /* 行注：定义 .action-btn:disabled 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
  opacity: 0.6;  /* 行注：设置 opacity 样式 */
  transform: none;  /* 行注：设置 transform 样式 */
  box-shadow: none;  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.accept {  /* 行注：定义 .action-btn.accept 样式 */
  background: rgba(0, 214, 143, 0.1);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.accept:hover:not(:disabled) {  /* 行注：定义 .action-btn.accept:hover:not(:disabled) 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.reject {  /* 行注：定义 .action-btn.reject 样式 */
  background: rgba(255, 61, 113, 0.1);  /* 行注：设置 background 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.reject:hover:not(:disabled) {  /* 行注：定义 .action-btn.reject:hover:not(:disabled) 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  padding: 40px 20px;  /* 行注：设置 padding 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-icon {  /* 行注：定义 .empty-icon 样式 */
  opacity: 0.3;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 820px) {  /* 行注：声明响应式样式区块 */
  .request-card {  /* 行注：定义 .request-card 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .request-card {  /* 行注：定义 .request-card 样式 */
    padding: 12px 10px;  /* 行注：设置 padding 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .request-actions {  /* 行注：定义 .request-actions 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
