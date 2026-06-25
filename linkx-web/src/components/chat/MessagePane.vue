<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="loadingMessages" class="chat-loading">
    <!-- 行注：渲染容器 -->
    <div class="loading-spinner"></div>
    <!-- 行注：展示“加载中...”文案 -->
    <span>加载中...</span>
  <!-- 行注：结束容器 -->
  </div>
  <!-- 行注：开始定义模板区域 -->
  <template v-else>
    <!-- 行注：渲染容器 -->
    <div
      v-if="showMentionBanner"
      role="button"
      tabindex="0"
      class="mention-banner"
      @click="$emit('mention-banner-click')"
      @keydown.enter.prevent="$emit('mention-banner-click')"
      @keydown.space.prevent="$emit('mention-banner-click')"
    >
      <!-- 行注：展示“特别提醒”文案 -->
      <span class="mention-banner-badge">特别提醒</span>
      <!-- 行注：渲染文本节点 -->
      <span class="mention-banner-text">{{ mentionBannerText }}</span>
      <!-- 行注：渲染文本节点 -->
      <span class="mention-banner-action">{{ mentionBannerActionText }}</span>
      <!-- 行注：渲染文本节点 -->
      <span class="mention-banner-close" @click.stop="$emit('dismiss-mention-banner')">
        <!-- 行注：渲染图标容器 -->
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标路径 -->
          <path d="M18 6L6 18M6 6l12 12" />
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束文本节点 -->
      </span>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div
      v-for="(msg, index) in messages"
      :key="msg.id || index"
      class="message-row"
      :class="{ self: msg.isMe, system: msg.isSystem, located: activeJumpMessageKey === getMessageAnchorKey(msg) }"
      :data-message-key="getMessageAnchorKey(msg)"
    >
      <!-- 行注：开始定义模板区域 -->
      <template v-if="msg.isSystem">
        <!-- 行注：渲染容器 -->
        <div class="system-message">{{ msg.content }}</div>
      <!-- 行注：结束模板区域 -->
      </template>
      <!-- 行注：开始定义模板区域 -->
      <template v-else>
        <!-- 行注：渲染容器 -->
        <div class="msg-avatar" :class="{ self: msg.isMe, group: isGroupSession && !msg.isMe }">
          <!-- 行注：渲染图片 -->
          <ProtectedImage v-if="msg.isMe ? userAvatar : msg.fromAvatar" :src="msg.isMe ? userAvatar : msg.fromAvatar" class="avatar-img" />
          <!-- 行注：渲染文本节点 -->
          <span v-else>{{ msg.isMe ? (userAvatar?.charAt(0) || '我') : (msg.name?.charAt(0) || '群') }}</span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="msg-content">
          <!-- 行注：渲染容器 -->
          <div v-if="isGroupSession && !msg.isMe" class="msg-sender">{{ msg.name }}</div>
          <!-- 行注：渲染容器 -->
          <div
            class="msg-bubble"
            :class="{ self: msg.isMe, recalled: msg.status === MESSAGE_STATUS_RECALLED }"
            @contextmenu.prevent="$emit('show-message-menu', $event, msg)"
          >
            <!-- 行注：开始定义模板区域 -->
            <template v-if="msg.status === MESSAGE_STATUS_RECALLED">
              <!-- 行注：展示“[消息已撤回]”文案 -->
              <span class="recalled-text">[消息已撤回]</span>
            <!-- 行注：结束模板区域 -->
            </template>
            <!-- 行注：开始定义模板区域 -->
            <template v-else-if="msg.msgType === MESSAGE_TYPE_IMAGE">
              <!-- 行注：渲染图片 -->
              <ProtectedImage
                :src="msg.content"
                class="msg-image"
                @load="$emit('message-media-load')"
                @click="$emit('preview-image', msg)"
              />
            <!-- 行注：结束模板区域 -->
            </template>
            <!-- 行注：开始定义模板区域 -->
            <template v-else-if="msg.msgType === MESSAGE_TYPE_FILE">
              <!-- 行注：渲染按钮 -->
              <button
                v-if="msg.deliveryStatus !== 'sent'"
                type="button"
                class="msg-file pending"
                :disabled="msg.deliveryStatus === 'sending'"
              >
                <!-- 行注：渲染图标容器 -->
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标路径 -->
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <!-- 行注：补充图标折线 -->
                  <polyline points="14 2 14 8 20 8" />
                <!-- 行注：结束图标容器 -->
                </svg>
                <!-- 行注：渲染容器 -->
                <div class="file-info">
                  <!-- 行注：渲染文本节点 -->
                  <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                  <!-- 行注：渲染文本节点 -->
                  <span class="file-size">{{ getFileSizeText(msg) || '待发送文件' }}</span>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束按钮 -->
              </button>
              <!-- 行注：渲染a 节点 -->
              <a v-else href="#" class="msg-file" @click.prevent="$emit('download-file', msg)">
                <!-- 行注：渲染图标容器 -->
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标路径 -->
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <!-- 行注：补充图标折线 -->
                  <polyline points="14 2 14 8 20 8" />
                <!-- 行注：结束图标容器 -->
                </svg>
                <!-- 行注：渲染容器 -->
                <div class="file-info">
                  <!-- 行注：渲染文本节点 -->
                  <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                  <!-- 行注：渲染文本节点 -->
                  <span class="file-size">{{ getFileSizeText(msg) || '未知大小' }}</span>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束a 节点 -->
              </a>
            <!-- 行注：结束模板区域 -->
            </template>
            <!-- 行注：开始定义模板区域 -->
            <template v-else>
              <template
                v-for="(segment, segmentIndex) in getMessageTextSegments(msg.content)"
                :key="`${getMessageAnchorKey(msg)}-${segmentIndex}`"
              >
                <span v-if="segment.type === 'text'">{{ segment.text }}</span>
                <span v-else-if="segment.type === 'mention'" class="mention-text">{{ segment.text }}</span>
                <button
                  v-else-if="segment.type === 'link'"
                  type="button"
                  class="msg-link"
                  @click.stop="$emit('open-external-link', segment.href)"
                >
                  {{ segment.text }}
                </button>
              </template>
            </template>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="msg-meta" :class="{ self: msg.isMe }">
            <!-- 行注：渲染文本节点 -->
            <span class="msg-time">{{ msg.time }}</span>
            <!-- 行注：渲染按钮 -->
            <button
              v-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'failed'"
              type="button"
              class="msg-status retry"
              @click="$emit('retry-failed-message', msg)"
            >
              <!-- 行注：展示“发送失败，点击重试”文案 -->
              发送失败，点击重试
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染文本节点 -->
            <span
              v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'sending'"
              class="msg-status pending"
            >
              <!-- 行注：展示“发送中”文案 -->
              发送中
            <!-- 行注：结束文本节点 -->
            </span>
            <!-- 行注：渲染文本节点 -->
            <span v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && !isGroupSession" class="msg-status" :class="{ read: msg.readStatus === '已读' }">
              <!-- 行注：渲染动态文本 -->
              {{ msg.readStatus }}
            <!-- 行注：结束文本节点 -->
            </span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束模板区域 -->
      </template>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-if="messages.length === 0" class="chat-empty">
      <!-- 行注：渲染容器 -->
      <div class="chat-empty-icon">
        <!-- 行注：渲染图标容器 -->
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <!-- 行注：补充图标路径 -->
          <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
        <!-- 行注：结束图标容器 -->
        </svg>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：展示“暂无消息”文案 -->
      <div class="chat-empty-title">暂无消息</div>
      <!-- 行注：展示“发送消息开始聊天吧”文案 -->
      <div class="chat-empty-text">发送消息开始聊天吧</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束模板区域 -->
  </template>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * MessagePane 组件，负责当前界面片段的展示与交互。
 */
import {  // 行注：引入 { 模块
  MESSAGE_STATUS_RECALLED,  // 行注：补充 MESSAGE_STATUS_RECALLED 配置项
  MESSAGE_TYPE_FILE,  // 行注：补充当前配置项
  MESSAGE_TYPE_IMAGE,  // 行注：补充当前配置项
  type DisplayMessage  // 行注：补充当前表达式
} from '../../types/chat'  // 行注：补充当前表达式
import ProtectedImage from '../ProtectedImage.vue'
import { formatSize, getFileName, getMessageAnchorKey } from '../../utils/chat'

defineProps<{  // 行注：开始当前逻辑块
  messages: DisplayMessage[]  // 行注：设置 messages 配置项
  loadingMessages: boolean  // 行注：设置 loadingMessages 配置项
  isGroupSession: boolean  // 行注：设置 isGroupSession 配置项
  userAvatar: string  // 行注：设置 userAvatar 配置项
  userNickname: string  // 行注：设置 userNickname 配置项
  activeJumpMessageKey: string  // 行注：设置 activeJumpMessageKey 配置项
  showMentionBanner: boolean  // 行注：设置 showMentionBanner 配置项
  mentionBannerText: string  // 行注：设置 mentionBannerText 配置项
  mentionBannerActionText: string  // 行注：设置 mentionBannerActionText 配置项
  getResolvedMessageFileUrl: (message: DisplayMessage) => string  // 行注：设置 getResolvedMessageFileUrl 配置项
  getMessageTextSegments: (content: string) => import('../../utils/chatText').ChatTextSegment[]
}>()

defineEmits<{
  (event: 'mention-banner-click'): void  // 行注：执行当前调用逻辑
  (event: 'dismiss-mention-banner'): void  // 行注：执行当前调用逻辑
  (event: 'show-message-menu', eventObj: MouseEvent, message: DisplayMessage): void  // 行注：执行当前调用逻辑
  (event: 'message-media-load'): void  // 行注：执行当前调用逻辑
  (event: 'preview-image', message: DisplayMessage): void  // 行注：执行当前调用逻辑
  (event: 'download-file', message: DisplayMessage): void  // 行注：执行当前调用逻辑
  (event: 'retry-failed-message', message: DisplayMessage): void
  (event: 'open-external-link', href: string): void
}>()

function getFileSizeText(message: DisplayMessage) {  // 行注：定义 getFileSizeText 方法
  return formatSize(message.fileSize)  // 行注：返回当前结果
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.mention-banner {  /* 行注：定义 .mention-banner 样式 */
  position: sticky;  /* 行注：设置 position 样式 */
  top: 0;  /* 行注：设置 top 样式 */
  z-index: 8;  /* 行注：设置 z-index 样式 */
  align-self: center;  /* 行注：设置 align-self 样式 */
  width: min(100%, 560px);  /* 行注：设置 width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  padding: 10px 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid rgba(255, 170, 0, 0.28);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, rgba(255, 170, 0, 0.18) 70%, var(--linkx-bg-card));  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.mention-banner-badge {  /* 行注：定义 .mention-banner-badge 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  padding: 2px 8px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  background: rgba(255, 170, 0, 0.18);  /* 行注：设置 background 样式 */
  color: #c97b00;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.mention-banner-text {  /* 行注：定义 .mention-banner-text 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 500;  /* 行注：设置 font-weight 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.mention-banner-action {  /* 行注：定义 .mention-banner-action 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.mention-banner-close {  /* 行注：定义 .mention-banner-close 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  width: 18px;  /* 行注：设置 width 样式 */
  height: 18px;  /* 行注：设置 height 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.chat-loading,  /* 行注：补充 .chat-loading 选择器 */
.chat-empty {  /* 行注：定义 .chat-empty 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.chat-loading {  /* 行注：定义 .chat-loading 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.loading-spinner {  /* 行注：定义 .loading-spinner 样式 */
  width: 20px;  /* 行注：设置 width 样式 */
  height: 20px;  /* 行注：设置 height 样式 */
  border: 2px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-top-color: var(--linkx-primary);  /* 行注：设置 border-top-color 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  to {  /* 行注：设置动画结束状态 */
    transform: rotate(360deg);  /* 行注：设置 transform 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

.message-row {  /* 行注：定义 .message-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  transition: background-color 0.25s ease, box-shadow 0.25s ease, padding 0.25s ease;  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.message-row.located {  /* 行注：定义 .message-row.located 样式 */
  padding: 8px 10px;  /* 行注：设置 padding 样式 */
  border-radius: 16px;  /* 行注：设置 border-radius 样式 */
  background: color-mix(in srgb, var(--linkx-primary) 12%, transparent);  /* 行注：设置 background 样式 */
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--linkx-primary) 22%, transparent);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.message-row.system {  /* 行注：定义 .message-row.system 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.message-row.self {  /* 行注：定义 .message-row.self 样式 */
  flex-direction: row-reverse;  /* 行注：设置 flex-direction 样式 */
}  /* 行注：结束当前样式块 */

.msg-avatar {  /* 行注：定义 .msg-avatar 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.msg-avatar.self {  /* 行注：定义 .msg-avatar.self 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.msg-avatar.group {  /* 行注：定义 .msg-avatar.group 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.msg-content {  /* 行注：定义 .msg-content 样式 */
  max-width: 60%;  /* 行注：设置 max-width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.message-row.self .msg-content {  /* 行注：定义 .message-row.self .msg-content 样式 */
  align-items: flex-end;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.msg-sender {  /* 行注：定义 .msg-sender 样式 */
  margin-left: 4px;  /* 行注：设置 margin-left 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.system-message {  /* 行注：定义 .system-message 样式 */
  max-width: 72%;  /* 行注：设置 max-width 样式 */
  padding: 6px 12px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.5;  /* 行注：设置 line-height 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.msg-bubble {  /* 行注：定义 .msg-bubble 样式 */
  padding: 12px 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bubble-other);  /* 行注：设置 background 样式 */
  color: var(--linkx-bubble-other-text);  /* 行注：设置 color 样式 */
  box-shadow: var(--linkx-shadow-sm);  /* 行注：设置 box-shadow 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.6;  /* 行注：设置 line-height 样式 */
  white-space: pre-wrap;  /* 行注：设置 white-space 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.msg-bubble.self {  /* 行注：定义 .msg-bubble.self 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  border-bottom-right-radius: 4px;  /* 行注：设置 border-bottom-right-radius 样式 */
}  /* 行注：结束当前样式块 */

.message-row:not(.self) .msg-bubble {  /* 行注：定义 .message-row:not(.self) .msg-bubble 样式 */
  border-bottom-left-radius: 4px;  /* 行注：设置 border-bottom-left-radius 样式 */
}  /* 行注：结束当前样式块 */

.msg-bubble.recalled {  /* 行注：定义 .msg-bubble.recalled 样式 */
  opacity: 0.6;  /* 行注：设置 opacity 样式 */
  background: var(--linkx-bg-hover) !important;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.mention-text {
  color: #e45252;
  font-weight: 600;
}

.msg-link {
  padding: 0;
  border: none;
  background: none;
  color: var(--linkx-primary, #00a8ff);
  text-decoration: underline;
  cursor: pointer;
  font: inherit;
  word-break: break-all;
}

.recalled-text {  /* 行注：定义 .recalled-text 样式 */
  font-style: italic;  /* 行注：设置 font-style 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.msg-meta {  /* 行注：定义 .msg-meta 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  margin-left: 4px;  /* 行注：设置 margin-left 样式 */
}  /* 行注：结束当前样式块 */

.msg-meta.self {  /* 行注：定义 .msg-meta.self 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
  margin-right: 4px;  /* 行注：设置 margin-right 样式 */
}  /* 行注：结束当前样式块 */

.msg-time,  /* 行注：补充 .msg-time 选择器 */
.msg-status {  /* 行注：定义 .msg-status 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.msg-status {  /* 行注：定义 .msg-status 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.msg-status.read {  /* 行注：定义 .msg-status.read 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.msg-status.pending {  /* 行注：定义 .msg-status.pending 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.msg-status.retry {  /* 行注：定义 .msg-status.retry 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.msg-image {  /* 行注：定义 .msg-image 样式 */
  max-width: 240px;  /* 行注：设置 max-width 样式 */
  max-height: 240px;  /* 行注：设置 max-height 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.msg-file {  /* 行注：定义 .msg-file 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  color: inherit;  /* 行注：设置 color 样式 */
  text-decoration: none;  /* 行注：设置 text-decoration 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
  text-align: left;  /* 行注：设置 text-align 样式 */
  font: inherit;  /* 行注：设置 font 样式 */
}  /* 行注：结束当前样式块 */

.msg-file.pending {  /* 行注：定义 .msg-file.pending 样式 */
  cursor: default;  /* 行注：设置 cursor 样式 */
  opacity: 0.82;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.file-info {  /* 行注：定义 .file-info 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.file-size {  /* 行注：定义 .file-size 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.chat-empty {  /* 行注：定义 .chat-empty 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.chat-empty-icon {  /* 行注：定义 .chat-empty-icon 样式 */
  opacity: 0.2;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.chat-empty-title {  /* 行注：定义 .chat-empty-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.chat-empty-text {  /* 行注：定义 .chat-empty-text 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */
</style>
