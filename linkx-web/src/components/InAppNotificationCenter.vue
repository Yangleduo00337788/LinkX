<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染 Teleport 组件 -->
  <Teleport to="body">
    <!-- 行注：渲染容器 -->
    <div v-if="items.length" class="in-app-notification-center" aria-live="polite" aria-atomic="false">
      <!-- 行注：渲染按钮 -->
      <button
        v-for="item in items"
        :key="item.id"
        type="button"
        class="in-app-notification-card"
        @click="handleOpen(item)"
      >
        <!-- 行注：渲染容器 -->
        <div class="in-app-notification-accent" :class="{ attention: item.attention }"></div>
        <!-- 行注：渲染容器 -->
        <div class="in-app-notification-body">
          <!-- 行注：渲染容器 -->
          <div class="in-app-notification-header">
            <!-- 行注：渲染文本节点 -->
            <span class="in-app-notification-title">{{ item.title }}</span>
            <!-- 行注：渲染文本节点 -->
            <span class="in-app-notification-time">{{ item.timeText }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="in-app-notification-content">{{ item.body }}</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染文本节点 -->
        <span
          class="in-app-notification-close"
          title="关闭"
          @click.stop="removeItem(item.id)"
        >
          <!-- 行注：展示“×”文案 -->
          ×
        <!-- 行注：结束文本节点 -->
        </span>
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束Teleport 节点 -->
  </Teleport>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * InAppNotificationCenter 组件，负责当前界面片段的展示与交互。
 */
import { onBeforeUnmount, onMounted, ref } from 'vue'  // 行注：引入 onBeforeUnmount, onMounted, ref 能力
import { useRouter } from 'vue-router'  // 行注：引入 useRouter 能力
import { onInAppNotification, type InAppNotificationPayload } from '../utils/notify'  // 行注：引入 onInAppNotification, type InAppNotificationPayload 能力

interface NotificationViewModel extends InAppNotificationPayload {  // 行注：开始当前逻辑块
  timeText: string  // 行注：设置 timeText 配置项
}  // 行注：结束当前代码块

const router = useRouter()  // 行注：获取路由实例
const items = ref<NotificationViewModel[]>([])  // 行注：初始化 items 变量
const timerMap = new Map<string, number>()  // 行注：初始化 timerMap 变量
let stopListening: (() => void) | null = null  // 行注：初始化 stopListening 变量

function formatNow() {  // 行注：定义 formatNow 方法
  return new Intl.DateTimeFormat('zh-CN', {  // 行注：返回当前结果
    hour: '2-digit',  // 行注：设置 hour 配置项
    minute: '2-digit'  // 行注：设置 minute 配置项
  }).format(new Date())  // 行注：调用 format 方法
}  // 行注：结束当前代码块

function clearTimer(id: string) {  // 行注：定义 clearTimer 方法
  const timer = timerMap.get(id)  // 行注：初始化 timer 变量
  if (timer) {  // 行注：判断当前条件是否成立
    window.clearTimeout(timer)  // 行注：调用 clearTimeout 方法
    timerMap.delete(id)  // 行注：调用 delete 方法
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

function removeItem(id: string) {  // 行注：定义 removeItem 方法
  clearTimer(id)  // 行注：调用 clearTimer 方法
  items.value = items.value.filter(item => item.id !== id)  // 行注：调用 filter 方法
}  // 行注：结束当前代码块

function removeItemsByMessageIds(messageIds: string[]) {  // 行注：定义 removeItemsByMessageIds 方法
  const targetIds = new Set(messageIds)  // 行注：初始化 targetIds 变量
  const removableItems = items.value.filter(item => item.messageId && targetIds.has(item.messageId))  // 行注：初始化 removableItems 变量
  for (const item of removableItems) {  // 行注：遍历当前数据集合
    clearTimer(item.id)  // 行注：调用 clearTimer 方法
  }  // 行注：结束当前代码块
  items.value = items.value.filter(item => !item.messageId || !targetIds.has(item.messageId))  // 行注：调用 filter 方法
}  // 行注：结束当前代码块

function scheduleDismiss(id: string) {  // 行注：定义 scheduleDismiss 方法
  clearTimer(id)  // 行注：调用 clearTimer 方法
  const timer = window.setTimeout(() => {  // 行注：开始解构当前返回值
    removeItem(id)  // 行注：调用 removeItem 方法
  }, 5200)  // 行注：补充当前表达式
  timerMap.set(id, timer)  // 行注：调用 set 方法
}  // 行注：结束当前代码块

async function handleOpen(item: NotificationViewModel) {  // 行注：定义异步 handleOpen 方法
  removeItem(item.id)  // 行注：调用 removeItem 方法
  if (!item.targetId) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const query: Record<string, string> = {}  // 行注：初始化 query 变量
  if (item.sessionType === 2) {  // 行注：判断当前条件是否成立
    query.sessionType = '2'  // 行注：更新 query.sessionType 值
  }  // 行注：结束当前代码块
  if (item.messageId) {  // 行注：判断当前条件是否成立
    query.messageId = item.messageId  // 行注：更新 query.messageId 值
  }  // 行注：结束当前代码块
  await router.push({  // 行注：开始当前逻辑块
    path: `/chat/${item.targetId}`,  // 行注：设置 path 配置项
    query  // 行注：传入 query 参数
  })  // 行注：结束当前调用配置
}  // 行注：结束当前代码块

function pushItem(payload: InAppNotificationPayload) {  // 行注：定义 pushItem 方法
  const nextItem: NotificationViewModel = {  // 行注：开始解构当前返回值
    ...payload,  // 行注：补充当前配置项
    title: payload.title || '新消息',  // 行注：设置 title 配置项
    body: payload.body || '你有一条新提醒',  // 行注：设置 body 配置项
    timeText: formatNow()  // 行注：设置 timeText 配置项
  }  // 行注：结束当前代码块
  items.value = [nextItem, ...items.value.filter(item => item.id !== payload.id)].slice(0, 3)  // 行注：调用 filter 方法
  scheduleDismiss(payload.id)  // 行注：调用 scheduleDismiss 方法
}  // 行注：结束当前代码块

onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
  stopListening = onInAppNotification(event => {  // 行注：开始当前逻辑块
    if (event.type === 'removeByMessageIds') {  // 行注：判断当前条件是否成立
      removeItemsByMessageIds(event.messageIds)  // 行注：调用 removeItemsByMessageIds 方法
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    pushItem(event.payload)  // 行注：调用 pushItem 方法
  })  // 行注：结束当前调用配置
})  // 行注：结束当前调用配置

onBeforeUnmount(() => {  // 行注：调用 onBeforeUnmount 方法
  stopListening?.()  // 行注：执行当前调用逻辑
  for (const id of timerMap.keys()) {  // 行注：遍历当前数据集合
    clearTimer(id)  // 行注：调用 clearTimer 方法
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.in-app-notification-center {  /* 行注：定义 .in-app-notification-center 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  right: 18px;  /* 行注：设置 right 样式 */
  bottom: 18px;  /* 行注：设置 bottom 样式 */
  z-index: 2200;  /* 行注：设置 z-index 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column-reverse;  /* 行注：设置 flex-direction 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-card {  /* 行注：定义 .in-app-notification-card 样式 */
  width: min(320px, calc(100vw - 24px));  /* 行注：设置 width 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, black 8%);  /* 行注：设置 background 样式 */
  border-radius: 14px;  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: stretch;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  pointer-events: auto;  /* 行注：设置 pointer-events 样式 */
  backdrop-filter: blur(16px);  /* 行注：设置 backdrop-filter 样式 */
  transition: transform 0.18s ease, border-color 0.18s ease;  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-card:hover {  /* 行注：定义 .in-app-notification-card:hover 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-accent {  /* 行注：定义 .in-app-notification-accent 样式 */
  width: 4px;  /* 行注：设置 width 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-accent.attention {  /* 行注：定义 .in-app-notification-accent.attention 样式 */
  background: #f6ad55;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-body {  /* 行注：定义 .in-app-notification-body 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  padding: 12px 0 12px 12px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-header {  /* 行注：定义 .in-app-notification-header 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-title {  /* 行注：定义 .in-app-notification-title 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-time {  /* 行注：定义 .in-app-notification-time 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-content {  /* 行注：定义 .in-app-notification-content 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  line-height: 1.45;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  display: -webkit-box;  /* 行注：设置 display 样式 */
  -webkit-line-clamp: 2;  /* 行注：设置 -webkit-line-clamp 样式 */
  -webkit-box-orient: vertical;  /* 行注：设置 -webkit-box-orient 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-close {  /* 行注：定义 .in-app-notification-close 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.in-app-notification-close:hover {  /* 行注：定义 .in-app-notification-close:hover 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 760px) {  /* 行注：声明响应式样式区块 */
  .in-app-notification-center {  /* 行注：定义 .in-app-notification-center 样式 */
    left: 12px;  /* 行注：设置 left 样式 */
    right: 12px;  /* 行注：设置 right 样式 */
    bottom: calc(84px + env(safe-area-inset-bottom, 0px));  /* 行注：设置 bottom 样式 */
  }  /* 行注：结束当前样式块 */

  .in-app-notification-card {  /* 行注：定义 .in-app-notification-card 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
