<template>
  <div class="app-layout">
    <TitleBar />
    <div class="app-content">
      <div class="sidebar">
        <div class="sidebar-top">
          <div class="sidebar-avatar" @click="goTo('profile')">
            <img v-if="userStore.avatar" :src="userStore.avatar" class="sidebar-avatar-img" />
            <span v-else class="sidebar-avatar-text">{{ userStore.nickname?.charAt(0) || 'L' }}</span>
          </div>
        </div>

        <div class="sidebar-menu">
          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'chat' }"
            @click="goTo('chat')"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><ChatbubblesOutline /></n-icon>
            </div>
            <span class="sidebar-label">聊天</span>
          </div>

          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'friends' }"
            @click="goTo('friends')"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><PeopleOutline /></n-icon>
            </div>
            <span class="sidebar-label">联系人</span>
          </div>

          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'files' }"
            @click="goTo('files')"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><FolderOpenOutline /></n-icon>
            </div>
            <span class="sidebar-label">文件</span>
          </div>

          <div
            class="sidebar-item"
            :class="{ active: currentTab === 'blacklist' }"
            @click="goTo('blacklist')"
          >
            <div class="sidebar-icon">
              <n-icon size="22"><BanOutline /></n-icon>
            </div>
            <span class="sidebar-label">黑名单</span>
          </div>
        </div>

        <div class="sidebar-bottom">
          <div class="sidebar-divider"></div>
          <div class="sidebar-item logout" @click="handleLogout">
            <div class="sidebar-icon">
              <n-icon size="20"><LogOutOutline /></n-icon>
            </div>
          </div>
        </div>
      </div>

      <div class="main-content">
        <router-view />
      </div>
    </div>

    <Teleport to="body">
      <div v-if="showProfile" class="profile-overlay" @click.self="showProfile = false">
        <div class="profile-popup">
          <button class="profile-close" @click="showProfile = false">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
          <div class="profile-popup-header">
            <div class="profile-popup-cover"></div>
            <div class="profile-popup-avatar-wrapper">
              <div class="profile-popup-avatar">
                <img v-if="userStore.avatar" :src="userStore.avatar" class="profile-popup-avatar-img" />
                <span v-else>{{ userStore.nickname?.charAt(0) || '?' }}</span>
              </div>
            </div>
          </div>
          <div class="profile-popup-content">
            <div class="profile-popup-name">
              <h2>{{ userStore.nickname || '未设置昵称' }}</h2>
              <span class="profile-popup-username">@{{ userStore.username }}</span>
            </div>
            <div class="profile-popup-info">
              <div class="profile-popup-info-item">
                <span class="info-label">昵称</span>
                <span class="info-value">{{ profile.nickname || '-' }}</span>
              </div>
              <div class="profile-popup-info-item">
                <span class="info-label">性别</span>
                <span class="info-value">{{ profile.gender === 1 ? '男' : profile.gender === 2 ? '女' : '未知' }}</span>
              </div>
              <div class="profile-popup-info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ profile.createTime?.substring(0, 10) || '-' }}</span>
              </div>
              <div class="profile-popup-info-item">
                <span class="info-label">用户名</span>
                <span class="info-value">{{ profile.username }}</span>
              </div>
            </div>
            <div class="profile-popup-theme">
              <span class="theme-label">主题</span>
              <div class="theme-options">
                <button class="theme-btn" :class="{ active: themeMode === 'light' }" @click="setThemeMode('light')">浅色</button>
                <button class="theme-btn" :class="{ active: themeMode === 'dark' }" @click="setThemeMode('dark')">深色</button>
                <button class="theme-btn" :class="{ active: themeMode === 'system' }" @click="setThemeMode('system')">跟随系统</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { userApi } from '../api/client'
import { NIcon, useMessage } from 'naive-ui'
import { ChatbubblesOutline, PeopleOutline, FolderOpenOutline, BanOutline, LogOutOutline } from '@vicons/ionicons5'
import TitleBar from '../components/TitleBar.vue'
import { useTheme } from '../utils/theme'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const message = useMessage()
const currentTab = ref('chat')
const showProfile = ref(false)
const { mode: themeMode, setMode: setThemeMode } = useTheme()
const profile = reactive({
  username: '',
  nickname: '',
  gender: 0,
  createTime: '',
  avatar: ''
})

watch(() => route.path, (path) => {
  if (path.startsWith('/chat')) currentTab.value = 'chat'
  else if (path.startsWith('/groups')) currentTab.value = 'chat'
  else if (path.startsWith('/friends')) currentTab.value = 'friends'
  else if (path.startsWith('/files')) currentTab.value = 'files'
  else if (path.startsWith('/blacklist')) currentTab.value = 'blacklist'
  else if (path.startsWith('/profile')) currentTab.value = 'profile'
}, { immediate: true })

watch(showProfile, async (val) => {
  if (val) {
    try {
      const res: any = await userApi.getProfile()
      const d = res.data
      profile.username = d.username
      profile.nickname = d.nickname
      profile.gender = d.gender || 0
      profile.createTime = d.createTime
      profile.avatar = d.avatar || ''
    } catch (e: any) {
      console.error('loadProfilePreview error:', e)
      message.error(e.response?.data?.message || '获取资料失败')
    }
  }
})

function goTo(tab: string) {
  currentTab.value = tab
  router.push(`/${tab}`)
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
}

.app-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: var(--linkx-sidebar-width);
  background: var(--linkx-sidebar-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 0;
  border-right: 1px solid var(--linkx-border);
  position: relative;
  z-index: 10;
}

.sidebar-top {
  margin-bottom: 16px;
}

.sidebar-avatar {
  width: 42px;
  height: 42px;
  border-radius: var(--linkx-radius);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 700;
  box-shadow: 0 4px 16px rgba(0, 214, 143, 0.3);
  transition: var(--linkx-transition);
  cursor: pointer;
  overflow: hidden;
}

.sidebar-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 24px rgba(0, 214, 143, 0.4);
}

.sidebar-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.sidebar-avatar-text {
  color: white;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.sidebar-item {
  width: 52px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 0;
  color: var(--linkx-text-secondary);
  cursor: pointer;
  transition: var(--linkx-transition);
  position: relative;
  border-radius: var(--linkx-radius);
}

.sidebar-item:hover {
  color: var(--linkx-text);
}

.sidebar-item.active {
  color: var(--linkx-primary);
}

.sidebar-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-sm);
  transition: var(--linkx-transition);
}

.sidebar-item:hover .sidebar-icon {
  background: var(--linkx-bg-hover);
}

.sidebar-item.active .sidebar-icon {
  background: var(--linkx-primary-glow);
}

.sidebar-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.sidebar-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.sidebar-divider {
  width: 24px;
  height: 1px;
  background: var(--linkx-border);
  margin-bottom: 8px;
}

.sidebar-item.logout {
  color: var(--linkx-text-muted);
}

.sidebar-item.logout:hover {
  color: var(--linkx-error);
}

.sidebar-item.logout:hover .sidebar-icon {
  background: rgba(255, 61, 113, 0.1);
}

.main-content {
  flex: 1;
  overflow: hidden;
  background: var(--linkx-bg);
  min-width: 0;
  min-height: 0;
}

.profile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.profile-popup {
  width: min(360px, calc(100vw - 24px));
  max-height: calc(100vh - 32px);
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius-lg);
  overflow: hidden;
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
  position: relative;
  overflow-y: auto;
}

.profile-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  border: none;
  border-radius: 50%;
  color: white;
  cursor: pointer;
  z-index: 10;
  transition: var(--linkx-transition);
}

.profile-close:hover {
  background: rgba(0, 0, 0, 0.5);
}

.profile-popup-header {
  position: relative;
  height: 90px;
}

.profile-popup-cover {
  height: 100%;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 50%, #0095ff 100%);
}

.profile-popup-avatar-wrapper {
  position: absolute;
  bottom: -36px;
  left: 50%;
  transform: translateX(-50%);
}

.profile-popup-avatar {
  width: 72px;
  height: 72px;
  border-radius: var(--linkx-radius-lg);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  color: white;
  border: 4px solid var(--linkx-bg-card);
  box-shadow: 0 4px 16px var(--linkx-primary-glow);
  overflow: hidden;
}

.profile-popup-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-popup-content {
  padding: 44px 20px 20px;
}

.profile-popup-name {
  text-align: center;
  margin-bottom: 16px;
}

.profile-popup-name h2 {
  font-size: 18px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 2px;
}

.profile-popup-username {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.profile-popup-info {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  overflow: hidden;
  margin-bottom: 12px;
}

.profile-popup-info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
}

.profile-popup-info-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.info-label {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.info-value {
  font-size: 13px;
  color: var(--linkx-text);
}

.profile-popup-theme {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  padding: 12px 16px;
}

.theme-label {
  font-size: 13px;
  color: var(--linkx-text-secondary);
  display: block;
  margin-bottom: 8px;
}

.theme-options {
  display: flex;
  gap: 8px;
}

.theme-btn {
  flex: 1;
  padding: 8px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.theme-btn:hover {
  border-color: var(--linkx-primary);
  color: var(--linkx-text);
}

.theme-btn.active {
  border-color: var(--linkx-primary);
  background: var(--linkx-primary-glow);
  color: var(--linkx-primary);
}

@media (max-width: 760px) {
  .app-content {
    flex-direction: column-reverse;
  }

  .sidebar {
    width: 100%;
    min-height: 72px;
    flex-direction: row;
    justify-content: space-between;
    padding: 8px 12px calc(8px + env(safe-area-inset-bottom, 0px));
    border-right: none;
    border-top: 1px solid var(--linkx-border);
  }

  .sidebar-top,
  .sidebar-bottom {
    margin: 0;
    display: flex;
    align-items: center;
  }

  .sidebar-menu {
    flex: 1;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    gap: 8px;
    min-width: 0;
  }

  .sidebar-item {
    width: 60px;
    padding: 6px 0;
  }

  .sidebar-divider {
    display: none;
  }

  .main-content {
    min-height: 0;
  }

  .profile-popup-content {
    padding: 44px 16px 16px;
  }

  .theme-options {
    flex-wrap: wrap;
  }

  .theme-btn {
    min-width: 84px;
  }
}

@media (max-width: 520px) {
  .sidebar {
    padding-inline: 8px;
  }

  .sidebar-item {
    width: 54px;
  }

  .sidebar-label {
    font-size: 9px;
  }

  .profile-popup {
    width: calc(100vw - 16px);
    max-height: calc(100vh - 16px);
  }

  .profile-popup-info-item {
    align-items: flex-start;
    gap: 12px;
  }

  .info-value {
    text-align: right;
    word-break: break-word;
  }
}
</style>
