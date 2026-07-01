/**
 * 维护登录态、用户资料缓存与退出登录流程。
 */
import { defineStore } from 'pinia'  // 行注：引入 defineStore 能力
import { ref } from 'vue'  // 行注：引入 ref 能力

const AUTH_STORAGE_KEYS = ['token', 'refreshToken', 'sessionId', 'userId', 'nickname', 'avatar', 'username'] as const

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const sessionId = ref(localStorage.getItem('sessionId') || '')
  const userId = ref(localStorage.getItem('userId') || '')  // 行注：初始化 userId 响应式状态
  const nickname = ref(localStorage.getItem('nickname') || '')  // 行注：初始化 nickname 响应式状态
  const avatar = ref(localStorage.getItem('avatar') || '')  // 行注：初始化 avatar 响应式状态
  const username = ref(localStorage.getItem('username') || '')  // 行注：初始化 username 响应式状态

  // 登录成功后同步刷新响应数据到 Pinia 和 localStorage，保证刷新页面后仍能恢复会话。
  function setLoginData(data: any) {  // 行注：定义 setLoginData 方法
    token.value = data.accessToken  // 行注：更新 token 状态
    refreshToken.value = data.refreshToken  // 行注：更新 refreshToken 状态
    userId.value = String(data.userId)  // 行注：更新 userId 状态
    nickname.value = data.nickname  // 行注：更新 nickname 状态
    avatar.value = data.avatar || ''  // 行注：更新 avatar 状态
    username.value = data.username
    if (data.sessionId != null && data.sessionId !== '') {
      sessionId.value = String(data.sessionId)
      localStorage.setItem('sessionId', String(data.sessionId))
    }

    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', String(data.userId))  // 行注：写入本地缓存
    localStorage.setItem('nickname', data.nickname)  // 行注：写入本地缓存
    localStorage.setItem('avatar', data.avatar || '')  // 行注：写入本地缓存
    localStorage.setItem('username', data.username)  // 行注：写入本地缓存
  }  // 行注：结束当前代码块

  // 退出登录时优先通知服务端吊销 token，随后无论接口是否成功都清理本地状态。
  async function logout() {  // 行注：定义异步 logout 方法
    const storedRefresh = refreshToken.value || localStorage.getItem('refreshToken') || ''  // 行注：初始化 storedRefresh 变量
    const storedAccess = token.value || localStorage.getItem('token') || ''  // 行注：初始化 storedAccess 变量
    try {  // 行注：尝试执行可能失败的逻辑
      const { authApi } = await import('../api/client')  // 行注：声明当前变量
      await authApi.logout({  // 行注：开始当前逻辑块
        refreshToken: storedRefresh || undefined,  // 行注：设置 refreshToken 配置项
        accessToken: storedAccess || undefined  // 行注：设置 accessToken 配置项
      })  // 行注：结束当前调用配置
    } catch {  // 行注：捕获并处理异常
      // clear local state even if server logout fails
    }  // 行注：结束当前代码块
    token.value = ''
    refreshToken.value = ''
    sessionId.value = ''
    userId.value = ''  // 行注：更新 userId 状态
    nickname.value = ''  // 行注：更新 nickname 状态
    avatar.value = ''  // 行注：更新 avatar 状态
    username.value = ''  // 行注：更新 username 状态
    for (const key of AUTH_STORAGE_KEYS) {  // 行注：遍历当前数据集合
      localStorage.removeItem(key)  // 行注：移除本地缓存
    }  // 行注：结束当前代码块
    try {  // 行注：尝试执行可能失败的逻辑
      const { revokeAllFileAccessBlobUrls } = await import('../utils/file-access')  // 行注：声明当前变量
      revokeAllFileAccessBlobUrls()  // 行注：调用 revokeAllFileAccessBlobUrls 方法
    } catch {  // 行注：捕获并处理异常
      // ignore
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  return { token, refreshToken, sessionId, userId, nickname, avatar, username, setLoginData, logout }
})  // 行注：结束当前调用配置
