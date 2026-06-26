<template>
  <AdminPageShell table>
    <template #toolbar>
      <div class="admin-toolbar-row">
        <n-input v-model:value="keyword" placeholder="群名称" clearable style="width: 240px" @keyup.enter="reload(1)" />
        <n-button type="primary" @click="reload(1)">搜索</n-button>
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
      :scroll-x="800"
      remote
      striped
      size="small"
      @update:page="(p) => { pagination.page = p; load() }"
      @update:page-size="(s) => { pagination.pageSize = s; pagination.page = 1; load() }"
    />
  </AdminPageShell>

  <n-drawer v-model:show="drawerShow" :width="640" placement="right">
      <n-drawer-content :title="detail?.groupName || '群详情'" closable>
        <n-spin :show="detailLoading">
          <template v-if="detail">
            <n-descriptions :column="1" label-placement="left" size="small" bordered>
              <n-descriptions-item label="群 ID">{{ detail.id }}</n-descriptions-item>
              <n-descriptions-item label="群主 ID">{{ detail.ownerId }}</n-descriptions-item>
              <n-descriptions-item label="成员数">{{ detail.memberCount }} / {{ detail.maxMembers }}</n-descriptions-item>
              <n-descriptions-item label="公告">{{ detail.notice || '—' }}</n-descriptions-item>
            </n-descriptions>
            <h4 class="sub-title">成员</h4>
            <n-data-table
              :columns="memberCols"
              :data="detail.members || []"
              size="small"
              :row-key="(m: Member) => m.userId"
            />
          </template>
        </n-spin>
      </n-drawer-content>
    </n-drawer>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import AdminPageShell from '../components/AdminPageShell.vue'
import {
  NButton,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NDrawer,
  NDrawerContent,
  NInput,
  NSpace,
  NSpin,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { adminApi } from '../api/client'

interface Row {
  id: number
  groupName: string
  ownerId: number
  maxMembers: number
  createTime: string
}

interface Member {
  userId: number
  username: string
  nickname: string
  role: number
  muted?: boolean
}

interface GroupDetail {
  id: number
  groupName: string
  ownerId: number
  maxMembers: number
  memberCount: number
  notice?: string
  members: Member[]
}

const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<Row[]>([])
const pagination = reactive({ page: 1, pageSize: 20, itemCount: 0, showSizePicker: true, pageSizes: [10, 20, 50] })
const drawerShow = ref(false)
const detailLoading = ref(false)
const detail = ref<GroupDetail | null>(null)
const currentGroupId = ref(0)

const memberCols: DataTableColumns<Member> = [
  { title: '用户 ID', key: 'userId', width: 90 },
  { title: '昵称', key: 'nickname' },
  {
    title: '角色',
    key: 'role',
    width: 70,
    render: (m) => (m.role === 2 ? '群主' : m.role === 1 ? '管理员' : '成员')
  },
  {
    title: '禁言',
    key: 'muted',
    width: 70,
    render: (m) => (m.muted ? '是' : '否')
  },
  {
    title: '操作',
    key: 'a',
    width: 120,
    render: (m) =>
      h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { size: 'tiny', quaternary: true, onClick: () => mute(m.userId, 60) },
          () => '禁言60分'
        ),
        h(NButton, { size: 'tiny', quaternary: true, onClick: () => unmute(m.userId) }, () => '解除')
      ])
  }
]

const columns: DataTableColumns<Row> = [
  { title: 'ID', key: 'id', width: 100 },
  { title: '群名称', key: 'groupName', minWidth: 140 },
  { title: '群主 ID', key: 'ownerId', width: 110 },
  { title: '人数上限', key: 'maxMembers', width: 90 },
  {
    title: '创建时间',
    key: 'createTime',
    minWidth: 160,
    render: (r) => r.createTime?.replace('T', ' ').substring(0, 19) || '—'
  },
  {
    title: '操作',
    key: 'a',
    width: 160,
    render: (row) =>
      h(NSpace, { size: 4 }, () => [
        h(NButton, { size: 'small', quaternary: true, type: 'primary', onClick: () => openDetail(row.id) }, () => '详情'),
        h(
          NButton,
          { size: 'small', type: 'error', quaternary: true, onClick: () => dissolve(row) },
          () => '解散'
        )
      ])
  }
]

async function openDetail(groupId: number) {
  currentGroupId.value = groupId
  drawerShow.value = true
  detailLoading.value = true
  try {
    const res = await adminApi.getGroup(groupId)
    detail.value = res.data.data
  } catch (e: unknown) {
    message.error((e as Error).message || '加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function mute(userId: number, minutes: number) {
  try {
    await adminApi.muteGroupMember(currentGroupId.value, userId, minutes)
    message.success('已禁言')
    openDetail(currentGroupId.value)
  } catch (e: unknown) {
    message.error((e as Error).message || '失败')
  }
}

async function unmute(userId: number) {
  try {
    await adminApi.unmuteGroupMember(currentGroupId.value, userId)
    message.success('已解除禁言')
    openDetail(currentGroupId.value)
  } catch (e: unknown) {
    message.error((e as Error).message || '失败')
  }
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.listGroups(pagination.page, pagination.pageSize, keyword.value || undefined)
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

async function dissolve(row: Row) {
  if (!confirm(`确定解散群「${row.groupName}」？`)) return
  try {
    await adminApi.dissolveGroup(row.id)
    message.success('已解散')
    load()
  } catch (e: unknown) {
    message.error((e as Error).message || '操作失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.sub-title {
  margin: 16px 0 8px;
  font-size: 14px;
}
</style>