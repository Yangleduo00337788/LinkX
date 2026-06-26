<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="username" placeholder="用户名" clearable style="width: 160px" @keyup.enter="reload(1)" />
        <n-input-number v-model:value="userId" placeholder="用户 ID" clearable style="width: 140px" />
        <n-select
          v-model:value="loginStatus"
          :options="statusOptions"
          clearable
          placeholder="登录结果"
          style="width: 120px"
        />
        <n-button type="primary" @click="reload(1)">查询</n-button>
        <n-button quaternary @click="onReset">重置</n-button>
        <div class="admin-toolbar-spacer" />
        <span class="admin-total-hint">共 {{ pagination.itemCount }} 条</span>
      </div>
    </template>
    <n-data-table
      class="admin-full-table"
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: Row) => r.id"
      :scroll-x="1100"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, NDataTable, NInput, NInputNumber, NSelect, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  userId: number | null
  username: string
  loginIp: string
  userAgent: string
  loginStatus: number
  failReason: string
  createTime: string
}

const route = useRoute()
const message = useMessage()
const username = ref('')
const userId = ref<number | null>(null)
const loginStatus = ref<number | null>(null)
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const statusOptions = [
  { label: '成功', value: 1 },
  { label: '失败', value: 0 }
]

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '用户 ID', key: 'userId', width: 90 },
  { title: '用户名', key: 'username', width: 120 },
  {
    title: '结果',
    key: 'loginStatus',
    width: 80,
    render: (r) =>
      h(NTag, { size: 'small', type: r.loginStatus === 1 ? 'success' : 'error', bordered: false }, () =>
        r.loginStatus === 1 ? '成功' : '失败'
      )
  },
  { title: 'IP', key: 'loginIp', width: 130 },
  { title: '失败原因', key: 'failReason', width: 140, ellipsis: { tooltip: true } },
  { title: 'UA', key: 'userAgent', minWidth: 180, ellipsis: { tooltip: true } },
  {
    title: '时间',
    key: 'createTime',
    width: 172,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listLoginLogs(
      pagination.page,
      pagination.pageSize,
      username.value || undefined,
      userId.value ?? undefined,
      loginStatus.value ?? undefined
    )
    const page = res.data.data
    rows.value = page.records || []
    pagination.itemCount = page.total || 0
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function reload(p: number) {
  pagination.page = p
  load()
}

function onReset() {
  username.value = ''
  userId.value = null
  loginStatus.value = null
  reload(1)
}

function applyRouteQuery() {
  const q = route.query.userId
  if (q != null && String(q).length > 0) {
    const n = Number(q)
    userId.value = Number.isFinite(n) ? n : null
  }
}

onMounted(() => {
  applyRouteQuery()
  load()
})

watch(
  () => route.query.userId,
  () => {
    applyRouteQuery()
    reload(1)
  }
)
</script>