<template>
  <n-message-provider>
    <ErrorToast />
    <InAppNotificationCenter />
    <router-view />
  </n-message-provider>
</template>

<script setup lang="ts">
import { NMessageProvider } from 'naive-ui'
import ErrorToast from './components/ErrorToast.vue'
import InAppNotificationCenter from './components/InAppNotificationCenter.vue'
import { useTheme } from './utils/theme'

useTheme()
</script>
