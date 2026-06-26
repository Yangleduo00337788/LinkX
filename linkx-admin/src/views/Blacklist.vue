<template>
  <AdminPageShell
    table
    hint="对应桌面端「黑名单」：用户主动拉黑关系。此处可查看并代为移除记录。"
  >
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input-number
          v-model:value="userId"
          placeholder="拉黑方用户 ID"
          clearable
          :show-button="false"
          class="filter-input"
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
      :scroll-x="720"
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
  userId: number
  blacklistUserId: number
  createTime: string
}

const message = useMessage()
const userId = ref<number | null>(null)
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 100 },
  { title: '用户 ID', key: 'userId', width: 120 },
  { title: '被拉黑用户 ID', key: 'blacklistUserId', width: 140 },
  {
    title: '时间',
    key: 'createTime',
    minWidth: 180,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  },
  {
    title: '操作',
    key: 'a',
    width: 120,
    fixed: 'right',
    align: 'center',
    render: (row) =>
      h(
        NButton,
        { size: 'small', type: 'error', secondary: true, onClick: () => remove(row) },
        () => '移除记录'
      )
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listBlacklist(pagination.page, pagination.pageSize, userId.value ?? undefined)
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
  userId.value = null
  reload(1)
}

async function remove(row: Row) {
  if (!confirm('确定移除该黑名单记录？')) return
  try {
    await adminApi.removeBlacklist(row.id)
    message.success('已移除')
    load()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.filter-input {
  width: 200px;
}
</style>