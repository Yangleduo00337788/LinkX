<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="auth-page">
    <!-- 行注：渲染 TitleBar 组件 -->
    <TitleBar />
    <!-- 行注：渲染容器 -->
    <div class="auth-body">
      <!-- 行注：渲染容器 -->
      <div class="auth-left">
        <!-- 行注：渲染容器 -->
        <div class="left-content">
          <!-- 行注：渲染图片 -->
          <img src="../assets/tray.png" alt="LinkX" class="left-logo" />
          <!-- 行注：展示“LinkX”文案 -->
          <h1 class="left-title">LinkX</h1>
          <!-- 行注：展示“— 连接一切可能 —”文案 -->
          <p class="left-slogan">— 连接一切可能 —</p>
          <!-- 行注：渲染容器 -->
          <div class="left-features">
            <!-- 行注：渲染容器 -->
            <div class="feature-item">
              <!-- 行注：渲染图标容器 -->
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“即时通讯”文案 -->
              <span>即时通讯</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="feature-item">
              <!-- 行注：渲染图标容器 -->
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="9" cy="7" r="4"/>
                <!-- 行注：补充图标路径 -->
                <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                <!-- 行注：补充图标路径 -->
                <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“好友管理”文案 -->
              <span>好友管理</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="feature-item">
              <!-- 行注：渲染图标容器 -->
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“文件传输”文案 -->
              <span>文件传输</span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="left-bg-orb left-bg-orb-1"></div>
        <!-- 行注：渲染容器 -->
        <div class="left-bg-orb left-bg-orb-2"></div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="auth-right">
        <!-- 行注：渲染容器 -->
        <div class="auth-card">
          <!-- 行注：渲染容器 -->
          <div class="auth-header">
            <!-- 行注：展示“欢迎回来”文案 -->
            <h2 class="auth-title">欢迎回来</h2>
            <!-- 行注：展示“登录账号继续聊天”文案 -->
            <p class="auth-subtitle">登录账号继续聊天</p>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染form 节点 -->
          <form class="auth-form" @submit.prevent="handleLogin">
            <!-- 行注：渲染容器 -->
            <div class="form-group">
              <!-- 行注：展示“用户名”文案 -->
              <label class="form-label">用户名</label>
              <!-- 行注：渲染容器 -->
              <div class="input-wrapper">
                <!-- 行注：渲染图标容器 -->
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标路径 -->
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <!-- 行注：补充图标圆形路径 -->
                  <circle cx="12" cy="7" r="4"/>
                <!-- 行注：结束图标容器 -->
                </svg>
                <!-- 行注：渲染输入框 -->
                <input
                  v-model="form.username"
                  type="text"
                  class="form-input"
                  placeholder="请输入用户名"
                  @keyup.enter="handleLogin"
                />
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="form-group">
              <!-- 行注：展示“密码”文案 -->
              <label class="form-label">密码</label>
              <!-- 行注：渲染容器 -->
              <div class="input-wrapper">
                <!-- 行注：渲染图标容器 -->
                <svg class="input-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <!-- 行注：补充图标矩形路径 -->
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                  <!-- 行注：补充图标路径 -->
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                <!-- 行注：结束图标容器 -->
                </svg>
                <!-- 行注：渲染输入框 -->
                <input
                  v-model="form.password"
                  type="password"
                  class="form-input"
                  placeholder="请输入密码"
                  @keyup.enter="handleLogin"
                />
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div v-if="captchaEnabled" class="form-group">
              <!-- 行注：展示“验证码”文案 -->
              <label class="form-label">验证码</label>
              <!-- 行注：渲染容器 -->
              <div class="captcha-row">
                <!-- 行注：渲染输入框 -->
                <input
                  v-model="captchaCode"
                  type="text"
                  class="form-input captcha-input"
                  placeholder="请输入验证码"
                  maxlength="8"
                  autocomplete="off"
                  @keyup.enter="handleLogin"
                />
                <!-- 行注：渲染按钮 -->
                <button
                  type="button"
                  class="captcha-image-btn"
                  :disabled="captchaLoading"
                  title="点击刷新验证码"
                  @click="refreshCaptcha"
                >
                  <!-- 行注：渲染图片 -->
                  <img v-if="captchaImageUrl" :src="captchaImageUrl" alt="验证码" class="captcha-image" />
                  <!-- 行注：渲染文本节点 -->
                  <span v-else class="captcha-placeholder">{{ captchaLoading ? '加载中' : '刷新' }}</span>
                <!-- 行注：结束按钮 -->
                </button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染按钮 -->
            <button
              type="submit"
              class="submit-btn"
              :class="{ loading }"
              :disabled="loading"
            >
              <!-- 行注：渲染容器 -->
              <div v-if="loading" class="btn-loading"></div>
              <!-- 行注：渲染动态文本 -->
              {{ loading ? '登录中...' : '登 录' }}
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束form 节点 -->
          </form>
          <!-- 行注：渲染容器 -->
          <div class="auth-footer">
            <!-- 行注：展示“还没有账号？”文案 -->
            <span class="footer-text">还没有账号？</span>
            <!-- 行注：展示“立即注册”文案 -->
            <button class="link-btn" @click="router.push('/register')">立即注册</button>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="error" class="error-message">
            <!-- 行注：渲染图标容器 -->
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <!-- 行注：补充图标圆形路径 -->
              <circle cx="12" cy="12" r="10"/>
              <!-- 行注：补充图标线段 -->
              <line x1="12" y1="8" x2="12" y2="12"/>
              <!-- 行注：补充图标线段 -->
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            <!-- 行注：结束图标容器 -->
            </svg>
            <!-- 行注：渲染文本节点 -->
            <span>{{ error }}</span>
            <!-- 行注：渲染按钮 -->
            <button class="error-close" @click="error = ''">
              <!-- 行注：渲染图标容器 -->
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标线段 -->
                <line x1="18" y1="6" x2="6" y2="18"/>
                <!-- 行注：补充图标线段 -->
                <line x1="6" y1="6" x2="18" y2="18"/>
              <!-- 行注：结束图标容器 -->
              </svg>
            <!-- 行注：结束按钮 -->
            </button>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 登录页面，处理账号登录、验证码和错误提示。
 */
import { ref, reactive } from 'vue'  // 行注：引入 ref, reactive 能力
import { useRouter } from 'vue-router'  // 行注：引入 useRouter 能力
import { authApi } from '../api/client'  // 行注：引入 authApi 能力
import { useAuthCaptcha } from '../hooks/useAuthCaptcha'  // 行注：引入 useAuthCaptcha 能力
import { useUserStore } from '../stores/user'  // 行注：引入 useUserStore 能力
import TitleBar from '../components/TitleBar.vue'  // 行注：引入 TitleBar 组件

const router = useRouter()  // 行注：获取路由实例
const userStore = useUserStore()  // 行注：获取 userStore 组合式能力
const loading = ref(false)  // 行注：初始化 loading 响应式状态
const error = ref('')  // 行注：初始化 error 响应式状态
const form = reactive({ username: '', password: '' })  // 行注：初始化 form 响应式对象

const {  // 行注：开始解构当前返回值
  captchaEnabled,  // 行注：解构 captchaEnabled 状态
  captchaCode,  // 行注：解构 captchaCode 状态
  captchaImageUrl,  // 行注：解构 captchaImageUrl 状态
  captchaLoading,  // 行注：解构 captchaLoading 状态
  refreshCaptcha,  // 行注：解构 refreshCaptcha 方法
  captchaPayload,  // 行注：解构 captchaPayload 状态
  validateCaptchaFilled  // 行注：解构 validateCaptchaFilled 状态
} = useAuthCaptcha('login')  // 行注：从 useAuthCaptcha 中解构所需能力

async function handleLogin() {  // 行注：定义异步 handleLogin 方法
  if (!form.username || !form.password) {  // 行注：判断当前条件是否成立
    error.value = '请输入用户名和密码'  // 行注：更新 error 状态
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  const captchaError = validateCaptchaFilled()  // 行注：初始化 captchaError 变量
  if (captchaError) {  // 行注：判断当前条件是否成立
    error.value = captchaError  // 行注：更新 error 状态
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  loading.value = true  // 行注：更新 loading 状态
  error.value = ''  // 行注：更新 error 状态
  try {  // 行注：尝试执行可能失败的逻辑
    const res: any = await authApi.login({ ...form, ...captchaPayload() })  // 行注：接收 res 异步结果
    userStore.setLoginData(res.data)  // 行注：调用 setLoginData 方法
    router.push('/')  // 行注：跳转到目标路由
  } catch (e: any) {  // 行注：捕获并处理异常
    error.value = e.response?.data?.message || e.message || '登录失败'  // 行注：更新 error 状态
    await refreshCaptcha()  // 行注：调用 refreshCaptcha 方法
  } finally {  // 行注：执行收尾清理逻辑
    loading.value = false  // 行注：更新 loading 状态
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
</script>
