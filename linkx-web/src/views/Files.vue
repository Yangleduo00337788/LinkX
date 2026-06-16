<template>
  <div class="content-area">
    <div class="files-panel">
      <div class="panel-header">
        <span class="header-title">文件</span>
        <button class="upload-btn" @click="triggerUpload" :disabled="uploading">
          <svg v-if="!uploading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
          <div v-else class="upload-loading"></div>
          {{ uploading ? '上传中...' : '上传' }}
        </button>
      </div>

      <div class="files-search">
        <div class="search-input-wrapper">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索文件..."
            class="search-input"
            @input="handleSearch"
          />
          <div v-if="searchKeyword" class="search-clear" @click="clearSearch">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.2"/>
              <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
      </div>

      <div class="files-list">
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>

        <template v-else>
          <div
            v-for="file in files"
            :key="file.id"
            class="file-item"
            :class="{ active: selectedFile?.id === file.id }"
            @click="previewFile(file)"
          >
            <div class="file-icon" :class="getFileTypeClass(file.originalName)">
              <svg v-if="isImage(file.originalName)" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <polyline points="21 15 16 10 5 21"/>
              </svg>
              <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="file-info">
              <div class="file-name">{{ file.originalName }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatSize(file.fileSize) }}</span>
                <span class="file-time">{{ formatTime(file.createTime) }}</span>
              </div>
            </div>
            <div class="file-actions">
              <button class="action-btn" @click.stop="copyLink(file.fileUrl)" title="复制链接">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
              </button>
              <button class="action-btn delete" @click.stop="deleteFile(file.id)" title="删除">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"/>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                </svg>
              </button>
            </div>
          </div>

          <div v-if="files.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
              </svg>
            </div>
            <div class="empty-title">{{ searchKeyword ? '未找到文件' : '暂无文件' }}</div>
            <div class="empty-subtitle">{{ searchKeyword ? '尝试其他关键词' : '点击上传按钮添加文件' }}</div>
          </div>
        </template>
      </div>
    </div>

    <div class="preview-panel">
      <template v-if="selectedFile">
        <div class="preview-header">
          <span class="preview-title">预览</span>
          <button class="preview-close" @click="selectedFile = null">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        <div class="preview-content">
          <div v-if="isImage(selectedFile.originalName)" class="preview-image">
            <img :src="selectedFile.fileUrl" :alt="selectedFile.originalName" />
          </div>
          <div v-else class="preview-placeholder">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <div class="preview-info">
            <div class="preview-info-item">
              <span class="info-label">文件名</span>
              <span class="info-value">{{ selectedFile.originalName }}</span>
            </div>
            <div class="preview-info-item">
              <span class="info-label">大小</span>
              <span class="info-value">{{ formatSize(selectedFile.fileSize) }}</span>
            </div>
            <div class="preview-info-item">
              <span class="info-label">类型</span>
              <span class="info-value">{{ selectedFile.fileType || '-' }}</span>
            </div>
            <div class="preview-info-item">
              <span class="info-label">上传时间</span>
              <span class="info-value">{{ formatTime(selectedFile.createTime) }}</span>
            </div>
            <div class="preview-info-item">
              <span class="info-label">链接</span>
              <span class="info-value link">{{ selectedFile.fileUrl }}</span>
            </div>
          </div>
          <div class="preview-actions">
            <button class="preview-action-btn" @click="copyLink(selectedFile.fileUrl)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              复制链接
            </button>
            <button class="preview-action-btn download" @click="downloadFile(selectedFile)">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <polyline points="7 10 12 15 17 10"/>
                <line x1="12" y1="15" x2="12" y2="3"/>
              </svg>
              下载
            </button>
          </div>
        </div>
      </template>
      <div v-else class="preview-empty">
        <div class="preview-empty-icon">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <polyline points="21 15 16 10 5 21"/>
          </svg>
        </div>
        <div class="preview-empty-text">选择文件查看预览</div>
      </div>
    </div>

    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { fileApi } from '../api/client'
import { useMessage } from 'naive-ui'

const message = useMessage()
const fileInputRef = ref<HTMLInputElement>()
const searchKeyword = ref('')
const selectedFile = ref<any>(null)
const files = ref<any[]>([])
const uploading = ref(false)
const loading = ref(false)
let searchTimer: any = null

onMounted(() => {
  loadFiles()
})

async function loadFiles(keyword?: string) {
  loading.value = true
  try {
    const res: any = await fileApi.list(keyword)
    files.value = res.data || []
  } catch (e) {
    console.error('loadFiles error:', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    loadFiles(searchKeyword.value || undefined)
  }, 300)
}

function clearSearch() {
  searchKeyword.value = ''
  loadFiles()
}

function triggerUpload() {
  fileInputRef.value?.click()
}

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const uploadApi = file.type.startsWith('image/') ? fileApi.uploadImage : fileApi.uploadFile
    const res: any = await uploadApi(file)
    message.success('上传成功')
    await loadFiles(searchKeyword.value || undefined)
    selectedFile.value = res.data
  } catch (e: any) {
    message.error(e.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

async function deleteFile(id: number) {
  try {
    await fileApi.delete(id)
    message.success('已删除')
    if (selectedFile.value?.id === id) {
      selectedFile.value = null
    }
    await loadFiles(searchKeyword.value || undefined)
  } catch (e: any) {
    message.error(e.response?.data?.message || '删除失败')
  }
}

function copyLink(url: string) {
  navigator.clipboard.writeText(url)
  message.success('链接已复制')
}

function downloadFile(file: any) {
  const a = document.createElement('a')
  a.href = file.fileUrl
  a.download = file.originalName
  a.click()
}

function previewFile(file: any) {
  selectedFile.value = file
}

function isImage(name: string) {
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(name)
}

function getFileTypeClass(name: string) {
  if (isImage(name)) return 'image'
  if (/\.(pdf)$/i.test(name)) return 'pdf'
  if (/\.(doc|docx)$/i.test(name)) return 'doc'
  if (/\.(xls|xlsx)$/i.test(name)) return 'xls'
  return 'default'
}

function formatSize(bytes: number) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatTime(time: string) {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.files-panel {
  width: 360px;
  background: var(--linkx-bg-card);
  border-right: 1px solid var(--linkx-border);
  display: flex;
  flex-direction: column;
  min-width: 300px;
}

.panel-header {
  height: 56px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
}

.header-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--linkx-text);
  letter-spacing: 0.3px;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: white;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.upload-btn:hover:not(:disabled) {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 12px var(--linkx-primary-glow);
  transform: translateY(-1px);
}

.upload-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.upload-loading {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.files-search {
  padding: 12px 16px;
  border-bottom: 1px solid var(--linkx-border);
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 12px;
  color: var(--linkx-text-muted);
  pointer-events: none;
}

.search-input {
  width: 100%;
  height: 36px;
  padding: 0 36px 0 36px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 13px;
  transition: var(--linkx-transition);
  outline: none;
}

.search-input::placeholder {
  color: var(--linkx-text-muted);
}

.search-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.search-clear {
  position: absolute;
  right: 8px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
  cursor: pointer;
  border-radius: 50%;
  transition: var(--linkx-transition-fast);
}

.search-clear:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.files-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--linkx-text-muted);
  gap: 12px;
  font-size: 13px;
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
  border-radius: var(--linkx-radius);
  transition: var(--linkx-transition-fast);
}

.file-item:hover {
  background: var(--linkx-bg-hover);
}

.file-item.active {
  background: var(--linkx-bg-active);
}

.file-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--linkx-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.file-icon.image {
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
}

.file-icon.pdf {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: white;
}

.file-icon.doc {
  background: linear-gradient(135deg, #4a90e2 0%, #3a7bd5 100%);
  color: white;
}

.file-icon.xls {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: white;
}

.file-icon.default {
  background: var(--linkx-bg);
  color: var(--linkx-text-secondary);
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--linkx-text-secondary);
}

.file-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: var(--linkx-transition-fast);
}

.file-item:hover .file-actions {
  opacity: 1;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.action-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-primary);
}

.action-btn.delete:hover {
  background: rgba(255, 61, 113, 0.1);
  color: var(--linkx-error);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--linkx-text-muted);
  gap: 12px;
}

.empty-icon {
  opacity: 0.3;
}

.empty-title {
  font-size: 14px;
  font-weight: 600;
}

.empty-subtitle {
  font-size: 13px;
}

/* Preview Panel */
.preview-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.preview-header {
  height: 56px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid var(--linkx-border);
}

.preview-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--linkx-text);
}

.preview-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.preview-close:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.preview-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  overflow-y: auto;
}

.preview-image {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius);
  margin-bottom: 16px;
  overflow: hidden;
  min-height: 300px;
}

.preview-image img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.preview-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius);
  margin-bottom: 16px;
  color: var(--linkx-text-muted);
}

.preview-info {
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius);
  overflow: hidden;
  margin-bottom: 16px;
}

.preview-info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}

.preview-info-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.info-label {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.info-value {
  font-size: 13px;
  color: var(--linkx-text);
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-value.link {
  color: var(--linkx-primary);
  text-decoration: underline;
}

.preview-actions {
  display: flex;
  gap: 12px;
}

.preview-action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.preview-action-btn:hover {
  border-color: var(--linkx-primary);
  color: var(--linkx-primary);
}

.preview-action-btn.download {
  background: var(--linkx-primary);
  border-color: var(--linkx-primary);
  color: white;
}

.preview-action-btn.download:hover {
  background: var(--linkx-primary-hover);
}

.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
  gap: 16px;
}

.preview-empty-icon {
  opacity: 0.2;
}

.preview-empty-text {
  font-size: 14px;
}

@media (max-width: 1100px) {
  .files-panel {
    width: 300px;
    min-width: 280px;
  }

  .preview-content {
    padding: 16px;
  }
}

@media (max-width: 820px) {
  .content-area {
    flex-direction: column;
  }

  .files-panel {
    width: 100%;
    min-width: 0;
    max-height: 42%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .file-actions {
    opacity: 1;
  }

  .preview-panel {
    min-height: 320px;
  }
}

@media (max-width: 560px) {
  .panel-header,
  .preview-header {
    padding-inline: 12px;
  }

  .files-search,
  .preview-content {
    padding: 12px;
  }

  .file-item {
    align-items: flex-start;
    flex-wrap: wrap;
    padding: 12px 10px;
  }

  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .file-meta,
  .preview-actions {
    flex-wrap: wrap;
  }

  .preview-info-item {
    align-items: flex-start;
    gap: 12px;
  }

  .info-value {
    max-width: 56%;
    white-space: normal;
    word-break: break-word;
    text-align: right;
  }

  .preview-action-btn {
    min-width: calc(50% - 6px);
  }
}
</style>
