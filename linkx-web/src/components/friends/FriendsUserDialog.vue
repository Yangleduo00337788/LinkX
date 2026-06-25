<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染 Teleport 组件 -->
  <Teleport to="body">
    <!-- 行注：渲染容器 -->
    <div v-if="visible && selectedFriend" class="user-info-overlay" @click.self="$emit('close')">
      <!-- 行注：渲染容器 -->
      <div class="user-info-popup">
        <!-- 行注：渲染按钮 -->
        <button class="user-info-close" @click="$emit('close')">
          <!-- 行注：渲染图标容器 -->
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标线段 -->
            <line x1="18" y1="6" x2="6" y2="18"/>
            <!-- 行注：补充图标线段 -->
            <line x1="6" y1="6" x2="18" y2="18"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染容器 -->
        <div class="user-info-avatar">
          <!-- 行注：渲染图片 -->
          <ProtectedImage v-if="selectedFriend.friendAvatar" :src="selectedFriend.friendAvatar" class="user-info-avatar-img" />
          <!-- 行注：渲染文本节点 -->
          <span v-else>{{ selectedFriend.friendNickname?.charAt(0) }}</span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="user-info-name">{{ selectedFriend.friendNickname }}</div>
        <!-- 行注：展示“@{{ selectedFriend”文案 -->
        <div class="user-info-username">@{{ selectedFriend.friendUsername }}</div>
        <!-- 行注：渲染容器 -->
        <div class="user-info-details">
          <!-- 行注：渲染容器 -->
          <div class="user-info-detail-item">
            <!-- 行注：展示“昵称”文案 -->
            <span class="detail-label">昵称</span>
            <!-- 行注：渲染文本节点 -->
            <span class="detail-value">{{ selectedFriend.friendNickname }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="user-info-detail-item">
            <!-- 行注：展示“用户名”文案 -->
            <span class="detail-label">用户名</span>
            <!-- 行注：渲染文本节点 -->
            <span class="detail-value">{{ selectedFriend.friendUsername }}</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染按钮 -->
        <button class="user-info-send-btn" @click="$emit('send-message', selectedFriend.friendId)">
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标路径 -->
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：展示“发送消息”文案 -->
          发送消息
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染按钮 -->
        <button class="user-info-blacklist-btn" @click="$emit('blacklist', selectedFriend.friendId)">
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="12" cy="12" r="10"/>
            <!-- 行注：补充图标线段 -->
            <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：展示“拉黑”文案 -->
          拉黑
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束Teleport 节点 -->
  </Teleport>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'

interface FriendListItem {  // 行注：开始当前逻辑块
  friendId: string | number  // 行注：设置 friendId 配置项
  friendAvatar?: string  // 行注：补充当前表达式
  friendNickname?: string  // 行注：补充当前表达式
  friendUsername?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  selectedFriend: FriendListItem | null  // 行注：设置 selectedFriend 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'send-message', friendId: string | number): void  // 行注：执行当前调用逻辑
  (event: 'blacklist', friendId: string | number): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.user-info-overlay {  /* 行注：定义 .user-info-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  z-index: 1000;  /* 行注：设置 z-index 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
}  /* 行注：结束当前样式块 */

.user-info-popup {  /* 行注：定义 .user-info-popup 样式 */
  width: 300px;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
  position: relative;  /* 行注：设置 position 样式 */
}  /* 行注：结束当前样式块 */

.user-info-close {  /* 行注：定义 .user-info-close 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  top: 12px;  /* 行注：设置 top 样式 */
  right: 12px;  /* 行注：设置 right 样式 */
  width: 28px;  /* 行注：设置 width 样式 */
  height: 28px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.user-info-close:hover {  /* 行注：定义 .user-info-close:hover 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.user-info-avatar {  /* 行注：定义 .user-info-avatar 样式 */
  width: 72px;  /* 行注：设置 width 样式 */
  height: 72px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  margin: 0 auto 12px;  /* 行注：设置 margin 样式 */
  font-size: 28px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: white;  /* 行注：设置 color 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.user-info-avatar-img {  /* 行注：定义 .user-info-avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.user-info-name {  /* 行注：定义 .user-info-name 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 2px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.user-info-username {  /* 行注：定义 .user-info-username 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.user-info-details {  /* 行注：定义 .user-info-details 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.user-info-detail-item {  /* 行注：定义 .user-info-detail-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  padding: 10px 14px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.user-info-detail-item:not(:last-child) {  /* 行注：定义 .user-info-detail-item:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.detail-label {  /* 行注：定义 .detail-label 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.detail-value {  /* 行注：定义 .detail-value 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.user-info-send-btn,  /* 行注：补充 .user-info-send-btn 选择器 */
.user-info-blacklist-btn {  /* 行注：定义 .user-info-blacklist-btn 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  padding: 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.user-info-send-btn {  /* 行注：定义 .user-info-send-btn 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.user-info-send-btn:hover {  /* 行注：定义 .user-info-send-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.user-info-blacklist-btn {  /* 行注：定义 .user-info-blacklist-btn 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

.user-info-blacklist-btn:hover {  /* 行注：定义 .user-info-blacklist-btn:hover 样式 */
  border-color: var(--linkx-error);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
  background: rgba(255, 61, 113, 0.05);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .user-info-popup {  /* 行注：定义 .user-info-popup 样式 */
    width: calc(100vw - 20px);  /* 行注：设置 width 样式 */
    max-height: calc(100vh - 20px);  /* 行注：设置 max-height 样式 */
    padding: 20px 16px;  /* 行注：设置 padding 样式 */
    overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  }  /* 行注：结束当前样式块 */

  .user-info-detail-item {  /* 行注：定义 .user-info-detail-item 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */

  .detail-value {  /* 行注：定义 .detail-value 样式 */
    text-align: right;  /* 行注：设置 text-align 样式 */
    word-break: break-word;  /* 行注：设置 word-break 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
