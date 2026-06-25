<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="group-notice-dialog-overlay" @click.self="handleClose">
    <!-- 行注：渲染容器 -->
    <div class="group-notice-dialog-card">
      <!-- 行注：渲染容器 -->
      <div class="group-notice-dialog-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“群公告提醒”文案 -->
          <div class="group-notice-dialog-title">群公告提醒</div>
          <!-- 行注：渲染容器 -->
          <div class="group-notice-dialog-subtitle">{{ groupName || '当前群聊' }}</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“×”文案 -->
        <button class="group-notice-dialog-close" :disabled="loading" @click="handleClose">×</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="group-notice-dialog-meta">
        <!-- 行注：展示“发布时间：{{ updateTime”文案 -->
        <span v-if="updateTimeText">发布时间：{{ updateTimeText }}</span>
        <!-- 行注：展示“你有一条新的群公告待查看”文案 -->
        <span v-else>你有一条新的群公告待查看</span>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="group-notice-dialog-body">
        <!-- 行注：渲染动态文本 -->
        {{ notice?.trim() || '暂无群公告内容' }}
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="group-notice-dialog-actions">
        <!-- 行注：渲染按钮 -->
        <button class="group-notice-dialog-primary-btn" :disabled="loading" @click="handleAcknowledge">
          <!-- 行注：渲染动态文本 -->
          {{ loading ? '我知道了...' : '我知道了' }}
        <!-- 行注：结束按钮 -->
        </button>
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
 * GroupNoticeDialog 组件，负责当前界面片段的展示与交互。
 */
const props = withDefaults(defineProps<{  // 行注：开始解构当前返回值
  visible: boolean  // 行注：设置 visible 配置项
  groupName?: string  // 行注：补充当前表达式
  notice?: string  // 行注：补充当前表达式
  updateTimeText?: string  // 行注：补充当前表达式
  loading?: boolean  // 行注：补充当前表达式
}>(), {  // 行注：执行当前调用逻辑
  groupName: '',  // 行注：设置 groupName 配置项
  notice: '',  // 行注：设置 notice 配置项
  updateTimeText: '',  // 行注：设置 updateTimeText 配置项
  loading: false  // 行注：设置 loading 配置项
})  // 行注：结束当前调用配置

const emit = defineEmits<{  // 行注：开始解构当前返回值
  'update:visible': [value: boolean]  // 行注：补充当前表达式
  close: []  // 行注：设置 close 配置项
  acknowledge: []  // 行注：设置 acknowledge 配置项
}>()  // 行注：执行当前调用逻辑

function handleClose() {  // 行注：定义 handleClose 方法
  if (props.loading) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  emit('update:visible', false)  // 行注：调用 emit 方法
  emit('close')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function handleAcknowledge() {  // 行注：定义 handleAcknowledge 方法
  if (props.loading) {  // 行注：判断当前条件是否成立
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  emit('acknowledge')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.group-notice-dialog-overlay {  /* 行注：定义 .group-notice-dialog-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding: 56px 24px 24px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 0, 0, 0.54);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
  z-index: 2250;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-card {  /* 行注：定义 .group-notice-dialog-card 样式 */
  width: min(460px, 100%);  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-header,  /* 行注：补充 .group-notice-dialog-header 选择器 */
.group-notice-dialog-actions {  /* 行注：定义 .group-notice-dialog-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-title {  /* 行注：定义 .group-notice-dialog-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-subtitle,  /* 行注：补充 .group-notice-dialog-subtitle 选择器 */
.group-notice-dialog-meta {  /* 行注：定义 .group-notice-dialog-meta 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-subtitle {  /* 行注：定义 .group-notice-dialog-subtitle 样式 */
  margin-top: 4px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-close {  /* 行注：定义 .group-notice-dialog-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-md);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  line-height: 1.2;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-meta {  /* 行注：定义 .group-notice-dialog-meta 样式 */
  margin-top: 16px;  /* 行注：设置 margin-top 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-body {  /* 行注：定义 .group-notice-dialog-body 样式 */
  margin-top: 14px;  /* 行注：设置 margin-top 样式 */
  padding: 16px;  /* 行注：设置 padding 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, var(--linkx-bg-hover) 74%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 84%, transparent);  /* 行注：设置 border 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.8;  /* 行注：设置 line-height 样式 */
  white-space: pre-wrap;  /* 行注：设置 white-space 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-actions {  /* 行注：定义 .group-notice-dialog-actions 样式 */
  margin-top: 20px;  /* 行注：设置 margin-top 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-primary-btn {  /* 行注：定义 .group-notice-dialog-primary-btn 样式 */
  min-width: 120px;  /* 行注：设置 min-width 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-notice-dialog-primary-btn:hover {  /* 行注：定义 .group-notice-dialog-primary-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 640px) {  /* 行注：声明响应式样式区块 */
  .group-notice-dialog-overlay {  /* 行注：定义 .group-notice-dialog-overlay 样式 */
    padding: 56px 16px 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .group-notice-dialog-actions {  /* 行注：定义 .group-notice-dialog-actions 样式 */
    justify-content: stretch;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */

  .group-notice-dialog-primary-btn {  /* 行注：定义 .group-notice-dialog-primary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
