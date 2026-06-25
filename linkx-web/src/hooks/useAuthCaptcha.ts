/**
 * useAuthCaptcha 组合式逻辑，负责抽离可复用的状态与行为。
 */
import { onMounted, ref } from 'vue'  // 行注：引入 onMounted, ref 能力
import { authApi } from '../api/client'  // 行注：引入 authApi 能力

export type AuthCaptchaScene = 'login' | 'register'  // 行注：导出当前能力

export function useAuthCaptcha(scene: AuthCaptchaScene) {  // 行注：导出当前能力
  const captchaEnabled = ref(false)  // 行注：初始化 captchaEnabled 响应式状态
  const captchaId = ref('')  // 行注：初始化 captchaId 响应式状态
  const captchaCode = ref('')  // 行注：初始化 captchaCode 响应式状态
  const captchaImageUrl = ref('')  // 行注：初始化 captchaImageUrl 响应式状态
  const captchaLoading = ref(false)  // 行注：初始化 captchaLoading 响应式状态

  async function loadMeta() {  // 行注：定义异步 loadMeta 方法
    try {  // 行注：尝试执行可能失败的逻辑
      const res: any = await authApi.getCaptchaMeta()  // 行注：接收 res 异步结果
      captchaEnabled.value = Boolean(res.data?.enabled)  // 行注：更新 captchaEnabled 状态
    } catch {  // 行注：捕获并处理异常
      captchaEnabled.value = false  // 行注：更新 captchaEnabled 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function refreshCaptcha() {  // 行注：定义异步 refreshCaptcha 方法
    if (!captchaEnabled.value) {  // 行注：判断当前条件是否成立
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    captchaLoading.value = true  // 行注：更新 captchaLoading 状态
    captchaCode.value = ''  // 行注：更新 captchaCode 状态
    try {  // 行注：尝试执行可能失败的逻辑
      const res: any = await authApi.issueCaptcha(scene)  // 行注：接收 res 异步结果
      captchaId.value = String(res.data?.captchaId || '')  // 行注：更新 captchaId 状态
      captchaImageUrl.value = String(res.data?.imageDataUrl || '')  // 行注：更新 captchaImageUrl 状态
    } catch {  // 行注：捕获并处理异常
      captchaId.value = ''  // 行注：更新 captchaId 状态
      captchaImageUrl.value = ''  // 行注：更新 captchaImageUrl 状态
    } finally {  // 行注：执行收尾清理逻辑
      captchaLoading.value = false  // 行注：更新 captchaLoading 状态
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  async function initCaptcha() {  // 行注：定义异步 initCaptcha 方法
    await loadMeta()  // 行注：调用 loadMeta 方法
    if (captchaEnabled.value) {  // 行注：判断当前条件是否成立
      await refreshCaptcha()  // 行注：调用 refreshCaptcha 方法
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function captchaPayload() {  // 行注：定义 captchaPayload 方法
    if (!captchaEnabled.value) {  // 行注：判断当前条件是否成立
      return {}  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return {  // 行注：返回当前结果
      captchaId: captchaId.value,  // 行注：设置 captchaId 配置项
      captchaCode: captchaCode.value.trim()  // 行注：设置 captchaCode 配置项
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块

  function validateCaptchaFilled(): string | null {  // 行注：定义 validateCaptchaFilled 方法
    if (!captchaEnabled.value) {  // 行注：判断当前条件是否成立
      return null  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    if (!captchaId.value || !captchaCode.value.trim()) {  // 行注：判断当前条件是否成立
      return '请输入验证码'  // 行注：返回当前结果
    }  // 行注：结束当前代码块
    return null  // 行注：返回当前结果
  }  // 行注：结束当前代码块

  onMounted(() => {  // 行注：注册组件挂载后的初始化逻辑
    void initCaptcha()  // 行注：调用 initCaptcha 方法
  })  // 行注：结束当前调用配置

  return {  // 行注：返回当前结果
    captchaEnabled,  // 行注：补充 captchaEnabled 配置项
    captchaId,  // 行注：补充当前配置项
    captchaCode,  // 行注：补充当前配置项
    captchaImageUrl,  // 行注：补充当前配置项
    captchaLoading,  // 行注：补充当前配置项
    refreshCaptcha,  // 行注：补充当前配置项
    captchaPayload,  // 行注：补充当前配置项
    validateCaptchaFilled  // 行注：补充当前表达式
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块
