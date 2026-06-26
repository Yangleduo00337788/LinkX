<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input
          v-model:value="action"
          placeholder="动作码，如 USER_DISABLE"
          clearable
          style="width: 260px"
          @keyup.enter="reload(1)"
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
      :scroll-x="1000"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NInput, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  adminUsername: string
  action: string
  targetType: string
  targetId: string
  detail: string
  clientIp: string
  createTime: string
}

const message = useMessage()
const action = ref('')
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const columns: DataTableColumns<Row> = [
  { title: '管理员', key: 'adminUsername', width: 120 },
  { title: '动作', key: 'action', width: 160 },
  { title: '目标类型', key: 'targetType', width: 100 },
  { title: '目标 ID', key: 'targetId', width: 120 },
  { title: '详情', key: 'detail', minWidth: 200, ellipsis: { tooltip: true } },
  { title: 'IP', key: 'clientIp', width: 130 },
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
    const res = await adminApi.listAuditLogs(pagination.page, pagination.pageSize, action.value || undefined)
    const page = res.data.data
    rows.value = page.records || []
    pagination.itemCount = page.total || 0
  } catch (e: unknown) {
    message.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
}

function reload(page: number) {
  pagination.page = page
  load()
}

function onReset() {
  action.value = ''
  reload(1)
}

onMounted(() => load())
</script>