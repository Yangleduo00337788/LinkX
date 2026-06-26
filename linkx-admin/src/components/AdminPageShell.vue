<template>
  <div class="admin-page">
    <n-card :bordered="false" class="admin-page-panel" size="small">
      <div v-if="$slots.toolbar" class="admin-page-toolbar">
        <slot name="toolbar" />
      </div>
      <n-alert v-if="hint" class="admin-page-hint" type="info" :bordered="false">
        {{ hint }}
      </n-alert>
      <div class="admin-page-body">
        <slot />
      </div>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { NAlert, NCard } from 'naive-ui'

defineProps<{
  hint?: string
  /** @deprecated 列表不再纵向撑满，避免分页下方大块空白 */
  table?: boolean
}>()
</script>

<style scoped>
.admin-page {
  width: 100%;
}
.admin-page-panel {
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.admin-page-toolbar {
  margin-bottom: 12px;
}
.admin-page-hint {
  margin-bottom: 12px;
}
.admin-page-body {
  width: 100%;
}
</style>