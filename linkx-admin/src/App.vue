<template>
  <n-config-provider :theme-overrides="themeOverrides" :locale="zhCN" :date-locale="dateZhCN">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { NConfigProvider, NMessageProvider, zhCN, dateZhCN } from 'naive-ui'
import type { GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#00A870',
    primaryColorHover: '#00c985',
    primaryColorPressed: '#008f5f',
    primaryColorSuppl: '#00A870',
    borderRadius: '8px',
    fontFamily:
      "system-ui, -apple-system, 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif"
  },
  Layout: {
    color: '#f0f2f5',
    siderColor: '#ffffff',
    headerColor: '#ffffff'
  },
  Menu: {
    itemColorActive: 'rgba(0, 168, 112, 0.1)',
    itemTextColorActive: '#00A870',
    itemTextColorActiveHover: '#00A870',
    itemIconColorActive: '#00A870',
    itemIconColorActiveHover: '#00A870',
    itemHeight: '42px'
  },
  Card: {
    borderRadius: '12px'
  }
}
</script>