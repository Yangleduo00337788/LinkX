<template>
  <div class="content-area">
    <div class="profile-panel">
      <div class="profile-header">
        <div class="profile-cover"></div>
        <div class="profile-avatar-wrapper">
          <div class="profile-avatar" @click="handleChangeAvatar">
            <img v-if="profile.avatar" :src="profile.avatar" class="avatar-img" />
            <span v-else>{{ profile.nickname?.charAt(0) || '?' }}</span>
            <div class="avatar-overlay">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <circle cx="12" cy="13" r="4"/>
              </svg>
              <span>更换头像</span>
            </div>
          </div>
        </div>
      </div>

      <div class="profile-content">
        <div class="profile-name">
          <h2>{{ profile.nickname || '未设置昵称' }}</h2>
          <span class="profile-username">@{{ profile.username }}</span>
        </div>

        <div class="info-section">
          <div class="info-group">
            <div class="info-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              昵称
            </div>
            <input v-model="profile.nickname" type="text" class="info-input" placeholder="输入昵称" />
          </div>

          <div class="info-divider"></div>

          <div class="info-group">
            <div class="info-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              </svg>
              性别
            </div>
            <select v-model="profile.gender" class="info-select">
              <option :value="0">未知</option>
              <option :value="1">男</option>
              <option :value="2">女</option>
            </select>
          </div>

          <div class="info-divider"></div>

          <div class="info-group">
            <div class="info-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/>
                <line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              注册时间
            </div>
            <div class="info-value">{{ profile.createTime?.substring(0, 10) || '-' }}</div>
          </div>

          <div class="info-divider"></div>

          <div class="info-group">
            <div class="info-label">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              用户名
            </div>
            <div class="info-value readonly">{{ profile.username }}</div>
          </div>
        </div>

        <div class="theme-section">
          <div class="info-label" style="margin-bottom: 12px;">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/>
              <line x1="12" y1="1" x2="12" y2="3"/>
              <line x1="12" y1="21" x2="12" y2="23"/>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
              <line x1="1" y1="12" x2="3" y2="12"/>
              <line x1="21" y1="12" x2="23" y2="12"/>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
            主题
          </div>
          <div class="theme-options">
            <button
              class="theme-btn"
              :class="{ active: themeMode === 'light' }"
              @click="setTheme('light')"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="5"/>
                <line x1="12" y1="1" x2="12" y2="3"/>
                <line x1="12" y1="21" x2="12" y2="23"/>
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
                <line x1="1" y1="12" x2="3" y2="12"/>
                <line x1="21" y1="12" x2="23" y2="12"/>
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
              </svg>
              浅色
            </button>
            <button
              class="theme-btn"
              :class="{ active: themeMode === 'dark' }"
              @click="setTheme('dark')"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
              </svg>
              深色
            </button>
            <button
              class="theme-btn"
              :class="{ active: themeMode === 'system' }"
              @click="setTheme('system')"
            >
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                <line x1="8" y1="21" x2="16" y2="21"/>
                <line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
              跟随系统
            </button>
          </div>
        </div>

        <button class="save-btn" :class="{ saving }" :disabled="saving" @click="handleSave">
          <svg v-if="!saving" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="20 6 9 17 4 12"/>
          </svg>
          <div v-else class="btn-loading"></div>
          {{ saving ? '保存中...' : '保存修改' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { userApi, fileApi } from '../api/client'
import { useUserStore } from '../stores/user'
import { useMessage } from 'naive-ui'
import { useTheme, type ThemeMode } from '../utils/theme'

const userStore = useUserStore()
const message = useMessage()
const { mode: themeMode, setMode: setThemeMode } = useTheme()
const saving = ref(false)
const profile = reactive({
  username: '',
  nickname: '',
  gender: 0,
  createTime: '',
  avatar: ''
})

function setTheme(val: ThemeMode) {
  setThemeMode(val)
}

onMounted(async () => {
  try {
    const res: any = await userApi.getProfile()
    const d = res.data
    profile.username = d.username
    profile.nickname = d.nickname
    profile.gender = d.gender || 0
    profile.createTime = d.createTime
    profile.avatar = d.avatar || ''
  } catch (e) {}
})

async function handleSave() {
  if (!profile.nickname?.trim()) {
    message.warning('请输入昵称')
    return
  }
  saving.value = true
  try {
    await userApi.updateProfile({
      nickname: profile.nickname,
      gender: profile.gender,
      avatar: profile.avatar
    })
    userStore.nickname = profile.nickname
    localStorage.setItem('nickname', profile.nickname)
    message.success('保存成功')
  } catch (e: any) {
    message.error(e.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function handleChangeAvatar() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async (e: any) => {
    const file = e.target.files[0]
    if (!file) return

    if (file.size > 5 * 1024 * 1024) {
      message.error('图片大小不能超过5MB')
      return
    }

    try {
      const res: any = await fileApi.uploadAvatar(file)
      const avatarUrl = res.data?.fileUrl || ''
      if (!avatarUrl) {
        throw new Error('头像地址缺失')
      }
      profile.avatar = avatarUrl
      await userApi.updateProfile({
        nickname: profile.nickname,
        gender: profile.gender,
        avatar: avatarUrl
      })
      userStore.avatar = avatarUrl
      localStorage.setItem('avatar', avatarUrl)
      message.success('头像上传成功')
    } catch (e: any) {
      message.error(e.response?.data?.message || '上传失败')
    }
  }
  input.click()
}
</script>

<style scoped>
.content-area {
  display: flex;
  height: 100%;
  background: var(--linkx-bg);
  justify-content: center;
  align-items: flex-start;
  padding: 24px;
  overflow-y: auto;
}

.profile-panel {
  width: 100%;
  max-width: 420px;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius-lg);
  overflow: hidden;
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
}

.profile-header {
  position: relative;
  height: 100px;
}

.profile-cover {
  height: 100%;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 50%, #0095ff 100%);
  position: relative;
  overflow: hidden;
}

.profile-cover::before {
  content: '';
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.profile-avatar-wrapper {
  position: absolute;
  bottom: -40px;
  left: 50%;
  transform: translateX(-50%);
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: var(--linkx-radius-lg);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: white;
  border: 4px solid var(--linkx-bg-card);
  box-shadow: 0 4px 16px var(--linkx-primary-glow);
  cursor: pointer;
  position: relative;
  transition: var(--linkx-transition);
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-avatar:hover {
  transform: scale(1.05);
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: var(--linkx-radius-lg);
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: white;
  font-size: 11px;
  font-weight: 600;
  opacity: 0;
  transition: var(--linkx-transition);
}

.profile-avatar:hover .avatar-overlay {
  opacity: 1;
}

.profile-content {
  padding: 48px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.profile-name {
  text-align: center;
}

.profile-name h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--linkx-text);
  margin-bottom: 4px;
}

.profile-username {
  font-size: 13px;
  color: var(--linkx-text-secondary);
}

.info-section {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  overflow: hidden;
}

.info-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: var(--linkx-text-secondary);
}

.info-label svg {
  color: var(--linkx-text-muted);
}

.info-input {
  width: 160px;
  height: 32px;
  padding: 0 12px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text);
  font-size: 13px;
  outline: none;
  transition: var(--linkx-transition);
}

.info-input:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.info-input::placeholder {
  color: var(--linkx-text-muted);
}

.info-select {
  width: 120px;
  height: 32px;
  padding: 0 12px;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-sm);
  color: var(--linkx-text);
  font-size: 13px;
  outline: none;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.info-select:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.info-value {
  font-size: 13px;
  color: var(--linkx-text);
}

.info-value.readonly {
  color: var(--linkx-text-secondary);
}

.info-divider {
  height: 1px;
  background: var(--linkx-border);
  margin: 0 16px;
}

.theme-section {
  padding: 16px;
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
}

.theme-options {
  display: flex;
  gap: 8px;
}

.theme-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 8px;
  background: var(--linkx-bg-card);
  border: 2px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  color: var(--linkx-text-secondary);
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.theme-btn:hover {
  border-color: var(--linkx-primary);
  color: var(--linkx-text);
}

.theme-btn.active {
  border-color: var(--linkx-primary);
  background: var(--linkx-primary-glow);
  color: var(--linkx-primary);
}

.save-btn {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  background: var(--linkx-primary);
  border: none;
  border-radius: var(--linkx-radius);
  color: white;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition);
}

.save-btn:hover:not(:disabled) {
  background: var(--linkx-primary-hover);
  box-shadow: 0 4px 16px var(--linkx-primary-glow);
  transform: translateY(-1px);
}

.save-btn:active:not(:disabled) {
  transform: translateY(0);
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.save-btn.saving {
  background: var(--linkx-primary-pressed);
}

.btn-loading {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 640px) {
  .content-area {
    padding: 16px 12px;
  }

  .profile-content {
    padding: 48px 16px 16px;
    gap: 16px;
  }

  .info-group {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .info-input,
  .info-select {
    width: 100%;
  }

  .info-value {
    width: 100%;
    word-break: break-word;
  }

  .theme-options {
    flex-direction: column;
  }

  .theme-btn {
    flex-direction: row;
    justify-content: center;
  }
}
</style>
