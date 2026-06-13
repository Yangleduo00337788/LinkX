<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">LinkX IM</div>
      <div class="auth-subtitle">安全稳定的即时通讯平台</div>

      <input class="auth-input" v-model="form.username" placeholder="用户名" @keyup.enter="handleLogin" />
      <input class="auth-input" type="password" v-model="form.password" placeholder="密码" @keyup.enter="handleLogin" />

      <button class="auth-btn primary" @click="handleLogin" :disabled="loading">
        {{ loading ? '登录中...' : '登 录' }}
      </button>
      <button class="auth-btn secondary" @click="router.push('/register')">注册新账号</button>

      <div class="auth-error">{{ error }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/client'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const error = ref('')
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    error.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res: any = await authApi.login(form)
    userStore.setLoginData(res.data)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>
