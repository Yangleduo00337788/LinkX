/**
 * url 工具模块，封装当前前端场景下的通用方法。
 */
import { getElectronAPI } from './electron'  // 行注：引入 getElectronAPI 能力

const ALLOWED_EXTERNAL_PROTOCOLS = new Set(['http:', 'https:'])  // 行注：初始化 ALLOWED_EXTERNAL_PROTOCOLS 变量
const ALLOWED_DOWNLOAD_PROTOCOLS = new Set(['http:', 'https:', 'blob:'])  // 行注：初始化 ALLOWED_DOWNLOAD_PROTOCOLS 变量

function resolveUrl(rawUrl: string, allowBlob: boolean) {  // 行注：定义 resolveUrl 方法
  const normalized = rawUrl?.trim()  // 行注：初始化 normalized 变量
  if (!normalized) {  // 行注：判断当前条件是否成立
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  try {  // 行注：尝试执行可能失败的逻辑
    const parsed = new URL(normalized, window.location.origin)  // 行注：初始化 parsed 变量
    const allowedProtocols = allowBlob ? ALLOWED_DOWNLOAD_PROTOCOLS : ALLOWED_EXTERNAL_PROTOCOLS  // 行注：初始化 allowedProtocols 变量
    if (!allowedProtocols.has(parsed.protocol)) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return parsed.href  // 行注：返回当前结果
  } catch {  // 行注：捕获并处理异常
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

export function resolveSafeExternalUrl(rawUrl: string) {  // 行注：导出当前能力
  return resolveUrl(rawUrl, false)  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function resolveSafeDownloadUrl(rawUrl: string) {  // 行注：导出当前能力
  return resolveUrl(rawUrl, true)  // 行注：返回当前结果
}  // 行注：结束当前代码块

export async function openSafeExternalUrl(rawUrl: string) {  // 行注：导出当前能力
  const nextUrl = resolveSafeExternalUrl(rawUrl)  // 行注：初始化 nextUrl 变量
  if (!nextUrl) {  // 行注：判断当前条件是否成立
    throw new Error('不支持打开该链接')  // 行注：抛出异常并终止当前流程
  }  // 行注：结束当前代码块
  const electronAPI = getElectronAPI()  // 行注：初始化 electronAPI 实例
  if (electronAPI) {  // 行注：判断当前条件是否成立
    await electronAPI.openExternal(nextUrl)  // 行注：调用 openExternal 方法
    return nextUrl  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  window.open(nextUrl, '_blank', 'noopener,noreferrer')  // 行注：调用 open 方法
  return nextUrl  // 行注：返回当前结果
}  // 行注：结束当前代码块

export function triggerSafeDownload(rawUrl: string, fileName?: string) {  // 行注：导出当前能力
  const nextUrl = resolveSafeDownloadUrl(rawUrl)  // 行注：初始化 nextUrl 变量
  if (!nextUrl) {  // 行注：判断当前条件是否成立
    throw new Error('不支持下载该链接')  // 行注：抛出异常并终止当前流程
  }  // 行注：结束当前代码块
  const anchor = document.createElement('a')  // 行注：初始化 anchor 变量
  anchor.href = nextUrl  // 行注：更新 anchor.href 值
  if (fileName?.trim()) {  // 行注：判断当前条件是否成立
    anchor.download = fileName.trim()  // 行注：更新 anchor.download 值
  }  // 行注：结束当前代码块
  anchor.rel = 'noopener noreferrer'  // 行注：更新 anchor.rel 值
  anchor.click()  // 行注：调用 click 方法
  return nextUrl  // 行注：返回当前结果
}  // 行注：结束当前代码块
