<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input-number v-model:value="sessionId" placeholder="会话 ID" clearable :show-button="false" style="width: 140px" />
        <n-input-number v-model:value="fromUserId" placeholder="发送者 ID" clearable :show-button="false" style="width: 140px" />
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
import { NButton, NDataTable, NInputNumber, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  sessionId: number
  fromUserId: number
  toUserId: number
  msgType: number
  contentPreview: string
  createTime: string
}

const message = useMessage()
const sessionId = ref<number | null>(null)
const fromUserId = ref<number | null>(null)
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 90 },
  { title: '会话', key: 'sessionId', width: 90 },
  { title: '发送者', key: 'fromUserId', width: 90 },
  { title: '类型', key: 'msgType', width: 70 },
  { title: '内容摘要', key: 'contentPreview', minWidth: 200, ellipsis: { tooltip: true } },
  {
    title: '时间',
    key: 'createTime',
    width: 172,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  },
  {
    title: '操作',
    key: 'a',
    width: 88,
    fixed: 'right',
    render: (row) =>
      h(NButton, { size: 'small', type: 'error', secondary: true, onClick: () => remove(row) }, () => '删除')
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listMessages(
      pagination.page,
      pagination.pageSize,
      sessionId.value ?? undefined,
      fromUserId.value ?? undefined
    )
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
  sessionId.value = null
  fromUserId.value = null
  reload(1)
}

async function remove(row: Row) {
  if (!confirm('确定删除该消息？')) return
  try {
    await adminApi.deleteMessage(row.id)
    message.success('已删除')
    load()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  }
}

onMounted(() => load())
</script>