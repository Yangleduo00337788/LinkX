import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userId = ref(Number(localStorage.getItem('userId')) || 0)
  const nickname = ref(localStorage.getItem('nickname') || '')
  const avatar = ref(localStorage.getItem('avatar') || '')
  const username = ref(localStorage.getItem('username') || '')

  function setLoginData(data: any) {
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userId.value = data.userId
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
    userId.value = 0
    nickname.value = ''
    avatar.value = ''
    username.value = ''
    localStorage.clear()
  }

  return { token, refreshToken, userId, nickname, avatar, username, setLoginData, logout }
})
