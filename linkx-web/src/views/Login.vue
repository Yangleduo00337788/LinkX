<template>
  <div class="auth-page">
    <TitleBar />
    <div class="auth-body">
      <div class="auth-left">
        <div class="left-content">
          <img src="../assets/tray.png" alt="LinkX" class="left-logo" />
          <h1 class="left-title">LinkX</h1>
          <p class="left-slogan">— 连接一切可能 —</p>
          <div class="left-features">
            <div class="feature-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <span>即时通讯</span>
            </div>
            <div class="feature-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              </svg>
              <span>好友管理</span>
            </div>
            <div class="feature-item">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
              </svg>
              <span>文件传输</span>
            </div>
          </div>
        </div>
        <div class="left-bg-orb left-bg-orb-1"></div>
        <div class="left-bg-orb left-bg-orb-2"></div>
      </div>

      <div class="auth-right">
        <div class="auth-card">
          <div class="auth-header">
            <h2 class="auth-title">欢迎回来</h2>
            <p class="auth-subtitle">登录账号继续聊天</p>
          </div>

          <form class="auth-form" @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <div class="input-wrapper">
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <input
                  v-model="form.username"
                  type="text"
                  class="form-input"
                  placeholder="请输入用户名"
                  @keyup.enter="handleLogin"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">密码</label>
              <div class="input-wrapper">
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                <input
                  v-model="form.password"
                  type="password"
                  class="form-input"
                  placeholder="请输入密码"
                  @keyup.enter="handleLogin"
                />
              </div>
            </div>

            <button
              type="submit"
              class="submit-btn"
              :class="{ loading }"
              :disabled="loading"
            >
              <div v-if="loading" class="btn-loading"></div>
              {{ loading ? '登录中...' : '登 录' }}
            </button>
          </form>

          <div class="auth-footer">
            <span class="footer-text">还没有账号？</span>
            <button class="link-btn" @click="router.push('/register')">立即注册</button>
          </div>

          <div v-if="error" class="error-message">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
            <span>{{ error }}</span>
            <button class="error-close" @click="error = ''">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api/client'
import { useUserStore } from '../stores/user'
import TitleBar from '../components/TitleBar.vue'

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

<style scoped>
.auth-page {
  height: 100vh;
  height: 100dvh;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg);
}

.auth-body {
  flex: 1;
  display: flex;
  overflow: auto;
}

.auth-left {
  flex: 1;
  background: linear-gradient(135deg, #00d68f 0%, #00b894 50%, #00a381 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  min-width: 0;
}

.left-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: white;
  padding: 40px;
}

.left-logo {
  width: 100px;
  height: 100px;
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  margin-bottom: 24px;
}

.left-title {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.left-slogan {
  font-size: 16px;
  opacity: 0.85;
  margin-bottom: 48px;
  letter-spacing: 3px;
}

.left-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  opacity: 0.9;
}

.feature-item svg {
  opacity: 0.8;
}

.left-bg-orb {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.left-bg-orb-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -80px;
}

.left-bg-orb-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: -60px;
}

.auth-right {
  width: 480px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--linkx-bg);
  padding: 40px;
}

.auth-card {
  width: 100%;
  max-width: 360px;
}

.auth-header {
  margin-bottom: 36px;
}

.auth-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 8px;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--linkx-text-secondary);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: var(--linkx-text-muted);
  pointer-events: none;
  transition: color 0.2s;
}

.form-input {
  width: 100%;
  height: 46px;
  padding: 0 14px 0 44px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text);
  font-size: 14px;
  outline: none;
  transition: var(--linkx-transition);
}

.form-input::placeholder {
  color: var(--linkx-text-muted);
}

.form-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.input-wrapper:focus-within .input-icon {
  color: var(--linkx-primary);
}

.submit-btn {
  width: 100%;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  border: none;
  border-radius: var(--linkx-radius);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
  margin-top: 8px;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 214, 143, 0.3);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.submit-btn.loading {
  background: rgba(0, 214, 143, 0.7);
}

.btn-loading {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: var(--linkx-text-secondary);
}

.footer-text {
  color: var(--linkx-text-muted);
}

.link-btn {
  background: none;
  border: none;
  color: var(--linkx-primary);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  margin-left: 4px;
}

.link-btn:hover {
  text-decoration: underline;
}

.error-message {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  padding: 12px 14px;
  background: rgba(255, 61, 113, 0.1);
  border: 1px solid rgba(255, 61, 113, 0.2);
  border-radius: var(--linkx-radius);
  color: var(--linkx-error);
  font-size: 13px;
}

.error-message svg:first-child {
  flex-shrink: 0;
}

.error-close {
  margin-left: auto;
  background: transparent;
  border: none;
  color: var(--linkx-error);
  cursor: pointer;
  padding: 2px;
  opacity: 0.7;
  transition: opacity 0.2s;
}

.error-close:hover {
  opacity: 1;
}

@media (max-width: 768px) {
  .auth-left {
    display: none;
  }

  .auth-right {
    width: 100%;
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .auth-right {
    padding: 16px 12px 24px;
    align-items: flex-start;
  }

  .auth-card {
    max-width: none;
  }

  .auth-header {
    margin-bottom: 24px;
  }

  .auth-title {
    font-size: 22px;
  }

  .left-content {
    padding: 24px;
  }
}
</style>
