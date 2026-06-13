<template>
  <div class="app-layout">
    <div class="sidebar">
      <div class="sidebar-logo">L</div>
      <div class="sidebar-menu">
        <button class="sidebar-btn" :class="{ active: currentTab === 'chat' }" @click="goTo('chat')">
          <span class="icon">💬</span>
          <span>消息</span>
        </button>
        <button class="sidebar-btn" :class="{ active: currentTab === 'friends' }" @click="goTo('friends')">
          <span class="icon">👥</span>
          <span>通讯录</span>
        </button>
        <button class="sidebar-btn" :class="{ active: currentTab === 'profile' }" @click="goTo('profile')">
          <span class="icon">👤</span>
          <span>我</span>
        </button>
      </div>
      <button class="sidebar-btn" @click="handleLogout" style="margin-top: auto;">
        <span class="icon">🚪</span>
        <span>退出</span>
      </button>
    </div>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const currentTab = ref('chat')

watch(() => route.path, (path) => {
  if (path.startsWith('/chat')) currentTab.value = 'chat'
  else if (path.startsWith('/friends')) currentTab.value = 'friends'
  else if (path.startsWith('/profile')) currentTab.value = 'profile'
}, { immediate: true })

function goTo(tab: string) {
  currentTab.value = tab
  router.push(`/${tab}`)
}

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>
