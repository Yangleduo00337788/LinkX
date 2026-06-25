<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="searchResults.length > 0" class="result-list">
    <!-- 行注：渲染容器 -->
    <div
      v-for="user in searchResults"
      :key="user.id"
      class="result-card"
    >
      <!-- 行注：渲染容器 -->
      <div class="result-avatar">
        <!-- 行注：渲染图片 -->
        <ProtectedImage v-if="user.avatar" :src="user.avatar" class="avatar-img" />
        <!-- 行注：渲染文本节点 -->
        <span v-else>{{ user.nickname?.charAt(0) }}</span>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="result-info">
        <!-- 行注：渲染容器 -->
        <div class="result-name">{{ user.nickname }}</div>
        <!-- 行注：展示“@{{ user.username ”文案 -->
        <div class="result-username">@{{ user.username }}</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染按钮 -->
      <button class="add-friend-btn" @click="$emit('add-friend', user.id)">
        <!-- 行注：渲染图标容器 -->
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标路径 -->
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <!-- 行注：补充图标圆形路径 -->
          <circle cx="8.5" cy="7" r="4"/>
          <!-- 行注：补充图标线段 -->
          <line x1="20" y1="8" x2="20" y2="14"/>
          <!-- 行注：补充图标线段 -->
          <line x1="23" y1="11" x2="17" y2="11"/>
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：展示“添加好友”文案 -->
        添加好友
      <!-- 行注：结束按钮 -->
      </button>
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
        <!-- 行注：补充图标圆形路径 -->
        <circle cx="11" cy="11" r="8"/>
        <!-- 行注：补充图标线段 -->
        <line x1="21" y1="21" x2="16.65" y2="16.65"/>
      <!-- 行注：结束图标容器 -->
      </svg>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="empty-title">{{ searchKeyword ? '未找到用户' : '搜索用户' }}</div>
    <!-- 行注：渲染容器 -->
    <div class="empty-subtitle">{{ searchKeyword ? '尝试其他关键词' : '输入用户名搜索' }}</div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
import ProtectedImage from '../ProtectedImage.vue'

interface SearchUserItem {  // 行注：开始当前逻辑块
  id: string | number  // 行注：设置 id 配置项
  avatar?: string  // 行注：补充当前表达式
  nickname?: string  // 行注：补充当前表达式
  username?: string  // 行注：补充当前表达式
}  // 行注：结束当前代码块

defineProps<{  // 行注：开始当前逻辑块
  searchKeyword: string  // 行注：设置 searchKeyword 配置项
  searchResults: SearchUserItem[]  // 行注：设置 searchResults 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'add-friend', userId: string | number): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.result-list {  /* 行注：定义 .result-list 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.result-card {  /* 行注：定义 .result-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 16px;  /* 行注：设置 padding 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.result-card:hover {  /* 行注：定义 .result-card:hover 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.result-avatar {  /* 行注：定义 .result-avatar 样式 */
  width: 48px;  /* 行注：设置 width 样式 */
  height: 48px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  box-shadow: 0 2px 8px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.result-info {  /* 行注：定义 .result-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.result-name {  /* 行注：定义 .result-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.result-username {  /* 行注：定义 .result-username 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.add-friend-btn {  /* 行注：定义 .add-friend-btn 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  padding: 8px 16px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.add-friend-btn:hover {  /* 行注：定义 .add-friend-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
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
  .result-card {  /* 行注：定义 .result-card 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .result-card {  /* 行注：定义 .result-card 样式 */
    padding: 12px 10px;  /* 行注：设置 padding 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .add-friend-btn {  /* 行注：定义 .add-friend-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
