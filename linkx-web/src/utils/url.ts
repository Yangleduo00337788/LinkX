import { getElectronAPI } from './electron'

const ALLOWED_EXTERNAL_PROTOCOLS = new Set(['http:', 'https:'])
const ALLOWED_DOWNLOAD_PROTOCOLS = new Set(['http:', 'https:', 'blob:'])

function resolveUrl(rawUrl: string, allowBlob: boolean) {
  const normalized = rawUrl?.trim()
  if (!normalized) {
    return null
  }
  try {
    const parsed = new URL(normalized, window.location.origin)
    const allowedProtocols = allowBlob ? ALLOWED_DOWNLOAD_PROTOCOLS : ALLOWED_EXTERNAL_PROTOCOLS
    if (!allowedProtocols.has(parsed.protocol)) {
      return null
    }
    return parsed.href
  } catch {
    return null
  }
}

export function resolveSafeExternalUrl(rawUrl: string) {
  return resolveUrl(rawUrl, false)
}

export function resolveSafeDownloadUrl(rawUrl: string) {
  return resolveUrl(rawUrl, true)
}

export async function openSafeExternalUrl(rawUrl: string) {
  const nextUrl = resolveSafeExternalUrl(rawUrl)
  if (!nextUrl) {
    throw new Error('不支持打开该链接')
  }
  const electronAPI = getElectronAPI()
  if (electronAPI) {
    await electronAPI.openExternal(nextUrl)
    return nextUrl
  }
  window.open(nextUrl, '_blank', 'noopener,noreferrer')
  return nextUrl
}

export function triggerSafeDownload(rawUrl: string, fileName?: string) {
  const nextUrl = resolveSafeDownloadUrl(rawUrl)
  if (!nextUrl) {
    throw new Error('不支持下载该链接')
  }
  const anchor = document.createElement('a')
  anchor.href = nextUrl
  if (fileName?.trim()) {
    anchor.download = fileName.trim()
  }
  anchor.rel = 'noopener noreferrer'
  anchor.click()
  return nextUrl
}
