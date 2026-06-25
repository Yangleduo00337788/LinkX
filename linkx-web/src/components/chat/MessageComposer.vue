<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="chat-input-area">
    <!-- 行注：渲染容器 -->
    <div class="input-toolbar">
      <!-- 行注：渲染容器 -->
      <div class="emoji-wrapper" :ref="emojiRefSetter">
        <!-- 行注：渲染按钮 -->
        <button class="toolbar-btn" title="表情" @click="$emit('toggle-emoji-picker')">
          <!-- 行注：渲染图标容器 -->
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="12" cy="12" r="10" />
            <!-- 行注：补充图标路径 -->
            <path d="M8 14s1.5 2 4 2 4-2 4-2" />
            <!-- 行注：补充图标线段 -->
            <line x1="9" y1="9" x2="9.01" y2="9" />
            <!-- 行注：补充图标线段 -->
            <line x1="15" y1="9" x2="15.01" y2="9" />
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束按钮 -->
        </button>
        <!-- 行注：渲染容器 -->
        <div v-if="showEmojiPicker" class="emoji-picker">
          <!-- 行注：渲染容器 -->
          <div class="emoji-grid">
            <!-- 行注：渲染文本节点 -->
            <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="$emit('insert-emoji', emoji)">{{ emoji }}</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染按钮 -->
      <button class="toolbar-btn" title="文件" :disabled="currentMuted" @click="$emit('trigger-file-upload')">
        <!-- 行注：渲染图标容器 -->
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <!-- 行注：补充图标路径 -->
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <!-- 行注：补充图标折线 -->
          <polyline points="14 2 14 8 20 8" />
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
      <!-- 行注：渲染按钮 -->
      <button class="toolbar-btn" title="图片" :disabled="currentMuted" @click="$emit('trigger-image-upload')">
        <!-- 行注：渲染图标容器 -->
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <!-- 行注：补充图标矩形路径 -->
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <!-- 行注：补充图标圆形路径 -->
          <circle cx="8.5" cy="8.5" r="1.5" />
          <!-- 行注：补充图标折线 -->
          <polyline points="21 15 16 10 5 21" />
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="input-container" :class="{ disabled: currentMuted }">
      <!-- 行注：渲染文本域 -->
      <textarea
        :ref="textareaRefSetter"
        :value="inputMessage"
        :placeholder="currentMuted ? '你已被禁言，暂时无法发送消息' : '输入消息...'"
        class="message-input"
        rows="1"
        :disabled="currentMuted"
        @input="handleInput"
        @keydown="$emit('input-keydown', $event)"
        @click="$emit('sync-mention-menu')"
      ></textarea>
      <!-- 行注：渲染按钮 -->
      <button
        class="send-btn"
        :disabled="!inputMessage.trim() || sending || currentMuted"
        :class="{ active: inputMessage.trim() && !currentMuted }"
        @click="$emit('send')"
      >
        <!-- 行注：渲染图标容器 -->
        <svg v-if="!sending" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标线段 -->
          <line x1="22" y1="2" x2="11" y2="13" />
          <!-- 行注：渲染polygon 节点 -->
          <polygon points="22 2 15 22 11 13 2 9 22 2" />
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：渲染容器 -->
        <div v-else class="send-loading"></div>
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-if="showMentionMenu" class="mention-menu" :ref="mentionMenuRefSetter">
      <!-- 行注：渲染容器 -->
      <div v-if="mentionCandidates.length > 0" class="mention-menu-list">
        <!-- 行注：渲染按钮 -->
        <button
          v-for="(candidate, index) in mentionCandidates"
          :key="candidate.key"
          type="button"
          class="mention-menu-item"
          :class="{ active: index === mentionHighlightedIndex }"
          @mousedown.prevent="$emit('select-mention-candidate', candidate)"
        >
          <!-- 行注：渲染文本节点 -->
          <span class="mention-menu-name">{{ candidate.label }}</span>
          <!-- 行注：渲染文本节点 -->
          <span class="mention-menu-meta">{{ candidate.meta }}</span>
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：展示“未找到可提及成员”文案 -->
      <div v-else class="mention-menu-empty">未找到可提及成员</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * MessageComposer 组件，负责当前界面片段的展示与交互。
 */
import type { ComponentPublicInstance } from 'vue'  // 行注：引入 type { ComponentPublicInstance } 模块
import type { MentionCandidate } from '../../types/chat'  // 行注：引入 type { MentionCandidate } 模块

defineProps<{  // 行注：开始当前逻辑块
  inputMessage: string  // 行注：设置 inputMessage 配置项
  sending: boolean  // 行注：设置 sending 配置项
  currentMuted: boolean  // 行注：设置 currentMuted 配置项
  showEmojiPicker: boolean  // 行注：设置 showEmojiPicker 配置项
  showMentionMenu: boolean  // 行注：设置 showMentionMenu 配置项
  emojis: string[]  // 行注：设置 emojis 配置项
  mentionCandidates: MentionCandidate[]  // 行注：设置 mentionCandidates 配置项
  mentionHighlightedIndex: number  // 行注：设置 mentionHighlightedIndex 配置项
  textareaRefSetter: (element: Element | ComponentPublicInstance | null) => void  // 行注：设置 textareaRefSetter 配置项
  emojiRefSetter: (element: Element | ComponentPublicInstance | null) => void  // 行注：设置 emojiRefSetter 配置项
  mentionMenuRefSetter: (element: Element | ComponentPublicInstance | null) => void  // 行注：设置 mentionMenuRefSetter 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'update:inputMessage', value: string): void  // 行注：执行当前调用逻辑
  (event: 'toggle-emoji-picker'): void  // 行注：执行当前调用逻辑
  (event: 'insert-emoji', value: string): void  // 行注：执行当前调用逻辑
  (event: 'trigger-file-upload'): void  // 行注：执行当前调用逻辑
  (event: 'trigger-image-upload'): void  // 行注：执行当前调用逻辑
  (event: 'input-keydown', value: KeyboardEvent): void  // 行注：执行当前调用逻辑
  (event: 'input-change'): void  // 行注：执行当前调用逻辑
  (event: 'sync-mention-menu'): void  // 行注：执行当前调用逻辑
  (event: 'send'): void  // 行注：执行当前调用逻辑
  (event: 'select-mention-candidate', value: MentionCandidate): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑

function handleInput(event: Event) {  // 行注：定义 handleInput 方法
  const target = event.target as HTMLTextAreaElement  // 行注：初始化 target 变量
  emit('update:inputMessage', target.value || '')  // 行注：调用 emit 方法
  emit('input-change')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.chat-input-area {  /* 行注：定义 .chat-input-area 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  padding: 12px 20px 16px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-top: 1px solid var(--linkx-border);  /* 行注：设置 border-top 样式 */
}  /* 行注：结束当前样式块 */

.input-toolbar {  /* 行注：定义 .input-toolbar 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  margin-bottom: 8px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.emoji-wrapper {  /* 行注：定义 .emoji-wrapper 样式 */
  position: relative;  /* 行注：设置 position 样式 */
}  /* 行注：结束当前样式块 */

.toolbar-btn {  /* 行注：定义 .toolbar-btn 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.toolbar-btn:hover {  /* 行注：定义 .toolbar-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.toolbar-btn:disabled,  /* 行注：补充 .toolbar-btn:disabled 选择器 */
.send-btn:disabled,  /* 行注：补充 .send-btn:disabled 选择器 */
.input-container.disabled {  /* 行注：定义 .input-container.disabled 样式 */
  opacity: 0.6;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.emoji-picker,  /* 行注：补充 .emoji-picker 选择器 */
.mention-menu {  /* 行注：定义 .mention-menu 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.emoji-picker {  /* 行注：定义 .emoji-picker 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  bottom: 40px;  /* 行注：设置 bottom 样式 */
  left: 0;  /* 行注：设置 left 样式 */
  width: 320px;  /* 行注：设置 width 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
  z-index: 100;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.emoji-grid {  /* 行注：定义 .emoji-grid 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: repeat(8, 1fr);  /* 行注：设置 grid-template-columns 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  max-height: 200px;  /* 行注：设置 max-height 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
}  /* 行注：结束当前样式块 */

.emoji-item {  /* 行注：定义 .emoji-item 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  font-size: 20px;  /* 行注：设置 font-size 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.emoji-item:hover {  /* 行注：定义 .emoji-item:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.input-container {  /* 行注：定义 .input-container 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-end;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 8px 12px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.message-input {  /* 行注：定义 .message-input 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-height: 24px;  /* 行注：设置 min-height 样式 */
  max-height: 120px;  /* 行注：设置 max-height 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  box-shadow: none;  /* 行注：设置 box-shadow 样式 */
  resize: none;  /* 行注：设置 resize 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-family: inherit;  /* 行注：设置 font-family 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.5;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.message-input::placeholder {  /* 行注：定义 .message-input::placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.message-input:focus,  /* 行注：补充 .message-input:focus 选择器 */
.message-input:focus-visible {  /* 行注：定义 .message-input:focus-visible 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  box-shadow: none;  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.send-btn {  /* 行注：定义 .send-btn 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.send-btn.active {  /* 行注：定义 .send-btn.active 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  box-shadow: 0 2px 8px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.send-loading {  /* 行注：定义 .send-loading 样式 */
  width: 16px;  /* 行注：设置 width 样式 */
  height: 16px;  /* 行注：设置 height 样式 */
  border: 2px solid var(--linkx-text-muted);  /* 行注：设置 border 样式 */
  border-top-color: transparent;  /* 行注：设置 border-top-color 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  to {  /* 行注：设置动画结束状态 */
    transform: rotate(360deg);  /* 行注：设置 transform 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

.mention-menu {  /* 行注：定义 .mention-menu 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  left: 20px;  /* 行注：设置 left 样式 */
  right: 20px;  /* 行注：设置 right 样式 */
  bottom: calc(100% - 8px);  /* 行注：设置 bottom 样式 */
  z-index: 120;  /* 行注：设置 z-index 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-list {  /* 行注：定义 .mention-menu-list 样式 */
  max-height: 240px;  /* 行注：设置 max-height 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-item {  /* 行注：定义 .mention-menu-item 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 10px 14px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-item:hover,  /* 行注：补充 .mention-menu-item:hover 选择器 */
.mention-menu-item.active {  /* 行注：定义 .mention-menu-item.active 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-name {  /* 行注：定义 .mention-menu-name 样式 */
  color: inherit;  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-meta,  /* 行注：补充 .mention-menu-meta 选择器 */
.mention-menu-empty {  /* 行注：定义 .mention-menu-empty 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.mention-menu-empty {  /* 行注：定义 .mention-menu-empty 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */
</style>
