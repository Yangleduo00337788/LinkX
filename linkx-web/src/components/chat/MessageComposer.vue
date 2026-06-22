<template>
  <div class="chat-input-area">
    <div class="input-toolbar">
      <div class="emoji-wrapper" :ref="emojiRefSetter">
        <button class="toolbar-btn" title="表情" @click="$emit('toggle-emoji-picker')">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10" />
            <path d="M8 14s1.5 2 4 2 4-2 4-2" />
            <line x1="9" y1="9" x2="9.01" y2="9" />
            <line x1="15" y1="9" x2="15.01" y2="9" />
          </svg>
        </button>
        <div v-if="showEmojiPicker" class="emoji-picker">
          <div class="emoji-grid">
            <span v-for="emoji in emojis" :key="emoji" class="emoji-item" @click="$emit('insert-emoji', emoji)">{{ emoji }}</span>
          </div>
        </div>
      </div>
      <button class="toolbar-btn" title="文件" :disabled="currentMuted" @click="$emit('trigger-file-upload')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
      </button>
      <button class="toolbar-btn" title="图片" :disabled="currentMuted" @click="$emit('trigger-image-upload')">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21 15 16 10 5 21" />
        </svg>
      </button>
    </div>
    <div class="input-container" :class="{ disabled: currentMuted }">
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
      <button
        class="send-btn"
        :disabled="!inputMessage.trim() || sending || currentMuted"
        :class="{ active: inputMessage.trim() && !currentMuted }"
        @click="$emit('send')"
      >
        <svg v-if="!sending" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="22" y1="2" x2="11" y2="13" />
          <polygon points="22 2 15 22 11 13 2 9 22 2" />
        </svg>
        <div v-else class="send-loading"></div>
      </button>
    </div>
    <div v-if="showMentionMenu" class="mention-menu" :ref="mentionMenuRefSetter">
      <div v-if="mentionCandidates.length > 0" class="mention-menu-list">
        <button
          v-for="(candidate, index) in mentionCandidates"
          :key="candidate.key"
          type="button"
          class="mention-menu-item"
          :class="{ active: index === mentionHighlightedIndex }"
          @mousedown.prevent="$emit('select-mention-candidate', candidate)"
        >
          <span class="mention-menu-name">{{ candidate.label }}</span>
          <span class="mention-menu-meta">{{ candidate.meta }}</span>
        </button>
      </div>
      <div v-else class="mention-menu-empty">未找到可提及成员</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { ComponentPublicInstance } from 'vue'
import type { MentionCandidate } from '../../types/chat'

defineProps<{
  inputMessage: string
  sending: boolean
  currentMuted: boolean
  showEmojiPicker: boolean
  showMentionMenu: boolean
  emojis: string[]
  mentionCandidates: MentionCandidate[]
  mentionHighlightedIndex: number
  textareaRefSetter: (element: Element | ComponentPublicInstance | null) => void
  emojiRefSetter: (element: Element | ComponentPublicInstance | null) => void
  mentionMenuRefSetter: (element: Element | ComponentPublicInstance | null) => void
}>()

const emit = defineEmits<{
  (event: 'update:inputMessage', value: string): void
  (event: 'toggle-emoji-picker'): void
  (event: 'insert-emoji', value: string): void
  (event: 'trigger-file-upload'): void
  (event: 'trigger-image-upload'): void
  (event: 'input-keydown', value: KeyboardEvent): void
  (event: 'input-change'): void
  (event: 'sync-mention-menu'): void
  (event: 'send'): void
  (event: 'select-mention-candidate', value: MentionCandidate): void
}>()

function handleInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:inputMessage', target.value || '')
  emit('input-change')
}
</script>

<style scoped>
.chat-input-area {
  position: relative;
  padding: 12px 20px 16px;
  background: var(--linkx-bg-card);
  border-top: 1px solid var(--linkx-border);
}

.input-toolbar {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
}

.emoji-wrapper {
  position: relative;
}

.toolbar-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: transparent;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.toolbar-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.toolbar-btn:disabled,
.send-btn:disabled,
.input-container.disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.emoji-picker,
.mention-menu {
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  box-shadow: var(--linkx-shadow-lg);
}

.emoji-picker {
  position: absolute;
  bottom: 40px;
  left: 0;
  width: 320px;
  padding: 12px;
  z-index: 100;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-sm);
  font-size: 20px;
  cursor: pointer;
}

.emoji-item:hover {
  background: var(--linkx-bg-hover);
}

.input-container {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 8px 12px;
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
}

.message-input {
  flex: 1;
  min-height: 24px;
  max-height: 120px;
  padding: 0;
  background: transparent;
  border: none;
  outline: none;
  box-shadow: none;
  resize: none;
  color: var(--linkx-text);
  font-family: inherit;
  font-size: 14px;
  line-height: 1.5;
}

.message-input::placeholder {
  color: var(--linkx-text-muted);
}

.message-input:focus,
.message-input:focus-visible {
  outline: none;
  box-shadow: none;
}

.send-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: var(--linkx-radius-sm);
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-muted);
  cursor: pointer;
}

.send-btn.active {
  background: var(--linkx-primary);
  color: white;
  box-shadow: 0 2px 8px var(--linkx-primary-glow);
}

.send-loading {
  width: 16px;
  height: 16px;
  border: 2px solid var(--linkx-text-muted);
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.mention-menu {
  position: absolute;
  left: 20px;
  right: 20px;
  bottom: calc(100% - 8px);
  z-index: 120;
  overflow: hidden;
}

.mention-menu-list {
  max-height: 240px;
  overflow-y: auto;
}

.mention-menu-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  border: none;
  background: transparent;
  color: var(--linkx-text-secondary);
  text-align: left;
  cursor: pointer;
}

.mention-menu-item:hover,
.mention-menu-item.active {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.mention-menu-name {
  color: inherit;
  font-size: 13px;
  font-weight: 600;
}

.mention-menu-meta,
.mention-menu-empty {
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.mention-menu-empty {
  padding: 12px 14px;
}
</style>
