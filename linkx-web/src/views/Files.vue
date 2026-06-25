<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="content-area">
    <!-- 行注：渲染容器 -->
    <div class="files-panel">
      <!-- 行注：渲染容器 -->
      <div class="panel-header">
        <!-- 行注：展示“文件”文案 -->
        <span class="header-title">文件</span>
        <!-- 行注：渲染按钮 -->
        <button class="upload-btn" @click="triggerUpload" :disabled="uploading">
          <!-- 行注：渲染图标容器 -->
          <svg v-if="!uploading" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标路径 -->
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <!-- 行注：补充图标折线 -->
            <polyline points="17 8 12 3 7 8"/>
            <!-- 行注：补充图标线段 -->
            <line x1="12" y1="3" x2="12" y2="15"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：渲染容器 -->
          <div v-else class="upload-loading"></div>
          <!-- 行注：渲染动态文本 -->
          {{ uploading ? '上传中...' : '上传' }}
        <!-- 行注：结束按钮 -->
        </button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="files-search">
        <!-- 行注：渲染容器 -->
        <div class="search-input-wrapper">
          <!-- 行注：渲染图标容器 -->
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="11" cy="11" r="8"/>
            <!-- 行注：补充图标线段 -->
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：渲染输入框 -->
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索文件..."
            class="search-input"
            @input="handleSearch"
          />
          <!-- 行注：渲染容器 -->
          <div v-if="searchKeyword" class="search-clear" @click="clearSearch">
            <!-- 行注：渲染图标容器 -->
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none">
              <!-- 行注：补充图标圆形路径 -->
              <circle cx="12" cy="12" r="10" fill="currentColor" opacity="0.2"/>
              <!-- 行注：补充图标路径 -->
              <path d="M15 9L9 15M9 9L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            <!-- 行注：结束图标容器 -->
            </svg>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="files-list">
        <!-- 行注：渲染容器 -->
        <div v-if="loading" class="loading-state">
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
            v-for="file in files"
            :key="file.id"
            class="file-item"
            :class="{ active: selectedFile?.id === file.id }"
            @click="previewFile(file)"
          >
            <!-- 行注：渲染容器 -->
            <div class="file-icon" :class="getFileTypeClass(file.originalName)">
              <!-- 行注：渲染图标容器 -->
              <svg v-if="isImage(file.originalName)" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <!-- 行注：补充图标矩形路径 -->
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="8.5" cy="8.5" r="1.5"/>
                <!-- 行注：补充图标折线 -->
                <polyline points="21 15 16 10 5 21"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：渲染图标容器 -->
              <svg v-else width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <!-- 行注：补充图标路径 -->
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <!-- 行注：补充图标折线 -->
                <polyline points="14 2 14 8 20 8"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="file-info">
              <!-- 行注：渲染容器 -->
              <div class="file-name">{{ file.originalName }}</div>
              <!-- 行注：渲染容器 -->
              <div class="file-meta">
                <!-- 行注：渲染文本节点 -->
                <span class="file-size">{{ formatSize(file.fileSize) }}</span>
                <!-- 行注：渲染文本节点 -->
                <span class="file-time">{{ formatTime(file.createTime) }}</span>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="file-actions">
              <!-- 行注：渲染按钮 -->
              <button class="action-btn" @click.stop="copyLink(file)" title="复制链接">
                <!-- 行注：渲染图标容器 -->
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标矩形路径 -->
                  <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                  <!-- 行注：补充图标路径 -->
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                <!-- 行注：结束图标容器 -->
                </svg>
              <!-- 行注：结束按钮 -->
              </button>
              <!-- 行注：渲染按钮 -->
              <button class="action-btn delete" @click.stop="deleteFile(file.id)" title="删除">
                <!-- 行注：渲染图标容器 -->
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标折线 -->
                  <polyline points="3 6 5 6 21 6"/>
                  <!-- 行注：补充图标路径 -->
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                <!-- 行注：结束图标容器 -->
                </svg>
              <!-- 行注：结束按钮 -->
              </button>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="files.length === 0" class="empty-state">
            <!-- 行注：渲染容器 -->
            <div class="empty-icon">
              <!-- 行注：渲染图标容器 -->
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
                <!-- 行注：补充图标路径 -->
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <!-- 行注：补充图标折线 -->
                <polyline points="14 2 14 8 20 8"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="empty-title">{{ searchKeyword ? '未找到文件' : '暂无文件' }}</div>
            <!-- 行注：渲染容器 -->
            <div class="empty-subtitle">{{ searchKeyword ? '尝试其他关键词' : '点击上传按钮添加文件' }}</div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束模板区域 -->
        </template>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染容器 -->
    <div class="preview-panel">
      <!-- 行注：开始定义模板区域 -->
      <template v-if="selectedFile">
        <!-- 行注：渲染容器 -->
        <div class="preview-header">
          <!-- 行注：展示“预览”文案 -->
          <span class="preview-title">预览</span>
          <!-- 行注：渲染按钮 -->
          <button class="preview-close" @click="selectedFile = null">
            <!-- 行注：渲染图标容器 -->
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <!-- 行注：补充图标线段 -->
              <line x1="18" y1="6" x2="6" y2="18"/>
              <!-- 行注：补充图标线段 -->
              <line x1="6" y1="6" x2="18" y2="18"/>
            <!-- 行注：结束图标容器 -->
            </svg>
          <!-- 行注：结束按钮 -->
          </button>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="preview-content">
          <!-- 行注：渲染容器 -->
          <div v-if="isImage(selectedFile.originalName)" class="preview-image">
            <!-- 行注：渲染图片 -->
            <ProtectedImage
              v-if="selectedFile.fileUrl || selectedFile.accessUrl"
              :src="selectedFile.fileUrl || selectedFile.accessUrl"
              :alt="selectedFile.originalName"
            />
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-else class="preview-placeholder">
            <!-- 行注：渲染图标容器 -->
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <!-- 行注：补充图标路径 -->
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <!-- 行注：补充图标折线 -->
              <polyline points="14 2 14 8 20 8"/>
            <!-- 行注：结束图标容器 -->
            </svg>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="preview-info">
            <!-- 行注：渲染容器 -->
            <div class="preview-info-item">
              <!-- 行注：展示“文件名”文案 -->
              <span class="info-label">文件名</span>
              <!-- 行注：渲染文本节点 -->
              <span class="info-value">{{ selectedFile.originalName }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="preview-info-item">
              <!-- 行注：展示“大小”文案 -->
              <span class="info-label">大小</span>
              <!-- 行注：渲染文本节点 -->
              <span class="info-value">{{ formatSize(selectedFile.fileSize) }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="preview-info-item">
              <!-- 行注：展示“类型”文案 -->
              <span class="info-label">类型</span>
              <!-- 行注：渲染文本节点 -->
              <span class="info-value">{{ selectedFile.fileType || '-' }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="preview-info-item">
              <!-- 行注：展示“上传时间”文案 -->
              <span class="info-label">上传时间</span>
              <!-- 行注：渲染文本节点 -->
              <span class="info-value">{{ formatTime(selectedFile.createTime) }}</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="preview-info-item">
              <!-- 行注：展示“链接”文案 -->
              <span class="info-label">链接</span>
              <!-- 行注：渲染文本节点 -->
              <span class="info-value link">{{ selectedFile.accessUrl || '-' }}</span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="preview-actions">
            <!-- 行注：渲染按钮 -->
            <button class="preview-action-btn" @click="copyLink(selectedFile)">
              <!-- 行注：渲染图标容器 -->
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标矩形路径 -->
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <!-- 行注：补充图标路径 -->
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“复制链接”文案 -->
              复制链接
            <!-- 行注：结束按钮 -->
            </button>
            <!-- 行注：渲染按钮 -->
            <button class="preview-action-btn download" @click="downloadFile(selectedFile)">
              <!-- 行注：渲染图标容器 -->
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                <!-- 行注：补充图标折线 -->
                <polyline points="7 10 12 15 17 10"/>
                <!-- 行注：补充图标线段 -->
                <line x1="12" y1="15" x2="12" y2="3"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“下载”文案 -->
              下载
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束模板区域 -->
      </template>
      <!-- 行注：渲染容器 -->
      <div v-else class="preview-empty">
        <!-- 行注：渲染容器 -->
        <div class="preview-empty-icon">
          <!-- 行注：渲染图标容器 -->
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8">
            <!-- 行注：补充图标矩形路径 -->
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <!-- 行注：补充图标圆形路径 -->
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <!-- 行注：补充图标折线 -->
            <polyline points="21 15 16 10 5 21"/>
          <!-- 行注：结束图标容器 -->
          </svg>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“选择文件查看预览”文案 -->
        <div class="preview-empty-text">选择文件查看预览</div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      style="display: none"
      @change="handleFileChange"
    />
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 文件页面，展示上传记录与文件管理操作。
 */
import { onMounted, ref } from 'vue'  // 行注：引入 onMounted, ref 能力
import { fileApi } from '../api/client'
import ProtectedImage from '../components/ProtectedImage.vue'
import { useMessage } from 'naive-ui'  // 行注：引入 useMessage 能力
import { parseDateTime } from '../utils/datetime'  // 行注：引入 parseDateTime 能力
import { hydrateFileAccessUrl, hydrateFileAccessUrls, resolveFileAccessUrl } from '../utils/file-access'  // 行注：引入 hydrateFileAccessUrl, hydrateFileAccessUrls, resolveFileAccessUrl 能力
import { triggerSafeDownload } from '../utils/url'  // 行注：引入 triggerSafeDownload 能力

const message = useMessage()  // 行注：获取全局消息实例
const fileInputRef = ref<HTMLInputElement>()  // 行注：初始化 fileInputRef 状态
const searchKeyword = ref('')  // 行注：初始化 searchKeyword 响应式状态
const selectedFile = ref<any>(null)  // 行注：初始化 selectedFile 变量
const files = ref<any[]>([])  // 行注：初始化 files 变量
const uploading = ref(false)  // 行注：初始化 uploading 响应式状态
const loading = ref(false)  // 行注：初始化 loading 响应式状态
let searchTimer: any = null  // 行注：初始化 searchTimer 变量

onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
  loadFiles()  // 行注：调用 loadFiles 方法
})  // 行注：结束当前调用配置

async function loadFiles(keyword?: string) {  // 行注：定义异步 loadFiles 方法
  loading.value = true  // 行注：更新 loading 状态
  try {  // 行注：尝试执行可能失败的逻辑
    const res: any = await fileApi.list(keyword)  // 行注：接收 res 异步结果
    files.value = await hydrateFileAccessUrls(res.data || [])  // 行注：更新 files 状态
    if (selectedFile.value?.id) {  // 行注：判断当前条件是否成立
      selectedFile.value = files.value.find(item => item.id === selectedFile.value.id) || null  // 行注：调用 find 方法
    }  // 行注：结束当前代码块
  } catch (e) {  // 行注：捕获并处理异常
    console.error('loadFiles error:', e)  // 行注：输出错误日志
  } finally {  // 行注：执行收尾清理逻辑
    loading.value = false  // 行注：更新 loading 状态
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

function handleSearch() {  // 行注：定义 handleSearch 方法
  if (searchTimer) clearTimeout(searchTimer)  // 行注：判断当前条件是否成立
  searchTimer = setTimeout(() => {  // 行注：执行当前调用逻辑
    loadFiles(searchKeyword.value || undefined)  // 行注：调用 loadFiles 方法
  }, 300)  // 行注：补充当前表达式
}  // 行注：结束当前代码块

function clearSearch() {  // 行注：定义 clearSearch 方法
  searchKeyword.value = ''  // 行注：更新 searchKeyword 状态
  loadFiles()  // 行注：调用 loadFiles 方法
}  // 行注：结束当前代码块

function triggerUpload() {  // 行注：定义 triggerUpload 方法
  fileInputRef.value?.click()  // 行注：调用 click 方法
}  // 行注：结束当前代码块

async function handleFileChange(e: Event) {  // 行注：定义异步 handleFileChange 方法
  const input = e.target as HTMLInputElement  // 行注：初始化 input 变量
  const file = input.files?.[0]  // 行注：初始化 file 变量
  if (!file) return  // 行注：判断当前条件是否成立

  uploading.value = true  // 行注：更新 uploading 状态
  try {  // 行注：尝试执行可能失败的逻辑
    const uploadApi = file.type.startsWith('image/') ? fileApi.uploadImage : fileApi.uploadFile  // 行注：初始化 uploadApi 实例
    const res: any = await uploadApi(file)  // 行注：接收 res 异步结果
    message.success('上传成功')  // 行注：提示成功信息
    await loadFiles(searchKeyword.value || undefined)  // 行注：调用 loadFiles 方法
    selectedFile.value = await hydrateFileAccessUrl(res.data)  // 行注：更新 selectedFile 状态
  } catch (e: any) {  // 行注：捕获并处理异常
    message.error(e.response?.data?.message || '上传失败')  // 行注：提示错误信息
  } finally {  // 行注：执行收尾清理逻辑
    uploading.value = false  // 行注：更新 uploading 状态
    if (fileInputRef.value) fileInputRef.value.value = ''  // 行注：判断当前条件是否成立
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

async function deleteFile(id: number) {  // 行注：定义异步 deleteFile 方法
  try {  // 行注：尝试执行可能失败的逻辑
    await fileApi.delete(id)  // 行注：调用 delete 方法
    message.success('已删除')  // 行注：提示成功信息
    if (selectedFile.value?.id === id) {  // 行注：判断当前条件是否成立
      selectedFile.value = null  // 行注：更新 selectedFile 状态
    }  // 行注：结束当前代码块
    await loadFiles(searchKeyword.value || undefined)  // 行注：调用 loadFiles 方法
  } catch (e: any) {  // 行注：捕获并处理异常
    message.error(e.response?.data?.message || '删除失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

async function copyLink(file: any) {  // 行注：定义异步 copyLink 方法
  const accessUrl = await resolveFileAccessUrl(file?.fileUrl)  // 行注：接收 accessUrl 异步结果
  if (!accessUrl) {  // 行注：判断当前条件是否成立
    message.error('无法生成访问链接')  // 行注：提示错误信息
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  await navigator.clipboard.writeText(accessUrl)  // 行注：写入剪贴板
  message.success('链接已复制')  // 行注：提示成功信息
}  // 行注：结束当前代码块

async function downloadFile(file: any) {  // 行注：定义异步 downloadFile 方法
  try {  // 行注：尝试执行可能失败的逻辑
    const accessUrl = await resolveFileAccessUrl(file?.fileUrl)  // 行注：接收 accessUrl 异步结果
    if (!accessUrl) {  // 行注：判断当前条件是否成立
      throw new Error('文件访问链接不可用')  // 行注：抛出异常并终止当前流程
    }  // 行注：结束当前代码块
    triggerSafeDownload(accessUrl, file.originalName)  // 行注：调用 triggerSafeDownload 方法
  } catch (error: any) {  // 行注：捕获并处理异常
    message.error(error.message || '下载失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

async function previewFile(file: any) {  // 行注：定义异步 previewFile 方法
  selectedFile.value = await hydrateFileAccessUrl(file)  // 行注：更新 selectedFile 状态
}  // 行注：结束当前代码块

function isImage(name: string) {  // 行注：定义 isImage 方法
  return /\.(jpg|jpeg|png|gif|webp|bmp|svg)$/i.test(name)  // 行注：返回当前结果
}  // 行注：结束当前代码块

function getFileTypeClass(name: string) {  // 行注：定义 getFileTypeClass 方法
  if (isImage(name)) return 'image'  // 行注：判断当前条件是否成立
  if (/\.(pdf)$/i.test(name)) return 'pdf'  // 行注：判断当前条件是否成立
  if (/\.(doc|docx)$/i.test(name)) return 'doc'  // 行注：判断当前条件是否成立
  if (/\.(xls|xlsx)$/i.test(name)) return 'xls'  // 行注：判断当前条件是否成立
  return 'default'  // 行注：返回当前结果
}  // 行注：结束当前代码块

function formatSize(bytes: number) {  // 行注：定义 formatSize 方法
  if (!bytes) return '0 B'  // 行注：判断当前条件是否成立
  if (bytes < 1024) return bytes + ' B'  // 行注：判断当前条件是否成立
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'  // 行注：判断当前条件是否成立
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'  // 行注：返回当前结果
}  // 行注：结束当前代码块

function formatTime(time: string) {  // 行注：定义 formatTime 方法
  if (!time) return ''  // 行注：判断当前条件是否成立
  const date = parseDateTime(time)  // 行注：初始化 date 变量
  if (!date) return ''  // 行注：判断当前条件是否成立
  return date.toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric', hour: '2-digit', minute: '2-digit' })  // 行注：返回当前结果
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.content-area {  /* 行注：定义 .content-area 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.files-panel {  /* 行注：定义 .files-panel 样式 */
  width: 360px;  /* 行注：设置 width 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-right: 1px solid var(--linkx-border);  /* 行注：设置 border-right 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  min-width: 300px;  /* 行注：设置 min-width 样式 */
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
  letter-spacing: 0.3px;  /* 行注：设置 letter-spacing 样式 */
}  /* 行注：结束当前样式块 */

.upload-btn {  /* 行注：定义 .upload-btn 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 6px;  /* 行注：设置 gap 样式 */
  padding: 8px 16px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.upload-btn:hover:not(:disabled) {  /* 行注：定义 .upload-btn:hover:not(:disabled) 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 12px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.upload-btn:disabled {  /* 行注：定义 .upload-btn:disabled 样式 */
  opacity: 0.7;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.upload-loading {  /* 行注：定义 .upload-loading 样式 */
  width: 14px;  /* 行注：设置 width 样式 */
  height: 14px;  /* 行注：设置 height 样式 */
  border: 2px solid rgba(255, 255, 255, 0.3);  /* 行注：设置 border 样式 */
  border-top-color: white;  /* 行注：设置 border-top-color 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  to { transform: rotate(360deg); }  /* 行注：定义 to 样式 */
}  /* 行注：结束当前样式块 */

.files-search {  /* 行注：定义 .files-search 样式 */
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
  pointer-events: none;  /* 行注：设置 pointer-events 样式 */
}  /* 行注：结束当前样式块 */

.search-input {  /* 行注：定义 .search-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  padding: 0 36px 0 36px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.search-input::placeholder {  /* 行注：定义 .search-input::placeholder 样式 */
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
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.search-clear:hover {  /* 行注：定义 .search-clear:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.files-list {  /* 行注：定义 .files-list 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 8px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.loading-state {  /* 行注：定义 .loading-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 200px;  /* 行注：设置 height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.loading-spinner {  /* 行注：定义 .loading-spinner 样式 */
  width: 24px;  /* 行注：设置 width 样式 */
  height: 24px;  /* 行注：设置 height 样式 */
  border: 2px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-top-color: var(--linkx-primary);  /* 行注：设置 border-top-color 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

.file-item {  /* 行注：定义 .file-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.file-item:hover {  /* 行注：定义 .file-item:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.file-item.active {  /* 行注：定义 .file-item.active 样式 */
  background: var(--linkx-bg-active);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.file-icon {  /* 行注：定义 .file-icon 样式 */
  width: 44px;  /* 行注：设置 width 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
}  /* 行注：结束当前样式块 */

.file-icon.image {  /* 行注：定义 .file-icon.image 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-icon.pdf {  /* 行注：定义 .file-icon.pdf 样式 */
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-icon.doc {  /* 行注：定义 .file-icon.doc 样式 */
  background: linear-gradient(135deg, #4a90e2 0%, #3a7bd5 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-icon.xls {  /* 行注：定义 .file-icon.xls 样式 */
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-icon.default {  /* 行注：定义 .file-icon.default 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-info {  /* 行注：定义 .file-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.file-name {  /* 行注：定义 .file-name 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.file-meta {  /* 行注：定义 .file-meta 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.file-actions {  /* 行注：定义 .file-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  opacity: 0;  /* 行注：设置 opacity 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.file-item:hover .file-actions {  /* 行注：定义 .file-item:hover .file-actions 样式 */
  opacity: 1;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.action-btn {  /* 行注：定义 .action-btn 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.action-btn:hover {  /* 行注：定义 .action-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.action-btn.delete:hover {  /* 行注：定义 .action-btn.delete:hover 样式 */
  background: rgba(255, 61, 113, 0.1);  /* 行注：设置 background 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.empty-state {  /* 行注：定义 .empty-state 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.empty-icon {  /* 行注：定义 .empty-icon 样式 */
  opacity: 0.3;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.empty-title {  /* 行注：定义 .empty-title 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.empty-subtitle {  /* 行注：定义 .empty-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

/* Preview Panel */
.preview-panel {  /* 行注：定义 .preview-panel 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.preview-header {  /* 行注：定义 .preview-header 样式 */
  height: 56px;  /* 行注：设置 height 样式 */
  padding: 0 20px;  /* 行注：设置 padding 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.preview-title {  /* 行注：定义 .preview-title 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preview-close {  /* 行注：定义 .preview-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.preview-close:hover {  /* 行注：定义 .preview-close:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preview-content {  /* 行注：定义 .preview-content 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  padding: 20px;  /* 行注：设置 padding 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
}  /* 行注：结束当前样式块 */

.preview-image {  /* 行注：定义 .preview-image 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  min-height: 300px;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

.preview-image img {  /* 行注：定义 .preview-image img 样式 */
  max-width: 100%;  /* 行注：设置 max-width 样式 */
  max-height: 400px;  /* 行注：设置 max-height 样式 */
  object-fit: contain;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.preview-placeholder {  /* 行注：定义 .preview-placeholder 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preview-info {  /* 行注：定义 .preview-info 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.preview-info-item {  /* 行注：定义 .preview-info-item 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  padding: 12px 16px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.preview-info-item:not(:last-child) {  /* 行注：定义 .preview-info-item:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.info-label {  /* 行注：定义 .info-label 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-value {  /* 行注：定义 .info-value 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  max-width: 200px;  /* 行注：设置 max-width 样式 */
  white-space: nowrap;  /* 行注：设置 white-space 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  text-overflow: ellipsis;  /* 行注：设置 text-overflow 样式 */
}  /* 行注：结束当前样式块 */

.info-value.link {  /* 行注：定义 .info-value.link 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
  text-decoration: underline;  /* 行注：设置 text-decoration 样式 */
}  /* 行注：结束当前样式块 */

.preview-actions {  /* 行注：定义 .preview-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.preview-action-btn {  /* 行注：定义 .preview-action-btn 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  padding: 12px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.preview-action-btn:hover {  /* 行注：定义 .preview-action-btn:hover 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preview-action-btn.download {  /* 行注：定义 .preview-action-btn.download 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.preview-action-btn.download:hover {  /* 行注：定义 .preview-action-btn.download:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.preview-empty {  /* 行注：定义 .preview-empty 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.preview-empty-icon {  /* 行注：定义 .preview-empty-icon 样式 */
  opacity: 0.2;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.preview-empty-text {  /* 行注：定义 .preview-empty-text 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .files-panel {  /* 行注：定义 .files-panel 样式 */
    width: 300px;  /* 行注：设置 width 样式 */
    min-width: 280px;  /* 行注：设置 min-width 样式 */
  }  /* 行注：结束当前样式块 */

  .preview-content {  /* 行注：定义 .preview-content 样式 */
    padding: 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 820px) {  /* 行注：声明响应式样式区块 */
  .content-area {  /* 行注：定义 .content-area 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  }  /* 行注：结束当前样式块 */

  .files-panel {  /* 行注：定义 .files-panel 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    min-width: 0;  /* 行注：设置 min-width 样式 */
    max-height: 42%;  /* 行注：设置 max-height 样式 */
    border-right: none;  /* 行注：设置 border-right 样式 */
    border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
  }  /* 行注：结束当前样式块 */

  .file-actions {  /* 行注：定义 .file-actions 样式 */
    opacity: 1;  /* 行注：设置 opacity 样式 */
  }  /* 行注：结束当前样式块 */

  .preview-panel {  /* 行注：定义 .preview-panel 样式 */
    min-height: 320px;  /* 行注：设置 min-height 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 560px) {  /* 行注：声明响应式样式区块 */
  .panel-header,  /* 行注：补充 .panel-header 选择器 */
  .preview-header {  /* 行注：定义 .preview-header 样式 */
    padding-inline: 12px;  /* 行注：设置 padding-inline 样式 */
  }  /* 行注：结束当前样式块 */

  .files-search,  /* 行注：补充 .files-search 选择器 */
  .preview-content {  /* 行注：定义 .preview-content 样式 */
    padding: 12px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .file-item {  /* 行注：定义 .file-item 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
    padding: 12px 10px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .file-actions {  /* 行注：定义 .file-actions 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */

  .file-meta,  /* 行注：补充 .file-meta 选择器 */
  .preview-actions {  /* 行注：定义 .preview-actions 样式 */
    flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  }  /* 行注：结束当前样式块 */

  .preview-info-item {  /* 行注：定义 .preview-info-item 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */

  .info-value {  /* 行注：定义 .info-value 样式 */
    max-width: 56%;  /* 行注：设置 max-width 样式 */
    white-space: normal;  /* 行注：设置 white-space 样式 */
    word-break: break-word;  /* 行注：设置 word-break 样式 */
    text-align: right;  /* 行注：设置 text-align 样式 */
  }  /* 行注：结束当前样式块 */

  .preview-action-btn {  /* 行注：定义 .preview-action-btn 样式 */
    min-width: calc(50% - 6px);  /* 行注：设置 min-width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
