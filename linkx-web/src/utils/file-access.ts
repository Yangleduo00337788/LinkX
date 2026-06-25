/**
 * 处理受保护文件访问地址解析与 Blob URL 生命周期。
 */
import axios from 'axios'  // 行注：引入 axios 模块
import { fileApi } from '../api/client'  // 行注：引入 fileApi 能力

/** 服务端 ticket 单次有效：前端只缓存 blob，且每次展示可重新换取 ticket */
const BLOB_CACHE_TTL_MS = 90 * 1000  // 行注：初始化 BLOB_CACHE_TTL_MS 变量

type BlobCacheEntry = {  // 行注：开始当前逻辑块
  blobUrl: string  // 行注：设置 blobUrl 配置项
  expireAt: number  // 行注：设置 expireAt 配置项
}  // 行注：结束当前代码块

const blobUrlCache = new Map<string, BlobCacheEntry>()  // 行注：初始化 blobUrlCache 变量
const inflight = new Map<string, Promise<string>>()  // 行注：初始化 inflight 变量

function normalizeFileUrl(fileUrl?: string) {  // 行注：定义 normalizeFileUrl 方法
  return fileUrl?.trim() || ''  // 行注：返回当前结果
}  // 行注：结束当前代码块

function getAuthHeaders(): Record<string, string> {  // 行注：定义 getAuthHeaders 方法
  const token = localStorage.getItem('token')  // 行注：初始化 token 变量
  if (!token) {  // 行注：判断当前条件是否成立
    return {}  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return { Authorization: `Bearer ${token}` }  // 行注：返回当前结果
}  // 行注：结束当前代码块

function cacheBlobUrl(fileUrl: string, blobUrl: string) {  // 行注：定义 cacheBlobUrl 方法
  const previous = blobUrlCache.get(fileUrl)  // 行注：初始化 previous 变量
  if (previous?.blobUrl && previous.blobUrl !== blobUrl) {  // 行注：判断当前条件是否成立
    URL.revokeObjectURL(previous.blobUrl)  // 行注：调用 revokeObjectURL 方法
  }  // 行注：结束当前代码块
  blobUrlCache.set(fileUrl, {  // 行注：开始当前逻辑块
    blobUrl,  // 行注：补充 blobUrl 配置项
    expireAt: Date.now() + BLOB_CACHE_TTL_MS  // 行注：设置 expireAt 配置项
  })  // 行注：结束当前调用配置
}  // 行注：结束当前代码块

function getCachedBlobUrl(fileUrl: string) {  // 行注：定义 getCachedBlobUrl 方法
  const entry = blobUrlCache.get(fileUrl)  // 行注：初始化 entry 变量
  if (!entry) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  if (entry.expireAt <= Date.now()) {  // 行注：判断当前条件是否成立
    URL.revokeObjectURL(entry.blobUrl)  // 行注：调用 revokeObjectURL 方法
    blobUrlCache.delete(fileUrl)  // 行注：调用 delete 方法
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return entry.blobUrl  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function getCachedFileAccessUrl(fileUrl?: string) {  // 行注：导出当前能力
  const normalized = normalizeFileUrl(fileUrl)  // 行注：初始化 normalized 变量
  if (!normalized) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return getCachedBlobUrl(normalized)  // 行注：返回当前结果
}  // 行注：结束当前代码块

async function fetchBlobAccessUrl(fileUrl: string, forceRefresh: boolean) {  // 行注：定义异步 fetchBlobAccessUrl 方法
  if (!forceRefresh) {  // 行注：判断当前条件是否成立
    const cached = getCachedBlobUrl(fileUrl)  // 行注：初始化 cached 变量
    if (cached) {  // 行注：判断当前条件是否成立
      return cached  // 行注：返回当前结果
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  const pending = inflight.get(fileUrl)  // 行注：初始化 pending 变量
  if (pending) {  // 行注：判断当前条件是否成立
    return pending  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  const task = (async () => {  // 行注：定义异步 task 方法
    const response: any = await fileApi.getAccessUrl(fileUrl)  // 行注：接收 response 异步结果
    // 处理后端返回的 Result 包装结构: { code: 200, data: { accessUrl: '...' }, message: '...' }
    const ticketUrl = String(response?.data?.accessUrl || response?.accessUrl || '')
    if (!ticketUrl) {
      console.error('[file-access] Failed to get ticket URL for:', fileUrl, 'response:', response)
      return ''
    }
    const blobResponse = await axios.get(ticketUrl, {
      responseType: 'blob',
      headers: getAuthHeaders(),
      timeout: 30000
    })
    const blobUrl = URL.createObjectURL(blobResponse.data)
    cacheBlobUrl(fileUrl, blobUrl)
    return blobUrl
  })().finally(() => {
    inflight.delete(fileUrl)
  })

  inflight.set(fileUrl, task)  // 行注：调用 set 方法
  return task  // 行注：返回当前结果
}  // 行注：结束当前代码块

export async function resolveFileAccessUrl(fileUrl?: string, forceRefresh = false) {  // 行注：导出当前能力
  const normalizedFileUrl = normalizeFileUrl(fileUrl)  // 行注：初始化 normalizedFileUrl 变量
  if (!normalizedFileUrl) {  // 行注：判断当前条件是否成立
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  try {  // 行注：尝试执行可能失败的逻辑
    return await fetchBlobAccessUrl(normalizedFileUrl, forceRefresh)  // 行注：返回当前结果
  } catch (error) {  // 行注：捕获并处理异常
    console.error('resolveFileAccessUrl error:', error)  // 行注：输出错误日志
    return ''  // 行注：返回当前结果
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export async function hydrateFileAccessUrl<T extends { fileUrl?: string; accessUrl?: string }>(file: T) {  // 行注：导出当前能力
  if (!file?.fileUrl) {  // 行注：判断当前条件是否成立
    return file  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const accessUrl = await resolveFileAccessUrl(file.fileUrl)  // 行注：接收 accessUrl 异步结果
  return {  // 行注：返回当前结果
    ...file,  // 行注：补充当前配置项
    accessUrl  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export async function hydrateFileAccessUrls<T extends { fileUrl?: string; accessUrl?: string }>(files: T[]) {  // 行注：导出当前能力
  return Promise.all(files.map(item => hydrateFileAccessUrl(item)))  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function revokeAllFileAccessBlobUrls() {  // 行注：导出当前能力
  for (const entry of blobUrlCache.values()) {  // 行注：遍历当前数据集合
    URL.revokeObjectURL(entry.blobUrl)  // 行注：调用 revokeObjectURL 方法
  }  // 行注：结束当前代码块
  blobUrlCache.clear()  // 行注：调用 clear 方法
  inflight.clear()  // 行注：调用 clear 方法
}  // 行注：结束当前代码块
