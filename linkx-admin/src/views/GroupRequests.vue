<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-select
          v-model:value="status"
          :options="statusOptions"
          clearable
          placeholder="状态"
          style="width: 140px"
          @update:value="reload(1)"
        />
        <n-input-number
          v-model:value="groupId"
          placeholder="群 ID"
          clearable
          :show-button="false"
          style="width: 140px"
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
      :scroll-x="960"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NInputNumber, NSelect, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'

const admin = useAdminStore()
const message = useMessage()

interface Row {
  id: number
  groupId: number
  groupName: string
  fromUserId: number
  toUserId: number
  requestType: number
  message: string
  status: number
  createTime: string
}

const status = ref<number | null>(null)
const groupId = ref<number | null>(null)
const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '已同意', value: 1 },
  { label: '已拒绝', value: 2 }
]
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '群', key: 'groupName', minWidth: 160, render: (r) => `${r.groupName || '—'} (${r.groupId})` },
  { title: '发起人', key: 'fromUserId', width: 100 },
  { title: '目标用户', key: 'toUserId', width: 100 },
  { title: '类型', key: 'requestType', width: 80, render: (r) => (r.requestType === 1 ? '邀请' : '申请') },
  { title: '说明', key: 'message', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 90,
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
    fixed: 'right',
    render: (r) => {
      if (r.status !== 0 || !canWriteOps(admin.role)) return '—'
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(NButton, { size: 'small', type: 'primary', tertiary: true, onClick: () => accept(r.id) }, () => '同意'),
        h(NButton, { size: 'small', tertiary: true, onClick: () => reject(r.id) }, () => '拒绝')
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listGroupRequests(
      pagination.page,
      pagination.pageSize,
      status.value ?? undefined,
      groupId.value ?? undefined
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
  groupId.value = null
  reload(1)
}

async function accept(id: number) {
  try {
    await adminApi.acceptGroupRequest(id)
    message.success('已同意')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function reject(id: number) {
  try {
    await adminApi.rejectGroupRequest(id)
    message.success('已拒绝')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

onMounted(() => load())
</script>