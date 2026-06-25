<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card search-panel">
    <!-- 行注：渲染容器 -->
    <div class="workspace-head">
      <!-- 行注：渲染容器 -->
      <div>
        <!-- 行注：展示“群聊消息搜索”文案 -->
        <div class="panel-title">群聊消息搜索</div>
        <!-- 行注：展示“按关键词检索当前群的历史消息、文件消”文案 -->
        <div class="panel-subtitle">按关键词检索当前群的历史消息、文件消息和系统记录。</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染按钮 -->
      <button class="secondary-btn compact-btn" :disabled="groupMessageSearchLoading" @click="$emit('search-group-messages')">
        <!-- 行注：渲染动态文本 -->
        {{ groupMessageSearchLoading ? '搜索中...' : '开始搜索' }}
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="workspace-toolbar">
      <!-- 行注：渲染容器 -->
      <div class="search-shell workspace-search">
        <!-- 行注：渲染图标容器 -->
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标圆形路径 -->
          <circle cx="11" cy="11" r="8" />
          <!-- 行注：补充图标路径 -->
          <path d="M21 21l-4.35-4.35" />
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：渲染输入框 -->
        <input
          :value="messageSearchKeyword"
          type="text"
          class="search-input"
          placeholder="搜索群内历史消息"
          @input="$emit('update:message-search-keyword', ($event.target as HTMLInputElement).value || '')"
          @keyup.enter="$emit('search-group-messages')"
        />
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-if="groupMessageSearchResults.length > 0" class="search-result-list">
      <!-- 行注：渲染article 节点 -->
      <article v-for="item in groupMessageSearchResults" :key="item.id" class="search-result-card">
        <!-- 行注：渲染容器 -->
        <div class="result-main">
          <!-- 行注：渲染容器 -->
          <div class="result-title-row">
            <!-- 行注：渲染strong 节点 -->
            <strong>{{ item.fromNickname || '成员' }}</strong>
            <!-- 行注：渲染文本节点 -->
            <span class="result-type-tag">{{ getMediaTypeText(item) }}</span>
            <!-- 行注：渲染文本节点 -->
            <span class="result-time">{{ formatDateTime(item.createTime) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="result-content">{{ getMessageSearchPreview(item) }}</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="result-actions">
          <!-- 行注：展示“去群聊查看”文案 -->
          <button v-if="item.content" class="mini-btn" @click="$emit('open-group-chat', item)">去群聊查看</button>
          <!-- 行注：展示“打开附件”文案 -->
          <button v-if="item.fileName && item.content" class="mini-btn" @click="$emit('open-media-resource', item)">打开附件</button>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束article 节点 -->
      </article>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-else class="empty-state compact">
      <!-- 行注：渲染容器 -->
      <div class="empty-title">{{ messageSearchKeyword.trim() ? '没有找到匹配消息' : '输入关键词后开始搜索' }}</div>
      <!-- 行注：渲染容器 -->
      <div class="empty-subtitle">{{ messageSearchKeyword.trim() ? '试试成员名、消息文本或文件名' : '支持搜索文本消息、系统消息和文件名' }}</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
  <!-- 行注：渲染section 节点 -->
  <section class="panel-card media-panel">
    <!-- 行注：渲染容器 -->
    <div class="workspace-head">
      <!-- 行注：渲染容器 -->
      <div>
        <!-- 行注：展示“群相册 / 文件库”文案 -->
        <div class="panel-title">群相册 / 文件库</div>
        <!-- 行注：展示“汇总查看当前群里发送过的图片和文件。”文案 -->
        <div class="panel-subtitle">汇总查看当前群里发送过的图片和文件。</div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染按钮 -->
      <button class="secondary-btn compact-btn" :disabled="groupMediaLoading" @click="$emit('load-group-media')">
        <!-- 行注：渲染动态文本 -->
        {{ groupMediaLoading ? '刷新中...' : '刷新列表' }}
      <!-- 行注：结束按钮 -->
      </button>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="workspace-toolbar media-toolbar">
      <!-- 行注：渲染容器 -->
      <div class="tab-group">
        <!-- 行注：展示“全部”文案 -->
        <button class="tab-btn" :class="{ active: mediaType === 'all' }" @click="$emit('update:media-type', 'all')">全部</button>
        <!-- 行注：展示“图片”文案 -->
        <button class="tab-btn" :class="{ active: mediaType === 'image' }" @click="$emit('update:media-type', 'image')">图片</button>
        <!-- 行注：展示“文件”文案 -->
        <button class="tab-btn" :class="{ active: mediaType === 'file' }" @click="$emit('update:media-type', 'file')">文件</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="search-shell workspace-search">
        <!-- 行注：渲染图标容器 -->
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <!-- 行注：补充图标圆形路径 -->
          <circle cx="11" cy="11" r="8" />
          <!-- 行注：补充图标路径 -->
          <path d="M21 21l-4.35-4.35" />
        <!-- 行注：结束图标容器 -->
        </svg>
        <!-- 行注：渲染输入框 -->
        <input
          :value="mediaKeyword"
          type="text"
          class="search-input"
          placeholder="搜索文件名或类型"
          @input="$emit('update:media-keyword', ($event.target as HTMLInputElement).value || '')"
          @keyup.enter="$emit('load-group-media')"
        />
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-if="groupMediaItems.length > 0" class="media-list">
      <!-- 行注：渲染article 节点 -->
      <article v-for="item in groupMediaItems" :key="item.id" class="media-card">
        <!-- 行注：渲染容器 -->
        <div class="media-cover" :class="{ image: isImageMedia(item) }">
          <!-- 行注：渲染图片 -->
          <ProtectedImage
            v-if="isImageMedia(item) && (item.accessUrl || item.fileUrl)"
            :src="item.accessUrl || item.fileUrl"
            :alt="item.fileName || '群图片'"
          />
          <!-- 行注：渲染图标容器 -->
          <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
            <!-- 行注：补充图标路径 -->
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <!-- 行注：补充图标折线 -->
            <polyline points="14 2 14 8 20 8" />
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="media-info">
          <!-- 行注：渲染容器 -->
          <div class="media-name">{{ item.fileName || (isImageMedia(item) ? '群图片' : '群文件') }}</div>
          <!-- 行注：渲染容器 -->
          <div class="media-meta">
            <!-- 行注：渲染文本节点 -->
            <span>{{ item.fromNickname || '成员' }}</span>
            <!-- 行注：渲染文本节点 -->
            <span>{{ formatDateTime(item.createTime) }}</span>
            <!-- 行注：渲染文本节点 -->
            <span>{{ formatSize(item.fileSize) }}</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="media-actions">
            <!-- 行注：展示“打开”文案 -->
            <button class="mini-btn" @click="$emit('open-media-resource', item)">打开</button>
            <!-- 行注：展示“复制链接”文案 -->
            <button class="mini-btn" @click="$emit('copy-media-link', item)">复制链接</button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束article 节点 -->
      </article>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div v-else class="empty-state compact">
      <!-- 行注：渲染容器 -->
      <div class="empty-title">{{ mediaKeyword.trim() ? '没有找到匹配文件' : '当前群还没有图片或文件' }}</div>
      <!-- 行注：渲染容器 -->
      <div class="empty-subtitle">{{ mediaKeyword.trim() ? '尝试其他关键词或切换分类' : '后续在群里发送的图片和文件会自动汇总到这里' }}</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束section 节点 -->
  </section>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupMembersResources 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import type { GroupMediaItem } from '../../types/chat'
import { formatDateTime, formatSize } from '../../utils/chat'

defineProps<{  // 行注：开始当前逻辑块
  groupMessageSearchLoading: boolean  // 行注：设置 groupMessageSearchLoading 配置项
  messageSearchKeyword: string  // 行注：设置 messageSearchKeyword 配置项
  groupMessageSearchResults: GroupMediaItem[]  // 行注：设置 groupMessageSearchResults 配置项
  getMediaTypeText: (item: GroupMediaItem) => string  // 行注：设置 getMediaTypeText 配置项
  getMessageSearchPreview: (item: GroupMediaItem) => string  // 行注：设置 getMessageSearchPreview 配置项
  groupMediaLoading: boolean  // 行注：设置 groupMediaLoading 配置项
  mediaType: 'all' | 'image' | 'file'  // 行注：设置 mediaType 配置项
  mediaKeyword: string  // 行注：设置 mediaKeyword 配置项
  groupMediaItems: GroupMediaItem[]  // 行注：设置 groupMediaItems 配置项
  isImageMedia: (item: GroupMediaItem) => boolean  // 行注：设置 isImageMedia 配置项
}>()  // 行注：执行当前调用逻辑

defineEmits<{  // 行注：开始当前逻辑块
  (event: 'update:message-search-keyword', value: string): void  // 行注：执行当前调用逻辑
  (event: 'search-group-messages'): void  // 行注：执行当前调用逻辑
  (event: 'open-group-chat', item: GroupMediaItem): void  // 行注：执行当前调用逻辑
  (event: 'open-media-resource', item: GroupMediaItem): void  // 行注：执行当前调用逻辑
  (event: 'update:media-type', value: 'all' | 'image' | 'file'): void  // 行注：执行当前调用逻辑
  (event: 'update:media-keyword', value: string): void  // 行注：执行当前调用逻辑
  (event: 'load-group-media'): void  // 行注：执行当前调用逻辑
  (event: 'copy-media-link', item: GroupMediaItem): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.panel-card {  /* 行注：定义 .panel-card 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  padding: 22px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.workspace-head,  /* 行注：补充 .workspace-head 选择器 */
.workspace-toolbar,  /* 行注：补充 .workspace-toolbar 选择器 */
.result-title-row,  /* 行注：补充 .result-title-row 选择器 */
.media-meta,  /* 行注：补充 .media-meta 选择器 */
.media-actions,  /* 行注：补充 .media-actions 选择器 */
.result-actions,  /* 行注：补充 .result-actions 选择器 */
.tab-group {  /* 行注：定义 .tab-group 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.workspace-head {  /* 行注：定义 .workspace-head 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.panel-title {  /* 行注：定义 .panel-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 17px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.panel-subtitle,  /* 行注：补充 .panel-subtitle 选择器 */
.result-time,  /* 行注：补充 .result-time 选择器 */
.media-meta,  /* 行注：补充 .media-meta 选择器 */
.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.panel-subtitle {  /* 行注：定义 .panel-subtitle 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
.mini-btn,  /* 行注：补充 .mini-btn 选择器 */
.tab-btn {  /* 行注：定义 .tab-btn 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.compact-btn {  /* 行注：定义 .compact-btn 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.secondary-btn:hover,  /* 行注：补充 .secondary-btn:hover 选择器 */
.mini-btn:hover {  /* 行注：定义 .mini-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.workspace-toolbar {  /* 行注：定义 .workspace-toolbar 样式 */
  margin-top: 16px;  /* 行注：设置 margin-top 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.search-shell {  /* 行注：定义 .search-shell 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.workspace-search {  /* 行注：定义 .workspace-search 样式 */
  min-width: min(300px, 100%);  /* 行注：设置 min-width 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
}  /* 行注：结束当前样式块 */

.search-input {  /* 行注：定义 .search-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.search-result-list,  /* 行注：补充 .search-result-list 选择器 */
.media-list {  /* 行注：定义 .media-list 样式 */
  margin-top: 18px;  /* 行注：设置 margin-top 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.search-result-card,  /* 行注：补充 .search-result-card 选择器 */
.media-card {  /* 行注：定义 .media-card 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 16px 18px;  /* 行注：设置 padding 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.search-result-card {  /* 行注：定义 .search-result-card 样式 */
  grid-template-columns: minmax(0, 1fr) auto;  /* 行注：设置 grid-template-columns 样式 */
}  /* 行注：结束当前样式块 */

.result-main,  /* 行注：补充 .result-main 选择器 */
.media-info {  /* 行注：定义 .media-info 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.result-title-row,  /* 行注：补充 .result-title-row 选择器 */
.media-meta,  /* 行注：补充 .media-meta 选择器 */
.media-actions,  /* 行注：补充 .media-actions 选择器 */
.result-actions {  /* 行注：定义 .result-actions 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.result-type-tag {  /* 行注：定义 .result-type-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  height: 22px;  /* 行注：设置 height 样式 */
  padding: 0 10px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  background: rgba(80, 145, 255, 0.12);  /* 行注：设置 background 样式 */
  color: #4f86ff;  /* 行注：设置 color 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.result-time,  /* 行注：补充 .result-time 选择器 */
.media-meta {  /* 行注：定义 .media-meta 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.result-content {  /* 行注：定义 .result-content 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.7;  /* 行注：设置 line-height 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.tab-group {  /* 行注：定义 .tab-group 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn {  /* 行注：定义 .tab-btn 样式 */
  height: 34px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border-radius: 999px;  /* 行注：设置 border-radius 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.tab-btn.active {  /* 行注：定义 .tab-btn.active 样式 */
  background: rgba(0, 214, 143, 0.14);  /* 行注：设置 background 样式 */
  border-color: rgba(0, 214, 143, 0.28);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.media-card {  /* 行注：定义 .media-card 样式 */
  grid-template-columns: 88px minmax(0, 1fr);  /* 行注：设置 grid-template-columns 样式 */
}  /* 行注：结束当前样式块 */

.media-cover {  /* 行注：定义 .media-cover 样式 */
  width: 88px;  /* 行注：设置 width 样式 */
  height: 88px;  /* 行注：设置 height 样式 */
  border-radius: 18px;  /* 行注：设置 border-radius 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid color-mix(in srgb, var(--linkx-border) 88%, transparent);  /* 行注：设置 border 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.media-cover.image {  /* 行注：定义 .media-cover.image 样式 */
  background: color-mix(in srgb, var(--linkx-bg-hover) 72%, transparent);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.media-cover img {  /* 行注：定义 .media-cover img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.media-name {  /* 行注：定义 .media-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 15px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  word-break: break-word;  /* 行注：设置 word-break 样式 */
}  /* 行注：结束当前样式块 */

.media-meta {  /* 行注：定义 .media-meta 样式 */
  margin-top: 8px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

.media-actions {  /* 行注：定义 .media-actions 样式 */
  margin-top: 12px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

.empty-state {  /* 行注：定义 .empty-state 样式 */
  min-height: 160px;  /* 行注：设置 min-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  padding: 12px 0;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .search-result-card,  /* 行注：补充 .search-result-card 选择器 */
  .media-card {  /* 行注：定义 .media-card 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .workspace-head,  /* 行注：补充 .workspace-head 选择器 */
  .workspace-toolbar {  /* 行注：定义 .workspace-toolbar 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .secondary-btn,  /* 行注：补充 .secondary-btn 选择器 */
  .mini-btn {  /* 行注：定义 .mini-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */

  .tab-group,  /* 行注：补充 .tab-group 选择器 */
  .media-actions,  /* 行注：补充 .media-actions 选择器 */
  .result-actions,  /* 行注：补充 .result-actions 选择器 */
  .result-title-row,  /* 行注：补充 .result-title-row 选择器 */
  .media-meta {  /* 行注：定义 .media-meta 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
