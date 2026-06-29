<template>
  <div class="group-files-tab">
    <div class="files-toolbar">
      <button
        v-for="t in mediaTabs"
        :key="t.key"
        type="button"
        class="media-tab"
        :class="{ active: mediaType === t.key }"
        @click="selectType(t.key)"
      >
        {{ t.label }}
      </button>
      <button type="button" class="refresh-btn" :disabled="loading" @click="load">
        {{ loading ? '…' : '刷新' }}
      </button>
    </div>
    <div v-if="loading && !items.length" class="files-state">加载中…</div>
    <div v-else-if="error" class="files-state error">{{ error }}</div>
    <div v-else-if="!items.length" class="files-state muted">暂无{{ currentTabLabel }}</div>
    <ul v-else class="files-list">
      <li v-for="item in items" :key="item.messageId || item.fileId" class="files-item">
        <div class="files-item-main">
          <span class="files-name">{{ item.fileName || item.content || '文件' }}</span>
          <span class="files-meta">{{ formatTime(item.sendTime) }} · {{ item.senderNickname || '成员' }}</span>
        </div>
        <button
          v-if="item.fileId"
          type="button"
          class="files-dl"
          @click="$emit('download', item)"
        >
          打开
        </button>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { groupApi } from '../../api/client'

export interface GroupMediaItem {
  messageId?: string | number
  fileId?: string | number
  fileName?: string
  content?: string
  sendTime?: string
  senderNickname?: string
  mediaType?: string
}

const props = defineProps<{
  groupId: string | number | null
  visible: boolean
}>()

defineEmits<{
  (e: 'download', item: GroupMediaItem): void
}>()

const mediaTabs = [
  { key: 'file', label: '文件' },
  { key: 'image', label: '图片' },
  { key: 'video', label: '视频' }
]

const mediaType = ref('file')
const loading = ref(false)
const error = ref('')
const items = ref<GroupMediaItem[]>([])

const currentTabLabel = computed(() => mediaTabs.find((t) => t.key === mediaType.value)?.label || '')

function formatTime(t?: string) {
  if (!t) return '—'
  return String(t).replace('T', ' ').substring(0, 16)
}

async function load() {
  if (!props.groupId) return
  loading.value = true
  error.value = ''
  try {
    const res: any = await groupApi.getMedia(props.groupId, { mediaType: mediaType.value, size: 80 })
    const data = res.data?.data ?? res.data
    items.value = Array.isArray(data) ? data : data?.records ?? []
  } catch (e: unknown) {
    items.value = []
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function selectType(key: string) {
  mediaType.value = key
}

watch(
  () => [props.visible, props.groupId, mediaType.value] as const,
  ([vis, gid]) => {
    if (vis && gid) void load()
  },
  { immediate: true }
)
</script>

<style scoped>
.group-files-tab {
  min-height: 200px;
}
.files-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.media-tab {
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--linkx-border);
  background: transparent;
  font-size: 13px;
  cursor: pointer;
}
.media-tab.active {
  border-color: var(--linkx-primary);
  color: var(--linkx-primary);
  background: rgba(0, 214, 143, 0.08);
}
.refresh-btn {
  margin-left: auto;
  font-size: 12px;
  padding: 4px 10px;
  border: none;
  background: transparent;
  color: var(--linkx-primary);
  cursor: pointer;
}
.files-state {
  padding: 24px;
  text-align: center;
  font-size: 13px;
  color: var(--linkx-text-muted);
}
.files-state.error {
  color: #d03050;
}
.files-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.files-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--linkx-border);
}
.files-name {
  font-size: 14px;
  font-weight: 500;
  word-break: break-all;
}
.files-meta {
  display: block;
  font-size: 11px;
  color: var(--linkx-text-muted);
  margin-top: 4px;
}
.files-dl {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--linkx-primary);
  border: none;
  background: transparent;
  cursor: pointer;
}
</style>