import { defineStore } from 'pinia'
import { ref } from 'vue'

const AUTH_STORAGE_KEYS = ['token', 'refreshToken', 'userId', 'nickname', 'avatar', 'username'] as const

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  const username = ref(localStorage.getItem('username') || '')

  function setLoginData(data: any) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userId.value = String(data.userId)
    nickname.value = data.nickname
    avatar.value = data.avatar || ''
    username.value = data.username

    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', String(data.userId))
    localStorage.setItem('nickname', data.nickname)
    localStorage.setItem('avatar', data.avatar || '')
    localStorage.setItem('username', data.username)
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userId.value = ''
    nickname.value = ''
    avatar.value = ''
    username.value = ''
    for (const key of AUTH_STORAGE_KEYS) {
      localStorage.removeItem(key)
    }
  }

  return { token, refreshToken, userId, nickname, avatar, username, setLoginData, logout }
})
