import axios from 'axios'
import { API_BASE_URL } from '../config/env'
import router from '../router'
import { useUserStore } from '../stores/user'

export { API_BASE_URL, WS_BASE_URL } from '../config/env'

const AUTH_STORAGE_KEYS = ['token', 'refreshToken', 'userId', 'nickname', 'avatar', 'username'] as const

let globalErrorHandler: ((message: string) => void) | null = null
let refreshTokenPromise: Promise<string> | null = null

export function setGlobalErrorHandler(handler: (message: string) => void) {
  globalErrorHandler = handler
}

function applyAuthResponse(data: any) {
  useUserStore().setLoginData(data)
}

function clearAuthState() {
  try {
    useUserStore().logout()
  } catch {
    for (const key of AUTH_STORAGE_KEYS) {
      localStorage.removeItem(key)
    }
  }
}

function isRefreshRequest(url?: string) {
  return typeof url === 'string' && url.includes('/api/auth/refresh')
}

async function refreshAccessToken() {
  const storedRefreshToken = localStorage.getItem('refreshToken')
  if (!storedRefreshToken) {
    throw new Error('登录已过期')
  }
  if (!refreshTokenPromise) {
    refreshTokenPromise = axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken: storedRefreshToken }, {
      timeout: 10000
    }).then(response => {
      const payload = response.data
      if (!payload || payload.code !== 200 || !payload.data?.accessToken) {
        throw new Error(payload?.message || '登录续期失败')
      }
      applyAuthResponse(payload.data)
      return String(payload.data.accessToken)
    }).finally(() => {
      refreshTokenPromise = null
    })
  }
  return refreshTokenPromise
}

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  res => {
    const payload = res.data
    if (payload && typeof payload === 'object' && 'code' in payload && payload.code !== 200) {
      const error: any = new Error(payload.message || '请求失败')
      error.response = {
        ...res,
        data: payload
      }
      return Promise.reject(error)
    }
    return payload
  },
  async error => {
    const originalRequest = error.config || {}
    const shouldRetryWithRefresh = error.response?.status === 401
      && !originalRequest._retry
      && !isRefreshRequest(originalRequest.url)
      && Boolean(localStorage.getItem('refreshToken'))

    if (shouldRetryWithRefresh) {
      originalRequest._retry = true
      try {
        const nextAccessToken = await refreshAccessToken()
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        clearAuthState()
        await router.push('/login')
        return Promise.reject(refreshError)
      }
    }

    if (error.response?.status === 401) {
      clearAuthState()
      await router.push('/login')
    } else {
      const msg = error.response?.data?.message || error.message || '请求失败'
      globalErrorHandler?.(msg)
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  getCaptchaMeta: () => api.get('/api/auth/captcha/meta'),
  issueCaptcha: (scene: 'login' | 'register' = 'login') =>
    api.get('/api/auth/captcha', { params: { scene } }),
  register: (data: {
    username: string
    nickname: string
    password: string
    captchaId?: string
    captchaCode?: string
  }) => api.post('/api/auth/register', data),
  login: (data: {
    username: string
    password: string
    captchaId?: string
    captchaCode?: string
  }) => api.post('/api/auth/login', data),
  refresh: (refreshToken: string) =>
    api.post('/api/auth/refresh', { refreshToken }),
  logout: (data: { refreshToken?: string; accessToken?: string }) =>
    api.post('/api/auth/logout', data)
}

export const chatApi = {
  createWsTicket: () => api.post('/api/chat/ws-ticket')
}

export const userApi = {
  getProfile: () => api.get('/api/user/me'),
  updateProfile: (data: any) => api.put('/api/user/me', data),
  search: (keyword: string) => api.get(`/api/user/search?keyword=${encodeURIComponent(keyword)}`),
  getUser: (id: string | number) => api.get(`/api/user/${id}`)
}

export const friendApi = {
  sendRequest: (toUserId: string | number, message: string) =>
    api.post('/api/friend/request', { toUserId, message }),
  getRequests: () => api.get('/api/friend/requests'),
  accept: (requestId: number) => api.post(`/api/friend/accept/${requestId}`),
  reject: (requestId: number) => api.post(`/api/friend/reject/${requestId}`),
  getList: () => api.get('/api/friend/list'),
  delete: (friendId: string | number) => api.delete(`/api/friend/${friendId}`)
}

export const groupApi = {
  create: (data: { groupName: string; groupAvatar?: string; notice?: string; memberIds: Array<string | number> }) =>
    api.post('/api/group', data),
  mine: () => api.get('/api/group/my'),
  getRequests: () => api.get('/api/group/requests'),
  joinRequest: (groupId: number | string, message?: string) =>
    api.post('/api/group/join-request', { groupId, message }),
  detail: (groupId: number | string) => api.get(`/api/group/${groupId}`),
  addMembers: (groupId: number | string, memberIds: Array<string | number>) =>
    api.post(`/api/group/${groupId}/members`, { memberIds }),
  inviteMembers: (groupId: number | string, memberIds: Array<string | number>, message?: string) =>
    api.post(`/api/group/${groupId}/invites`, { memberIds, message }),
  removeMember: (groupId: number | string, memberUserId: number | string) =>
    api.delete(`/api/group/${groupId}/members/${memberUserId}`),
  setAdmin: (groupId: number | string, memberUserId: number | string) =>
    api.put(`/api/group/${groupId}/admin/${memberUserId}`),
  removeAdmin: (groupId: number | string, memberUserId: number | string) =>
    api.delete(`/api/group/${groupId}/admin/${memberUserId}`),
  updateProfile: (groupId: number | string, data: { groupName: string; groupAvatar?: string }) =>
    api.put(`/api/group/${groupId}/profile`, data),
  updateNotice: (groupId: number | string, notice: string) =>
    api.put(`/api/group/${groupId}/notice`, { notice }),
  updatePreferences: (groupId: number | string, data: { groupRemark?: string; notificationMuted?: boolean }) =>
    api.put(`/api/group/${groupId}/preferences`, data),
  markNoticeRead: (groupId: number | string) =>
    api.post(`/api/group/${groupId}/notice/read`),
  getMedia: (groupId: number | string, params?: { mediaType?: string; keyword?: string; size?: number }) => {
    const searchParams = new URLSearchParams()
    if (params?.mediaType) {
      searchParams.set('mediaType', params.mediaType)
    }
    if (params?.keyword) {
      searchParams.set('keyword', params.keyword)
    }
    if (params?.size != null) {
      searchParams.set('size', String(params.size))
    }
    const suffix = searchParams.toString()
    return api.get(`/api/group/${groupId}/media${suffix ? `?${suffix}` : ''}`)
  },
  searchMessages: (groupId: number | string, keyword: string, size?: number) => {
    const searchParams = new URLSearchParams()
    searchParams.set('keyword', keyword)
    if (size != null) {
      searchParams.set('size', String(size))
    }
    return api.get(`/api/group/${groupId}/messages/search?${searchParams.toString()}`)
  },
  muteMember: (groupId: number | string, memberUserId: number | string, muteMinutes: number) =>
    api.post(`/api/group/${groupId}/mute/${memberUserId}`, { muteMinutes }),
  unmuteMember: (groupId: number | string, memberUserId: number | string) =>
    api.delete(`/api/group/${groupId}/mute/${memberUserId}`),
  dissolve: (groupId: number | string) => api.delete(`/api/group/${groupId}`),
  leaveGroup: (groupId: number | string) => api.post(`/api/group/${groupId}/leave`),
  acceptRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/accept`),
  rejectRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/reject`),
  transferOwner: (groupId: number | string, newOwnerId: number | string) =>
    api.post(`/api/group/${groupId}/transfer/${newOwnerId}`),
  search: (keyword: string) => api.get(`/api/group/search?keyword=${encodeURIComponent(keyword)}`)
}

export const fileApi = {
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/file/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  uploadImage: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/file/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  uploadFile: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/file/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  list: (keyword?: string) => {
    const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''
    return api.get(`/api/file/list${params}`)
  },
  getAccessUrl: (fileUrl: string) =>
    api.get(`/api/file/access-url?fileUrl=${encodeURIComponent(fileUrl)}`),
  delete: (id: number) => api.delete(`/api/file/${id}`)
}

export const blacklistApi = {
  add: (targetUserId: string | number) => api.post(`/api/blacklist/${targetUserId}`),
  remove: (targetUserId: string | number) => api.delete(`/api/blacklist/${targetUserId}`),
  list: () => api.get('/api/blacklist/list'),
  check: (targetUserId: string | number) => api.get(`/api/blacklist/check/${targetUserId}`)
}

export default api
