<template>
  <section class="panel-card search-panel">
    <div class="workspace-head">
      <div>
        <div class="panel-title">群聊消息搜索</div>
        <div class="panel-subtitle">按关键词检索当前群的历史消息、文件消息和系统记录。</div>
      </div>
      <button class="secondary-btn compact-btn" :disabled="groupMessageSearchLoading" @click="$emit('search-group-messages')">
        {{ groupMessageSearchLoading ? '搜索中...' : '开始搜索' }}
      </button>
    </div>

    <div class="workspace-toolbar">
      <div class="search-shell workspace-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          :value="messageSearchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索群内历史消息"
          @input="$emit('update:message-search-keyword', ($event.target as HTMLInputElement).value || '')"
          @keyup.enter="$emit('search-group-messages')"
        />
      </div>
    </div>

    <div v-if="groupMessageSearchResults.length > 0" class="search-result-list">
      <article v-for="item in groupMessageSearchResults" :key="item.id" class="search-result-card">
        <div class="result-main">
          <div class="result-title-row">
            <strong>{{ item.fromNickname || '成员' }}</strong>
            <span class="result-type-tag">{{ getMediaTypeText(item) }}</span>
            <span class="result-time">{{ formatDateTime(item.createTime) }}</span>
          </div>
          <div class="result-content">{{ getMessageSearchPreview(item) }}</div>
        </div>
        <div class="result-actions">
          <button v-if="item.content" class="mini-btn" @click="$emit('open-group-chat', item)">去群聊查看</button>
          <button v-if="item.fileName && item.content" class="mini-btn" @click="$emit('open-media-resource', item)">打开附件</button>
        </div>
      </article>
    </div>
    <div v-else class="empty-state compact">
      <div class="empty-title">{{ messageSearchKeyword.trim() ? '没有找到匹配消息' : '输入关键词后开始搜索' }}</div>
      <div class="empty-subtitle">{{ messageSearchKeyword.trim() ? '试试成员名、消息文本或文件名' : '支持搜索文本消息、系统消息和文件名' }}</div>
    </div>
  </section>

  <section class="panel-card media-panel">
    <div class="workspace-head">
      <div>
        <div class="panel-title">群相册 / 文件库</div>
        <div class="panel-subtitle">汇总查看当前群里发送过的图片和文件。</div>
      </div>
      <button class="secondary-btn compact-btn" :disabled="groupMediaLoading" @click="$emit('load-group-media')">
        {{ groupMediaLoading ? '刷新中...' : '刷新列表' }}
      </button>
    </div>

    <div class="workspace-toolbar media-toolbar">
      <div class="tab-group">
        <button class="tab-btn" :class="{ active: mediaType === 'all' }" @click="$emit('update:media-type', 'all')">全部</button>
        <button class="tab-btn" :class="{ active: mediaType === 'image' }" @click="$emit('update:media-type', 'image')">图片</button>
        <button class="tab-btn" :class="{ active: mediaType === 'file' }" @click="$emit('update:media-type', 'file')">文件</button>
      </div>
      <div class="search-shell workspace-search">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <path d="M21 21l-4.35-4.35" />
        </svg>
        <input
          :value="mediaKeyword"
          type="text"
          class="search-input"
          placeholder="搜索文件名或类型"
          @input="$emit('update:media-keyword', ($event.target as HTMLInputElement).value || '')"
          @keyup.enter="$emit('load-group-media')"
        />
      </div>
    </div>

    <div v-if="groupMediaItems.length > 0" class="media-list">
      <article v-for="item in groupMediaItems" :key="item.id" class="media-card">
        <div class="media-cover" :class="{ image: isImageMedia(item) }">
          <img v-if="isImageMedia(item) && item.accessUrl" :src="item.accessUrl" :alt="item.fileName || '群图片'" />
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
        </div>
        <div class="media-info">
          <div class="media-name">{{ item.fileName || (isImageMedia(item) ? '群图片' : '群文件') }}</div>
          <div class="media-meta">
            <span>{{ item.fromNickname || '成员' }}</span>
            <span>{{ formatDateTime(item.createTime) }}</span>
            <span>{{ formatSize(item.fileSize) }}</span>
          </div>
          <div class="media-actions">
            <button class="mini-btn" @click="$emit('open-media-resource', item)">打开</button>
            <button class="mini-btn" @click="$emit('copy-media-link', item)">复制链接</button>
          </div>
        </div>
      </article>
    </div>
    <div v-else class="empty-state compact">
      <div class="empty-title">{{ mediaKeyword.trim() ? '没有找到匹配文件' : '当前群还没有图片或文件' }}</div>
      <div class="empty-subtitle">{{ mediaKeyword.trim() ? '尝试其他关键词或切换分类' : '后续在群里发送的图片和文件会自动汇总到这里' }}</div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { GroupMediaItem } from '../../types/chat'
import { formatDateTime, formatSize } from '../../utils/chat'

defineProps<{
  groupMessageSearchLoading: boolean
  messageSearchKeyword: string
  groupMessageSearchResults: GroupMediaItem[]
  getMediaTypeText: (item: GroupMediaItem) => string
  getMessageSearchPreview: (item: GroupMediaItem) => string
  groupMediaLoading: boolean
  mediaType: 'all' | 'image' | 'file'
  mediaKeyword: string
  groupMediaItems: GroupMediaItem[]
  isImageMedia: (item: GroupMediaItem) => boolean
}>()

defineEmits<{
  (event: 'update:message-search-keyword', value: string): void
  (event: 'search-group-messages'): void
  (event: 'open-group-chat', item: GroupMediaItem): void
  (event: 'open-media-resource', item: GroupMediaItem): void
  (event: 'update:media-type', value: 'all' | 'image' | 'file'): void
  (event: 'update:media-keyword', value: string): void
  (event: 'load-group-media'): void
  (event: 'copy-media-link', item: GroupMediaItem): void
}>()
</script>

<style scoped>
.panel-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-md);
  padding: 22px;
}

.workspace-head,
.workspace-toolbar,
.result-title-row,
.media-meta,
.media-actions,
.result-actions,
.tab-group {
  display: flex;
  align-items: center;
}

.workspace-head {
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.panel-subtitle,
.result-time,
.media-meta,
.empty-subtitle {
  color: var(--linkx-text-muted);
}

.panel-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.secondary-btn,
.mini-btn,
.tab-btn {
  transition: var(--linkx-transition-fast);
}

.secondary-btn,
.mini-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 13px;
  font-weight: 600;
}

.compact-btn {
  height: 34px;
  padding: 0 14px;
  font-size: 12px;
}

.secondary-btn:hover,
.mini-btn:hover {
  background: var(--linkx-bg-hover);
}

.workspace-toolbar {
  margin-top: 16px;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.search-shell {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 44px;
  padding: 0 14px;
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  color: var(--linkx-text-muted);
}

.workspace-search {
  min-width: min(300px, 100%);
  flex: 1;
}

.search-input {
  width: 100%;
  border: none;
  background: transparent;
  color: var(--linkx-text);
  outline: none;
  font-size: 14px;
}

.search-result-list,
.media-list {
  margin-top: 18px;
  display: grid;
  gap: 14px;
}

.search-result-card,
.media-card {
  display: grid;
  gap: 16px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.search-result-card {
  grid-template-columns: minmax(0, 1fr) auto;
}

.result-main,
.media-info {
  min-width: 0;
}

.result-title-row,
.media-meta,
.media-actions,
.result-actions {
  gap: 10px;
  flex-wrap: wrap;
}

.result-type-tag {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(80, 145, 255, 0.12);
  color: #4f86ff;
  font-size: 11px;
  font-weight: 700;
}

.result-time,
.media-meta {
  font-size: 12px;
}

.result-content {
  margin-top: 8px;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;
}

.tab-group {
  gap: 10px;
  flex-wrap: wrap;
}

.tab-btn {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text-secondary);
  font-size: 12px;
  font-weight: 700;
}

.tab-btn.active {
  background: rgba(0, 214, 143, 0.14);
  border-color: rgba(0, 214, 143, 0.28);
  color: var(--linkx-primary);
}

.media-card {
  grid-template-columns: 88px minmax(0, 1fr);
}

.media-cover {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg-card);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);
  color: var(--linkx-text-muted);
}

.media-cover.image {
  background: color-mix(in srgb, var(--linkx-bg-hover) 72%, transparent);
}

.media-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.media-name {
  color: var(--linkx-text);
  font-size: 15px;
  font-weight: 700;
  word-break: break-word;
}

.media-meta {
  margin-top: 8px;
}

.media-actions {
  margin-top: 12px;
}

.empty-state {
  min-height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
}

.empty-title {
  color: var(--linkx-text);
  font-size: 16px;
  font-weight: 700;
}

@media (max-width: 1100px) {
  .search-result-card,
  .media-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .workspace-head,
  .workspace-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .secondary-btn,
  .mini-btn {
    width: 100%;
    justify-content: center;
  }

  .tab-group,
  .media-actions,
  .result-actions,
  .result-title-row,
  .media-meta {
    width: 100%;
  }
}
</style>
