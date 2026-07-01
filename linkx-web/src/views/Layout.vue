<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="app-layout">
    <!-- 行注：渲染 TitleBar 组件 -->
    <TitleBar />
    <!-- 行注：渲染容器 -->
    <div class="app-content">
      <!-- 行注：渲染容器 -->
      <div class="sidebar">
        <!-- 行注：渲染容器 -->
        <div class="sidebar-top">
          <!-- 行注：渲染容器 -->
          <div ref="sidebarAvatarRef" class="sidebar-avatar" @click="toggleProfileCard">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="userStore.avatar" :src="userStore.avatar" class="sidebar-avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else class="sidebar-avatar-text">{{ userStore.nickname?.charAt(0) || 'L' }}</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="sidebar-menu">
          <!-- 行注：渲染容器 -->
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'chat' }"
            @click="goTo('chat')"
          >
            <!-- 行注：渲染容器 -->
            <div class="sidebar-icon">
              <!-- 行注：渲染n-icon 节点 -->
              <n-icon size="22"><ChatbubblesOutline /></n-icon>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“聊天”文案 -->
            <span class="sidebar-label">聊天</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'friends' }"
            @click="goTo('friends')"
          >
            <!-- 行注：渲染容器 -->
            <div class="sidebar-icon">
              <!-- 行注：渲染n-icon 节点 -->
              <n-icon size="22"><PeopleOutline /></n-icon>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“联系人”文案 -->
            <span class="sidebar-label">联系人</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div
            class="sidebar-item sidebar-item-notif"
            title="系统通知"
            @click="showSystemNotifications = true"
          >
            <div class="sidebar-icon notif-icon-wrap">
              <n-icon size="22"><NotificationsOutline /></n-icon>
              <span v-if="notificationStore.unreadCount > 0" class="notif-badge">
                {{ notificationStore.unreadCount > 99 ? '99+' : notificationStore.unreadCount }}
              </span>
            </div>
            <span class="sidebar-label">通知</span>
          </div>
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'files' }"
            @click="goTo('files')"
          >
            <!-- 行注：渲染容器 -->
            <div class="sidebar-icon">
              <!-- 行注：渲染n-icon 节点 -->
              <n-icon size="22"><FolderOpenOutline /></n-icon>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“文件”文案 -->
            <span class="sidebar-label">文件</span>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'reports' }"
            title="我的举报"
            @click="goTo('reports')"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><WarningOutline /></n-icon>
            </div>
            <span class="sidebar-label">举报</span>
          </div>
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'settings' }"
            @click="goTo('settings')"
            title="设置"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><SettingsOutline /></n-icon>
            </div>
            <span class="sidebar-label">设置</span>
          </div>
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'blacklist' }"
            @click="goTo('blacklist')"
          >
            <!-- 行注：渲染容器 -->
            <div class="sidebar-icon">
              <!-- 行注：渲染n-icon 节点 -->
              <n-icon size="22"><BanOutline /></n-icon>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：展示“黑名单”文案 -->
            <span class="sidebar-label">黑名单</span>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="sidebar-bottom">
          <!-- 行注：渲染容器 -->
          <div class="sidebar-divider"></div>
          <!-- 行注：渲染容器 -->
          <div class="sidebar-item logout" @click="handleLogout">
            <!-- 行注：渲染容器 -->
            <div class="sidebar-icon">
              <!-- 行注：渲染n-icon 节点 -->
              <n-icon size="20"><LogOutOutline /></n-icon>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="main-content">
        <!-- 行注：渲染路由视图 -->
        <router-view />
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染 Teleport 组件 -->
    <Teleport to="body">
      <div
        v-if="showProfile"
        class="profile-overlay"
        @click.self="closeProfileCard"
      >
        <div
          class="profile-popup"
          :style="profilePopupStyle"
          @click.stop
        >
          <div class="profile-popup-arrow" aria-hidden="true" />
          <div class="profile-popup-top">
            <div class="profile-popup-avatar">
              <ProtectedImage
                v-if="profile.avatar || userStore.avatar"
                :src="profile.avatar || userStore.avatar"
                class="profile-popup-avatar-img"
              />
              <span v-else>{{ displayNickname.charAt(0) || '?' }}</span>
            </div>
            <div class="profile-popup-meta">
              <h2 class="profile-popup-title">{{ displayNickname }}</h2>
              <p class="profile-popup-username">用户名：{{ displayUsername }}</p>
              <p class="profile-popup-gender">性别：{{ genderLabel }}</p>
            </div>
          </div>
          <div class="profile-popup-info">
            <div class="profile-popup-info-item">
              <span class="info-label">昵称</span>
              <span class="info-value">{{ profile.nickname || displayNickname }}</span>
            </div>
            <div class="profile-popup-info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">{{ profile.username || displayUsername }}</span>
            </div>
          </div>
          <button type="button" class="profile-popup-settings-link" @click="openProfileEditPage">
            编辑资料
          </button>
        </div>
      </div>
    </Teleport>
    <SystemNotificationsDrawer v-model:visible="showSystemNotifications" />
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 主布局页面，提供侧边导航和公共应用壳层。
 */
import { ref, watch, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useNotificationStore } from '../stores/notification'
import ProtectedImage from '../components/ProtectedImage.vue'
import { userApi } from '../api/client'  // 行注：引入 userApi 能力
import { NIcon, useMessage } from 'naive-ui'  // 行注：引入 NIcon, useMessage 能力
import {
  ChatbubblesOutline,
  PeopleOutline,
  FolderOpenOutline,
  BanOutline,
  LogOutOutline,
  SettingsOutline,
  NotificationsOutline,
  WarningOutline
} from '@vicons/ionicons5'
import TitleBar from '../components/TitleBar.vue'  // 行注：引入 TitleBar 组件
import SystemNotificationsDrawer from '../components/SystemNotificationsDrawer.vue'
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()
const message = useMessage()
const currentTab = ref('chat')
const showProfile = ref(false)
const showSystemNotifications = ref(false)
const sidebarAvatarRef = ref<HTMLElement | null>(null)
const profilePopupPos = ref({ top: 0, left: 0 })
const profile = reactive({
  username: '',
  nickname: '',
  gender: 0,
  createTime: '',
  avatar: ''
})

const displayNickname = computed(
  () => profile.nickname || userStore.nickname || '未设置昵称'
)
const displayUsername = computed(() => profile.username || userStore.username || '-')
const genderLabel = computed(() => {
  const g = profile.gender
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '未知'
})

const profilePopupStyle = computed(() => ({
  top: `${profilePopupPos.value.top}px`,
  left: `${profilePopupPos.value.left}px`
}))

watch(() => route.path, (path) => {  // 行注：监听状态变化
  if (path.startsWith('/chat')) currentTab.value = 'chat'  // 行注：判断当前条件是否成立
  else if (path.startsWith('/groups')) currentTab.value = 'chat'  // 行注：继续判断其他分支条件
  else if (path.startsWith('/friends')) currentTab.value = 'friends'  // 行注：继续判断其他分支条件
  else if (path.startsWith('/files')) currentTab.value = 'files'  // 行注：继续判断其他分支条件
  else if (path.startsWith('/blacklist')) currentTab.value = 'blacklist'
  else if (path.startsWith('/reports')) currentTab.value = 'reports'
  else if (path.startsWith('/settings')) currentTab.value = 'settings'
}, { immediate: true })  // 行注：补充当前表达式

watch(showProfile, async (val) => {  // 行注：监听状态变化
  if (val) {  // 行注：判断当前条件是否成立
    try {  // 行注：尝试执行可能失败的逻辑
      const res: any = await userApi.getProfile()  // 行注：接收 res 异步结果
      const d = res.data  // 行注：初始化 d 变量
      profile.username = d.username  // 行注：更新 profile.username 值
      profile.nickname = d.nickname  // 行注：更新 profile.nickname 值
      profile.gender = d.gender || 0  // 行注：更新 profile.gender 值
      profile.createTime = d.createTime  // 行注：更新 profile.createTime 值
      profile.avatar = d.avatar || ''  // 行注：更新 profile.avatar 值
    } catch (e: any) {  // 行注：捕获并处理异常
      console.error('loadProfilePreview error:', e)  // 行注：输出错误日志
      message.error(e.response?.data?.message || '获取资料失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

function goTo(tab: string) {
  currentTab.value = tab
  router.push(`/${tab}`)
}

const PROFILE_POPUP_WIDTH = 300
const PROFILE_POPUP_GAP = 10

function updateProfilePopupPosition() {
  const el = sidebarAvatarRef.value
  if (!el) return
  const rect = el.getBoundingClientRect()
  const margin = 12
  const popupH = 220
  const isBottomNav = window.matchMedia('(max-width: 760px)').matches
  let top: number
  let left: number
  if (isBottomNav) {
    left = Math.max(
      margin,
      Math.min(rect.left, window.innerWidth - PROFILE_POPUP_WIDTH - margin)
    )
    top = rect.top - popupH - PROFILE_POPUP_GAP
    if (top < margin) top = rect.bottom + PROFILE_POPUP_GAP
  } else {
    top = rect.top + rect.height / 2 - 48
    left = rect.right + PROFILE_POPUP_GAP
    top = Math.max(margin, Math.min(top, window.innerHeight - margin - popupH))
    left = Math.min(left, window.innerWidth - PROFILE_POPUP_WIDTH - margin)
  }
  profilePopupPos.value = { top, left }
}

function closeProfileCard() {
  showProfile.value = false
}

async function toggleProfileCard() {
  if (showProfile.value) {
    closeProfileCard()
    return
  }
  showProfile.value = true
  await nextTick()
  updateProfilePopupPosition()
}

function openProfileEditPage() {
  closeProfileCard()
  router.push('/profile')
}

function onProfilePopupLayout() {
  if (showProfile.value) updateProfilePopupPosition()
}

function handleLogout() {  // 行注：定义 handleLogout 方法
  userStore.logout()  // 行注：调用 logout 方法
  router.push('/login')  // 行注：跳转到目标路由
}  // 行注：结束当前代码块

onMounted(() => {
  void notificationStore.refreshUnread()
  window.addEventListener('resize', onProfilePopupLayout)
  window.addEventListener('scroll', onProfilePopupLayout, true)
})

onUnmounted(() => {
  window.removeEventListener('resize', onProfilePopupLayout)
  window.removeEventListener('scroll', onProfilePopupLayout, true)
})

watch(showSystemNotifications, (open) => {
  if (!open) void notificationStore.refreshUnread()
})
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.app-layout {  /* 行注：定义 .app-layout 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  height: 100vh;  /* 行注：设置 height 样式 */
  height: 100dvh;  /* 行注：设置 height 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.app-content {  /* 行注：定义 .app-content 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.sidebar {  /* 行注：定义 .sidebar 样式 */
  width: var(--linkx-sidebar-width);  /* 行注：设置 width 样式 */
  background: var(--linkx-sidebar-bg);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(12px);
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  padding: 12px 0;  /* 行注：设置 padding 样式 */
  border-right: 1px solid var(--linkx-border);  /* 行注：设置 border-right 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  z-index: 10;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-top {  /* 行注：定义 .sidebar-top 样式 */
  margin-bottom: 16px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-avatar {  /* 行注：定义 .sidebar-avatar 样式 */
  width: 42px;  /* 行注：设置 width 样式 */
  height: 42px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  box-shadow: 0 4px 16px rgba(0, 214, 143, 0.3);  /* 行注：设置 box-shadow 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-avatar:hover {  /* 行注：定义 .sidebar-avatar:hover 样式 */
  transform: scale(1.05);  /* 行注：设置 transform 样式 */
  box-shadow: 0 6px 24px rgba(0, 214, 143, 0.4);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-avatar-img {  /* 行注：定义 .sidebar-avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-avatar-text {  /* 行注：定义 .sidebar-avatar-text 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-menu {  /* 行注：定义 .sidebar-menu 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item {  /* 行注：定义 .sidebar-item 样式 */
  width: 52px;  /* 行注：设置 width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  padding: 10px 0;  /* 行注：设置 padding 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item:hover {  /* 行注：定义 .sidebar-item:hover 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item.active {  /* 行注：定义 .sidebar-item.active 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-icon {  /* 行注：定义 .sidebar-icon 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item:hover .sidebar-icon {  /* 行注：定义 .sidebar-item:hover .sidebar-icon 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item.active .sidebar-icon {  /* 行注：定义 .sidebar-item.active .sidebar-icon 样式 */
  background: var(--linkx-primary-glow);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-label {  /* 行注：定义 .sidebar-label 样式 */
  font-size: 10px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  letter-spacing: 0.3px;  /* 行注：设置 letter-spacing 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-bottom {  /* 行注：定义 .sidebar-bottom 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-divider {  /* 行注：定义 .sidebar-divider 样式 */
  width: 24px;  /* 行注：设置 width 样式 */
  height: 1px;  /* 行注：设置 height 样式 */
  background: var(--linkx-border);  /* 行注：设置 background 样式 */
  margin-bottom: 8px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item.logout {  /* 行注：定义 .sidebar-item.logout 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item.logout:hover {  /* 行注：定义 .sidebar-item.logout:hover 样式 */
  color: var(--linkx-error);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.sidebar-item.logout:hover .sidebar-icon {  /* 行注：定义 .sidebar-item.logout:hover .sidebar-icon 样式 */
  background: rgba(255, 61, 113, 0.1);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.main-content {  /* 行注：定义 .main-content 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  min-height: 0;  /* 行注：设置 min-height 样式 */
}  /* 行注：结束当前样式块 */

/* 侧栏头像点击：微信式气泡，锚定在头像右侧 */
.profile-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: transparent;
}

.profile-popup {
  position: fixed;
  z-index: 1001;
  width: 300px;
  max-width: calc(100vw - 24px);
  padding: 16px 16px 0;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: 8px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.14);
  color: var(--linkx-text);
}

.profile-popup-arrow {
  position: absolute;
  left: -6px;
  top: 40px;
  width: 12px;
  height: 12px;
  background: var(--linkx-bg-card);
  border-left: 1px solid var(--linkx-border);
  border-bottom: 1px solid var(--linkx-border);
  transform: rotate(45deg);
  box-shadow: -2px 2px 4px rgba(0, 0, 0, 0.04);
}

.profile-popup-top {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 14px;
}

.profile-popup-avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 6px;
  background: var(--linkx-bg-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
  overflow: hidden;
}

.profile-popup-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-popup-meta {
  flex: 1;
  min-width: 0;
  padding-top: 2px;
}

.profile-popup-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.35;
  color: var(--linkx-text);
  word-break: break-word;
}

.profile-popup-username,
.profile-popup-gender {
  margin: 0 0 4px;
  font-size: 12px;
  line-height: 1.45;
  color: var(--linkx-text-secondary);
}

.profile-popup-info {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  overflow: hidden;
  margin-bottom: 10px;
}

.profile-popup-info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
}

.profile-popup-info-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.info-label {
  font-size: 13px;
  color: var(--linkx-text-secondary);
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: var(--linkx-text);
  text-align: right;
  word-break: break-word;
}

.profile-popup-settings-link {
  width: 100%;
  padding: 10px 12px;
  border: none;
  border-top: 1px solid var(--linkx-border);
  border-radius: 0;
  background: transparent;
  color: var(--linkx-primary);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.profile-popup-settings-link:hover {
  background: var(--linkx-bg-hover);
}

@media (max-width: 760px) {  /* 行注：声明响应式样式区块 */
  .app-content {  /* 行注：定义 .app-content 样式 */
    flex-direction: column-reverse;  /* 行注：设置 flex-direction 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar {  /* 行注：定义 .sidebar 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    min-height: 72px;  /* 行注：设置 min-height 样式 */
    flex-direction: row;  /* 行注：设置 flex-direction 样式 */
    justify-content: space-between;  /* 行注：设置 justify-content 样式 */
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom, 0px));  /* 行注：设置 padding 样式 */
    border-right: none;  /* 行注：设置 border-right 样式 */
    border-top: 1px solid var(--linkx-border);  /* 行注：设置 border-top 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-top,  /* 行注：补充 .sidebar-top 选择器 */
  .sidebar-bottom {  /* 行注：定义 .sidebar-bottom 样式 */
    margin: 0;  /* 行注：设置 margin 样式 */
    display: flex;  /* 行注：设置 display 样式 */
    align-items: center;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-menu {  /* 行注：定义 .sidebar-menu 样式 */
    flex: 1;  /* 行注：设置 flex 样式 */
    flex-direction: row;  /* 行注：设置 flex-direction 样式 */
    align-items: center;  /* 行注：设置 align-items 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
    gap: 8px;  /* 行注：设置 gap 样式 */
    min-width: 0;  /* 行注：设置 min-width 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-item {  /* 行注：定义 .sidebar-item 样式 */
    width: 60px;  /* 行注：设置 width 样式 */
    padding: 6px 0;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-divider {  /* 行注：定义 .sidebar-divider 样式 */
    display: none;  /* 行注：设置 display 样式 */
  }  /* 行注：结束当前样式块 */

  .main-content {  /* 行注：定义 .main-content 样式 */
    min-height: 0;  /* 行注：设置 min-height 样式 */
  }  /* 行注：结束当前样式块 */

}

@media (max-width: 520px) {  /* 行注：声明响应式样式区块 */
  .sidebar {  /* 行注：定义 .sidebar 样式 */
    padding-inline: 8px;  /* 行注：设置 padding-inline 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-item {  /* 行注：定义 .sidebar-item 样式 */
    width: 54px;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .sidebar-label {  /* 行注：定义 .sidebar-label 样式 */
    font-size: 9px;  /* 行注：设置 font-size 样式 */
  }  /* 行注：结束当前样式块 */

  .profile-popup {  /* 行注：定义 .profile-popup 样式 */
    width: calc(100vw - 16px);  /* 行注：设置 width 样式 */
    max-height: calc(100vh - 16px);  /* 行注：设置 max-height 样式 */
  }  /* 行注：结束当前样式块 */

  .profile-popup-info-item {  /* 行注：定义 .profile-popup-info-item 样式 */
    align-items: flex-start;  /* 行注：设置 align-items 样式 */
    gap: 12px;  /* 行注：设置 gap 样式 */
  }  /* 行注：结束当前样式块 */

  .info-value {  /* 行注：定义 .info-value 样式 */
    text-align: right;  /* 行注：设置 text-align 样式 */
    word-break: break-word;  /* 行注：设置 word-break 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 760px) {
  .profile-popup-arrow {
    display: none;
  }
}
</style>
