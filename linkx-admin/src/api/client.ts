import axios from 'axios'

const TOKEN_KEY = 'linkx.admin.token'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 30000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return res
  },
  (err) => {
    if (err.response?.status === 401) {
      clearAdminToken()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname)}`
      }
    }
    return Promise.reject(err)
  }
)

export function setAdminToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearAdminToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getAdminToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export const adminApi = {
  getCaptchaMeta() {
    return api.get('/api/admin/auth/captcha/meta')
  },
  issueCaptcha() {
    return api.get('/api/admin/auth/captcha')
  },
  login(body: {
    username: string
    password: string
    captchaId?: string
    captchaCode?: string
  }) {
    return api.post('/api/admin/auth/login', body)
  },
  profile() {
    return api.get('/api/admin/auth/profile')
  },
  dashboardStats(days: 7 | 30 = 7) {
    return api.get('/api/admin/dashboard/stats', { params: { days } })
  },
  listUsers(page: number, size: number, keyword?: string) {
    return api.get('/api/admin/users', { params: { page, size, keyword } })
  },
  setUserStatus(userId: number | string, status: number) {
    return api.put(`/api/admin/users/${userId}/status`, null, { params: { status } })
  },
  getUser(userId: number | string) {
    return api.get(`/api/admin/users/${userId}`)
  },
  kickUser(userId: number | string) {
    return api.post(`/api/admin/users/${userId}/kick`)
  },
  listUserFriends(userId: number | string, page: number, size: number) {
    return api.get(`/api/admin/users/${userId}/friends`, { params: { page, size } })
  },
  listFriendRequests(page: number, size: number, status?: number) {
    return api.get('/api/admin/friend-requests', { params: { page, size, status } })
  },
  listGroupRequests(page: number, size: number, status?: number, groupId?: number) {
    return api.get('/api/admin/group-requests', { params: { page, size, status, groupId } })
  },
  getGroup(groupId: number) {
    return api.get(`/api/admin/groups/${groupId}`)
  },
  muteGroupMember(groupId: number, userId: number, muteMinutes: number) {
    return api.put(`/api/admin/groups/${groupId}/members/${userId}/mute`, null, {
      params: { muteMinutes }
    })
  },
  unmuteGroupMember(groupId: number, userId: number) {
    return api.put(`/api/admin/groups/${groupId}/members/${userId}/unmute`)
  },
  listGroups(page: number, size: number, keyword?: string) {
    return api.get('/api/admin/groups', { params: { page, size, keyword } })
  },
  dissolveGroup(groupId: number) {
    return api.delete(`/api/admin/groups/${groupId}`)
  },
  listMessages(page: number, size: number, sessionId?: number, fromUserId?: number) {
    return api.get('/api/admin/messages', { params: { page, size, sessionId, fromUserId } })
  },
  deleteMessage(messageId: number) {
    return api.delete(`/api/admin/messages/${messageId}`)
  },
  listFiles(page: number, size: number, userId?: number) {
    return api.get('/api/admin/files', { params: { page, size, userId } })
  },
  deleteFile(fileId: number) {
    return api.delete(`/api/admin/files/${fileId}`)
  },
  listBlacklist(page: number, size: number, userId?: number) {
    return api.get('/api/admin/blacklist', { params: { page, size, userId } })
  },
  removeBlacklist(id: number) {
    return api.delete(`/api/admin/blacklist/${id}`)
  },
  listAuditLogs(page: number, size: number, action?: string) {
    return api.get('/api/admin/audit-logs', { params: { page, size, action } })
  },
  listReleases(page: number, size: number, platform?: string) {
    return api.get('/api/admin/releases', { params: { page, size, platform } })
  },
  createRelease(body: {
    platform: string
    version: string
    downloadUrl?: string
    releaseNotes?: string
    forceUpdate?: boolean
    published?: boolean
  }) {
    return api.post('/api/admin/releases', body)
  },
  setReleasePublished(id: number, published: boolean) {
    return api.put(`/api/admin/releases/${id}/published`, null, { params: { published } })
  },
  listLoginLogs(page: number, size: number, username?: string, userId?: number, loginStatus?: number) {
    return api.get('/api/admin/login-logs', { params: { page, size, username, userId, loginStatus } })
  },
  listReports(page: number, size: number, status?: number, targetType?: string) {
    return api.get('/api/admin/reports', { params: { page, size, status, targetType } })
  },
  handleReport(
    reportId: number,
    body: { status: number; resolution?: string; resolutionNote?: string; notifyReporter?: boolean }
  ) {
    return api.put(`/api/admin/reports/${reportId}/handle`, body)
  },
  listSensitiveWords(page: number, size: number, keyword?: string, enabled?: number) {
    return api.get('/api/admin/sensitive-words', { params: { page, size, keyword, enabled } })
  },
  createSensitiveWord(body: {
    word: string
    matchMode?: number
    category?: string
    action?: number
    enabled?: number
  }) {
    return api.post('/api/admin/sensitive-words', body)
  },
  updateSensitiveWord(
    id: number,
    body: { word?: string; matchMode?: number; category?: string; action?: number; enabled?: number }
  ) {
    return api.put(`/api/admin/sensitive-words/${id}`, body)
  },
  deleteSensitiveWord(id: number) {
    return api.delete(`/api/admin/sensitive-words/${id}`)
  },
  refreshSensitiveWordCache() {
    return api.post('/api/admin/sensitive-words/refresh-cache')
  },
  listSensitiveHits(page: number, size: number, userId?: number) {
    return api.get('/api/admin/sensitive-words/hits', { params: { page, size, userId } })
  },
  listAdmins(page: number, size: number, keyword?: string) {
    return api.get('/api/admin/admins', { params: { page, size, keyword } })
  },
  createAdmin(body: { username: string; password: string; displayName: string; role: string }) {
    return api.post('/api/admin/admins', body)
  },
  setAdminStatus(adminId: number, status: number) {
    return api.put(`/api/admin/admins/${adminId}/status`, null, { params: { status } })
  },
  resetAdminPassword(adminId: number, newPassword: string) {
    return api.put(`/api/admin/admins/${adminId}/password`, null, { params: { newPassword } })
  }
}