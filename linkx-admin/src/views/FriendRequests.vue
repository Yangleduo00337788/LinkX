<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-select
          v-model:value="status"
          :options="statusOptions"
          clearable
          placeholder="状态"
          style="width: 160px"
          @update:value="reload(1)"
        />
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
      :scroll-x="900"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NSelect, NTag, useMessage } from 'naive-ui'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  fromUserId: number
  fromUsername: string
  toUserId: number
  toUsername: string
  message: string
  status: number
  createTime: string
}

const admin = useAdminStore()
const canWrite = computed(() => canWriteOps(admin.role))
const message = useMessage()
const status = ref<number | null>(null)
const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '已同意', value: 1 },
  { label: '已拒绝', value: 2 }
]
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 90 },
  { title: '申请人', key: 'fromUsername', width: 120 },
  { title: '申请人 ID', key: 'fromUserId', width: 100 },
  { title: '被申请人', key: 'toUsername', width: 120 },
  { title: '被申请人 ID', key: 'toUserId', width: 110 },
  { title: '附言', key: 'message', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 96,
    render: (r) => {
      const map: Record<number, string> = { 0: '待处理', 1: '已同意', 2: '已拒绝' }
      const type = r.status === 1 ? 'success' : r.status === 2 ? 'default' : 'warning'
      return h(NTag, { type, size: 'small', round: true, bordered: false }, () => map[r.status] ?? String(r.status))
    }
  },
  {
    title: '时间',
    key: 'createTime',
    width: 172,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  },
  {
    title: '操作',
    key: 'op',
    width: 140,
    render: (r) => {
      if (!canWrite.value || r.status !== 0) return '—'
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => accept(r.id) }, () => '同意'),
        h(NButton, { size: 'small', tertiary: true, onClick: () => reject(r.id) }, () => '拒绝')
      ])
    }
  }
]

async function accept(id: number) {
  try {
    await adminApi.acceptFriendRequest(id)
    message.success('已同意')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function reject(id: number) {
  try {
    await adminApi.rejectFriendRequest(id)
    message.success('已拒绝')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listFriendRequests(
      pagination.page,
      pagination.pageSize,
      status.value ?? undefined
    )
    const page = res.data.data
    rows.value = page.records || []
    pagination.itemCount = page.total || 0
  } finally {
    loading.value = false
  }
}

function reload(page: number) {
  pagination.page = page
  load()
}

function onReset() {
  status.value = null
  reload(1)
}

onMounted(() => load())
</script>