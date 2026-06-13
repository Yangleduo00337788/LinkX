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
  res => res.data,
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
  getUser: (id: number) => api.get(`/api/user/${id}`)
}

export const friendApi = {
  sendRequest: (toUserId: number, message: string) =>
    api.post('/api/friend/request', { toUserId, message }),
  getRequests: () => api.get('/api/friend/requests'),
  accept: (requestId: number) => api.post(`/api/friend/accept/${requestId}`),
  reject: (requestId: number) => api.post(`/api/friend/reject/${requestId}`),
  getList: () => api.get('/api/friend/list'),
  delete: (friendId: number) => api.delete(`/api/friend/${friendId}`)
}

export const chatApi = {
  send: (toUserId: number, content: string) =>
    api.post('/api/chat/send', { toUserId, content, msgType: 0 }),
  getHistory: (targetId: number) =>
    api.get(`/api/chat/history?targetId=${targetId}`),
  getSessions: () => api.get('/api/chat/sessions'),
  markRead: (targetId: number) => api.post(`/api/chat/read/${targetId}`)
}

export const fileApi = {
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/api/file/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export default api
