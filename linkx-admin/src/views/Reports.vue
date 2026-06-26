<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-select
          v-model:value="status"
          :options="statusOptions"
          clearable
          placeholder="工单状态"
          style="width: 140px"
        />
        <n-select
          v-model:value="targetType"
          :options="typeOptions"
          clearable
          placeholder="举报类型"
          style="width: 140px"
        />
        <n-button type="primary" @click="reload(1)">查询</n-button>
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
      :scroll-x="1200"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
    <n-modal v-model:show="handleVisible" preset="card" title="处理举报" style="width: 520px">
      <n-form label-placement="left" label-width="88">
        <n-form-item label="状态">
          <n-select v-model:value="handleForm.status" :options="statusOptions" />
        </n-form-item>
        <n-form-item label="处置">
          <n-select v-model:value="handleForm.resolution" :options="resolutionOptions" clearable />
        </n-form-item>
        <n-form-item label="说明">
          <n-input v-model:value="handleForm.resolutionNote" type="textarea" :rows="3" />
        </n-form-item>
        <n-form-item label="通知举报人">
          <n-switch v-model:value="handleForm.notifyReporter" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="handleVisible = false">取消</n-button>
        <n-button type="primary" :loading="handleLoading" @click="submitHandle">提交</n-button>
      </template>
    </n-modal>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NSwitch, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  reporterUsername: string
  targetType: string
  targetId: string
  reasonCategory: string
  reasonDetail: string
  status: number
  resolution: string
  createTime: string
}

const message = useMessage()
const status = ref<number | null>(0)
const targetType = ref<string | null>(null)
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已结案', value: 2 },
  { label: '已驳回', value: 3 }
]
const typeOptions = [
  { label: '用户', value: 'USER' },
  { label: '消息', value: 'MESSAGE' },
  { label: '群组', value: 'GROUP' },
  { label: '文件', value: 'FILE' }
]
const resolutionOptions = [
  { label: '警告', value: 'WARN' },
  { label: '删除内容', value: 'DELETE_CONTENT' },
  { label: '封禁用户', value: 'BAN_USER' },
  { label: '驳回', value: 'DISMISS' }
]

const handleVisible = ref(false)
const handleLoading = ref(false)
const currentId = ref(0)
const handleForm = reactive({
  status: 2,
  resolution: '' as string | null,
  resolutionNote: '',
  notifyReporter: true
})

const statusLabel: Record<number, string> = { 0: '待处理', 1: '处理中', 2: '已结案', 3: '已驳回' }

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '举报人', key: 'reporterUsername', width: 110 },
  { title: '类型', key: 'targetType', width: 90 },
  { title: '目标 ID', key: 'targetId', width: 100 },
  { title: '原因', key: 'reasonCategory', width: 100 },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (r) => h(NTag, { size: 'small', bordered: false }, () => statusLabel[r.status] ?? String(r.status))
  },
  { title: '说明', key: 'reasonDetail', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    fixed: 'right',
    render: (r) =>
      r.status === 0 || r.status === 1
        ? h(NButton, { size: 'small', type: 'primary', onClick: () => openHandle(r.id) }, () => '处理')
        : '—'
  }
]

function openHandle(id: number) {
  currentId.value = id
  handleForm.status = 2
  handleForm.resolution = null
  handleForm.resolutionNote = ''
  handleForm.notifyReporter = true
  handleVisible.value = true
}

async function submitHandle() {
  handleLoading.value = true
  try {
    await adminApi.handleReport(currentId.value, {
      status: handleForm.status,
      resolution: handleForm.resolution || undefined,
      resolutionNote: handleForm.resolutionNote || undefined,
      notifyReporter: handleForm.notifyReporter
    })
    message.success('已处理')
    handleVisible.value = false
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '处理失败')
  } finally {
    handleLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listReports(
      pagination.page,
      pagination.pageSize,
      status.value ?? undefined,
      targetType.value ?? undefined
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

onMounted(() => load())
</script>