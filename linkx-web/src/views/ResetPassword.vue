<template>
  <div class="auth-page">
    <TitleBar />
    <div class="auth-body compact">
      <div class="auth-card">
        <h2>设置新密码</h2>
        <label class="field">
          <span>重置令牌</span>
          <input v-model="resetToken" />
        </label>
        <label class="field">
          <span>新密码</span>
          <input v-model="newPassword" type="password" autocomplete="new-password" />
        </label>
        <label class="field">
          <span>确认新密码</span>
          <input v-model="confirmPassword" type="password" autocomplete="new-password" />
        </label>
        <button type="button" class="primary" :disabled="loading" @click="submit">
          {{ loading ? '提交中…' : '确认重置' }}
        </button>
        <router-link to="/login" class="link">返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import TitleBar from '../components/TitleBar.vue'
import { authApi } from '../api/client'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const resetToken = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)

onMounted(() => {
  const q = route.query.token
  if (typeof q === 'string' && q) {
    resetToken.value = q
  }
})

async function submit() {
  if (!resetToken.value || !newPassword.value) {
    message.warning('请填写令牌和新密码')
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    message.warning('两次密码不一致')
    return
  }
  loading.value = true
  try {
    await authApi.confirmPasswordReset({
      resetToken: resetToken.value.trim(),
      newPassword: newPassword.value
    })
    message.success('密码已重置，请登录')
    await router.push('/login')
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err.message || '重置失败')
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
}
.link {
  display: inline-block;
  margin-top: 16px;
}
</style>