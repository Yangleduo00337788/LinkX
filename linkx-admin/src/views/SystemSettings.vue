<template>
  <AdminPageShell>
    <n-spin :show="loading">
      <n-card title="认证与安全" size="small">
        <div class="settings-row">
          <div>
            <div class="label">开放用户注册</div>
            <div class="hint">关闭后 /api/auth/register 将拒绝新用户</div>
          </div>
          <n-switch v-model:value="registerEnabled" :disabled="!canWrite" />
        </div>
        <div class="settings-row">
          <div>
            <div class="label">用户端验证码</div>
            <div class="hint">登录/注册图形验证码（管理端登录仍受 yml 控制）</div>
          </div>
          <n-switch v-model:value="userCaptchaEnabled" :disabled="!canWrite" />
        </div>
        <n-button v-if="canWrite" type="primary" :loading="saveLoading" style="margin-top: 16px" @click="save">
          保存
        </n-button>
      </n-card>
    </n-spin>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { NButton, NCard, NSpin, NSwitch, useMessage } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'

const admin = useAdminStore()
const canWrite = computed(() => canWriteOps(admin.role))
const message = useMessage()
const loading = ref(true)
const saveLoading = ref(false)
const registerEnabled = ref(true)
const userCaptchaEnabled = ref(true)

async function load() {
  loading.value = true
  try {
    const res = await adminApi.getSystemSettings()
    const d = (res.data as { data?: Record<string, boolean> }).data ?? res.data
    registerEnabled.value = Boolean(d.registerEnabled)
    userCaptchaEnabled.value = Boolean(d.userCaptchaEnabled)
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  saveLoading.value = true
  try {
    await adminApi.updateSystemSettings({
      registerEnabled: registerEnabled.value,
      userCaptchaEnabled: userCaptchaEnabled.value
    })
    message.success('已保存')
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saveLoading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid var(--n-border-color);
}
.label {
  font-weight: 500;
}
.hint {
  font-size: 12px;
  color: var(--n-text-color-3);
  margin-top: 4px;
}
</style>