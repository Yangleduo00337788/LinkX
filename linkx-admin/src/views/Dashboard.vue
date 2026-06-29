<template>
  <AdminPageShell>
    <n-spin :show="loading">
      <div class="dashboard">
        <div class="dash-toolbar">
          <n-radio-group v-model:value="trendDays" size="small" @update:value="reload">
            <n-radio-button :value="7">近 7 天</n-radio-button>
            <n-radio-button :value="30">近 30 天</n-radio-button>
          </n-radio-group>
          <n-button size="small" quaternary :loading="loading" @click="reload">刷新</n-button>
        </div>

        <div v-if="alerts.length" class="alert-row">
          <n-alert
            v-for="(a, i) in alerts"
            :key="i"
            :type="alertType(a.level)"
            :bordered="false"
            size="small"
            class="alert-item"
          >
            {{ a.message }}
          </n-alert>
        </div>

        <div class="kpi-row">
          <div v-for="kpi in kpis" :key="kpi.key" class="kpi-card" @click="kpi.route && go(kpi.route)">
            <div class="kpi-top">
              <span class="kpi-label">{{ kpi.title }}</span>
              <n-icon :component="kpi.icon" class="kpi-icon" />
            </div>
            <div class="kpi-value">{{ formatNum(kpi.value) }}</div>
            <div v-if="kpi.sub" class="kpi-sub">{{ kpi.sub }}</div>
            <div v-if="kpi.rate != null" class="kpi-rate" :class="{ up: kpi.rate > 0, down: kpi.rate < 0 }">
              较昨日 {{ formatRate(kpi.rate) }}
            </div>
          </div>
        </div>

        <div class="todo-row">
          <div class="todo-card" @click="go('friend-requests')">
            <span class="todo-label">待处理好友申请</span>
            <span class="todo-num">{{ stats.pendingFriendRequests ?? 0 }}</span>
          </div>
          <div class="todo-card" @click="go('group-requests')">
            <span class="todo-label">待处理入群申请</span>
            <span class="todo-num">{{ stats.pendingGroupRequests ?? 0 }}</span>
          </div>
          <div class="todo-card todo-card--muted">
            <span class="todo-label">WebSocket 在线</span>
            <span class="todo-num">{{ stats.onlineUserCount ?? 0 }}</span>
          </div>
          <div class="todo-card todo-card--muted">
            <span class="todo-label">今日登录用户</span>
            <span class="todo-num">{{ stats.todayLoginUserCount ?? 0 }}</span>
          </div>
          <div class="todo-card todo-card--muted">
            <span class="todo-label">今日发消息用户</span>
            <span class="todo-num">{{ stats.todayActiveMessageUserCount ?? 0 }}</span>
          </div>
        </div>

        <div class="meta-row">
          <n-tag size="small" :bordered="false" type="default">禁用 {{ stats.disabledUserCount ?? 0 }}</n-tag>
          <n-tag size="small" :bordered="false" type="default">黑名单 {{ stats.blacklistCount ?? 0 }}</n-tag>
          <n-tag size="small" :bordered="false" type="warning">今日强退 {{ stats.todayKickCount ?? 0 }}</n-tag>
          <n-tag size="small" :bordered="false" :type="healthTag('mysql')">MySQL {{ stats.health?.mysql ?? '—' }}</n-tag>
          <n-tag size="small" :bordered="false" :type="healthTag('redis')">Redis {{ stats.health?.redis ?? '—' }}</n-tag>
          <n-tag size="small" :bordered="false" :type="healthTag('minio')">MinIO {{ stats.health?.minio ?? '—' }}</n-tag>
          <span class="storage-hint">
            存储 {{ formatBytes(stats.storage?.totalBytes ?? 0) }} / {{ stats.storage?.fileCount ?? stats.fileCount }} 个文件
          </span>
        </div>

        <n-grid cols="1 m:2" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card :title="`近 ${trendDays} 日新增用户`" size="small" :bordered="false" class="chart-card">
              <div ref="userChartRef" class="chart-box" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card :title="`近 ${trendDays} 日新增消息`" size="small" :bordered="false" class="chart-card">
              <div ref="msgChartRef" class="chart-box" />
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid cols="1 m:2 l:3" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card title="消息类型分布" size="small" :bordered="false" class="chart-card">
              <div ref="msgTypeChartRef" class="chart-box chart-box--pie" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="业务规模占比" size="small" :bordered="false" class="chart-card">
              <div ref="pieChartRef" class="chart-box chart-box--pie" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="客户端已发布版本" size="small" :bordered="false" class="chart-card">
              <n-empty v-if="!releases.length" description="暂无发布记录" size="small" />
              <ul v-else class="release-list">
                <li v-for="r in releases" :key="r.platform">
                  <n-tag size="small" round>{{ r.platform }}</n-tag>
                  <span class="release-ver">{{ r.version }}</span>
                </li>
              </ul>
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid cols="1 m:2" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card title="群成员数 Top5" size="small" :bordered="false" class="chart-card">
              <div ref="groupMemberChartRef" class="chart-box" />
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="近 7 日群消息 Top5" size="small" :bordered="false" class="chart-card">
              <div ref="groupMsgChartRef" class="chart-box" />
            </n-card>
          </n-gi>
        </n-grid>

        <n-grid cols="1 m:2" :x-gap="16" :y-gap="16">
          <n-gi>
            <n-card title="最近操作审计" size="small" :bordered="false" class="chart-card">
              <n-timeline v-if="auditLogs.length" size="small">
                <n-timeline-item
                  v-for="log in auditLogs"
                  :key="log.id"
                  :title="log.action"
                  :time="fmtTime(log.createTime)"
                >
                  {{ log.adminUsername }} · {{ log.detail || log.targetType + ' ' + (log.targetId || '') }}
                </n-timeline-item>
              </n-timeline>
              <n-empty v-else description="暂无审计记录" size="small" />
              <n-button size="tiny" quaternary type="primary" class="more-audit" @click="go('audit-logs')">
                查看全部
              </n-button>
            </n-card>
          </n-gi>
          <n-gi>
            <n-card title="快捷入口" size="small" :bordered="false" class="info-card">
              <n-space :size="8" wrap>
                <n-button size="small" secondary type="primary" @click="go('users')">用户管理</n-button>
                <n-button size="small" secondary @click="go('friend-requests')">好友申请</n-button>
                <n-button size="small" secondary @click="go('group-requests')">入群申请</n-button>
                <n-button size="small" secondary @click="go('messages')">消息监管</n-button>
                <n-button size="small" secondary @click="go('files')">文件管理</n-button>
                <n-button size="small" secondary @click="go('releases')">客户端发布</n-button>
                <n-button size="small" secondary @click="go('audit-logs')">操作审计</n-button>
              </n-space>
            </n-card>
          </n-gi>
        </n-grid>
      </div>
    </n-spin>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import type { ECharts } from 'echarts'
import {
  ChatbubblesOutline,
  FolderOpenOutline,
  PeopleOutline,
  PersonOutline
} from '@vicons/ionicons5'
import {
  NAlert,
  NButton,
  NCard,
  NEmpty,
  NGi,
  NGrid,
  NIcon,
  NRadioButton,
  NRadioGroup,
  NSpace,
  NSpin,
  NTag,
  NTimeline,
  NTimelineItem
} from 'naive-ui'
import AdminPageShell from '../components/AdminPageShell.vue'
import { adminApi } from '../api/client'

interface TrendPoint {
  date: string
  newUsers: number
  newMessages: number
}

interface Stats {
  userCount: number
  groupCount: number
  messageCount: number
  fileCount: number
  todayNewUsers?: number
  todayNewMessages?: number
  todayLoginUserCount?: number
  todayActiveMessageUserCount?: number
  disabledUserCount?: number
  blacklistCount?: number
  todayKickCount?: number
  pendingFriendRequests?: number
  pendingGroupRequests?: number
  trendDays?: number
  trend?: TrendPoint[]
  compare?: {
    newUsersDayOverDayRate?: number | null
    newMessagesDayOverDayRate?: number | null
  }
  messageTypeBreakdown?: { msgType: number; label: string; count: number }[]
  topGroupsByMembers?: { groupId: number; groupName: string; memberCount: number }[]
  topGroupsByMessages7d?: { groupId: number; groupName: string; messageCount7d: number }[]
  recentAuditLogs?: {
    id: number
    adminUsername: string
    action: string
    targetType?: string
    targetId?: string
    detail?: string
    createTime: string
  }[]
  onlineUserCount?: number
  health?: { mysql: string; redis: string; minio: string }
  alerts?: { level: string; code: string; message: string }[]
  storage?: { fileCount: number; totalBytes: number }
  publishedReleases?: { platform: string; version: string }[]
}

const router = useRouter()
const loading = ref(true)
const trendDays = ref<7 | 30>(7)
const stats = ref<Stats>({
  userCount: 0,
  groupCount: 0,
  messageCount: 0,
  fileCount: 0
})

const userChartRef = ref<HTMLElement | null>(null)
const msgChartRef = ref<HTMLElement | null>(null)
const pieChartRef = ref<HTMLElement | null>(null)
const msgTypeChartRef = ref<HTMLElement | null>(null)
const groupMemberChartRef = ref<HTMLElement | null>(null)
const groupMsgChartRef = ref<HTMLElement | null>(null)
const charts: ECharts[] = []

const PRIMARY = '#00A870'

const alerts = computed(() => stats.value.alerts || [])
const auditLogs = computed(() => stats.value.recentAuditLogs || [])
const releases = computed(() => stats.value.publishedReleases || [])

const kpis = computed(() => [
  {
    key: 'users',
    title: '注册用户',
    value: stats.value.userCount,
    sub: `今日 +${stats.value.todayNewUsers ?? 0}`,
    rate: stats.value.compare?.newUsersDayOverDayRate ?? null,
    icon: PersonOutline,
    route: 'users'
  },
  {
    key: 'groups',
    title: '群组',
    value: stats.value.groupCount,
    sub: '社群规模',
    rate: null,
    icon: PeopleOutline,
    route: 'groups'
  },
  {
    key: 'messages',
    title: '消息总量',
    value: stats.value.messageCount,
    sub: `今日 +${stats.value.todayNewMessages ?? 0}`,
    rate: stats.value.compare?.newMessagesDayOverDayRate ?? null,
    icon: ChatbubblesOutline,
    route: 'messages'
  },
  {
    key: 'files',
    title: '文件',
    value: stats.value.fileCount,
    sub: formatBytes(stats.value.storage?.totalBytes ?? 0),
    rate: null,
    icon: FolderOpenOutline,
    route: 'files'
  }
])

function formatNum(n: number) {
  if (n >= 10000) return `${(n / 10000).toFixed(1)}万`
  return String(n)
}

function formatBytes(n: number) {
  if (!n) return '0 B'
  if (n < 1024) return `${n} B`
  if (n < 1024 ** 2) return `${(n / 1024).toFixed(1)} KB`
  if (n < 1024 ** 3) return `${(n / 1024 ** 2).toFixed(2)} MB`
  return `${(n / 1024 ** 3).toFixed(2)} GB`
}

function formatRate(r: number | null) {
  if (r == null) return '—'
  const sign = r > 0 ? '+' : ''
  return `${sign}${r.toFixed(1)}%`
}

function fmtDateLabel(iso: string) {
  const p = iso.split('-')
  return p.length >= 3 ? `${p[1]}-${p[2]}` : iso
}

function fmtTime(t?: string) {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 19)
}

function alertType(level: string) {
  if (level === 'ERROR') return 'error'
  if (level === 'WARN') return 'warning'
  return 'info'
}

function healthTag(key: 'mysql' | 'redis' | 'minio') {
  const v = stats.value.health?.[key]
  if (v === 'UP') return 'success'
  if (v === 'DOWN') return 'error'
  return 'default'
}

function initChart(el: HTMLElement | null): ECharts | null {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

function lineOption(title: string, labels: string[], data: number[], color: string): echarts.EChartsOption {
  return {
    color: [color],
    grid: { left: 48, right: 24, top: 32, bottom: 32 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels, axisLabel: { color: '#666' } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f0f0f0' } } },
    series: [
      {
        name: title,
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        data
      }
    ]
  }
}

function barOption(names: string[], values: number[], color: string): echarts.EChartsOption {
  return {
    color: [color],
    grid: { left: 100, right: 24, top: 16, bottom: 24 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: names, axisLabel: { width: 90, overflow: 'truncate' } },
    series: [{ type: 'bar', data: values, barMaxWidth: 24 }]
  }
}

function renderCharts() {
  disposeCharts()
  const trend = stats.value.trend || []
  const labels = trend.map((t) => fmtDateLabel(t.date))
  const users = trend.map((t) => t.newUsers)
  const msgs = trend.map((t) => t.newMessages)

  initChart(userChartRef.value)?.setOption(lineOption('新增用户', labels, users, PRIMARY), true)
  initChart(msgChartRef.value)?.setOption(lineOption('新增消息', labels, msgs, '#3b82f6'), true)

  initChart(pieChartRef.value)?.setOption(
    {
      color: [PRIMARY, '#3b82f6', '#f59e0b', '#8b5cf6'],
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '65%'],
          data: [
            { name: '用户', value: stats.value.userCount },
            { name: '群组', value: stats.value.groupCount },
            { name: '消息', value: stats.value.messageCount },
            { name: '文件', value: stats.value.fileCount }
          ]
        }
      ]
    },
    true
  )

  const typeSlices = stats.value.messageTypeBreakdown || []
  initChart(msgTypeChartRef.value)?.setOption(
    {
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: '65%',
          data: typeSlices.map((s) => ({ name: s.label, value: s.count }))
        }
      ]
    },
    true
  )

  const topM = stats.value.topGroupsByMembers || []
  initChart(groupMemberChartRef.value)?.setOption(
    barOption(
      topM.map((g) => g.groupName || String(g.groupId)).reverse(),
      topM.map((g) => g.memberCount).reverse(),
      PRIMARY
    ),
    true
  )

  const topMsg = stats.value.topGroupsByMessages7d || []
  initChart(groupMsgChartRef.value)?.setOption(
    barOption(
      topMsg.map((g) => g.groupName || String(g.groupId)).reverse(),
      topMsg.map((g) => g.messageCount7d).reverse(),
      '#3b82f6'
    ),
    true
  )
}

function disposeCharts() {
  charts.forEach((c) => c.dispose())
  charts.length = 0
}

function onResize() {
  charts.forEach((c) => c.resize())
}

async function load() {
  loading.value = true
  try {
    const res = await adminApi.dashboardStats(trendDays.value)
    stats.value = res.data.data
  } finally {
    loading.value = false
    disposeCharts()
    setTimeout(renderCharts, 0)
  }
}

function reload() {
  load()
}

function go(name: string) {
  router.push({ name })
}

watch(trendDays, () => load())

let onlinePollTimer: ReturnType<typeof setInterval> | null = null

async function refreshOnlineCount() {
  try {
    const res = await adminApi.dashboardOnlineCount()
    const n = res.data?.data?.onlineUsers ?? res.data?.onlineUsers
    if (typeof n === 'number') {
      stats.value = { ...stats.value, onlineUserCount: n }
    }
  } catch {
    /* ignore */
  }
}

onMounted(() => {
  window.addEventListener('resize', onResize)
  load()
  refreshOnlineCount()
  onlinePollTimer = setInterval(refreshOnlineCount, 15000)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  disposeCharts()
  if (onlinePollTimer) clearInterval(onlinePollTimer)
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}
.dash-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.alert-row {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 1200px) {
  .kpi-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
.kpi-card {
  background: #fff;
  border: 1px solid #eef0f2;
  border-radius: 12px;
  padding: 16px 18px;
  cursor: pointer;
  transition: box-shadow 0.15s;
}
.kpi-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}
.kpi-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.kpi-label {
  font-size: 13px;
  color: #8c8c8c;
}
.kpi-icon {
  font-size: 22px;
  color: #00a870;
}
.kpi-value {
  font-size: 26px;
  font-weight: 700;
  margin-top: 6px;
}
.kpi-sub {
  font-size: 12px;
  color: #00a870;
  margin-top: 4px;
}
.kpi-rate {
  font-size: 12px;
  margin-top: 4px;
  color: #888;
}
.kpi-rate.up {
  color: #00a870;
}
.kpi-rate.down {
  color: #d03050;
}
.todo-row {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
@media (max-width: 1100px) {
  .todo-row {
    grid-template-columns: repeat(2, 1fr);
  }
}
.todo-card {
  background: #fff;
  border: 1px solid #eef0f2;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
}
.todo-card--muted {
  cursor: default;
}
.todo-label {
  display: block;
  font-size: 12px;
  color: #8c8c8c;
}
.todo-num {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a1a;
}
.meta-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.storage-hint {
  font-size: 12px;
  color: #666;
  margin-left: 8px;
}
.chart-card,
.info-card {
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.chart-box {
  width: 100%;
  height: 260px;
}
.chart-box--pie {
  height: 280px;
}
.release-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.release-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.release-ver {
  font-weight: 600;
}
.more-audit {
  margin-top: 8px;
}
</style>