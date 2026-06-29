<template>
  <div class="content-area">
    <div class="settings-panel">
      <div class="panel-header">
        <div class="header-text">
          <span class="header-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="3" />
              <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
            </svg>
          </span>
          <div class="header-titles">
            <span class="header-title">设置</span>
            <span class="header-sub">外观、桌面集成与更新</span>
          </div>
        </div>
      </div>
      <div class="settings-scroll">
      <section class="settings-card">
        <h2 class="section-title">外观</h2>
        <p class="section-desc">浅色、深色或跟随系统外观</p>
        <div class="theme-grid">
          <button
            type="button"
            class="theme-card"
            :class="{ active: themeMode === 'light' }"
            @click="setThemeMode('light')"
          >
            <span class="theme-card-icon light">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="5" />
                <path d="M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42" />
              </svg>
            </span>
            <span class="theme-card-label">浅色</span>
          </button>
          <button
            type="button"
            class="theme-card"
            :class="{ active: themeMode === 'dark' }"
            @click="setThemeMode('dark')"
          >
            <span class="theme-card-icon dark">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            </span>
            <span class="theme-card-label">深色</span>
          </button>
          <button
            type="button"
            class="theme-card"
            :class="{ active: themeMode === 'system' }"
            @click="setThemeMode('system')"
          >
            <span class="theme-card-icon system">
              <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" />
                <path d="M8 21h8M12 17v4" />
              </svg>
            </span>
            <span class="theme-card-label">跟随系统</span>
          </button>
        </div>
      </section>

      <section v-if="isDesktop" class="settings-card">
        <h2 class="section-title">桌面</h2>
        <div class="settings-row">
          <div class="settings-row-text">
            <span class="settings-label">开机自启</span>
            <span class="settings-hint">登录系统后自动启动 LinkX</span>
          </div>
          <label class="settings-switch">
            <input v-model="autoLaunch" type="checkbox" :disabled="autoLaunchLoading" @change="saveAutoLaunch" />
            <span class="switch-track"><span class="switch-knob" /></span>
          </label>
        </div>
        <div class="settings-row">
          <div class="settings-row-text">
            <span class="settings-label">剪贴板粘贴图片</span>
            <span class="settings-hint">在聊天输入框按 Ctrl+V 粘贴截图发送</span>
          </div>
          <label class="settings-switch">
            <input v-model="clipboardImagePaste" type="checkbox" />
            <span class="switch-track"><span class="switch-knob" /></span>
          </label>
        </div>
        <div class="settings-row about-row">
          <div class="settings-row-text">
            <span class="settings-label">关于</span>
            <div class="version-grid">
              <span>应用版本 <strong>{{ versions.app || '—' }}</strong></span>
              <span>Electron <strong>{{ versions.electron || '—' }}</strong></span>
            </div>
          </div>
          <button type="button" class="settings-action" :disabled="checkingUpdate" @click="checkUpdates">
            {{ checkingUpdate ? '检查中…' : '检查更新' }}
          </button>
        </div>
        <p v-if="updateHint" class="update-hint">{{ updateHint }}</p>
        <div v-if="releasePreview" class="release-preview">
          <div class="release-preview-head">
            <span class="release-preview-title">服务端发布 v{{ releasePreview.version }}</span>
            <span v-if="releasePreview.forceUpdate" class="release-badge">建议更新</span>
          </div>
          <p v-if="releasePreview.releaseNotes" class="release-notes">{{ releasePreview.releaseNotes }}</p>
          <p v-else class="release-notes muted">暂无更新说明</p>
          <a
            v-if="releasePreview.downloadUrl"
            class="release-download"
            :href="releasePreview.downloadUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            下载安装包
          </a>
        </div>
      </section>

      <section v-else class="settings-card">
        <h2 class="section-title">版本与更新</h2>
        <p class="section-desc">在 LinkX 桌面客户端中可使用「检查更新」与开机自启</p>
        <button type="button" class="settings-action" :disabled="releaseLoading" @click="loadPublishedRelease">
          {{ releaseLoading ? '加载中…' : '查看发布说明' }}
        </button>
        <div v-if="releasePreview" class="release-preview">
          <div class="release-preview-head">
            <span class="release-preview-title">最新发布 v{{ releasePreview.version }}</span>
            <span v-if="releasePreview.forceUpdate" class="release-badge">强制更新</span>
          </div>
          <p v-if="releasePreview.releaseNotes" class="release-notes">{{ releasePreview.releaseNotes }}</p>
          <p v-else class="release-notes muted">暂无更新说明</p>
          <a
            v-if="releasePreview.downloadUrl"
            class="release-download"
            :href="releasePreview.downloadUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            下载安装包
          </a>
        </div>
        <p v-else-if="releaseLoadHint" class="update-hint">{{ releaseLoadHint }}</p>
      </section>
      </div>
    </div>
    <div class="right-panel">
      <div class="info-card">
        <div class="info-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
        </div>
        <h3>设置说明</h3>
        <ul>
          <li>主题会立即应用到整个客户端</li>
          <li>剪贴板贴图仅在聊天输入框生效</li>
          <li>开机自启与更新检查需使用桌面版</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { useMessage } from 'naive-ui'
import { releaseApi } from '../api/client'
import { getElectronAPI, isElectron } from '../utils/electron'
import { useTheme } from '../utils/theme'

const CLIPBOARD_IMAGE_KEY = 'linkx.clipboardImagePaste'

const message = useMessage()
const { mode: themeMode, setMode: setThemeMode } = useTheme()
const isDesktop = isElectron()
const autoLaunch = ref(false)
const autoLaunchLoading = ref(true)
const clipboardImagePaste = ref(localStorage.getItem(CLIPBOARD_IMAGE_KEY) !== '0')
const checkingUpdate = ref(false)
const updateHint = ref('')
const releaseLoading = ref(false)
const releaseLoadHint = ref('')
const releasePreview = ref<{
  version?: string
  releaseNotes?: string
  downloadUrl?: string
  forceUpdate?: boolean
} | null>(null)
const versions = reactive({
  app: '',
  electron: ''
})

onMounted(async () => {
  const api = getElectronAPI()
  if (!api) {
    autoLaunchLoading.value = false
    return
  }
  try {
    autoLaunch.value = await api.getAutoLaunch()
    const v = await api.getVersions()
    versions.app = v.app
    versions.electron = v.electron
  } catch (e) {
    console.warn('settings load failed', e)
  } finally {
    autoLaunchLoading.value = false
  }
})

async function saveAutoLaunch() {
  const api = getElectronAPI()
  if (!api) return
  try {
    await api.setAutoLaunch(autoLaunch.value)
    message.success(autoLaunch.value ? '已开启开机自启' : '已关闭开机自启')
  } catch {
    message.error('保存失败')
  }
}

function persistClipboardPref() {
  localStorage.setItem(CLIPBOARD_IMAGE_KEY, clipboardImagePaste.value ? '1' : '0')
}

function resolveReleasePlatform(): string {
  const p = (typeof process !== 'undefined' && process.platform) || ''
  if (p === 'win32') return 'win'
  if (p === 'darwin') return 'mac'
  if (p === 'linux') return 'linux'
  return 'win'
}

async function loadPublishedRelease() {
  releaseLoading.value = true
  releaseLoadHint.value = ''
  try {
    const platform = isDesktop ? resolveReleasePlatform() : 'win'
    const res: any = await releaseApi.latest(platform)
    const latest = res.data?.data ?? res.data
    if (latest?.version) {
      releasePreview.value = {
        version: latest.version,
        releaseNotes: latest.releaseNotes,
        downloadUrl: latest.downloadUrl,
        forceUpdate: latest.forceUpdate
      }
    } else {
      releasePreview.value = null
      releaseLoadHint.value = '暂无已发布版本'
    }
  } catch (e: unknown) {
    releasePreview.value = null
    releaseLoadHint.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    releaseLoading.value = false
  }
}

async function checkUpdates() {
  checkingUpdate.value = true
  updateHint.value = ''
  try {
    const platform = resolveReleasePlatform()
    try {
      const res: any = await releaseApi.latest(platform)
      const latest = res.data?.data ?? res.data
      const latestVersion = latest?.version || ''
      const current = versions.app || ''
      if (latest?.version) {
        releasePreview.value = {
          version: latest.version,
          releaseNotes: latest.releaseNotes,
          downloadUrl: latest.downloadUrl,
          forceUpdate: latest.forceUpdate
        }
      }
      if (latestVersion && current && latestVersion !== current) {
        const force = latest.forceUpdate ? '（建议立即更新）' : ''
        updateHint.value = `服务端已发布新版本 ${latestVersion}${force}${latest.downloadUrl ? '，可在下方下载安装包' : ''}`
      } else if (latestVersion) {
        updateHint.value = `与服务器发布版本一致（${latestVersion}）`
      }
    } catch (e: any) {
      const msg = e?.message || ''
      if (!msg.includes('暂无')) {
        updateHint.value = msg || '无法从服务器获取版本信息'
      }
    }

    const api = getElectronAPI()
    if (api?.checkForUpdates) {
      const result = await api.checkForUpdates()
      if (result.available) {
        updateHint.value = `Electron 更新通道：发现新版本 ${result.version || ''}`
      } else if (result.error && !updateHint.value) {
        updateHint.value = result.error
      } else if (!updateHint.value) {
        updateHint.value = '本地更新通道：当前已是最新版本'
      }
    } else if (!updateHint.value) {
      updateHint.value = '未配置本地自动更新，已尝试查询服务器发布版本'
    }
  } catch {
    if (!updateHint.value) {
      updateHint.value = '检查更新失败'
    }
  } finally {
    checkingUpdate.value = false
  }
}

watch(clipboardImagePaste, () => {
  persistClipboardPref()
})
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  min-height: 0;
  min-width: 0;
  background: var(--linkx-bg);
  color: var(--linkx-text);
}

.settings-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--linkx-bg-card);
  border-right: 1px solid var(--linkx-border);
}

.panel-header {
  min-height: 64px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--linkx-border);
  flex-shrink: 0;
}

.header-text {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 214, 143, 0.14) 0%, rgba(0, 149, 255, 0.1) 100%);
  color: var(--linkx-primary);
  border: 1px solid var(--linkx-border);
  flex-shrink: 0;
}

.header-titles {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--linkx-text);
}

.header-sub {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.settings-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px 32px;
  max-width: 640px;
}

.right-panel {
  width: 360px;
  min-width: 280px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 24px;
  background: radial-gradient(ellipse 80% 60% at 50% 40%, rgba(0, 214, 143, 0.04) 0%, transparent 70%);
}

.info-card {
  max-width: 320px;
  padding: 28px 32px;
  text-align: center;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg, 14px);
  box-shadow: var(--linkx-shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.06));
}

.info-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 18px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(0, 214, 143, 0.12) 0%, rgba(0, 149, 255, 0.08) 100%);
  color: var(--linkx-primary);
}

.info-card h3 {
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 700;
}

.info-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
  text-align: left;
  font-size: 13px;
  color: var(--linkx-text-secondary);
  line-height: 1.7;
}

.info-card li {
  position: relative;
  padding-left: 16px;
  margin-bottom: 8px;
}

.info-card li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.55em;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--linkx-primary);
  opacity: 0.6;
}

.settings-card {
  margin-bottom: 20px;
  padding: 22px 22px 18px;
  border-radius: var(--linkx-radius-lg, 14px);
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg, 0 8px 24px rgba(0, 0, 0, 0.06));
}

.settings-card-muted {
  padding: 20px 22px;
}

.section-title {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 700;
  color: var(--linkx-text);
}

.section-desc {
  margin: 0 0 18px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
  line-height: 1.5;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.theme-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px 8px;
  border-radius: 12px;
  border: 2px solid var(--linkx-border);
  background: var(--linkx-bg);
  cursor: pointer;
  transition: var(--linkx-transition-fast, 0.15s ease);
}

.theme-card:hover {
  border-color: rgba(0, 214, 143, 0.45);
  transform: translateY(-1px);
}

.theme-card.active {
  border-color: var(--linkx-primary);
  background: var(--linkx-primary-glow, rgba(0, 214, 143, 0.1));
  box-shadow: 0 4px 14px rgba(0, 214, 143, 0.15);
}

.theme-card-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.theme-card-icon.light {
  background: linear-gradient(135deg, #fff9e6 0%, #ffe8a3 100%);
  color: #c9a227;
}

.theme-card-icon.dark {
  background: linear-gradient(135deg, #2a2d3a 0%, #1a1c24 100%);
  color: #8b9dc3;
}

.theme-card-icon.system {
  background: linear-gradient(135deg, #e8f4ff 0%, #c5dff8 100%);
  color: #3d7dd6;
}

.theme-card-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.theme-card.active .theme-card-label {
  color: var(--linkx-primary);
}

.settings-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--linkx-border);
}

.settings-row:last-of-type {
  border-bottom: none;
}

.about-row {
  align-items: flex-end;
  flex-wrap: wrap;
}

.settings-label {
  font-size: 14px;
  font-weight: 600;
}

.settings-hint {
  display: block;
  font-size: 12px;
  color: var(--linkx-text-secondary);
  margin-top: 4px;
  font-weight: 400;
}

.settings-switch {
  position: relative;
  display: inline-flex;
  cursor: pointer;
  flex-shrink: 0;
}

.settings-switch input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-track {
  position: relative;
  width: 46px;
  height: 26px;
  border-radius: 13px;
  background: var(--linkx-border);
  transition: background 0.2s ease;
}

.switch-knob {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.2);
  transition: transform 0.2s ease;
}

.settings-switch input:checked + .switch-track {
  background: var(--linkx-primary);
}

.settings-switch input:checked + .switch-track .switch-knob {
  transform: translateX(20px);
}

.settings-switch input:disabled + .switch-track {
  opacity: 0.5;
}

.version-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.version-grid strong {
  color: var(--linkx-text);
  font-weight: 600;
}

.settings-action {
  padding: 10px 18px;
  border-radius: 10px;
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.settings-action:hover:not(:disabled) {
  border-color: var(--linkx-primary);
  color: var(--linkx-primary);
}

.settings-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.release-preview {
  margin-top: 16px;
  padding: 14px 16px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-md, 10px);
  background: var(--linkx-bg);
}

.release-preview-head {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.release-preview-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--linkx-text);
}

.release-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 6px;
  background: rgba(230, 81, 0, 0.12);
  color: #e65100;
  font-weight: 600;
}

.release-notes {
  margin: 10px 0 0;
  font-size: 13px;
  line-height: 1.55;
  white-space: pre-wrap;
  color: var(--linkx-text-secondary);
}

.release-notes.muted {
  font-style: italic;
}

.release-download {
  display: inline-block;
  margin-top: 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-primary);
}

.update-hint,
.muted-text {
  margin: 12px 0 0;
  font-size: 13px;
  color: var(--linkx-text-secondary);
  line-height: 1.5;
}

@media (max-width: 960px) {
  .right-panel {
    width: 300px;
    min-width: 240px;
  }
}

@media (max-width: 760px) {
  .content-area {
    flex-direction: column;
  }

  .settings-panel {
    max-height: 62%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .right-panel {
    width: 100%;
    min-width: 0;
    flex: 1;
    align-items: stretch;
    padding: 16px;
  }

  .info-card {
    max-width: none;
    text-align: left;
  }

  .settings-scroll {
    max-width: none;
    padding: 16px;
  }
}

@media (max-width: 520px) {
  .theme-grid {
    grid-template-columns: 1fr;
  }

  .about-row {
    flex-direction: column;
    align-items: stretch;
  }

  .settings-action {
    width: 100%;
  }
}
</style>