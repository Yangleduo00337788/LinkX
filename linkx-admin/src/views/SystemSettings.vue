<template>
  <AdminPageShell hint="运行时开关写入数据库，部分项需重启服务后完全生效（以服务端说明为准）。">
    <n-spin :show="loading">
      <n-card title="常用开关" style="max-width: 640px">
        <n-form label-placement="left" label-width="160">
          <n-form-item label="开放用户注册">
            <n-switch v-model:value="registerOpen" :disabled="!canEdit" @update:value="saveKey('auth.register.enabled', $event)" />
          </n-form-item>
          <n-form-item label="登录需要图形验证码">
            <n-switch v-model:value="captchaOnLogin" :disabled="!canEdit" @update:value="saveKey('auth.captcha.enabled', $event)" />
          </n-form-item>
        </n-form>
        <p v-if="!canEdit" class="muted">当前角色只读，无法修改。</p>
      </n-card>
      <n-card title="全部配置项" size="small" style="max-width: 900px; margin-top: 16px">
        <n-data-table :columns="configColumns" :data="configRows" size="small" :bordered="false" />
      </n-card>
    </n-spin>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { NButton, NDataTable, NForm, NFormItem, NSwitch, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { ADMIN_ROLES } from '../utils/adminPermissions'

interface ConfigRow {
  configKey: string
  configValue: string
  description?: string
}

const admin = useAdminStore()
const message = useMessage()
const loading = ref(false)
const configRows = ref<ConfigRow[]>([])

const canEdit = computed(() => admin.role === ADMIN_ROLES.SUPER_ADMIN)

function truthy(v: string | undefined): boolean {
  if (!v) return false
  return v === '1' || v.toLowerCase() === 'true' || v.toLowerCase() === 'yes'
}

const registerOpen = computed({
  get: () => truthy(configRows.value.find((r) => r.configKey === 'auth.register.enabled')?.configValue),
  set: () => {}
})

const captchaOnLogin = computed({
  get: () => truthy(configRows.value.find((r) => r.configKey === 'auth.captcha.enabled')?.configValue),
  set: () => {}
})

const configColumns: DataTableColumns<ConfigRow> = [
  { title: '键', key: 'configKey', width: 220 },
  { title: '值', key: 'configValue', minWidth: 120 },
  { title: '说明', key: 'description', minWidth: 200, ellipsis: { tooltip: true } }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listRuntimeConfig()
    configRows.value = res.data.data || []
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function saveKey(key: string, on: boolean) {
  if (!canEdit.value) return
  try {
    await adminApi.upsertRuntimeConfig({
      configKey: key,
      configValue: on ? 'true' : 'false',
      description: key === 'auth.register.enabled' ? '是否开放注册' : key === 'auth.captcha.enabled' ? '登录验证码' : undefined
    })
    message.success('已保存')
    await load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.muted {
  color: var(--n-text-color-3);
  font-size: 13px;
}
</style>