<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-logo">LinkX IM</div>
      <div class="auth-subtitle">创建新账号</div>

      <input class="auth-input" v-model="form.username" placeholder="用户名（3-50位）" />
      <input class="auth-input" v-model="form.nickname" placeholder="昵称" />
      <input class="auth-input" type="password" v-model="form.password" placeholder="密码（6位以上）" />
      <input class="auth-input" type="password" v-model="confirmPassword" placeholder="确认密码" />

      <button class="auth-btn primary" @click="handleRegister" :disabled="loading">
        {{ loading ? '注册中...' : '注 册' }}
      </button>
      <button class="auth-btn secondary" @click="router.push('/login')">返回登录</button>

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
const confirmPassword = ref('')
const form = reactive({ username: '', nickname: '', password: '' })

async function handleRegister() {
  if (!form.username || !form.nickname || !form.password) {
    error.value = '请填写所有字段'
    return
  }
  if (form.username.length < 3) {
    error.value = '用户名至少3位'
    return
  }
  if (form.password.length < 6) {
    error.value = '密码至少6位'
    return
  }
  if (form.password !== confirmPassword.value) {
    error.value = '两次密码不一致'
    return
  }

  loading.value = true
  error.value = ''
  try {
    const res: any = await authApi.register(form)
    userStore.setLoginData(res.data)
    router.push('/')
  } catch (e: any) {
    error.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>
