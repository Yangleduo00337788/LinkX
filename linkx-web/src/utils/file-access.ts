import axios from 'axios'
import { fileApi } from '../api/client'

/** 服务端 ticket 单次有效：前端只缓存 blob，且每次展示可重新换取 ticket */
const BLOB_CACHE_TTL_MS = 90 * 1000

type BlobCacheEntry = {
  blobUrl: string
  expireAt: number
}

const blobUrlCache = new Map<string, BlobCacheEntry>()
const inflight = new Map<string, Promise<string>>()

function normalizeFileUrl(fileUrl?: string) {
  return fileUrl?.trim() || ''
}

function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem('token')
  if (!token) {
    return {}
  }
  return { Authorization: `Bearer ${token}` }
}

function cacheBlobUrl(fileUrl: string, blobUrl: string) {
  const previous = blobUrlCache.get(fileUrl)
  if (previous?.blobUrl && previous.blobUrl !== blobUrl) {
    URL.revokeObjectURL(previous.blobUrl)
  }
  blobUrlCache.set(fileUrl, {
    blobUrl,
    expireAt: Date.now() + BLOB_CACHE_TTL_MS
  })
}

function getCachedBlobUrl(fileUrl: string) {
  const entry = blobUrlCache.get(fileUrl)
  if (!entry) {
    return ''
  }
  if (entry.expireAt <= Date.now()) {
    URL.revokeObjectURL(entry.blobUrl)
    blobUrlCache.delete(fileUrl)
    return ''
  }
  return entry.blobUrl
}

export function getCachedFileAccessUrl(fileUrl?: string) {
  const normalized = normalizeFileUrl(fileUrl)
  if (!normalized) {
    return ''
  }
  return getCachedBlobUrl(normalized)
}

async function fetchBlobAccessUrl(fileUrl: string, forceRefresh: boolean) {
  if (!forceRefresh) {
    const cached = getCachedBlobUrl(fileUrl)
    if (cached) {
      return cached
    }
  }

  const pending = inflight.get(fileUrl)
  if (pending) {
    return pending
  }

  const task = (async () => {
    const response: any = await fileApi.getAccessUrl(fileUrl)
    const ticketUrl = String(response.data?.accessUrl || '')
    if (!ticketUrl) {
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

  inflight.set(fileUrl, task)
  return task
}

export async function resolveFileAccessUrl(fileUrl?: string, forceRefresh = false) {
  const normalizedFileUrl = normalizeFileUrl(fileUrl)
  if (!normalizedFileUrl) {
    return ''
  }
  try {
    return await fetchBlobAccessUrl(normalizedFileUrl, forceRefresh)
  } catch (error) {
    console.error('resolveFileAccessUrl error:', error)
    return ''
  }
}

export async function hydrateFileAccessUrl<T extends { fileUrl?: string; accessUrl?: string }>(file: T) {
  if (!file?.fileUrl) {
    return file
  }
  const accessUrl = await resolveFileAccessUrl(file.fileUrl)
  return {
    ...file,
    accessUrl
  }
}

export async function hydrateFileAccessUrls<T extends { fileUrl?: string; accessUrl?: string }>(files: T[]) {
  return Promise.all(files.map(item => hydrateFileAccessUrl(item)))
}

export function revokeAllFileAccessBlobUrls() {
  for (const entry of blobUrlCache.values()) {
    URL.revokeObjectURL(entry.blobUrl)
  }
  blobUrlCache.clear()
  inflight.clear()
}