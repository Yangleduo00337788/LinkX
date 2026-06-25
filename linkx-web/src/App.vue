<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染消息提供器 -->
  <n-message-provider>
    <!-- 行注：渲染 ErrorToast 组件 -->
    <ErrorToast />
    <!-- 行注：渲染 InAppNotificationCenter 组件 -->
    <InAppNotificationCenter />
    <!-- 行注：渲染路由视图 -->
    <router-view />
  <!-- 行注：结束消息提供器 -->
  </n-message-provider>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 应用根组件，占位承载全局路由视图。
 */
import { NMessageProvider } from 'naive-ui'  // 行注：引入 NMessageProvider 能力
import ErrorToast from './components/ErrorToast.vue'  // 行注：引入 ErrorToast 组件
import InAppNotificationCenter from './components/InAppNotificationCenter.vue'  // 行注：引入 InAppNotificationCenter 组件
import { useTheme } from './utils/theme'
import { useDesktopIntegration } from './hooks/useDesktopIntegration'

useTheme()
useDesktopIntegration()
</script>
