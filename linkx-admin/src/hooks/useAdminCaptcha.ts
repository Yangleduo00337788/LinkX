import { onMounted, ref } from 'vue'
import { adminApi } from '../api/client'

export function useAdminCaptcha() {
  const captchaEnabled = ref(false)
  const captchaId = ref('')
  const captchaCode = ref('')
  const captchaImageUrl = ref('')
  const captchaLoading = ref(false)

  async function loadMeta() {
    try {
      const res: any = await adminApi.getCaptchaMeta()
      captchaEnabled.value = Boolean(res.data?.data?.enabled ?? res.data?.enabled)
    } catch {
      captchaEnabled.value = false
    }
  }

  async function refreshCaptcha() {
    if (!captchaEnabled.value) return
    captchaLoading.value = true
    captchaCode.value = ''
    try {
      const res: any = await adminApi.issueCaptcha()
      const data = res.data?.data ?? res.data
      captchaId.value = String(data?.captchaId || '')
      captchaImageUrl.value = String(data?.imageDataUrl || '')
    } catch {
      captchaId.value = ''
      captchaImageUrl.value = ''
    } finally {
      captchaLoading.value = false
    }
  }

  async function initCaptcha() {
    await loadMeta()
    if (captchaEnabled.value) {
      await refreshCaptcha()
    }
  }

  function captchaPayload() {
    if (!captchaEnabled.value) return {}
    return { captchaId: captchaId.value, captchaCode: captchaCode.value.trim() }
  }

  function validateCaptchaFilled(): string | null {
    if (!captchaEnabled.value) return null
    if (!captchaId.value || !captchaCode.value.trim()) return '请输入验证码'
    return null
  }

  onMounted(() => {
    void initCaptcha()
  })

  return {
    captchaEnabled,
    captchaId,
    captchaCode,
    captchaImageUrl,
    captchaLoading,
    refreshCaptcha,
    captchaPayload,
    validateCaptchaFilled
  }
}