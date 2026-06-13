<template>
  <div class="content-area" style="flex-direction: column;">
    <div class="panel-header">个人资料</div>
    <div style="flex: 1; overflow-y: auto; background: #ededed;">
      <!-- Avatar -->
      <div class="profile-avatar">
        <div class="profile-avatar-img" style="display:flex;align-items:center;justify-content:center;font-size:28px;color:white;">
          {{ profile.nickname?.charAt(0) || '?' }}
        </div>
        <button style="background:none;border:none;color:#576b95;cursor:pointer;font-size:13px;" @click="handleChangeAvatar">更换头像</button>
      </div>

      <!-- Info -->
      <div class="profile-section">
        <div class="profile-item">
          <span class="profile-label">用户名</span>
          <span class="profile-value">{{ profile.username }}</span>
        </div>
        <div class="profile-item">
          <span class="profile-label">昵称</span>
          <input class="profile-input" v-model="profile.nickname" />
        </div>
        <div class="profile-item">
          <span class="profile-label">性别</span>
          <select class="profile-input" v-model="profile.gender" style="border:none;background:#f5f5f5;">
            <option :value="0">未知</option>
            <option :value="1">男</option>
            <option :value="2">女</option>
          </select>
        </div>
        <div class="profile-item">
          <span class="profile-label">注册时间</span>
          <span class="profile-value">{{ profile.createTime?.substring(0, 10) }}</span>
        </div>
      </div>

      <button class="profile-save-btn" @click="handleSave" :disabled="saving">
        {{ saving ? '保存中...' : '保存' }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { userApi, fileApi } from '../api/client'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const saving = ref(false)
const profile = reactive({
  username: '', nickname: '', gender: 0, createTime: '', avatar: ''
})

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
  saving.value = true
  try {
    await userApi.updateProfile({
      nickname: profile.nickname,
      gender: profile.gender,
      avatar: profile.avatar
    })
    userStore.nickname = profile.nickname
    localStorage.setItem('nickname', profile.nickname)
    alert('保存成功')
  } catch (e: any) {
    alert(e.response?.data?.message || '保存失败')
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
    try {
      const res: any = await fileApi.uploadAvatar(file)
      profile.avatar = res.data
      await userApi.updateProfile({
        nickname: profile.nickname,
        gender: profile.gender,
        avatar: res.data
      })
      userStore.avatar = res.data
      localStorage.setItem('avatar', res.data)
      alert('头像上传成功')
    } catch (e: any) {
      alert(e.response?.data?.message || '上传失败')
    }
  }
  input.click()
}
</script>
