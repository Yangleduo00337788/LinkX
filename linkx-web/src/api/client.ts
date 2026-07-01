/**
 * 统一封装 HTTP 客户端、认证续期和各业务 API。
 */
import axios from 'axios'  // 行注：引入 axios 模块
import { API_BASE_URL } from '../config/env'  // 行注：引入 API_BASE_URL 能力
import router from '../router'  // 行注：引入 router 模块
import { useUserStore } from '../stores/user'  // 行注：引入 useUserStore 能力

export { API_BASE_URL, WS_BASE_URL } from '../config/env'  // 行注：导出当前能力

const AUTH_STORAGE_KEYS = ['token', 'refreshToken', 'sessionId', 'userId', 'nickname', 'avatar', 'username'] as const

let globalErrorHandler: ((message: string) => void) | null = null  // 行注：初始化 globalErrorHandler 变量
let refreshTokenPromise: Promise<string> | null = null  // 行注：初始化 refreshTokenPromise 方法

// 允许页面层注册一个全局错误提示函数，统一消费接口异常消息。
export function setGlobalErrorHandler(handler: (message: string) => void) {  // 行注：导出当前能力
  globalErrorHandler = handler  // 行注：更新 globalErrorHandler 值
}  // 行注：结束当前代码块

// 登录或刷新 token 成功后，统一把认证结果回写到用户状态仓库。
function applyAuthResponse(data: any) {  // 行注：定义 applyAuthResponse 方法
  useUserStore().setLoginData(data)  // 行注：调用 setLoginData 方法
}  // 行注：结束当前代码块

// 当 token 失效或刷新失败时，彻底清理本地登录痕迹，避免脏状态残留。
function clearAuthState() {  // 行注：定义 clearAuthState 方法
  try {  // 行注：尝试执行可能失败的逻辑
    useUserStore().logout()  // 行注：调用 logout 方法
  } catch {  // 行注：捕获并处理异常
    for (const key of AUTH_STORAGE_KEYS) {  // 行注：遍历当前数据集合
      localStorage.removeItem(key)  // 行注：移除本地缓存
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

// 避免拦截器对 refresh 自己再次触发刷新逻辑，导致递归请求。
function isRefreshRequest(url?: string) {  // 行注：定义 isRefreshRequest 方法
  return typeof url === 'string' && url.includes('/api/auth/refresh')  // 行注：返回当前结果
}  // 行注：结束当前代码块

// 通过单例 Promise 合并并发刷新请求，防止多个 401 同时触发重复刷新。
async function refreshAccessToken() {  // 行注：定义异步 refreshAccessToken 方法
  const storedRefreshToken = localStorage.getItem('refreshToken')  // 行注：初始化 storedRefreshToken 变量
  if (!storedRefreshToken) {  // 行注：判断当前条件是否成立
    throw new Error('登录已过期')  // 行注：抛出异常并终止当前流程
  }  // 行注：结束当前代码块
  if (!refreshTokenPromise) {  // 行注：判断当前条件是否成立
    refreshTokenPromise = axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken: storedRefreshToken }, {  // 行注：更新 refreshTokenPromise 值
      timeout: 10000  // 行注：设置 timeout 配置项
    }).then(response => {  // 行注：调用 then 方法
      const payload = response.data  // 行注：初始化 payload 变量
      if (!payload || payload.code !== 200 || !payload.data?.accessToken) {  // 行注：判断当前条件是否成立
        throw new Error(payload?.message || '登录续期失败')  // 行注：抛出异常并终止当前流程
      }  // 行注：结束当前代码块
      applyAuthResponse(payload.data)  // 行注：调用 applyAuthResponse 方法
      return String(payload.data.accessToken)  // 行注：返回当前结果
    }).finally(() => {  // 行注：调用 finally 方法
      refreshTokenPromise = null  // 行注：更新 refreshTokenPromise 值
    })  // 行注：结束当前调用配置
  }  // 行注：结束当前代码块
  return refreshTokenPromise  // 行注：返回当前结果
}  // 行注：结束当前代码块

const api = axios.create({  // 行注：开始解构当前返回值
  baseURL: API_BASE_URL,  // 行注：设置 baseURL 配置项
  timeout: 10000  // 行注：设置 timeout 配置项
})  // 行注：结束当前调用配置

// 所有业务请求在发出前自动附带 Bearer token。
api.interceptors.request.use(config => {  // 行注：开始当前逻辑块
  const token = localStorage.getItem('token')  // 行注：初始化 token 变量
  if (token) {  // 行注：判断当前条件是否成立
    config.headers.Authorization = `Bearer ${token}`  // 行注：更新 config.headers.Authorization 值
  }  // 行注：结束当前代码块
  return config  // 行注：返回当前结果
})  // 行注：结束当前调用配置

// 统一处理业务码、401 自动续期和全局错误提示。
api.interceptors.response.use(  // 行注：补充当前表达式
  res => {  // 行注：开始当前逻辑块
    const payload = res.data  // 行注：初始化 payload 变量
    if (payload && typeof payload === 'object' && 'code' in payload && payload.code !== 200) {  // 行注：判断当前条件是否成立
      const error: any = new Error(payload.message || '请求失败')  // 行注：初始化 error 变量
      error.response = {  // 行注：更新 error.response 值
        ...res,  // 行注：补充当前配置项
        data: payload  // 行注：设置 data 配置项
      }  // 行注：结束当前代码块
      return Promise.reject(error)  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return payload  // 行注：返回当前结果
  },  // 行注：补充当前配置项
  async error => {  // 行注：开始当前逻辑块
    const originalRequest = error.config || {}  // 行注：初始化 originalRequest 变量
    const shouldRetryWithRefresh = error.response?.status === 401  // 行注：初始化 shouldRetryWithRefresh 状态
      && !originalRequest._retry  // 行注：补充当前表达式
      && !isRefreshRequest(originalRequest.url)  // 行注：执行当前调用逻辑
      && Boolean(localStorage.getItem('refreshToken'))  // 行注：调用 getItem 方法

    if (shouldRetryWithRefresh) {  // 行注：判断当前条件是否成立
      originalRequest._retry = true  // 行注：更新 originalRequest._retry 值
      try {  // 行注：尝试执行可能失败的逻辑
        const nextAccessToken = await refreshAccessToken()  // 行注：接收 nextAccessToken 异步结果
        originalRequest.headers = originalRequest.headers || {}  // 行注：更新 originalRequest.headers 值
        originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`  // 行注：更新 originalRequest.headers.Authorization 值
        return api(originalRequest)  // 行注：返回当前结果
      } catch (refreshError) {  // 行注：捕获并处理异常
        clearAuthState()  // 行注：调用 clearAuthState 方法
        await router.push('/login')  // 行注：跳转到目标路由
        return Promise.reject(refreshError)  // 行注：返回当前结果
      }  // 行注：结束当前代码块
    }  // 行注：结束当前代码块

    if (error.response?.status === 401) {  // 行注：判断当前条件是否成立
      clearAuthState()  // 行注：调用 clearAuthState 方法
      await router.push('/login')  // 行注：跳转到目标路由
    } else {  // 行注：开始当前逻辑块
      const msg = error.response?.data?.message || error.message || '请求失败'  // 行注：初始化 msg 变量
      globalErrorHandler?.(msg)  // 行注：执行当前调用逻辑
    }  // 行注：结束当前代码块
    return Promise.reject(error)  // 行注：返回当前结果
  }  // 行注：结束当前代码块
)  // 行注：结束当前调用

export const authApi = {  // 行注：导出当前能力
  getCaptchaMeta: () => api.get('/api/auth/captcha/meta'),  // 行注：传入 getCaptchaMeta 回调
  issueCaptcha: (scene: 'login' | 'register' = 'login') =>  // 行注：设置 issueCaptcha 配置项
    api.get('/api/auth/captcha', { params: { scene } }),  // 行注：调用 get 方法
  register: (data: {  // 行注：设置 register 配置项
    username: string  // 行注：设置 username 配置项
    nickname: string  // 行注：设置 nickname 配置项
    password: string  // 行注：设置 password 配置项
    captchaId?: string  // 行注：补充当前表达式
    captchaCode?: string  // 行注：补充当前表达式
  }) => api.post('/api/auth/register', data),  // 行注：调用 post 方法
  login: (data: {  // 行注：设置 login 配置项
    username: string  // 行注：设置 username 配置项
    password: string  // 行注：设置 password 配置项
    captchaId?: string  // 行注：补充当前表达式
    captchaCode?: string  // 行注：补充当前表达式
  }) => api.post('/api/auth/login', data),  // 行注：调用 post 方法
  refresh: (refreshToken: string) =>  // 行注：设置 refresh 配置项
    api.post('/api/auth/refresh', { refreshToken }),  // 行注：调用 post 方法
  logout: (data: { refreshToken?: string; accessToken?: string }) =>  // 行注：设置 logout 配置项
    api.post('/api/auth/logout', data),  // 行注：调用 post 方法
  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    api.post('/api/auth/change-password', data),
  requestPasswordReset: (data: { username: string; email: string }) =>
    api.post('/api/auth/password-reset/request', data),
  confirmPasswordReset: (data: { resetToken: string; newPassword: string }) =>
    api.post('/api/auth/password-reset/confirm', data),
  listSessions: (currentSessionId?: string) =>
    api.get('/api/auth/sessions', {
      params: currentSessionId ? { currentSessionId } : undefined,
      headers: currentSessionId ? { 'X-Session-Id': currentSessionId } : undefined
    }),
  revokeSession: (sessionId: string, currentSessionId?: string) =>
    api.delete(`/api/auth/sessions/${sessionId}`, {
      headers: currentSessionId ? { 'X-Session-Id': currentSessionId } : undefined
    }),
  revokeOtherSessions: (currentSessionId?: string) =>
    api.post('/api/auth/sessions/revoke-others', null, {
      params: currentSessionId ? { currentSessionId } : undefined,
      headers: currentSessionId ? { 'X-Session-Id': currentSessionId } : undefined
    })
}

/**
 * 聊天 REST（与 ChatController 对齐）。
 * 桌面端主路径仍为 WebSocket 命令；REST 供重试、无 WS 或第三方集成使用。
 */
export const chatApi = {
  createWsTicket: () => api.post('/api/chat/ws-ticket'),
  getSessions: () => api.get('/api/chat/sessions'),
  getHistory: (params: {
    targetId: number | string
    sessionType?: number
    page?: number
    size?: number
  }) => api.get('/api/chat/history', { params }),
  sendMessage: (data: {
    toUserId: number | string
    content: string
    msgType?: number
    sessionType?: number
    clientMessageId?: string
    mentionAll?: boolean
    mentionUserIds?: Array<number | string>
  }) => api.post('/api/chat/send', data),
  sendFileMessage: (data: {
    toUserId: number | string
    fileId: number
    msgType?: number
    sessionType?: number
    clientMessageId?: string
  }) => api.post('/api/chat/send-file', data),
  markAsRead: (targetId: number | string, sessionType = 1) =>
    api.post(`/api/chat/read/${targetId}`, null, { params: { sessionType } }),
  recallMessage: (messageId: number | string) => api.post(`/api/chat/recall/${messageId}`),
  updateSessionSettings: (data: {
    targetId: number | string
    sessionType?: number
    pinned?: boolean
    notificationMuted?: boolean
    sessionRemark?: string
  }) => api.put('/api/chat/session/settings', data),
  clearChatHistory: (targetId: number | string, sessionType = 1) =>
    api.post('/api/chat/session/clear-history', null, { params: { targetId, sessionType } }),
  searchMessages: (keyword: string, params?: { sessionType?: number; size?: number }) =>
    api.get('/api/chat/messages/search', { params: { keyword, ...params } })
}

export const userApi = {  // 行注：导出当前能力
  getProfile: () => api.get('/api/user/me'),  // 行注：传入 getProfile 回调
  updateProfile: (data: any) => api.put('/api/user/me', data),  // 行注：设置 updateProfile 配置项
  search: (keyword: string) => api.get(`/api/user/search?keyword=${encodeURIComponent(keyword)}`),  // 行注：设置 search 配置项
  getUser: (id: string | number) => api.get(`/api/user/${id}`)  // 行注：设置 getUser 配置项
}  // 行注：结束当前代码块

export const friendApi = {  // 行注：导出当前能力
  sendRequest: (toUserId: string | number, message: string) =>  // 行注：设置 sendRequest 配置项
    api.post('/api/friend/request', { toUserId, message }),  // 行注：调用 post 方法
  getRequests: () => api.get('/api/friend/requests'),  // 行注：传入 getRequests 回调
  accept: (requestId: number) => api.post(`/api/friend/accept/${requestId}`),  // 行注：设置 accept 配置项
  reject: (requestId: number) => api.post(`/api/friend/reject/${requestId}`),  // 行注：设置 reject 配置项
  getList: () => api.get('/api/friend/list'),  // 行注：传入 getList 回调
  delete: (friendId: string | number) => api.delete(`/api/friend/${friendId}`),  // 行注：设置 delete 配置项
  updateRemark: (friendId: string | number, remark: string) =>
    api.put(`/api/friend/${friendId}/remark`, { remark })
}  // 行注：结束当前代码块

export const groupApi = {  // 行注：导出当前能力
  create: (data: { groupName: string; groupAvatar?: string; notice?: string; memberIds: Array<string | number> }) =>  // 行注：设置 create 配置项
    api.post('/api/group', data),  // 行注：调用 post 方法
  mine: () => api.get('/api/group/my'),  // 行注：传入 mine 回调
  getRequests: () => api.get('/api/group/requests'),  // 行注：传入 getRequests 回调
  joinRequest: (groupId: number | string, message?: string) =>  // 行注：设置 joinRequest 配置项
    api.post('/api/group/join-request', { groupId, message }),  // 行注：调用 post 方法
  detail: (groupId: number | string) => api.get(`/api/group/${groupId}`),  // 行注：设置 detail 配置项
  addMembers: (groupId: number | string, memberIds: Array<string | number>) =>  // 行注：设置 addMembers 配置项
    api.post(`/api/group/${groupId}/members`, { memberIds }),  // 行注：调用 post 方法
  inviteMembers: (groupId: number | string, memberIds: Array<string | number>, message?: string) =>  // 行注：设置 inviteMembers 配置项
    api.post(`/api/group/${groupId}/invites`, { memberIds, message }),  // 行注：调用 post 方法
  removeMember: (groupId: number | string, memberUserId: number | string) =>  // 行注：设置 removeMember 配置项
    api.delete(`/api/group/${groupId}/members/${memberUserId}`),  // 行注：调用 delete 方法
  setAdmin: (groupId: number | string, memberUserId: number | string) =>  // 行注：设置 setAdmin 配置项
    api.put(`/api/group/${groupId}/admin/${memberUserId}`),  // 行注：调用 put 方法
  removeAdmin: (groupId: number | string, memberUserId: number | string) =>  // 行注：设置 removeAdmin 配置项
    api.delete(`/api/group/${groupId}/admin/${memberUserId}`),  // 行注：调用 delete 方法
  updateProfile: (groupId: number | string, data: { groupName: string; groupAvatar?: string }) =>  // 行注：设置 updateProfile 配置项
    api.put(`/api/group/${groupId}/profile`, data),  // 行注：调用 put 方法
  updateNotice: (groupId: number | string, notice: string) =>  // 行注：设置 updateNotice 配置项
    api.put(`/api/group/${groupId}/notice`, { notice }),  // 行注：调用 put 方法
  updatePreferences: (
    groupId: number | string,
    data: { groupRemark?: string; notificationMuted?: boolean; memberCardName?: string }
  ) => api.put(`/api/group/${groupId}/preferences`, data),
  listNotices: (groupId: number | string) => api.get(`/api/group/${groupId}/notices`),
  createNotice: (groupId: number | string, data: { content: string; pinned?: boolean }) =>
    api.post(`/api/group/${groupId}/notices`, data),
  pinNotice: (groupId: number | string, noticeId: number | string, pinned = true) =>
    api.put(`/api/group/${groupId}/notices/${noticeId}/pin`, null, { params: { pinned } }),
  listHighlights: (groupId: number | string) => api.get(`/api/group/${groupId}/highlights`),
  addHighlight: (groupId: number | string, data: { messageId: number | string; title?: string }) =>
    api.post(`/api/group/${groupId}/highlights`, data),
  removeHighlight: (groupId: number | string, highlightId: number | string) =>
    api.delete(`/api/group/${groupId}/highlights/${highlightId}`),
  markNoticeRead: (groupId: number | string) =>  // 行注：设置 markNoticeRead 配置项
    api.post(`/api/group/${groupId}/notice/read`),  // 行注：调用 post 方法
  getMedia: (groupId: number | string, params?: { mediaType?: string; keyword?: string; size?: number }) => {  // 行注：设置 getMedia 配置项
    const searchParams = new URLSearchParams()  // 行注：初始化 searchParams 变量
    if (params?.mediaType) {  // 行注：判断当前条件是否成立
      searchParams.set('mediaType', params.mediaType)  // 行注：调用 set 方法
    }  // 行注：结束当前代码块
    if (params?.keyword) {  // 行注：判断当前条件是否成立
      searchParams.set('keyword', params.keyword)  // 行注：调用 set 方法
    }  // 行注：结束当前代码块
    if (params?.size != null) {  // 行注：判断当前条件是否成立
      searchParams.set('size', String(params.size))  // 行注：调用 set 方法
    }  // 行注：结束当前代码块
    const suffix = searchParams.toString()  // 行注：初始化 suffix 变量
    return api.get(`/api/group/${groupId}/media${suffix ? `?${suffix}` : ''}`)  // 行注：返回当前结果
  },  // 行注：补充当前配置项
  searchMessages: (groupId: number | string, keyword: string, size?: number) => {  // 行注：设置 searchMessages 配置项
    const searchParams = new URLSearchParams()  // 行注：初始化 searchParams 变量
    searchParams.set('keyword', keyword)  // 行注：调用 set 方法
    if (size != null) {  // 行注：判断当前条件是否成立
      searchParams.set('size', String(size))  // 行注：调用 set 方法
    }  // 行注：结束当前代码块
    return api.get(`/api/group/${groupId}/messages/search?${searchParams.toString()}`)  // 行注：返回当前结果
  },  // 行注：补充当前配置项
  muteMember: (groupId: number | string, memberUserId: number | string, muteMinutes: number) =>  // 行注：设置 muteMember 配置项
    api.post(`/api/group/${groupId}/mute/${memberUserId}`, { muteMinutes }),  // 行注：调用 post 方法
  unmuteMember: (groupId: number | string, memberUserId: number | string) =>  // 行注：设置 unmuteMember 配置项
    api.delete(`/api/group/${groupId}/mute/${memberUserId}`),  // 行注：调用 delete 方法
  dissolve: (groupId: number | string) => api.delete(`/api/group/${groupId}`),  // 行注：设置 dissolve 配置项
  leaveGroup: (groupId: number | string) => api.post(`/api/group/${groupId}/leave`),  // 行注：设置 leaveGroup 配置项
  acceptRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/accept`),  // 行注：设置 acceptRequest 配置项
  rejectRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/reject`),  // 行注：设置 rejectRequest 配置项
  transferOwner: (groupId: number | string, newOwnerId: number | string) =>  // 行注：设置 transferOwner 配置项
    api.post(`/api/group/${groupId}/transfer/${newOwnerId}`),  // 行注：调用 post 方法
  search: (keyword: string) => api.get(`/api/group/search?keyword=${encodeURIComponent(keyword)}`)  // 行注：设置 search 配置项
}  // 行注：结束当前代码块

export const fileApi = {  // 行注：导出当前能力
  uploadAvatar: (file: File) => {  // 行注：设置 uploadAvatar 配置项
    const formData = new FormData()  // 行注：初始化 formData 变量
    formData.append('file', file)  // 行注：调用 append 方法
    return api.post('/api/file/upload/avatar', formData, {  // 行注：返回当前结果
      headers: { 'Content-Type': 'multipart/form-data' }  // 行注：设置 headers 配置项
    })  // 行注：结束当前调用配置
  },  // 行注：补充当前配置项
  uploadImage: (file: File) => {  // 行注：设置 uploadImage 配置项
    const formData = new FormData()  // 行注：初始化 formData 变量
    formData.append('file', file)  // 行注：调用 append 方法
    return api.post('/api/file/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    })
  },
  uploadFile: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/file/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 300000
    })
  },  // 行注：补充当前配置项
  list: (keyword?: string) => {  // 行注：设置 list 配置项
    const params = keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''  // 行注：初始化 params 变量
    return api.get(`/api/file/list${params}`)  // 行注：返回当前结果
  },  // 行注：补充当前配置项
  getAccessUrl: (fileUrl: string) =>  // 行注：设置 getAccessUrl 配置项
    api.get(`/api/file/access-url?fileUrl=${encodeURIComponent(fileUrl)}`),  // 行注：调用 get 方法
  delete: (id: number) => api.delete(`/api/file/${id}`)  // 行注：设置 delete 配置项
}  // 行注：结束当前代码块

export const blacklistApi = {  // 行注：导出当前能力
  add: (targetUserId: string | number) => api.post(`/api/blacklist/${targetUserId}`),  // 行注：设置 add 配置项
  remove: (targetUserId: string | number) => api.delete(`/api/blacklist/${targetUserId}`),  // 行注：设置 remove 配置项
  list: () => api.get('/api/blacklist/list'),  // 行注：传入 list 回调
  check: (targetUserId: string | number) => api.get(`/api/blacklist/check/${targetUserId}`)  // 行注：设置 check 配置项
}

export const reportApi = {
  submit: (body: {
    targetType: string
    targetId: string
    reasonCategory: string
    reasonDetail?: string
  }) => api.post('/api/reports', body),
  mine: (page = 1, size = 20) => api.get('/api/reports/mine', { params: { page, size } })
}

export const releaseApi = {
  latest: (platform: string) =>
    api.get('/api/releases/latest', { params: { platform } })
}

/** 会话置顶/免打扰/备注：统一走 ChatController，与 WebSocket 会话推送一致 */
export const sessionApi = {
  updatePreferences: (
    targetId: number | string,
    sessionType: number,
    body: { sessionRemark?: string; pinned?: boolean; notificationMuted?: boolean }
  ) =>
    chatApi.updateSessionSettings({
      targetId,
      sessionType,
      pinned: body.pinned,
      notificationMuted: body.notificationMuted,
      sessionRemark: body.sessionRemark
    }),
  listDrafts: () => api.get('/api/user/chat-drafts'),
  saveDraft: (body: {
    targetId: number | string
    sessionType: number
    draftContent?: string
  }) => api.put('/api/user/chat-drafts', body),
  deleteDraft: (targetId: number | string, sessionType: number) =>
    api.delete('/api/user/chat-drafts', { params: { targetId, sessionType } })
}

/** 与 UserNotificationController（/api/notifications）对齐 */
export const notificationApi = {
  list: (page = 1, size = 20) =>
    api.get('/api/notifications', { params: { page, size } }),
  unreadCount: () => api.get('/api/notifications/unread-count'),
  markRead: (id: number | string) => api.put(`/api/notifications/${id}/read`),
  markAllRead: () => api.put('/api/notifications/read-all')
}

export default api  // 行注：导出默认组件或配置
