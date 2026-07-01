<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="session-panel">
    <!-- 行注：渲染容器 -->
    <div class="panel-header">
      <span class="header-title">消息</span>
    </div>
    <div class="session-search">
      <div class="search-input-wrapper">
        <!-- 行注：渲染n-icon 节点 -->
        <n-icon :component="SearchOutline" class="search-icon" />
        <!-- 行注：渲染输入框 -->
        <input
          :value="searchText"
          type="text"
          placeholder="搜索会话或聊天记录…"
          class="search-input"
          @input="handleSearchInput"
        />
        <!-- 行注：渲染容器 -->
        <ChatPlusMenu @create-group="$emit('create-group')" @add-friend="$emit('add-friend')" />
        <div v-if="searchText" class="search-clear" @click="$emit('update:searchText', '')">
          <!-- 行注：渲染图标容器 -->
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.2" />
            <!-- 行注：补充图标路径 -->
            <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="session-list">
      <div v-if="searchKeyword && messageSearchLoading" class="message-search-hint">正在搜索聊天记录…</div>
      <template v-if="searchKeyword && messageHits.length">
        <div class="message-search-label">聊天记录</div>
        <button
          v-for="hit in messageHits"
          :key="`msg-${hit.id}`"
          type="button"
          class="message-hit-item"
          @click="openMessageHit(hit)"
        >
          <span class="message-hit-title">{{ hit.targetName || '会话' }}</span>
          <span class="message-hit-preview">{{ hit.content }}</span>
        </button>
      </template>
      <div
        v-for="session in filteredSessions"
        :key="buildSessionKey(session.targetId, session.sessionType)"
        class="session-item"
        :class="{
          active: isCurrentSession(session),
          flash: flashSessionKey === buildSessionKey(session.targetId, session.sessionType)
        }"
        @click="$emit('select-session', session)"
      >
        <!-- 行注：渲染容器 -->
        <div class="session-avatar" :class="{ 'has-unread': session.unreadCount > 0, group: session.sessionType === SESSION_TYPE_GROUP }">
          <!-- 行注：渲染图片 -->
          <ProtectedImage v-if="session.targetAvatar" :src="session.targetAvatar" class="avatar-img" />
          <!-- 行注：渲染文本节点 -->
          <span v-else class="avatar-text">{{ session.targetNickname?.charAt(0) || (session.sessionType === SESSION_TYPE_GROUP ? '群' : '聊') }}</span>
          <!-- 行注：渲染容器 -->
          <div class="session-type-badge" :class="{ group: session.sessionType === SESSION_TYPE_GROUP }">
            <!-- 行注：渲染动态文本 -->
            {{ session.sessionType === SESSION_TYPE_GROUP ? '群' : '单' }}
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="session.sessionType === SESSION_TYPE_SINGLE" class="online-indicator" :class="{ active: session.targetOnline }"></div>
          <!-- 行注：渲染容器 -->
          <div v-if="session.unreadCount > 0" class="unread-badge">
            <!-- 行注：渲染动态文本 -->
            {{ session.unreadCount > 99 ? '99+' : session.unreadCount }}
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="session-info">
          <!-- 行注：渲染容器 -->
          <div class="session-header">
            <!-- 行注：渲染容器 -->
            <div class="session-title-row">
              <!-- 行注：渲染文本节点 -->
              <span class="session-name">{{ session.targetNickname }}</span>
              <!-- 行注：渲染文本节点 -->
              <span v-if="session.sessionType === SESSION_TYPE_GROUP" class="session-tag">{{ session.memberCount || 0 }}人</span>
              <!-- 行注：展示“新公告”文案 -->
              <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.noticeUnread" class="session-tag notice">新公告</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染文本节点 -->
            <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="session-preview-row">
            <!-- 行注：渲染文本节点 -->
            <span class="session-preview">
              <template v-if="draftPreviewFor(session)">
                <span class="draft-tag">[草稿]</span>{{ draftPreviewFor(session) }}
              </template>
              <template v-else>{{ session.lastMessage || (session.sessionType === SESSION_TYPE_GROUP ? '群聊已创建' : '暂无消息') }}</template>
            </span>
            <span v-if="session.pinned" class="session-pin" title="已置顶">📌</span>
            <!-- 行注：展示“已禁言”文案 -->
            <span v-if="session.sessionType === SESSION_TYPE_GROUP && session.muted" class="session-muted">已禁言</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-if="sessions.length === 0 && !loadingSessions" class="empty-state">
        <!-- 行注：渲染容器 -->
        <div class="empty-icon">
          <!-- 行注：渲染图标容器 -->
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <!-- 行注：补充图标路径 -->
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“暂无会话”文案 -->
        <div class="empty-title">暂无会话</div>
        <!-- 行注：展示“创建群聊或找好友开始聊天”文案 -->
        <div class="empty-subtitle">创建群聊或找好友开始聊天</div>
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
 * SessionSidebar 组件，负责当前界面片段的展示与交互。
 */
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import { chatApi } from '../../api/client'
import { SESSION_TYPE_GROUP, SESSION_TYPE_SINGLE, type ChatSession } from '../../types/chat'
import ProtectedImage from '../ProtectedImage.vue'
import ChatPlusMenu from './ChatPlusMenu.vue'
import { buildSessionKey, formatTime } from '../../utils/chat'
import { mapSearchHits, type MessageSearchHitView } from '../../utils/messageSearchHit'

const router = useRouter()
const messageHits = ref<MessageSearchHitView[]>([])
const messageSearchLoading = ref(false)
let messageSearchTimer: ReturnType<typeof setTimeout> | null = null

const props = defineProps<{
  sessions: ChatSession[]
  draftPreviewByKey?: Record<string, string>
  loadingSessions: boolean
  currentTargetId: string | null
  currentSessionType: number
  searchText: string
  flashSessionKey: string | null
}>()

function draftPreviewFor(session: ChatSession) {
  const key = buildSessionKey(session.targetId, session.sessionType)
  const text = props.draftPreviewByKey?.[key]?.trim()
  if (!text) return ''
  return text.length > 48 ? `${text.slice(0, 48)}…` : text
}

const emit = defineEmits<{
  (event: 'create-group'): void
  (event: 'add-friend'): void
  (event: 'select-session', session: ChatSession): void
  (event: 'update:searchText', value: string): void
}>()

const searchKeyword = computed(() => props.searchText.trim())

const filteredSessions = computed(() => {
  if (!searchKeyword.value) {
    return props.sessions
  }
  const keyword = searchKeyword.value.toLowerCase()
  return props.sessions.filter(
    session =>
      session.targetNickname?.toLowerCase().includes(keyword) ||
      session.lastMessage?.toLowerCase().includes(keyword)
  )
})

watch(searchKeyword, keyword => {
  if (messageSearchTimer) {
    clearTimeout(messageSearchTimer)
    messageSearchTimer = null
  }
  if (!keyword) {
    messageHits.value = []
    messageSearchLoading.value = false
    return
  }
  messageSearchTimer = setTimeout(async () => {
    messageSearchLoading.value = true
    try {
      const res = await chatApi.searchMessages(keyword, { size: 25 })
      messageHits.value = mapSearchHits(res)
    } catch {
      messageHits.value = []
    } finally {
      messageSearchLoading.value = false
    }
  }, 350)
})

function openMessageHit(hit: MessageSearchHitView) {
  const targetId = hit.targetId
  if (targetId == null) return
  const sessionType = hit.sessionType ?? SESSION_TYPE_SINGLE
  if (sessionType === SESSION_TYPE_GROUP) {
    void router.push({ name: 'Chat', query: { groupId: String(targetId) } })
  } else {
    void router.push({ name: 'ChatRoom', params: { targetId: String(targetId) } })
  }
}

function isCurrentSession(session: ChatSession) {  // 行注：定义 isCurrentSession 方法
  if (!props.currentTargetId) {  // 行注：判断当前条件是否成立
    return false  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return buildSessionKey(session.targetId, session.sessionType) === buildSessionKey(props.currentTargetId, props.currentSessionType)  // 行注：返回当前结果
}  // 行注：结束当前代码块

function handleSearchInput(event: Event) {  // 行注：定义 handleSearchInput 方法
  const target = event.target as HTMLInputElement  // 行注：初始化 target 变量
  const value = target.value || ''  // 行注：初始化 value 状态
  emit('update:searchText', value)  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.session-panel {  /* 行注：定义 .session-panel 样式 */
  width: 320px;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-right: 1px solid var(--linkx-border);  /* 行注：设置 border-right 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  min-width: 280px;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.panel-header {  /* 行注：定义 .panel-header 样式 */
  height: 56px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.header-title {  /* 行注：定义 .header-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.create-group-btn {  /* 行注：定义 .create-group-btn 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.create-group-btn:hover {  /* 行注：定义 .create-group-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.message-search-hint,
.message-search-label {
  padding: 8px 16px 4px;
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.message-search-label {
  font-weight: 600;
  color: var(--linkx-primary);
}

.message-hit-item {
  display: block;
  width: 100%;
  text-align: left;
  padding: 10px 16px;
  border: none;
  border-bottom: 1px solid var(--linkx-border);
  background: var(--linkx-bg-muted, rgba(0, 0, 0, 0.03));
  cursor: pointer;
}

.message-hit-item:hover {
  background: var(--linkx-bg-hover, rgba(0, 0, 0, 0.06));
}

.message-hit-title {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text);
}

.message-hit-preview {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--linkx-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-search {  /* 行注：定义 .session-search 样式 */
  padding: 12px 16px;  /* 行注：设置 padding 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.search-input-wrapper {  /* 行注：定义 .search-input-wrapper 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.search-icon {  /* 行注：定义 .search-icon 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  left: 12px;  /* 行注：设置 left 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
}  /* 行注：结束当前样式块 */

.search-input {  /* 行注：定义 .search-input 样式 */
  flex: 1;
  min-width: 0;
  width: 100%;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  padding: 0 72px 0 36px;  /* 右侧留出加号与清除按钮 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.search-input::placeholder,  /* 行注：补充 .search-input::placeholder 选择器 */
.session-time,  /* 行注：补充 .session-time 选择器 */
.session-muted {  /* 行注：定义 .session-muted 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.search-input:focus {  /* 行注：定义 .search-input:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.search-clear {  /* 行注：定义 .search-clear 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  right: 8px;  /* 行注：设置 right 样式 */
  width: 24px;  /* 行注：设置 width 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.session-list {  /* 行注：定义 .session-list 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 8px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.session-item {  /* 行注：定义 .session-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.session-item:hover {  /* 行注：定义 .session-item:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.session-item.active {  /* 行注：定义 .session-item.active 样式 */
  background: var(--linkx-bg-active);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.session-item.flash {  /* 行注：定义 .session-item.flash 样式 */
  animation: flashHighlight 2s ease-out;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes flashHighlight {  /* 行注：声明关键帧动画 */
  0%,  /* 行注：补充 0% 选择器 */
  100% {  /* 行注：定义 100% 样式 */
    background: transparent;  /* 行注：设置 background 样式 */
  }  /* 行注：结束当前样式块 */
  15%,  /* 行注：补充 15% 选择器 */
  45%,  /* 行注：补充 45% 选择器 */
  75% {  /* 行注：定义 75% 样式 */
    background: var(--linkx-primary-glow);  /* 行注：设置 background 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

.session-avatar {  /* 行注：定义 .session-avatar 样式 */
  width: 48px;  /* 行注：设置 width 样式 */
  height: 48px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.session-avatar.group {  /* 行注：定义 .session-avatar.group 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.session-type-badge,  /* 行注：补充 .session-type-badge 选择器 */
.session-tag {  /* 行注：定义 .session-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.session-type-badge {  /* 行注：定义 .session-type-badge 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  left: -2px;  /* 行注：设置 left 样式 */
  bottom: -2px;  /* 行注：设置 bottom 样式 */
  min-width: 18px;  /* 行注：设置 min-width 样式 */
  height: 18px;  /* 行注：设置 height 样式 */
  padding: 0 5px;  /* 行注：设置 padding 样式 */
  background: rgba(0, 0, 0, 0.56);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.session-type-badge.group {  /* 行注：定义 .session-type-badge.group 样式 */
  background: rgba(77, 107, 255, 0.18);  /* 行注：设置 background 样式 */
  color: #90a7ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.session-tag {  /* 行注：定义 .session-tag 样式 */
  padding: 2px 8px;  /* 行注：设置 padding 样式 */
  background: rgba(77, 107, 255, 0.12);  /* 行注：设置 background 样式 */
  color: #90a7ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.session-tag.notice {  /* 行注：定义 .session-tag.notice 样式 */
  background: rgba(0, 214, 143, 0.14);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.unread-badge {  /* 行注：定义 .unread-badge 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  top: -4px;  /* 行注：设置 top 样式 */
  right: -4px;  /* 行注：设置 right 样式 */
  min-width: 20px;  /* 行注：设置 min-width 样式 */
  height: 20px;  /* 行注：设置 height 样式 */
  padding: 0 6px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-error);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.online-indicator {  /* 行注：定义 .online-indicator 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  right: -2px;  /* 行注：设置 right 样式 */
  bottom: 2px;  /* 行注：设置 bottom 样式 */
  width: 12px;  /* 行注：设置 width 样式 */
  height: 12px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  border: 2px solid var(--linkx-bg-card);  /* 行注：设置 border 样式 */
  background: #64748b;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.online-indicator.active {  /* 行注：定义 .online-indicator.active 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.session-info {  /* 行注：定义 .session-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.session-header {  /* 行注：定义 .session-header 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.session-title-row,  /* 行注：补充 .session-title-row 选择器 */
.session-preview-row {  /* 行注：定义 .session-preview-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.session-name {  /* 行注：定义 .session-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.session-preview,  /* 行注：补充 .session-preview 选择器 */
.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.session-preview {  /* 行注：定义 .session-preview 样式 */
  flex: 1;
  min-width: 0;
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.draft-tag {
  color: #e6a23c;
  font-weight: 600;
  margin-right: 2px;
}

.session-pin {
  flex-shrink: 0;
  font-size: 12px;
  opacity: 0.85;
}

.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-icon {  /* 行注：定义 .empty-icon 样式 */
  opacity: 0.2;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */
</style>
