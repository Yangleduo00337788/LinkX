<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="requests.length > 0" class="request-list">
    <!-- 行注：渲染容器 -->
    <div
      v-for="request in requests"
      :key="request.id"
      class="request-card"
    >
      <!-- 行注：渲染容器 -->
      <div class="request-avatar">
        <!-- 行注：渲染图片 -->
        <ProtectedImage v-if="request.fromAvatar" :src="request.fromAvatar" class="avatar-img" />
        <!-- 行注：渲染文本节点 -->
        <span v-else>{{ request.fromNickname?.charAt(0) }}</span>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="request-info">
        <!-- 行注：渲染容器 -->
        <div class="request-name">{{ request.fromNickname }}</div>
        <!-- 行注：渲染容器 -->
        <div class="request-message">{{ request.message || '请求加你为好友' }}</div>
        <!-- 行注：渲染容器 -->
        <div class="request-time">{{ request.createTime?.substring(0, 10) }}</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="request-actions">
        <!-- 行注：渲染按钮 -->
        <button class="action-btn accept" @click="$emit('accept', request.id)">
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <!-- 行注：补充图标折线 -->
            <polyline points="20 6 9 17 4 12"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button class="action-btn reject" @click="$emit('reject', request.id)">
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
        <!-- 行注：补充图标矩形路径 -->
        <rect x="2" y="4" width="20" height="16" rx="2"/>
        <!-- 行注：补充图标路径 -->
        <path d="M22 7l-10 7L2 7"/>
      <!-- 行注：结束图标容器 -->
      </svg>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：展示“暂无好友申请”文案 -->
    <div class="empty-title">暂无好友申请</div>
    <!-- 行注：展示“新的好友申请会在这里显示”文案 -->
    <div class="empty-subtitle">新的好友申请会在这里显示</div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'
interface FriendRequestItem {  // 行注：开始当前逻辑块
  id: number  // 行注：设置 id 配置项
  fromAvatar?: string  // 行注：补充当前表达式
  fromNickname?: string  // 行注：补充当前表达式
  message?: string  // 行注：补充当前表达式
  createTime?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

defineProps<{  // 行注：开始当前逻辑块
  requests: FriendRequestItem[]  // 行注：设置 requests 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'accept', requestId: number): void  // 行注：执行当前调用逻辑
  (event: 'reject', requestId: number): void  // 行注：执行当前调用逻辑
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

.request-avatar {  /* 行注：定义 .request-avatar 样式 */
  width: 48px;  /* 行注：设置 width 样式 */
  height: 48px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #ffaa00 0%, #ffcc00 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  box-shadow: 0 2px 8px rgba(255, 170, 0, 0.3);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
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

.request-name {  /* 行注：定义 .request-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
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

.action-btn.accept {  /* 行注：定义 .action-btn.accept 样式 */
  background: rgba(0, 214, 143, 0.1);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.accept:hover {  /* 行注：定义 .action-btn.accept:hover 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.reject {  /* 行注：定义 .action-btn.reject 样式 */
  background: rgba(255, 61, 113, 0.1);  /* 行注：设置 background 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.reject:hover {  /* 行注：定义 .action-btn.reject:hover 样式 */
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
