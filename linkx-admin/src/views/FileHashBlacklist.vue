<template>
  <AdminPageShell table hint="封禁指定文件内容哈希后，用户上传/发送相同哈希的文件将被拦截。">
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="keyword" placeholder="哈希关键词" clearable style="width: 220px" @keyup.enter="reload(1)" />
        <n-button type="primary" @click="reload(1)">查询</n-button>
        <n-button @click="openAdd">新增</n-button>
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
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
    <n-modal v-model:show="addVisible" preset="card" title="封禁文件哈希" style="width: 480px">
      <n-form label-placement="left" label-width="72">
        <n-form-item label="哈希" required>
          <n-input v-model:value="form.contentHash" placeholder="SHA-256 等" />
        </n-form-item>
        <n-form-item label="原因">
          <n-input v-model:value="form.reason" type="textarea" :rows="2" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="addVisible = false">取消</n-button>
        <n-button type="primary" :loading="saving" @click="saveAdd">保存</n-button>
      </template>
    </n-modal>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NForm, NFormItem, NInput, NModal, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'
import { useAdminStore } from '../stores/admin'
import { canWriteOps } from '../utils/adminPermissions'

interface Row {
  id: number
  contentHash: string
  reason?: string
  enabled: number
  createTime?: string
}

const admin = useAdminStore()
const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })
const addVisible = ref(false)
const saving = ref(false)
const form = reactive({ contentHash: '', reason: '' })

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '内容哈希', key: 'contentHash', minWidth: 280, ellipsis: { tooltip: true } },
  { title: '原因', key: 'reason', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '状态',
    key: 'enabled',
    width: 88,
    render: (r) =>
      h(NTag, { size: 'small', type: r.enabled === 1 ? 'success' : 'default', bordered: false }, () =>
        r.enabled === 1 ? '启用' : '禁用'
      )
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
    width: 160,
    render: (r) => {
      if (!canWriteOps(admin.role)) return '—'
      return h('div', { style: 'display:flex;gap:6px' }, [
        h(
          NButton,
          { size: 'small', tertiary: true, onClick: () => toggleEnabled(r) },
          () => (r.enabled === 1 ? '禁用' : '启用')
        ),
        h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => remove(r.id) }, () => '删除')
      ])
    }
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listFileHashBlacklist(pagination.page, pagination.pageSize, keyword.value || undefined)
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

function openAdd() {
  form.contentHash = ''
  form.reason = ''
  addVisible.value = true
}

async function saveAdd() {
  if (!form.contentHash.trim()) {
    message.warning('请填写哈希')
    return
  }
  saving.value = true
  try {
    await adminApi.addFileHashBlacklist({ contentHash: form.contentHash.trim(), reason: form.reason || undefined })
    message.success('已添加')
    addVisible.value = false
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function toggleEnabled(row: Row) {
  try {
    await adminApi.setFileHashBlacklistEnabled(row.id, row.enabled === 1 ? 0 : 1)
    message.success('已更新')
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