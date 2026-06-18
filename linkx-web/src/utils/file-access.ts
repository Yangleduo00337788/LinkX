import { fileApi } from '../api/client'

const ACCESS_URL_CACHE_TTL_MS = 8 * 60 * 1000

type AccessUrlCacheEntry = {
  accessUrl: string
  expireAt: number
}

const accessUrlCache = new Map<string, AccessUrlCacheEntry>()

function normalizeFileUrl(fileUrl?: string) {
  return fileUrl?.trim() || ''
}

export function getCachedFileAccessUrl(fileUrl?: string) {
  const normalizedFileUrl = normalizeFileUrl(fileUrl)
  if (!normalizedFileUrl) {
    return ''
  }
  const cachedEntry = accessUrlCache.get(normalizedFileUrl)
  if (!cachedEntry) {
    return ''
  }
  if (cachedEntry.expireAt <= Date.now()) {
    accessUrlCache.delete(normalizedFileUrl)
    return ''
  }
  return cachedEntry.accessUrl
}

export async function resolveFileAccessUrl(fileUrl?: string, forceRefresh = false) {
  const normalizedFileUrl = normalizeFileUrl(fileUrl)
  if (!normalizedFileUrl) {
    return ''
  }
  if (!forceRefresh) {
    const cachedAccessUrl = getCachedFileAccessUrl(normalizedFileUrl)
    if (cachedAccessUrl) {
      return cachedAccessUrl
    }
  }
  const response: any = await fileApi.getAccessUrl(normalizedFileUrl)
  const accessUrl = String(response.data?.accessUrl || '')
  if (accessUrl) {
    accessUrlCache.set(normalizedFileUrl, {
      accessUrl,
      expireAt: Date.now() + ACCESS_URL_CACHE_TTL_MS
    })
  }
  return accessUrl
}

export async function hydrateFileAccessUrl<T extends { fileUrl?: string; accessUrl?: string }>(file: T) {
  if (!file?.fileUrl) {
    return file
  }
  try {
    const accessUrl = await resolveFileAccessUrl(file.fileUrl)
    return {
      ...file,
      accessUrl
    }
  } catch (error) {
    console.error('hydrateFileAccessUrl error:', error)
    return {
      ...file,
      accessUrl: ''
    }
  }
}

export async function hydrateFileAccessUrls<T extends { fileUrl?: string; accessUrl?: string }>(files: T[]) {
  return Promise.all(files.map(item => hydrateFileAccessUrl(item)))
}
