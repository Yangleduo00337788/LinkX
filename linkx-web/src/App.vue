<template>
  <n-message-provider>
    <ErrorToast />
    <router-view />
  </n-message-provider>
</template>

<script setup lang="ts">
import { NMessageProvider } from 'naive-ui'
import ErrorToast from './components/ErrorToast.vue'
import { useTheme } from './utils/theme'

useTheme()
</script>
