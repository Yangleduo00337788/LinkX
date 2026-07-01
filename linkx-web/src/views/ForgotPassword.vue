<template>
  <div class="auth-page">
    <TitleBar />
    <div class="auth-body compact">
      <div class="auth-card">
        <h2>找回密码</h2>
        <p class="hint">输入注册时的用户名与已绑定邮箱。开发环境可能直接在页面返回重置令牌。</p>
        <label class="field">
          <span>用户名</span>
          <input v-model="username" autocomplete="username" />
        </label>
        <label class="field">
          <span>邮箱</span>
          <input v-model="email" type="email" autocomplete="email" />
        </label>
        <button type="button" class="primary" :disabled="loading" @click="submit">
          {{ loading ? '提交中…' : '申请重置' }}
        </button>
        <p v-if="devToken" class="dev-token">
          重置令牌（联调用）：<code>{{ devToken }}</code>
          <router-link :to="{ name: 'ResetPassword', query: { token: devToken } }">去设置新密码</router-link>
        </p>
        <router-link to="/login" class="link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import TitleBar from '../components/TitleBar.vue'
import { authApi } from '../api/client'

const message = useMessage()
const username = ref('')
const email = ref('')
const loading = ref(false)
const devToken = ref('')

async function submit() {
  if (!username.value.trim() || !email.value.trim()) {
    message.warning('请填写用户名和邮箱')
    return
  }
  loading.value = true
  devToken.value = ''
  try {
    const res: any = await authApi.requestPasswordReset({
      username: username.value.trim(),
      email: email.value.trim()
    })
    message.success(res?.message || res?.data?.message || '已提交')
    const token = res?.data?.resetToken ?? res?.resetToken
    if (token) {
      devToken.value = String(token)
    }
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '申请失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-body.compact {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 40px);
}
.auth-card {
  width: min(420px, 92vw);
  padding: 28px;
  border-radius: 12px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
}
.auth-card h2 {
  margin: 0 0 8px;
}
.hint {
  font-size: 13px;
  color: var(--linkx-text-muted);
  margin-bottom: 16px;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
  font-size: 13px;
}
.field input {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid var(--linkx-border);
}
.primary {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: var(--linkx-primary);
  color: #fff;
  cursor: pointer;
  margin-top: 8px;
}
.dev-token {
  margin-top: 16px;
  font-size: 12px;
  word-break: break-all;
}
.link {
  display: inline-block;
  margin-top: 16px;
  font-size: 14px;
}
</style>