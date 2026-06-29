<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="keyword" placeholder="内容哈希" clearable style="width: 220px" @keyup.enter="reload(1)" />
        <n-button type="primary" @click="reload(1)">查询</n-button>
        <n-button v-if="canWrite" @click="openCreate">新增</n-button>
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
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
    />
    <n-modal v-model:show="createVisible" preset="card" title="封禁文件哈希" style="width: 480px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="哈希"><n-input v-model:value="form.contentHash" placeholder="SHA-256 等" /></n-form-item>
        <n-form-item label="原因"><n-input v-model:value="form.reason" type="textarea" /></n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="createVisible = false">取消</n-button>
        <n-button type="primary" :loading="saveLoading" @click="saveCreate">保存</n-button>
      </template>
    </n-modal>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NForm, NFormItem, NInput, NModal, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'

interface Row {
  id: number
  contentHash: string
  reason: string
  enabled: number
  createTime: string
}

const admin = useAdminStore()
const canWrite = computed(() => canWriteOps(admin.role))
const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0 })
const createVisible = ref(false)
const saveLoading = ref(false)
const form = reactive({ contentHash: '', reason: '' })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 90 },
  { title: '哈希', key: 'contentHash', ellipsis: { tooltip: true } },
  { title: '原因', key: 'reason', width: 160, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: (r) =>
      h(NTag, { size: 'small', type: r.enabled === 1 ? 'error' : 'default', bordered: false }, () =>
        r.enabled === 1 ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'op',
    width: 140,
    render: (r) =>
      canWrite.value
        ? h('div', { style: 'display:flex;gap:6px' }, [
            h(
              NButton,
              { size: 'small', tertiary: true, onClick: () => toggleEnabled(r) },
              () => (r.enabled === 1 ? '禁用' : '启用')
            ),
            h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => remove(r.id) }, () => '删除')
          ])
        : '—'
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listFileHashBlacklist(pagination.page, pagination.pageSize, keyword.value || undefined)
    const page = (res.data as { data?: { records?: Row[]; total?: number } }).data ?? res.data
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

function openCreate() {
  form.contentHash = ''
  form.reason = ''
  createVisible.value = true
}

async function saveCreate() {
  if (!form.contentHash.trim()) {
    message.warning('请输入哈希')
    return
  }
  saveLoading.value = true
  try {
    await adminApi.createFileHashBlacklist({ contentHash: form.contentHash, reason: form.reason, enabled: 1 })
    message.success('已添加')
    createVisible.value = false
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function toggleEnabled(row: Row) {
  try {
    await adminApi.updateFileHashBlacklist(row.id, { enabled: row.enabled === 1 ? 0 : 1 })
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

async function remove(id: number) {
  try {
    await adminApi.deleteFileHashBlacklist(id)
    message.success('已删除')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

onMounted(() => load())
</script>