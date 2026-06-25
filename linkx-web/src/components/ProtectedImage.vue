<!-- 受保护资源：fileUrl 自动换 ticket 拉 blob；blob:/data: 直接使用 -->
<template>
  <img
    v-if="displayUrl"
    :src="displayUrl"
    v-bind="$attrs"
    @load="(e) => $emit('load', e)"
    @click="(e) => $emit('click', e)"
    @error="(e) => $emit('error', e)"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useProtectedImageUrl } from '../hooks/useProtectedImageUrl'

defineOptions({ inheritAttrs: false })

const props = defineProps<{
  src?: string | null
}>()

defineEmits<{
  load: [event: Event]
  click: [event: MouseEvent]
  error: [event: Event]
}>()

const srcRef = computed(() => props.src?.trim() || '')
const displayUrl = useProtectedImageUrl(srcRef)
</script>