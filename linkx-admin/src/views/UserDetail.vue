<template>
  <div class="page">
    <n-button quaternary size="small" class="back" @click="router.push({ name: 'users' })">← 返回用户列表</n-button>
    <n-spin :show="loading">
      <n-card v-if="user" title="基本信息" size="small" class="card">
        <n-descriptions :column="2" label-placement="left" bordered size="small">
          <n-descriptions-item label="ID">{{ user.id }}</n-descriptions-item>
          <n-descriptions-item label="用户名">{{ user.username }}</n-descriptions-item>
          <n-descriptions-item label="昵称">{{ user.nickname }}</n-descriptions-item>
          <n-descriptions-item label="状态">
            <n-tag :type="user.status === 1 ? 'success' : 'error'" size="small">
              {{ user.status === 1 ? '正常' : '已禁用' }}
            </n-tag>
          </n-descriptions-item>
          <n-descriptions-item label="手机">{{ user.phone || '—' }}</n-descriptions-item>
          <n-descriptions-item label="邮箱">{{ user.email || '—' }}</n-descriptions-item>
          <n-descriptions-item label="注册时间">{{ fmt(user.createTime) }}</n-descriptions-item>
        </n-descriptions>
        <n-space class="actions" :size="12">
          <n-button
            v-if="user.status === 1"
            type="error"
            size="small"
            @click="setStatus(0)"
          >禁用账号</n-button>
          <n-button v-else type="primary" size="small" @click="setStatus(1)">解禁账号</n-button>
          <n-button type="warning" size="small" @click="kick">强制下线</n-button>
        </n-space>
      </n-card>

      <n-card title="最近登录" size="small" class="card">
        <template #header-extra>
          <n-button text type="primary" size="small" @click="router.push({ name: 'login-logs', query: { userId: userId } })">
            全部登录日志
          </n-button>
        </template>
        <n-data-table
          :columns="loginCols"
          :data="loginRows"
          :loading="loginLoading"
          :pagination="false"
          size="small"
        />
      </n-card>

      <n-card title="好友列表" size="small" class="card">
        <n-data-table
          :columns="friendCols"
          :data="friends"
          :loading="friendsLoading"
          :pagination="friendPagination"
          :row-key="(r: FriendRow) => r.id"
          remote
          size="small"
          @update:page="(p) => { friendPagination.page = p; loadFriends() }"
        />
      </n-card>
    </n-spin>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NCard,
  NDataTable,
  NDescriptions,
  NDescriptionsItem,
  NSpace,
  NSpin,
  NTag,
  useMessage
} from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import { adminApi } from '../api/client'

interface UserDetail {
  id: number | string
  username: string
  nickname: string
  phone?: string
  email?: string
  status: number
  createTime: string
}

interface FriendRow {
  id: number
  friendId: number
  friendUsername: string
  friendNickname: string
  remark: string
  createTime: string
}

const route = useRoute()
const router = useRouter()
const message = useMessage()
/** 路由 ID 用字符串，避免大整数经 Number() 精度丢失 */
const userId = String(route.params.id)
const loading = ref(true)
const user = ref<UserDetail | null>(null)
const friends = ref<FriendRow[]>([])
const friendsLoading = ref(false)
const friendPagination = reactive({ page: 1, pageSize: 10, itemCount: 0 })
const loginRows = ref<
  { id: number; loginIp: string; loginStatus: number; failReason: string; createTime: string }[]
>([])
const loginLoading = ref(false)

const loginCols: DataTableColumns<(typeof loginRows.value)[number]> = [
  { title: 'IP', key: 'loginIp', width: 130 },
  {
    title: '结果',
    key: 'loginStatus',
    width: 80,
    render: (r) => (r.loginStatus === 1 ? '成功' : '失败')
  },
  { title: '失败原因', key: 'failReason', ellipsis: { tooltip: true } },
  { title: '时间', key: 'createTime', render: (r) => fmt(r.createTime) }
]

function fmt(t?: string) {
  return t?.replace('T', ' ').substring(0, 19) || '—'
}

const friendCols: DataTableColumns<FriendRow> = [
  { title: '好友 ID', key: 'friendId', width: 100 },
  { title: '用户名', key: 'friendUsername' },
  { title: '昵称', key: 'friendNickname' },
  { title: '备注', key: 'remark' },
  { title: '添加时间', key: 'createTime', render: (r) => fmt(r.createTime) }
]

async function loadUser() {
  loading.value = true
  try {
    const res = await adminApi.getUser(userId)
    user.value = res.data.data
  } catch (e: unknown) {
    message.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function loadFriends() {
  friendsLoading.value = true
  try {
    const res = await adminApi.listUserFriends(userId, friendPagination.page, friendPagination.pageSize)
    const page = res.data.data
    friends.value = page.records || []
    friendPagination.itemCount = page.total || 0
  } finally {
    friendsLoading.value = false
  }
}

async function loadLoginLogs() {
  loginLoading.value = true
  try {
    const res = await adminApi.listLoginLogs(1, 8, undefined, userId)
    loginRows.value = res.data.data.records || []
  } finally {
    loginLoading.value = false
  }
}

async function setStatus(status: number) {
  try {
    await adminApi.setUserStatus(userId, status)
    message.success('已更新')
    loadUser()
  } catch (e: unknown) {
    message.error((e as Error).message || '失败')
  }
}

async function kick() {
  if (!confirm('强制该用户下线？将使其当前 access/refresh 失效。')) return
  try {
    await adminApi.kickUser(userId)
    message.success('已强制下线')
  } catch (e: unknown) {
    message.error((e as Error).message || '失败')
  }
}

onMounted(() => {
  loadUser()
  loadFriends()
  loadLoginLogs()
})
</script>

<style scoped>
.page {
  max-width: 900px;
}
.back {
  margin-bottom: 12px;
}
.card {
  margin-bottom: 16px;
  border-radius: 12px;
}
.actions {
  margin-top: 16px;
}
</style>