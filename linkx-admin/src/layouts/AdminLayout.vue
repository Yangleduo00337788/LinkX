<template>
  <n-layout has-sider class="admin-root">
    <n-layout-sider
      v-model:collapsed="collapsed"
      bordered
      collapse-mode="width"
      :width="SIDER_WIDTH"
      :collapsed-width="SIDER_COLLAPSED_WIDTH"
      :native-scrollbar="false"
      :show-trigger="false"
      class="admin-sider"
    >
      <div class="brand" :class="{ 'brand--collapsed': collapsed }">
        <div class="brand-main">
          <img :src="brandLogo" alt="LinkX" class="brand-logo" />
          <transition name="brand-fade">
            <div v-if="!collapsed" class="brand-text">
              <span class="brand-title">LinkX</span>
              <span class="brand-sub">管理后台</span>
            </div>
          </transition>
        </div>
        <n-button
          quaternary
          circle
          size="small"
          class="sider-toggle"
          :title="collapsed ? '展开菜单' : '收起菜单'"
          @click="toggleCollapsed"
        >
          <template #icon>
            <n-icon :component="collapsed ? ChevronForwardOutline : ChevronBackOutline" />
          </template>
        </n-button>
      </div>

      <n-menu
        :value="activeKey"
        :options="menuOptions"
        :collapsed="collapsed"
        :collapsed-width="SIDER_COLLAPSED_WIDTH"
        :collapsed-icon-size="22"
        :indent="20"
        :root-indent="collapsed ? 0 : 12"
        @update:value="onMenuSelect"
      />
    </n-layout-sider>

    <n-layout class="main-layout">
      <n-layout-header bordered class="header">
        <div class="header-left">
          <n-button
            quaternary
            circle
            class="collapse-btn"
            :title="collapsed ? '展开菜单' : '收起菜单'"
            @click="toggleCollapsed"
          >
            <template #icon>
              <n-icon :size="20" :component="MenuOutline" />
            </template>
          </n-button>
          <h1 class="header-title">{{ headerTitle }}</h1>
        </div>
        <div class="header-right">
          <n-tag size="small" :bordered="false" type="success" class="role-tag">
            {{ admin.displayName || '管理员' }}
          </n-tag>
          <n-button size="small" secondary @click="onLogout">退出登录</n-button>
        </div>
      </n-layout-header>

      <n-layout-content class="content">
        <div class="content-inner">
          <router-view :key="route.fullPath" />
        </div>
      </n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  NButton,
  NIcon,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NTag
} from 'naive-ui'
import type { MenuOption } from 'naive-ui'
import type { Component } from 'vue'
import {
  BanOutline,
  ChatbubblesOutline,
  ChevronBackOutline,
  ChevronForwardOutline,
  CloudDownloadOutline,
  DocumentTextOutline,
  FolderOpenOutline,
  GridOutline,
  KeyOutline,
  MenuOutline,
  ShieldCheckmarkOutline,
  WarningOutline,
  PeopleOutline,
  PersonAddOutline,
  PersonOutline
} from '@vicons/ionicons5'
import { useAdminStore } from '../stores/admin'
import brandLogo from '../assets/tray.png'

const SIDER_WIDTH = 240
const SIDER_COLLAPSED_WIDTH = 64

const route = useRoute()
const router = useRouter()
const admin = useAdminStore()
const collapsed = ref(false)

function toggleCollapsed() {
  collapsed.value = !collapsed.value
}

const activeKey = computed(() => {
  const name = route.name as string
  if (name === 'user-detail') return 'users'
  return name
})

const titleMap: Record<string, string> = {
  dashboard: '数据概览',
  users: '用户管理',
  'user-detail': '用户详情',
  'friend-requests': '好友申请',
  'group-requests': '入群申请',
  groups: '群组管理',
  messages: '消息监管',
  files: '文件管理',
  blacklist: '黑名单',
  releases: '客户端发布',
  'audit-logs': '操作审计',
  'login-logs': '登录日志',
  reports: '举报管理',
  'sensitive-words': '敏感词库',
  admins: '管理员'
}

const headerTitle = computed(() => titleMap[route.name as string] || 'LinkX 管理后台')

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

/** 路由叶子菜单（单一数据源） */
const routeMenuItems: Array<Pick<MenuOption, 'label' | 'key'> & { icon: Component }> = [
  { label: '数据概览', key: 'dashboard', icon: GridOutline },
  { label: '用户管理', key: 'users', icon: PersonOutline },
  { label: '好友申请', key: 'friend-requests', icon: PersonAddOutline },
  { label: '黑名单', key: 'blacklist', icon: BanOutline },
  { label: '群组管理', key: 'groups', icon: PeopleOutline },
  { label: '入群申请', key: 'group-requests', icon: PersonAddOutline },
  { label: '消息监管', key: 'messages', icon: ChatbubblesOutline },
  { label: '文件管理', key: 'files', icon: FolderOpenOutline },
  { label: '客户端发布', key: 'releases', icon: CloudDownloadOutline },
  { label: '操作审计', key: 'audit-logs', icon: DocumentTextOutline },
  { label: '登录日志', key: 'login-logs', icon: KeyOutline },
  { label: '举报管理', key: 'reports', icon: WarningOutline },
  { label: '敏感词库', key: 'sensitive-words', icon: ShieldCheckmarkOutline },
  { label: '管理员', key: 'admins', icon: PeopleOutline }
]

function toLeafOption(item: (typeof routeMenuItems)[number]): MenuOption {
  return {
    label: item.label,
    key: item.key,
    icon: renderIcon(item.icon)
  }
}

/**
 * 展开：分组菜单；折叠：仅叶子 + 图标（Naive 折叠态对 divider/group 支持差，勿混用）
 */
const menuOptions = computed<MenuOption[]>(() => {
  if (collapsed.value) {
    return routeMenuItems.map(toLeafOption)
  }

  const pick = (...keys: string[]) =>
    keys.map((k) => routeMenuItems.find((i) => i.key === k)).filter(Boolean).map((i) => toLeafOption(i!))

  return [
    toLeafOption(routeMenuItems[0]),
    { type: 'divider', key: 'divider-1' },
    {
      label: '用户与社交',
      key: 'group-social',
      type: 'group',
      children: pick('users', 'friend-requests', 'blacklist')
    },
    {
      label: '群组',
      key: 'group-chat',
      type: 'group',
      children: pick('groups', 'group-requests')
    },
    {
      label: '内容与资源',
      key: 'group-content',
      type: 'group',
      children: pick('messages', 'files')
    },
    { type: 'divider', key: 'divider-2' },
    {
      label: '合规与安全',
      key: 'group-compliance',
      type: 'group',
      children: pick('login-logs', 'reports', 'sensitive-words')
    },
    { type: 'divider', key: 'divider-3' },
    ...pick('releases', 'audit-logs', 'admins')
  ]
})

function onMenuSelect(key: string) {
  if (key.startsWith('group-') || key.startsWith('divider')) return
  if (route.name === key) {
    router.replace({ name: key, query: { _r: String(Date.now()) } })
    return
  }
  router.push({ name: key })
}

function onLogout() {
  admin.logout()
  router.push({ name: 'login' })
}

onMounted(() => {
  admin.fetchProfile().catch(() => {
    admin.logout()
    router.push({ name: 'login' })
  })
})
</script>

<style scoped>
.admin-root {
  height: 100vh;
}

.main-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

.admin-sider :deep(.n-layout-sider-scroll-container) {
  display: flex;
  flex-direction: column;
}

.brand {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-height: 72px;
  padding: 14px 48px 14px 16px;
  border-bottom: 1px solid var(--n-border-color);
  flex-shrink: 0;
  overflow: hidden;
}

.brand--collapsed {
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 12px 8px;
}

.brand-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.brand--collapsed .brand-main {
  flex: 0;
  justify-content: center;
}

.sider-toggle {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  flex-shrink: 0;
}

.brand--collapsed .sider-toggle {
  position: static;
  transform: none;
}

.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  flex-shrink: 0;
}

.brand--collapsed .brand-logo {
  width: 40px;
  height: 40px;
}

.brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
  min-width: 0;
}

.brand-title {
  font-weight: 700;
  font-size: 16px;
  color: #1a1a1a;
}

.brand-sub {
  font-size: 12px;
  color: #8c8c8c;
}

.brand-fade-enter-active,
.brand-fade-leave-active {
  transition: opacity 0.15s ease;
}

.brand-fade-enter-from,
.brand-fade-leave-to {
  opacity: 0;
}

.admin-sider :deep(.n-menu) {
  flex: 1;
  padding-top: 8px;
  padding-bottom: 12px;
}

.admin-sider :deep(.n-menu-item-group-title) {
  font-size: 11px;
  letter-spacing: 0.04em;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.collapse-btn {
  flex-shrink: 0;
}

.header-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.content {
  padding: 16px;
  height: calc(100vh - 60px);
  overflow: auto;
}

.content-inner {
  width: 100%;
  min-height: min-content;
}
</style>