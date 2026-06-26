<template>
  <div class="login-page">
    <n-card class="login-card">
      <div class="login-brand">
        <img :src="brandLogo" alt="LinkX" class="login-logo" />
        <h1 class="login-title">管理后台</h1>
        <p class="login-sub">桌面端 IM 运营与监管</p>
      </div>
      <n-form @submit.prevent="onSubmit">
        <n-form-item label="用户名">
          <n-input v-model:value="username" placeholder="管理员用户名" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" show-password-on="click" placeholder="密码" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" attr-type="submit">登录</n-button>
      </n-form>
      <p class="hint">默认账号见服务端启动日志（首次无管理员时自动创建）</p>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NForm, NFormItem, NInput, useMessage } from 'naive-ui'
import { useAdminStore } from '../stores/admin'
import brandLogo from '../assets/tray.png'

const username = ref('admin')
const password = ref('')
const loading = ref(false)
const message = useMessage()
const admin = useAdminStore()
const router = useRouter()
const route = useRoute()

async function onSubmit() {
  loading.value = true
  try {
    await admin.login(username.value, password.value)
    const redirect = (route.query.redirect as string) || '/dashboard'
    await router.replace(redirect)
  } catch (e: unknown) {
    const err = e as Error
    message.error(err.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 400px;
}
.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 20px;
}
.login-logo {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  margin-bottom: 12px;
  box-shadow: 0 6px 16px rgba(0, 168, 112, 0.25);
}
.login-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}
.login-sub {
  margin: 6px 0 0;
  font-size: 13px;
  color: #888;
}
.hint {
  margin: 16px 0 0;
  font-size: 12px;
  color: #888;
}
</style>