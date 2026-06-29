<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input
          v-model:value="keyword"
          placeholder="搜索用户名或昵称"
          clearable
          class="search-input"
          @keyup.enter="reload(1)"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="reload(1)">搜索</n-button>
        <n-button quaternary @click="onReset">重置</n-button>
        <n-button secondary @click="exportCsv">导出 CSV</n-button>
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
      :row-key="(row: UserRow) => row.id"
      :scroll-x="900"
      remote
      striped
      size="small"
      @update:page="onPageChange"
      @update:page-size="onPageSizeChange"
    />
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { SearchOutline } from '@vicons/ionicons5'
import {
  NButton,
  NDataTable,
  NIcon,
  NInput,
  NSpace,
  NTag,
  useMessage,
  type DataTableColumns
} from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { downloadBlob } from '../utils/downloadBlob'

interface UserRow {
  id: number
  username: string
  nickname: string
  status: number
  createTime: string
}

const message = useMessage()
const router = useRouter()
const keyword = ref('')
const loading = ref(false)
const rows = ref<UserRow[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50]
})

const columns: DataTableColumns<UserRow> = [
  { title: 'ID', key: 'id', width: 88, fixed: 'left' },
  { title: '用户名', key: 'username', width: 120, ellipsis: { tooltip: true } },
  { title: '昵称', key: 'nickname', minWidth: 140, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'status',
    width: 96,
    align: 'center',
    render(row) {
      const ok = row.status === 1
      return h(NTag, { type: ok ? 'success' : 'error', size: 'small', round: true, bordered: false }, () =>
        ok ? '正常' : '已禁用'
      )
    }
  },
  {
    title: '注册时间',
    key: 'createTime',
    width: 172,
    render(row) {
      return row.createTime?.replace('T', ' ').substring(0, 19) || '—'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 148,
    fixed: 'right',
    align: 'center',
    render(row) {
      const enable = row.status !== 1
      return h(NSpace, { justify: 'center', size: 8 }, () => [
        h(
          NButton,
          {
            size: 'small',
            type: 'primary',
            secondary: true,
            onClick: () => router.push({ name: 'user-detail', params: { id: String(row.id) } })
          },
          () => '详情'
        ),
        h(
          NButton,
          {
            size: 'small',
            type: enable ? 'primary' : 'error',
            secondary: true,
            onClick: () => toggleStatus(row)
          },
          () => (enable ? '解禁' : '禁用')
        )
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listUsers(pagination.page, pagination.pageSize, keyword.value || undefined)
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
  keyword.value = ''
  reload(1)
}

async function exportCsv() {
  try {
    const res = await adminApi.exportUsersCsv(keyword.value || undefined)
    downloadBlob(res.data as Blob, 'users.csv')
    message.success('已开始下载')
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '导出失败')
  }
}

function onPageChange(page: number) {
  pagination.page = page
  load()
}

function onPageSizeChange(size: number) {
  pagination.pageSize = size
  pagination.page = 1
  load()
}

async function toggleStatus(row: UserRow) {
  const next = row.status === 1 ? 0 : 1
  try {
    await adminApi.setUserStatus(row.id, next)
    message.success(next === 1 ? '已解禁' : '已禁用')
    await load()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.search-input {
  width: min(320px, 100%);
}
</style>