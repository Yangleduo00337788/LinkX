<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="keyword" placeholder="敏感词" clearable style="width: 180px" @keyup.enter="reload(1)" />
        <n-button type="primary" @click="reload(1)">查询</n-button>
        <n-button @click="openCreate">新增</n-button>
        <n-button secondary @click="refreshCache">刷新词库缓存</n-button>
        <n-button quaternary @click="showHits = !showHits">{{ showHits ? '词库列表' : '命中记录' }}</n-button>
        <div class="admin-toolbar-spacer" />
        <span class="admin-total-hint">共 {{ pagination.itemCount }} 条</span>
      </div>
    </template>
    <n-data-table
      v-if="!showHits"
      class="admin-full-table"
      :columns="wordColumns"
      :data="wordRows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: WordRow) => r.id"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; loadWords() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; loadWords() }"
    />
    <n-data-table
      v-else
      class="admin-full-table"
      :columns="hitColumns"
      :data="hitRows"
      :loading="hitLoading"
      :pagination="hitPagination"
      :row-key="(r: HitRow) => r.id"
      remote
      striped
      size="small"
      @update:page="(p) => { hitPagination.page = p; loadHits() }"
    />
    <n-modal v-model:show="createVisible" preset="card" title="新增敏感词" style="width: 440px">
      <n-form label-placement="left" label-width="80">
        <n-form-item label="词语"><n-input v-model:value="form.word" /></n-form-item>
        <n-form-item label="动作">
          <n-select v-model:value="form.action" :options="actionOptions" />
        </n-form-item>
        <n-form-item label="匹配">
          <n-select v-model:value="form.matchMode" :options="matchOptions" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-button @click="createVisible = false">取消</n-button>
        <n-button type="primary" :loading="saveLoading" @click="saveWord">保存</n-button>
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

interface WordRow {
  id: number
  word: string
  matchMode: number
  action: number
  enabled: number
}
interface HitRow {
  id: number
  userId: number
  matchedWord: string
  contentSnippet: string
  blocked: number
  createTime: string
}

const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const wordRows = ref<WordRow[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })
const showHits = ref(false)
const hitLoading = ref(false)
const hitRows = ref<HitRow[]>([])
const hitPagination = reactive({ page: 1, pageSize: 20, itemCount: 0 })

const actionOptions = [
  { label: '拦截发送', value: 1 },
  { label: '仅记录', value: 2 }
]
const matchOptions = [
  { label: '包含', value: 1 },
  { label: '全词', value: 2 }
]

const createVisible = ref(false)
const saveLoading = ref(false)
const form = reactive({ word: '', action: 1, matchMode: 1 })

const wordColumns: DataTableColumns<WordRow> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '词语', key: 'word', width: 160 },
  {
    title: '动作',
    key: 'action',
    width: 100,
    render: (r) => (r.action === 1 ? '拦截' : '记录')
  },
  {
    title: '状态',
    key: 'enabled',
    width: 80,
    render: (r) =>
      h(NTag, { size: 'small', type: r.enabled === 1 ? 'success' : 'default', bordered: false }, () =>
        r.enabled === 1 ? '启用' : '禁用'
      )
  },
  {
    title: '操作',
    key: 'op',
    width: 90,
    render: (r) =>
      h(NButton, { size: 'small', type: 'error', tertiary: true, onClick: () => removeWord(r.id) }, () => '删除')
  }
]

const hitColumns: DataTableColumns<HitRow> = [
  { title: '用户', key: 'userId', width: 90 },
  { title: '命中词', key: 'matchedWord', width: 120 },
  { title: '摘要', key: 'contentSnippet', ellipsis: { tooltip: true } },
  {
    title: '拦截',
    key: 'blocked',
    width: 70,
    render: (r) => (r.blocked === 1 ? '是' : '否')
  },
  {
    title: '时间',
    key: 'createTime',
    width: 172,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  }
]

async function loadWords() {
  loading.value = true
  try {
    const res = await adminApi.listSensitiveWords(pagination.page, pagination.pageSize, keyword.value || undefined)
    const page = res.data.data
    wordRows.value = page.records || []
    pagination.itemCount = page.total || 0
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadHits() {
  hitLoading.value = true
  try {
    const res = await adminApi.listSensitiveHits(hitPagination.page, hitPagination.pageSize)
    const page = res.data.data
    hitRows.value = page.records || []
    hitPagination.itemCount = page.total || 0
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    hitLoading.value = false
  }
}

function reload(p: number) {
  pagination.page = p
  loadWords()
}

function openCreate() {
  form.word = ''
  form.action = 1
  form.matchMode = 1
  createVisible.value = true
}

async function saveWord() {
  if (!form.word.trim()) {
    message.warning('请输入词语')
    return
  }
  saveLoading.value = true
  try {
    await adminApi.createSensitiveWord({ ...form, enabled: 1 })
    message.success('已添加')
    createVisible.value = false
    loadWords()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    saveLoading.value = false
  }
}

async function removeWord(id: number) {
  try {
    await adminApi.deleteSensitiveWord(id)
    message.success('已删除')
    loadWords()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

async function refreshCache() {
  try {
    await adminApi.refreshSensitiveWordCache()
    message.success('缓存已刷新')
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '刷新失败')
  }
}

onMounted(() => loadWords())
</script>