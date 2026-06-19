function normalizeBaseUrl(value: string | undefined, fallback: string) {
  const normalized = value?.trim()
  const nextValue = normalized || fallback
  return nextValue.replace(/\/+$/, '')
}

function resolveWsBaseUrl(apiBaseUrl: string) {
  const configuredValue = normalizeBaseUrl(import.meta.env.VITE_WS_BASE_URL, '')
  if (configuredValue) {
    return configuredValue
  }
  return apiBaseUrl.replace(/^http/i, protocol => protocol.toLowerCase() === 'https' ? 'wss' : 'ws')
}

export const API_BASE_URL = normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL, 'http://localhost:8080')
export const WS_BASE_URL = resolveWsBaseUrl(API_BASE_URL)
