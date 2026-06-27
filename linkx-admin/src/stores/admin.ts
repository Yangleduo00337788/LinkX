import { defineStore } from 'pinia'
import { ref } from 'vue'
import { adminApi, clearAdminToken, getAdminToken, setAdminToken } from '../api/client'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(getAdminToken() || '')
  const displayName = ref('')
  const role = ref('')

  async function login(
    username: string,
    password: string,
    captcha?: { captchaId?: string; captchaCode?: string }
  ) {
    const res = await adminApi.login({
      username,
      password,
      captchaId: captcha?.captchaId,
      captchaCode: captcha?.captchaCode
    })
    const d = res.data.data
    token.value = d.accessToken
    displayName.value = d.displayName
    role.value = d.role
    setAdminToken(d.accessToken)
  }

  async function fetchProfile() {
    if (!token.value) return
    const res = await adminApi.profile()
    const d = res.data.data
    displayName.value = d.displayName
    role.value = d.role
  }

  function logout() {
    token.value = ''
    displayName.value = ''
    role.value = ''
    clearAdminToken()
  }

  return { token, displayName, role, login, fetchProfile, logout }
})