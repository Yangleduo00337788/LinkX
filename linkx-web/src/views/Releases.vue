<template>
  <div class="content-area">
    <div class="releases-panel">
      <div class="panel-header">
        <div class="header-text">
          <span class="header-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
          </span>
          <div class="header-titles">
            <span class="header-title">版本与更新</span>
            <span class="header-sub">查看当前平台最新发布说明</span>
          </div>
        </div>
      </div>
      <div class="releases-body">
        <div class="platform-tabs">
          <button
            v-for="p in platforms"
            :key="p.key"
            type="button"
            class="platform-tab"
            :class="{ active: platform === p.key }"
            @click="selectPlatform(p.key)"
          >
            {{ p.label }}
          </button>
        </div>
        <div v-if="loading" class="state-box">加载中…</div>
        <div v-else-if="error" class="state-box error">{{ error }}</div>
        <div v-else-if="!release" class="state-box muted">暂无已发布的 {{ platformLabel }} 版本</div>
        <div v-else class="release-card">
          <div class="release-meta">
            <span class="version">v{{ release.version }}</span>
            <span v-if="release.forceUpdate" class="badge force">强制更新</span>
          </div>
          <p v-if="release.releaseNotes" class="notes">{{ release.releaseNotes }}</p>
          <p v-else class="notes muted">暂无更新说明</p>
          <a
            v-if="release.downloadUrl"
            class="download-link"
            :href="release.downloadUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            下载安装包
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { releaseApi } from '../api/client'
import { isElectron } from '../utils/electron'

interface LatestRelease {
  platform?: string
  version?: string
  downloadUrl?: string
  releaseNotes?: string
  forceUpdate?: boolean
}

const platforms = [
  { key: 'win', label: 'Windows' },
  { key: 'mac', label: 'macOS' },
  { key: 'linux', label: 'Linux' }
]

const platform = ref('win')
const loading = ref(false)
const error = ref('')
const release = ref<LatestRelease | null>(null)

const platformLabel = computed(() => platforms.find((p) => p.key === platform.value)?.label || platform.value)

function detectDefaultPlatform() {
  if (typeof process !== 'undefined' && process.platform) {
    if (process.platform === 'win32') return 'win'
    if (process.platform === 'darwin') return 'mac'
    if (process.platform === 'linux') return 'linux'
  }
  return 'win'
}

async function load() {
  loading.value = true
  error.value = ''
  release.value = null
  try {
    const res: any = await releaseApi.latest(platform.value)
    const d = res.data?.data ?? res.data
    release.value = d && d.version ? d : null
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function selectPlatform(key: string) {
  platform.value = key
}

watch(platform, () => load())

onMounted(() => {
  if (isElectron()) {
    platform.value = detectDefaultPlatform()
  }
  load()
})
</script>

<style scoped>
.releases-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--lx-surface, #fff);
  border-radius: 12px;
  overflow: hidden;
}
.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--lx-border, #eee);
}
.header-text {
  display: flex;
  align-items: center;
  gap: 12px;
}
.header-titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.header-title {
  font-weight: 600;
  font-size: 16px;
}
.header-sub {
  font-size: 12px;
  color: var(--lx-text-muted, #888);
}
.releases-body {
  padding: 20px;
  flex: 1;
}
.platform-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.platform-tab {
  padding: 6px 14px;
  border-radius: 8px;
  border: 1px solid var(--lx-border, #ddd);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}
.platform-tab.active {
  background: var(--lx-primary-soft, #e8f4ff);
  border-color: var(--lx-primary, #2080f0);
  color: var(--lx-primary, #2080f0);
}
.state-box {
  padding: 24px;
  text-align: center;
  color: var(--lx-text-muted, #888);
}
.state-box.error {
  color: #d03050;
}
.release-card {
  padding: 20px;
  border: 1px solid var(--lx-border, #eee);
  border-radius: 12px;
  max-width: 560px;
}
.release-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.version {
  font-size: 20px;
  font-weight: 700;
}
.badge.force {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: #fff3e0;
  color: #e65100;
}
.notes {
  white-space: pre-wrap;
  line-height: 1.6;
  margin: 0 0 16px;
  font-size: 14px;
}
.notes.muted {
  color: var(--lx-text-muted, #888);
}
.download-link {
  display: inline-block;
  color: var(--lx-primary, #2080f0);
  font-weight: 500;
}
</style>