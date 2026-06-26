<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="keyword" placeholder="用户名/显示名" clearable style="width: 200px" @keyup.enter="reload(1)" />
        <n-button type="primary" @click="reload(1)">查询</n-button>
        <n-button @click="openCreate">新建管理员</n-button>
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
    <n-modal v-model:show="createVisible" preset="card" title="新建管理员" style="width: 440px">
      <n-form label-placement="left" label-width="88">
        <n-form-item label="登录名"><n-input v-model:value="form.username" /></n-form-item>
        <n-form-item label="密码"><n-input v-model:value="form.password" type="password" show-password-on="click" /></n-form-item>
        <n-form-item label="显示名"><n-input v-model:value="form.displayName" /></n-form-item>
        <n-form-item label="角色"><n-select v-model:value="form.role" :options="roleOptions" /></n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="createVisible = false">取消</n-button>
        <n-button type="primary" :loading="saveLoading" @click="save">创建</n-button>
      </template>
    </n-modal>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NDataTable, NForm, NFormItem, NInput, NModal, NSelect, NTag, useMessage } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface Row {
  id: number
  username: string
  displayName: string
  role: string
  status: number
  lastLoginTime: string
}

const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })

const roleOptions = [
  { label: '超级管理员', value: 'SUPER_ADMIN' },
  { label: '运营', value: 'OPERATOR' },
  { label: '审核', value: 'AUDITOR' },
  { label: '只读', value: 'VIEWER' }
]

const createVisible = ref(false)
const saveLoading = ref(false)
const form = reactive({
  username: '',
  password: '',
  displayName: '',
  role: 'OPERATOR'
})

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '登录名', key: 'username', width: 120 },
  { title: '显示名', key: 'displayName', width: 120 },
  { title: '角色', key: 'role', width: 120 },
  {
    title: '状态',
    key: 'status',
    width: 80,
    render: (r) =>
      h(NTag, { size: 'small', type: r.status === 1 ? 'success' : 'error', bordered: false }, () =>
        r.status === 1 ? '正常' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'op',
    width: 120,
    render: (r) =>
      h(
        NButton,
        {
          size: 'small',
          onClick: () => toggleStatus(r)
        },
        () => (r.status === 1 ? '禁用' : '启用')
      )
  }
]

async function toggleStatus(r: Row) {
  try {
    await adminApi.setAdminStatus(r.id, r.status === 1 ? 0 : 1)
    message.success('已更新')
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  }
}

function openCreate() {
  form.username = ''
  form.password = ''
  form.displayName = ''
  form.role = 'OPERATOR'
  createVisible.value = true
}

async function save() {
  saveLoading.value = true
  try {
    await adminApi.createAdmin({ ...form })
    message.success('已创建')
    createVisible.value = false
    load()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '创建失败')
  } finally {
    saveLoading.value = false
  }
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listAdmins(pagination.page, pagination.pageSize, keyword.value || undefined)
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