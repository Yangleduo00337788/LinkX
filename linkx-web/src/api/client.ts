import axios from 'axios'
import router from '../router'

const api = axios.create({
  baseURL: 'http://localhost:8080',
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
  error => {
    if (error.response?.status === 401) {
      localStorage.clear()
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export const authApi = {
  register: (data: { username: string; nickname: string; password: string }) =>
    api.post('/api/auth/register', data),
  login: (data: { username: string; password: string }) =>
    api.post('/api/auth/login', data),
  refresh: (refreshToken: string) =>
    api.post(`/api/auth/refresh?refreshToken=${refreshToken}`)
}

export const userApi = {
  getProfile: () => api.get('/api/user/me'),
  updateProfile: (data: any) => api.put('/api/user/me', data),
  search: (keyword: string) => api.get(`/api/user/search?keyword=${keyword}`),
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

export const chatApi = {
  send: (toUserId: string | number, content: string, sessionType = 1) =>
    api.post('/api/chat/send', { toUserId, content, msgType: 0, sessionType }),
  sendFile: (toUserId: string | number, fileId: number, msgType: number, sessionType = 1) =>
    api.post(`/api/chat/send-file?toUserId=${toUserId}&fileId=${fileId}&msgType=${msgType}&sessionType=${sessionType}`),
  getHistory: (targetId: string | number, sessionType = 1) =>
    api.get(`/api/chat/history?targetId=${targetId}&sessionType=${sessionType}`),
  getSessions: () => api.get('/api/chat/sessions'),
  markRead: (targetId: string | number, sessionType = 1) => api.post(`/api/chat/read/${targetId}?sessionType=${sessionType}`),
  recall: (messageId: string | number) => api.post(`/api/chat/recall/${messageId}`)
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
  muteMember: (groupId: number | string, memberUserId: number | string, muteMinutes: number) =>
    api.post(`/api/group/${groupId}/mute/${memberUserId}`, { muteMinutes }),
  unmuteMember: (groupId: number | string, memberUserId: number | string) =>
    api.delete(`/api/group/${groupId}/mute/${memberUserId}`),
  dissolve: (groupId: number | string) => api.delete(`/api/group/${groupId}`),
  leaveGroup: (groupId: number | string) => api.post(`/api/group/${groupId}/leave`),
  acceptRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/accept`),
  rejectRequest: (requestId: number | string) => api.post(`/api/group/requests/${requestId}/reject`),
  transferOwner: (groupId: number | string, newOwnerId: number | string) =>
    api.post(`/api/group/${groupId}/transfer/${newOwnerId}`)
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
    return api.post('/api/file/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  list: (keyword?: string) => {
    const params = keyword ? `?keyword=${keyword}` : ''
    return api.get(`/api/file/list${params}`)
  },
  delete: (id: number) => api.delete(`/api/file/${id}`)
}

export const blacklistApi = {
  add: (targetUserId: string | number) => api.post(`/api/blacklist/${targetUserId}`),
  remove: (targetUserId: string | number) => api.delete(`/api/blacklist/${targetUserId}`),
  list: () => api.get('/api/blacklist/list'),
  check: (targetUserId: string | number) => api.get(`/api/blacklist/check/${targetUserId}`)
}

export default api
