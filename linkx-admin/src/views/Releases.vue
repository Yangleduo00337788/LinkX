<template>
  <AdminPageShell
    table
    hint="管理桌面端（Electron）检查更新所用的版本与下载地址；对应客户端「设置 → 检查更新」。"
  >
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-select v-model:value="platform" :options="platformOptions" clearable placeholder="平台" style="width: 140px" />
        <n-button quaternary @click="onResetPlatform">重置</n-button>
        <div class="admin-toolbar-spacer" />
        <span class="admin-total-hint">共 {{ pagination.itemCount }} 条</span>
        <n-button type="primary" @click="showCreate = true">发布新版本</n-button>
      </div>
    </template>
    <n-data-table
      flex-height
      class="admin-full-table"
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: Row) => r.id"
      :scroll-x="880"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>

  <n-modal v-model:show="showCreate" preset="card" title="新建客户端版本" style="width: 480px">
      <n-form label-placement="left" label-width="88">
        <n-form-item label="平台">
          <n-select v-model:value="form.platform" :options="platformOptions" />
        </n-form-item>
        <n-form-item label="版本号">
          <n-input v-model:value="form.version" placeholder="如 1.0.1" />
        </n-form-item>
        <n-form-item label="下载地址">
          <n-input v-model:value="form.downloadUrl" placeholder="https://..." />
        </n-form-item>
        <n-form-item label="更新说明">
          <n-input v-model:value="form.releaseNotes" type="textarea" :rows="3" />
        </n-form-item>
        <n-form-item label="强制更新">
          <n-switch v-model:value="form.forceUpdate" />
        </n-form-item>
        <n-form-item label="立即发布">
          <n-switch v-model:value="form.published" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="create">保存</n-button>
        </n-space>
      </template>
    </n-modal>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import AdminPageShell from '../components/AdminPageShell.vue'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NSelect,
  NSpace,
  NSwitch,
  NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { adminApi } from '../api/client'

interface Row {
  id: number
  platform: string
  version: string
  downloadUrl: string
  forceUpdate: number
  published: number
  createTime: string
}

const message = useMessage()
const platform = ref<string | null>(null)
const platformOptions = [
  { label: 'Windows', value: 'win' },
  { label: 'macOS', value: 'mac' },
  { label: 'Linux', value: 'linux' }
]
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20] })

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({
  platform: 'win',
  version: '',
  downloadUrl: '',
  releaseNotes: '',
  forceUpdate: false,
  published: false
})

const columns: DataTableColumns<Row> = [
  { title: '平台', key: 'platform', width: 80 },
  { title: '版本', key: 'version', width: 100 },
  { title: '下载地址', key: 'downloadUrl', ellipsis: { tooltip: true } },
  {
    title: '强制',
    key: 'forceUpdate',
    width: 70,
    render: (r) => (r.forceUpdate === 1 ? '是' : '否')
  },
  {
    title: '状态',
    key: 'published',
    width: 90,
    render: (r) =>
      h(NTag, { type: r.published === 1 ? 'success' : 'default', size: 'small' }, () =>
        r.published === 1 ? '已发布' : '草稿'
      )
  },
  {
    title: '操作',
    key: 'a',
    width: 100,
    render: (row) =>
      h(
        NButton,
        {
          size: 'small',
          quaternary: true,
          onClick: () => togglePub(row)
        },
        () => (row.published === 1 ? '下架' : '发布')
      )
  }
]

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listReleases(
      pagination.page,
      pagination.pageSize,
      platform.value || undefined
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

watch(platform, () => {
  pagination.page = 1
  load()
})

function onResetPlatform() {
  platform.value = null
  pagination.page = 1
  load()
}

async function create() {
  if (!form.version.trim()) {
    message.warning('请填写版本号')
    return
  }
  creating.value = true
  try {
    await adminApi.createRelease({ ...form })
    message.success('已创建')
    showCreate.value = false
    form.version = ''
    form.downloadUrl = ''
    form.releaseNotes = ''
    load()
  } catch (e: unknown) {
    message.error((e as Error).message || '保存失败')
  } finally {
    creating.value = false
  }
}

async function togglePub(row: Row) {
  try {
    await adminApi.setReleasePublished(row.id, row.published !== 1)
    message.success('已更新')
    load()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  }
}

onMounted(() => load())
</script>