<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="titlebar" :class="{ 'is-maximized': isMaximized }">
    <!-- 行注：渲染容器 -->
    <div class="titlebar-drag">
      <!-- 行注：渲染容器 -->
      <div class="titlebar-brand">
        <!-- 行注：渲染图片 -->
        <img :src="logoImg" class="brand-icon-img" alt="LinkX" />
        <!-- 行注：展示“LinkX”文案 -->
        <span class="brand-text">LinkX</span>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="titlebar-controls">
      <!-- 行注：渲染按钮 -->
      <button
        class="titlebar-btn minimize"
        @click="minimize"
        title="最小化"
      >
        <!-- 行注：渲染图标容器 -->
        <svg width="10" height="1" viewBox="0 0 10 1">
          <!-- 行注：补充图标矩形路径 -->
          <rect fill="currentColor" width="10" height="1"/>
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button
        class="titlebar-btn maximize"
        @click="maximize"
        :title="isMaximized ? '还原' : '最大化'"
      >
        <!-- 行注：渲染图标容器 -->
        <svg v-if="!isMaximized" width="10" height="10" viewBox="0 0 10 10">
          <!-- 行注：补充图标矩形路径 -->
          <rect fill="none" stroke="currentColor" x="0.5" y="0.5" width="9" height="9" rx="1"/>
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：渲染图标容器 -->
        <svg v-else width="10" height="10" viewBox="0 0 10 10">
          <!-- 行注：补充图标矩形路径 -->
          <rect fill="none" stroke="currentColor" x="2" y="0" width="8" height="8" rx="1"/>
          <!-- 行注：补充图标矩形路径 -->
          <rect fill="var(--linkx-bg-card)" stroke="currentColor" x="0" y="2" width="8" height="8" rx="1"/>
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button
        class="titlebar-btn close"
        @click="close"
        title="关闭"
      >
        <!-- 行注：渲染图标容器 -->
        <svg width="10" height="10" viewBox="0 0 10 10">
          <!-- 行注：补充图标路径 -->
          <path fill="currentColor" d="M1 0L0 1L4 5L0 9L1 10L5 6L9 10L10 9L6 5L10 1L9 0L5 4L1 0Z"/>
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * TitleBar 组件，负责当前界面片段的展示与交互。
 */
import { ref, onMounted } from 'vue'  // 行注：引入 ref, onMounted 能力
import logoImg from '../assets/tray.png'  // 行注：引入 logoImg 模块

const isMaximized = ref(false)  // 行注：初始化 isMaximized 响应式状态

const electronAPI = (window as any).electronAPI  // 行注：初始化 electronAPI 实例

onMounted(async () => {  // 行注：注册组件挂载后的初始化逻辑
  if (electronAPI) {  // 行注：判断当前条件是否成立
    isMaximized.value = await electronAPI.isMaximized()  // 行注：更新 isMaximized 状态
    electronAPI.onWindowMaximize((maximized: boolean) => {  // 行注：调用 onWindowMaximize 方法
      isMaximized.value = maximized  // 行注：更新 isMaximized 状态
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

async function minimize() {  // 行注：定义异步 minimize 方法
  await electronAPI?.minimizeWindow()  // 行注：调用 minimizeWindow 方法
}  // 行注：结束当前代码块

async function maximize() {  // 行注：定义异步 maximize 方法
  await electronAPI?.maximizeWindow()  // 行注：调用 maximizeWindow 方法
}  // 行注：结束当前代码块

async function close() {  // 行注：定义异步 close 方法
  await electronAPI?.closeWindow()  // 行注：调用 closeWindow 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.titlebar {  /* 行注：定义 .titlebar 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
  user-select: none;  /* 行注：设置 user-select 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  z-index: 100;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.titlebar::after {  /* 行注：定义 .titlebar::after 样式 */
  content: '';  /* 行注：设置 content 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  bottom: 0;  /* 行注：设置 bottom 样式 */
  left: 0;  /* 行注：设置 left 样式 */
  right: 0;  /* 行注：设置 right 样式 */
  height: 1px;  /* 行注：设置 height 样式 */
  background: linear-gradient(90deg, transparent, var(--linkx-primary-glow), transparent);  /* 行注：设置 background 样式 */
  opacity: 0.5;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-drag {  /* 行注：定义 .titlebar-drag 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  -webkit-app-region: drag;  /* 行注：设置 -webkit-app-region 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding-left: 12px;  /* 行注：设置 padding-left 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-brand {  /* 行注：定义 .titlebar-brand 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.brand-icon-img {  /* 行注：定义 .brand-icon-img 样式 */
  width: 22px;  /* 行注：设置 width 样式 */
  height: 22px;  /* 行注：设置 height 样式 */
  border-radius: 6px;  /* 行注：设置 border-radius 样式 */
  object-fit: contain;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.brand-text {  /* 行注：定义 .brand-text 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  letter-spacing: 0.5px;  /* 行注：设置 letter-spacing 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-controls {  /* 行注：定义 .titlebar-controls 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  -webkit-app-region: no-drag;  /* 行注：设置 -webkit-app-region 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn {  /* 行注：定义 .titlebar-btn 样式 */
  width: 46px;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  position: relative;  /* 行注：设置 position 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn::before {  /* 行注：定义 .titlebar-btn::before 样式 */
  content: '';  /* 行注：设置 content 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: 4px;  /* 行注：设置 inset 样式 */
  border-radius: 4px;  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  transition: background 0.15s ease;  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn:hover::before {  /* 行注：定义 .titlebar-btn:hover::before 样式 */
  background: rgba(255, 255, 255, 0.08);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn:active::before {  /* 行注：定义 .titlebar-btn:active::before 样式 */
  background: rgba(255, 255, 255, 0.04);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn.close:hover::before {  /* 行注：定义 .titlebar-btn.close:hover::before 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn.close:hover {  /* 行注：定义 .titlebar-btn.close:hover 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.titlebar-btn svg {  /* 行注：定义 .titlebar-btn svg 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  z-index: 1;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */
</style>
