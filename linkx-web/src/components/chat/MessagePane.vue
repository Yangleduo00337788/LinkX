<template>
  <div v-if="loadingMessages" class="chat-loading">
    <div class="loading-spinner"></div>
    <span>加载中...</span>
  </div>

  <template v-else>
    <div
      v-if="showMentionBanner"
      role="button"
      tabindex="0"
      class="mention-banner"
      @click="$emit('mention-banner-click')"
      @keydown.enter.prevent="$emit('mention-banner-click')"
      @keydown.space.prevent="$emit('mention-banner-click')"
    >
      <span class="mention-banner-badge">特别提醒</span>
      <span class="mention-banner-text">{{ mentionBannerText }}</span>
      <span class="mention-banner-action">{{ mentionBannerActionText }}</span>
      <span class="mention-banner-close" @click.stop="$emit('dismiss-mention-banner')">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      </span>
    </div>
    <div
      v-for="(msg, index) in messages"
      :key="msg.id || index"
      class="message-row"
      :class="{ self: msg.isMe, system: msg.isSystem, located: activeJumpMessageKey === getMessageAnchorKey(msg) }"
      :data-message-key="getMessageAnchorKey(msg)"
    >
      <template v-if="msg.isSystem">
        <div class="system-message">{{ msg.content }}</div>
      </template>
      <template v-else>
        <div class="msg-avatar" :class="{ self: msg.isMe, group: isGroupSession && !msg.isMe }">
          <img v-if="msg.isMe ? userAvatar : msg.fromAvatar" :src="msg.isMe ? userAvatar : msg.fromAvatar" class="avatar-img" />
          <span v-else>{{ msg.isMe ? (userAvatar?.charAt(0) || '我') : (msg.name?.charAt(0) || '群') }}</span>
        </div>
        <div class="msg-content">
          <div v-if="isGroupSession && !msg.isMe" class="msg-sender">{{ msg.name }}</div>
          <div
            class="msg-bubble"
            :class="{ self: msg.isMe, recalled: msg.status === MESSAGE_STATUS_RECALLED }"
            @contextmenu.prevent="$emit('show-message-menu', $event, msg)"
          >
            <template v-if="msg.status === MESSAGE_STATUS_RECALLED">
              <span class="recalled-text">[消息已撤回]</span>
            </template>
            <template v-else-if="msg.msgType === MESSAGE_TYPE_IMAGE">
              <img :src="getResolvedMessageFileUrl(msg)" class="msg-image" @load="$emit('message-media-load')" @click="$emit('preview-image', msg)" />
            </template>
            <template v-else-if="msg.msgType === MESSAGE_TYPE_FILE">
              <button
                v-if="msg.deliveryStatus !== 'sent'"
                type="button"
                class="msg-file pending"
                :disabled="msg.deliveryStatus === 'sending'"
              >
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                </svg>
                <div class="file-info">
                  <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                  <span class="file-size">{{ getFileSizeText(msg) || '待发送文件' }}</span>
                </div>
              </button>
              <a v-else href="#" class="msg-file" @click.prevent="$emit('download-file', msg)">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                </svg>
                <div class="file-info">
                  <span class="file-name">{{ msg.fileName || getFileName(msg.content) }}</span>
                  <span class="file-size">{{ getFileSizeText(msg) || '未知大小' }}</span>
                </div>
              </a>
            </template>
            <template v-else>
              <span
                v-for="(segment, segmentIndex) in getMessageTextSegments(msg.content)"
                :key="`${getMessageAnchorKey(msg)}-${segmentIndex}`"
                :class="{ 'mention-text': segment.mention }"
              >
                {{ segment.text }}
              </span>
            </template>
          </div>
          <div class="msg-meta" :class="{ self: msg.isMe }">
            <span class="msg-time">{{ msg.time }}</span>
            <button
              v-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'failed'"
              type="button"
              class="msg-status retry"
              @click="$emit('retry-failed-message', msg)"
            >
              发送失败，点击重试
            </button>
            <span
              v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && msg.deliveryStatus === 'sending'"
              class="msg-status pending"
            >
              发送中
            </span>
            <span v-else-if="msg.isMe && msg.status !== MESSAGE_STATUS_RECALLED && !isGroupSession" class="msg-status" :class="{ read: msg.readStatus === '已读' }">
              {{ msg.readStatus }}
            </span>
          </div>
        </div>
      </template>
    </div>

    <div v-if="messages.length === 0" class="chat-empty">
      <div class="chat-empty-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z" />
        </svg>
      </div>
      <div class="chat-empty-title">暂无消息</div>
      <div class="chat-empty-text">发送消息开始聊天吧</div>
    </div>
  </template>
</template>

<script setup lang="ts">
import {
  MESSAGE_STATUS_RECALLED,
  MESSAGE_TYPE_FILE,
  MESSAGE_TYPE_IMAGE,
  type DisplayMessage
} from '../../types/chat'
import { formatSize, getFileName, getMessageAnchorKey } from '../../utils/chat'

defineProps<{
  messages: DisplayMessage[]
  loadingMessages: boolean
  isGroupSession: boolean
  userAvatar: string
  userNickname: string
  activeJumpMessageKey: string
  showMentionBanner: boolean
  mentionBannerText: string
  mentionBannerActionText: string
  getResolvedMessageFileUrl: (message: DisplayMessage) => string
  getMessageTextSegments: (content: string) => Array<{ text: string; mention: boolean }>
}>()

defineEmits<{
  (event: 'mention-banner-click'): void
  (event: 'dismiss-mention-banner'): void
  (event: 'show-message-menu', eventObj: MouseEvent, message: DisplayMessage): void
  (event: 'message-media-load'): void
  (event: 'preview-image', message: DisplayMessage): void
  (event: 'download-file', message: DisplayMessage): void
  (event: 'retry-failed-message', message: DisplayMessage): void
}>()

function getFileSizeText(message: DisplayMessage) {
  return formatSize(message.fileSize)
}
</script>

<style scoped>
.mention-banner {
  position: sticky;
  top: 0;
  z-index: 8;
  align-self: center;
  width: min(100%, 560px);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border: 1px solid rgba(255, 170, 0, 0.28);
  border-radius: var(--linkx-radius);
  background: color-mix(in srgb, rgba(255, 170, 0, 0.18) 70%, var(--linkx-bg-card));
  color: var(--linkx-text);
  box-shadow: var(--linkx-shadow-md);
  cursor: pointer;
}

.mention-banner-badge {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--linkx-radius-full);
  background: rgba(255, 170, 0, 0.18);
  color: #c97b00;
  font-size: 12px;
  font-weight: 700;
}

.mention-banner-text {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  font-weight: 500;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.mention-banner-action {
  flex-shrink: 0;
  color: var(--linkx-primary);
  font-size: 12px;
  font-weight: 600;
}

.mention-banner-close {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
}

.chat-loading,
.chat-empty {
  display: flex;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.chat-loading {
  gap: 8px;
  color: var(--linkx-text-muted);
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  transition: background-color 0.25s ease, box-shadow 0.25s ease, padding 0.25s ease;
}

.message-row.located {
  padding: 8px 10px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--linkx-primary) 12%, transparent);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--linkx-primary) 22%, transparent);
}

.message-row.system {
  justify-content: center;
}

.message-row.self {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: var(--linkx-radius-sm);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  color: white;
  font-weight: 700;
  flex-shrink: 0;
}

.msg-avatar.self {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
}

.msg-avatar.group {
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.msg-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-row.self .msg-content {
  align-items: flex-end;
}

.msg-sender {
  margin-left: 4px;
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.system-message {
  max-width: 72%;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.5;
  text-align: center;
  word-break: break-word;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: var(--linkx-radius);
  background: var(--linkx-bubble-other);
  color: var(--linkx-bubble-other-text);
  box-shadow: var(--linkx-shadow-sm);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-bubble.self {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message-row:not(.self) .msg-bubble {
  border-bottom-left-radius: 4px;
}

.msg-bubble.recalled {
  opacity: 0.6;
  background: var(--linkx-bg-hover) !important;
}

.mention-text {
  color: #e45252;
  font-weight: 600;
}

.recalled-text {
  font-style: italic;
  color: var(--linkx-text-muted);
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 4px;
}

.msg-meta.self {
  justify-content: flex-end;
  margin-right: 4px;
}

.msg-time,
.msg-status {
  color: var(--linkx-text-muted);
}

.msg-status {
  border: none;
  background: transparent;
  padding: 0;
  font-size: 12px;
}

.msg-status.read {
  color: var(--linkx-primary);
}

.msg-status.pending {
  color: var(--linkx-text-muted);
}

.msg-status.retry {
  color: var(--linkx-error);
  cursor: pointer;
}

.msg-image {
  max-width: 240px;
  max-height: 240px;
  border-radius: var(--linkx-radius-sm);
  cursor: pointer;
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  color: inherit;
  text-decoration: none;
  border: none;
  background: transparent;
  padding: 0;
  text-align: left;
  font: inherit;
}

.msg-file.pending {
  cursor: default;
  opacity: 0.82;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-size {
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.chat-empty {
  gap: 12px;
  color: var(--linkx-text-muted);
}

.chat-empty-icon {
  opacity: 0.2;
}

.chat-empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.chat-empty-text {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}
</style>
