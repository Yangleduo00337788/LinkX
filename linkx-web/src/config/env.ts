/**
 * 统一读取前端运行环境变量并推导 API 与 WebSocket 地址。
 */
function normalizeBaseUrl(value: string | undefined, fallback: string) {  // 行注：定义 normalizeBaseUrl 方法
  const normalized = value?.trim()  // 行注：初始化 normalized 变量
  const nextValue = normalized || fallback  // 行注：初始化 nextValue 状态
  return nextValue.replace(/\/+$/, '')  // 行注：返回当前结果
}  // 行注：结束当前代码块

// WebSocket 地址优先读取配置，否则由 HTTP 地址自动推导。
function resolveWsBaseUrl(apiBaseUrl: string) {  // 行注：定义 resolveWsBaseUrl 方法
  const configuredValue = normalizeBaseUrl(import.meta.env.VITE_WS_BASE_URL, '')  // 行注：初始化 configuredValue 状态
  if (configuredValue) {  // 行注：判断当前条件是否成立
    return configuredValue  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  return apiBaseUrl.replace(/^http/i, protocol => protocol.toLowerCase() === 'https' ? 'wss' : 'ws')  // 行注：返回当前结果
}  // 行注：结束当前代码块

export const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL, 'http://localhost:8080')  // 行注：导出当前能力
export const WS_BASE_URL = resolveWsBaseUrl(API_BASE_URL)  // 行注：导出当前能力
