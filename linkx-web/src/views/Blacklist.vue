<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="content-area">
    <div class="blacklist-panel">
      <div class="panel-header">
        <div class="header-text">
          <span class="header-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
            </svg>
          </span>
          <div class="header-titles">
            <span class="header-title">黑名单</span>
            <span class="header-sub">已屏蔽的用户</span>
          </div>
          <span v-if="blacklist.length" class="header-count">{{ blacklist.length }}</span>
        </div>
        <!-- 行注：渲染按钮 -->
        <button class="refresh-btn" @click="loadBlacklist" title="刷新">
          <!-- 行注：渲染图标容器 -->
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标折线 -->
            <polyline points="23 4 23 10 17 10"/>
            <!-- 行注：补充图标路径 -->
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="blacklist-list">
        <div v-if="loading" class="list-loading">
          <div class="list-loading-spinner" />
          <span>加载中…</span>
        </div>
        <div
          v-for="user in blacklist"
          v-show="!loading"
          :key="user.id"
          class="blacklist-item"
        >
          <!-- 行注：渲染容器 -->
          <div class="user-avatar">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="user.avatar" :src="user.avatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ user.nickname?.charAt(0) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="user-info">
            <!-- 行注：渲染容器 -->
            <div class="user-name">{{ user.nickname }}</div>
            <!-- 行注：展示“@{{ user.username ”文案 -->
            <div class="user-username">@{{ user.username }}</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染按钮 -->
          <button class="remove-btn" @click="handleRemove(user.id)">
            <!-- 行注：渲染图标容器 -->
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <!-- 行注：补充图标线段 -->
              <line x1="18" y1="6" x2="6" y2="18"/>
              <!-- 行注：补充图标线段 -->
              <line x1="6" y1="6" x2="18" y2="18"/>
            <!-- 行注：结束图标容器 -->
            </svg>
            <!-- 行注：展示“移除”文案 -->
            移除
          <!-- 行注：结束按钮 -->
          </button>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div v-if="blacklist.length === 0 && !loading" class="empty-state">
          <!-- 行注：渲染容器 -->
          <div class="empty-icon">
            <!-- 行注：渲染图标容器 -->
            <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <!-- 行注：补充图标圆形路径 -->
              <circle cx="12" cy="12" r="10"/>
              <!-- 行注：补充图标线段 -->
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07"/>
            <!-- 行注：结束图标容器 -->
            </svg>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：展示“暂无黑名单用户”文案 -->
          <div class="empty-title">暂无黑名单用户</div>
          <!-- 行注：展示“被拉黑的用户会显示在这里”文案 -->
          <div class="empty-subtitle">被拉黑的用户会显示在这里</div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="right-panel">
      <!-- 行注：渲染容器 -->
      <div class="info-card">
        <!-- 行注：渲染容器 -->
        <div class="info-icon">
          <!-- 行注：渲染图标容器 -->
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="12" cy="12" r="10"/>
            <!-- 行注：补充图标线段 -->
            <line x1="12" y1="16" x2="12" y2="12"/>
            <!-- 行注：补充图标线段 -->
            <line x1="12" y1="8" x2="12.01" y2="8"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“黑名单说明”文案 -->
        <h3>黑名单说明</h3>
        <!-- 行注：渲染ul 节点 -->
        <ul>
          <!-- 行注：展示“拉黑后对方无法给你发消息”文案 -->
          <li>拉黑后对方无法给你发消息</li>
          <!-- 行注：展示“拉黑后对方无法看到你的在线状态”文案 -->
          <li>拉黑后对方无法看到你的在线状态</li>
          <!-- 行注：展示“你可以随时将对方从黑名单移除”文案 -->
          <li>你可以随时将对方从黑名单移除</li>
        <!-- 行注：结束ul 节点 -->
        </ul>
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
 * 黑名单页面，处理拉黑列表和移除操作。
 */
import { ref, onMounted } from 'vue'
import { blacklistApi } from '../api/client'
import ProtectedImage from '../components/ProtectedImage.vue'
import { useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力

const message = useMessage()  // 行注：获取全局消息实例
const blacklist = ref<any[]>([])  // 行注：初始化 blacklist 集合
const loading = ref(false)  // 行注：初始化 loading 响应式状态

onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
  loadBlacklist()  // 行注：调用 loadBlacklist 方法
})  // 行注：结束当前调用配置

async function loadBlacklist() {  // 行注：定义异步 loadBlacklist 方法
  loading.value = true  // 行注：更新 loading 状态
  try {  // 行注：尝试执行可能失败的逻辑
    const res: any = await blacklistApi.list()  // 行注：接收 res 异步结果
    blacklist.value = res.data || []  // 行注：更新 blacklist 状态
  } catch (e) {  // 行注：捕获并处理异常
    console.error('loadBlacklist error:', e)  // 行注：输出错误日志
  } finally {  // 行注：执行收尾清理逻辑
    loading.value = false  // 行注：更新 loading 状态
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

async function handleRemove(userId: number) {  // 行注：定义异步 handleRemove 方法
  try {  // 行注：尝试执行可能失败的逻辑
    await blacklistApi.remove(userId)  // 行注：调用 remove 方法
    message.success('已移除')  // 行注：提示成功信息
    await loadBlacklist()  // 行注：调用 loadBlacklist 方法
  } catch (e: any) {  // 行注：捕获并处理异常
    message.error(e.response?.data?.message || '操作失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.content-area {  /* 行注：定义 .content-area 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.blacklist-panel {  /* 行注：定义 .blacklist-panel 样式 */
  width: 360px;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-right: 1px solid var(--linkx-border);  /* 行注：设置 border-right 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  min-width: 300px;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.panel-header {
  min-height: 64px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
}

.header-text {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.12) 0%, rgba(238, 90, 90, 0.08) 100%);
  color: #ee5a5a;
  border: 1px solid var(--linkx-border);
  flex-shrink: 0;
}

.header-titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--linkx-text);
  letter-spacing: -0.01em;
  line-height: 1.2;
}

.header-sub {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.header-count {
  font-size: 12px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--linkx-bg);
  color: var(--linkx-text-secondary);
  border: 1px solid var(--linkx-border);
}

.list-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 48px 16px;
  color: var(--linkx-text-muted);
  font-size: 13px;
}

.list-loading-spinner {
  width: 28px;
  height: 28px;
  border: 2px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  border-radius: 50%;
  animation: blacklist-spin 0.75s linear infinite;
}

@keyframes blacklist-spin {
  to {
    transform: rotate(360deg);
  }
}

.refresh-btn {  /* 行注：定义 .refresh-btn 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.refresh-btn:hover {  /* 行注：定义 .refresh-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.blacklist-list {  /* 行注：定义 .blacklist-list 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 8px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.blacklist-item {
  display: flex;
  align-items: center;
  padding: 12px 14px;
  gap: 12px;
  margin-bottom: 6px;
  border-radius: var(--linkx-radius);
  border: 1px solid transparent;
  transition: var(--linkx-transition-fast);
}

.blacklist-item:hover {
  background: var(--linkx-bg-hover);
  border-color: var(--linkx-border);
}

.user-avatar {  /* 行注：定义 .user-avatar 样式 */
  width: 44px;  /* 行注：设置 width 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.user-info {  /* 行注：定义 .user-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.user-name {  /* 行注：定义 .user-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 2px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.user-username {  /* 行注：定义 .user-username 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.remove-btn {  /* 行注：定义 .remove-btn 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  padding: 8px 12px;  /* 行注：设置 padding 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.remove-btn:hover {  /* 行注：定义 .remove-btn:hover 样式 */
  border-color: var(--linkx-error);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
  background: rgba(255, 61, 113, 0.05);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.empty-icon {  /* 行注：定义 .empty-icon 样式 */
  opacity: 0.3;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-width: 0;
  background: radial-gradient(ellipse 80% 60% at 50% 40%, rgba(0, 214, 143, 0.04) 0%, transparent 70%);
}

.info-card {
  max-width: 380px;
  padding: 32px 36px;
  text-align: center;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg, 14px);
  box-shadow: var(--linkx-shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.06));
}

.info-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 18px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 214, 143, 0.12) 0%, rgba(0, 149, 255, 0.08) 100%);
  color: var(--linkx-primary);
}

.info-card h3 {  /* 行注：定义 .info-card h3 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.info-card ul {  /* 行注：定义 .info-card ul 样式 */
  list-style: none;  /* 行注：设置 list-style 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
}  /* 行注：结束当前样式块 */

.info-card li {  /* 行注：定义 .info-card li 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  padding: 8px 0;  /* 行注：设置 padding 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.info-card li:last-child {  /* 行注：定义 .info-card li:last-child 样式 */
  border-bottom: none;  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.info-card li::before {  /* 行注：定义 .info-card li::before 样式 */
  content: '•';  /* 行注：设置 content 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  margin-right: 8px;  /* 行注：设置 margin-right 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 960px) {  /* 行注：声明响应式样式区块 */
  .blacklist-panel {  /* 行注：定义 .blacklist-panel 样式 */
    width: 300px;  /* 行注：设置 width 样式 */
    min-width: 260px;  /* 行注：设置 min-width 样式 */
  }  /* 行注：结束当前样式块 */

  .right-panel {  /* 行注：定义 .right-panel 样式 */
    padding: 24px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 760px) {  /* 行注：声明响应式样式区块 */
  .content-area {  /* 行注：定义 .content-area 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  }  /* 行注：结束当前样式块 */

  .blacklist-panel {  /* 行注：定义 .blacklist-panel 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    min-width: 0;  /* 行注：设置 min-width 样式 */
    max-height: 46%;  /* 行注：设置 max-height 样式 */
    border-right: none;  /* 行注：设置 border-right 样式 */
    border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
  }  /* 行注：结束当前样式块 */

  .right-panel {  /* 行注：定义 .right-panel 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .info-card {  /* 行注：定义 .info-card 样式 */
    max-width: none;  /* 行注：设置 max-width 样式 */
    text-align: left;  /* 行注：设置 text-align 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .blacklist-item {  /* 行注：定义 .blacklist-item 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
    padding: 12px 10px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .remove-btn {  /* 行注：定义 .remove-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */

  .right-panel {  /* 行注：定义 .right-panel 样式 */
    padding: 16px 12px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
