<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="content-area">
    <div class="profile-panel">
      <div class="panel-header">
        <div class="header-text">
          <span class="header-icon" aria-hidden="true">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </span>
          <div class="header-titles">
            <span class="header-title">个人资料</span>
            <span class="header-sub">管理头像、昵称与账号信息</span>
          </div>
        </div>
      </div>
      <div class="profile-scroll">
    <div class="profile-card">
      <!-- 行注：渲染容器 -->
      <div class="profile-header">
        <!-- 行注：渲染容器 -->
        <div class="profile-cover"></div>
        <!-- 行注：渲染容器 -->
        <div class="profile-avatar-wrapper">
          <!-- 行注：渲染容器 -->
          <div class="profile-avatar" @click="handleChangeAvatar">
            <!-- 行注：渲染图片 -->
            <ProtectedImage v-if="profile.avatar" :src="profile.avatar" class="avatar-img" />
            <!-- 行注：渲染文本节点 -->
            <span v-else>{{ profile.nickname?.charAt(0) || '?' }}</span>
            <!-- 行注：渲染容器 -->
            <div class="avatar-overlay">
              <!-- 行注：渲染图标容器 -->
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/>
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="12" cy="13" r="4"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“更换头像”文案 -->
              <span>更换头像</span>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div class="profile-content">
        <!-- 行注：渲染容器 -->
        <div class="profile-name">
          <!-- 行注：渲染二级标题 -->
          <h2>{{ profile.nickname || '未设置昵称' }}</h2>
          <!-- 行注：展示“@{{ profile.userna”文案 -->
          <span class="profile-username">@{{ profile.username }}</span>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="info-section">
          <!-- 行注：渲染容器 -->
          <div class="info-group">
            <!-- 行注：渲染容器 -->
            <div class="info-label">
              <!-- 行注：渲染图标容器 -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标路径 -->
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="12" cy="7" r="4"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“昵称”文案 -->
              昵称
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染输入框 -->
            <input v-model="profile.nickname" type="text" class="info-input" placeholder="输入昵称" />
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="info-divider"></div>
          <!-- 行注：渲染容器 -->
          <div class="info-group">
            <!-- 行注：渲染容器 -->
            <div class="info-label">
              <!-- 行注：渲染图标容器 -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标圆形路径 -->
                <circle cx="12" cy="12" r="10"/>
                <!-- 行注：补充图标路径 -->
                <path d="M8 14s1.5 2 4 2 4-2 4-2"/>
                <!-- 行注：补充图标线段 -->
                <line x1="9" y1="9" x2="9.01" y2="9"/>
                <!-- 行注：补充图标线段 -->
                <line x1="15" y1="9" x2="15.01" y2="9"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“性别”文案 -->
              性别
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染select 节点 -->
            <select v-model="profile.gender" class="info-select">
              <!-- 行注：展示“未知”文案 -->
              <option :value="0">未知</option>
              <!-- 行注：展示“男”文案 -->
              <option :value="1">男</option>
              <!-- 行注：展示“女”文案 -->
              <option :value="2">女</option>
            <!-- 行注：结束select 节点 -->
            </select>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="info-divider"></div>
          <!-- 行注：渲染容器 -->
          <div class="info-group">
            <!-- 行注：渲染容器 -->
            <div class="info-label">
              <!-- 行注：渲染图标容器 -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标矩形路径 -->
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <!-- 行注：补充图标线段 -->
                <line x1="16" y1="2" x2="16" y2="6"/>
                <!-- 行注：补充图标线段 -->
                <line x1="8" y1="2" x2="8" y2="6"/>
                <!-- 行注：补充图标线段 -->
                <line x1="3" y1="10" x2="21" y2="10"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“注册时间”文案 -->
              注册时间
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="info-value">{{ profile.createTime?.substring(0, 10) || '-' }}</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="info-divider"></div>
          <!-- 行注：渲染容器 -->
          <div class="info-group">
            <!-- 行注：渲染容器 -->
            <div class="info-label">
              <!-- 行注：渲染图标容器 -->
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <!-- 行注：补充图标矩形路径 -->
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <!-- 行注：补充图标路径 -->
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              <!-- 行注：结束图标容器 -->
              </svg>
              <!-- 行注：展示“用户名”文案 -->
              用户名
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="info-value readonly">{{ profile.username }}</div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染按钮 -->
        <p class="profile-settings-hint">
          主题、开机自启等请前往
          <router-link to="/settings" class="profile-settings-link">设置</router-link>
        </p>
        <button class="save-btn" :class="{ saving }" :disabled="saving" @click="handleSave">
          <!-- 行注：渲染图标容器 -->
          <svg v-if="!saving" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <!-- 行注：补充图标折线 -->
            <polyline points="20 6 9 17 4 12"/>
          <!-- 行注：结束图标容器 -->
          </svg>
          <!-- 行注：渲染容器 -->
          <div v-else class="btn-loading"></div>
          <!-- 行注：渲染动态文本 -->
          {{ saving ? '保存中...' : '保存修改' }}
        <!-- 行注：结束按钮 -->
        </button>
      </div>
    </div>
      </div>
    </div>
    <div class="right-panel">
      <div class="info-card">
        <div class="info-icon">
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          </svg>
        </div>
        <h3>账号提示</h3>
        <ul>
          <li>头像与昵称会展示在聊天与联系人中</li>
          <li>用户名注册后不可修改</li>
          <li>主题、开机自启等请前往侧栏「设置」</li>
        </ul>
        <router-link to="/settings" class="info-link">打开设置</router-link>
      </div>
    </div>
  </div>
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 个人资料页面，处理资料查看与更新。
 */
import { ref, reactive, onMounted } from 'vue'
import { userApi, fileApi } from '../api/client'
import { useUserStore } from '../stores/user'
import ProtectedImage from '../components/ProtectedImage.vue'
import { useMessage } from 'naive-ui'

const userStore = useUserStore()
const message = useMessage()
const saving = ref(false)
const profile = reactive({  // 行注：开始解构当前返回值
  username: '',  // 行注：设置 username 配置项
  nickname: '',  // 行注：设置 nickname 配置项
  gender: 0,  // 行注：设置 gender 配置项
  createTime: '',  // 行注：设置 createTime 配置项
  avatar: ''
})

onMounted(async () => {  // 行注：注册组件挂载后的初始化逻辑
  try {  // 行注：尝试执行可能失败的逻辑
    const res: any = await userApi.getProfile()  // 行注：接收 res 异步结果
    const d = res.data  // 行注：初始化 d 变量
    profile.username = d.username  // 行注：更新 profile.username 值
    profile.nickname = d.nickname  // 行注：更新 profile.nickname 值
    profile.gender = d.gender || 0  // 行注：更新 profile.gender 值
    profile.createTime = d.createTime  // 行注：更新 profile.createTime 值
    profile.avatar = d.avatar || ''  // 行注：更新 profile.avatar 值
  } catch (e: any) {  // 行注：捕获并处理异常
    console.error('loadProfile error:', e)  // 行注：输出错误日志
    message.error(e.response?.data?.message || '获取个人资料失败')  // 行注：提示错误信息
  }  // 行注：结束当前代码块
})  // 行注：结束当前调用配置

async function handleSave() {  // 行注：定义异步 handleSave 方法
  if (!profile.nickname?.trim()) {  // 行注：判断当前条件是否成立
    message.warning('请输入昵称')  // 行注：提示警告信息
    return  // 行注：返回当前结果
  }  // 行注：结束当前代码块
  saving.value = true  // 行注：更新 saving 状态
  try {  // 行注：尝试执行可能失败的逻辑
    await userApi.updateProfile({  // 行注：开始当前逻辑块
      nickname: profile.nickname,  // 行注：设置 nickname 配置项
      gender: profile.gender,  // 行注：设置 gender 配置项
      avatar: profile.avatar  // 行注：设置 avatar 配置项
    })  // 行注：结束当前调用配置
    userStore.nickname = profile.nickname  // 行注：更新 userStore.nickname 值
    localStorage.setItem('nickname', profile.nickname)  // 行注：写入本地缓存
    message.success('保存成功')  // 行注：提示成功信息
  } catch (e: any) {  // 行注：捕获并处理异常
    message.error(e.response?.data?.message || '保存失败')  // 行注：提示错误信息
  } finally {  // 行注：执行收尾清理逻辑
    saving.value = false  // 行注：更新 saving 状态
  }  // 行注：结束当前代码块
}  // 行注：结束当前代码块

function handleChangeAvatar() {  // 行注：定义 handleChangeAvatar 方法
  const input = document.createElement('input')  // 行注：初始化 input 变量
  input.type = 'file'  // 行注：更新 input.type 值
  input.accept = 'image/*'  // 行注：更新 input.accept 值
  input.onchange = async (e: any) => {  // 行注：执行当前调用逻辑
    const file = e.target.files[0]  // 行注：初始化 file 变量
    if (!file) return  // 行注：判断当前条件是否成立

    if (file.size > 5 * 1024 * 1024) {  // 行注：判断当前条件是否成立
      message.error('图片大小不能超过5MB')  // 行注：提示错误信息
      return  // 行注：返回当前结果
    }  // 行注：结束当前代码块

    try {  // 行注：尝试执行可能失败的逻辑
      const res: any = await fileApi.uploadAvatar(file)  // 行注：接收 res 异步结果
      const avatarUrl = res.data?.fileUrl || ''  // 行注：初始化 avatarUrl 变量
      if (!avatarUrl) {  // 行注：判断当前条件是否成立
        throw new Error('头像地址缺失')  // 行注：抛出异常并终止当前流程
      }  // 行注：结束当前代码块
      profile.avatar = avatarUrl  // 行注：更新 profile.avatar 值
      await userApi.updateProfile({  // 行注：开始当前逻辑块
        nickname: profile.nickname,  // 行注：设置 nickname 配置项
        gender: profile.gender,  // 行注：设置 gender 配置项
        avatar: avatarUrl  // 行注：设置 avatar 配置项
      })  // 行注：结束当前调用配置
      userStore.avatar = avatarUrl  // 行注：更新 userStore.avatar 值
      localStorage.setItem('avatar', avatarUrl)  // 行注：写入本地缓存
      message.success('头像上传成功')  // 行注：提示成功信息
    } catch (e: any) {  // 行注：捕获并处理异常
      message.error(e.response?.data?.message || '上传失败')  // 行注：提示错误信息
    }  // 行注：结束当前代码块
  }  // 行注：结束当前代码块
  input.click()  // 行注：调用 click 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.content-area {
  display: flex;
  height: 100%;
  min-height: 0;
  min-width: 0;
  background: var(--linkx-bg);
}

.profile-panel {
  width: 420px;
  min-width: 320px;
  max-width: 48%;
  flex-shrink: 0;
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
  background: linear-gradient(135deg, rgba(0, 214, 143, 0.14) 0%, rgba(0, 201, 167, 0.1) 100%);
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
  letter-spacing: -0.01em;
}

.header-sub {
  font-size: 12px;
  color: var(--linkx-text-muted);
}

.profile-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px 16px 24px;
  display: flex;
  justify-content: center;
}

.profile-card {
  width: 100%;
  max-width: 400px;
  background: var(--linkx-bg-card);
  border-radius: var(--linkx-radius-lg);
  overflow: hidden;
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-sm, 0 1px 3px rgba(0, 0, 0, 0.06));
}

.right-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-width: 0;
  background: radial-gradient(ellipse 80% 60% at 50% 40%, rgba(0, 214, 143, 0.04) 0%, transparent 70%);
}

.info-card {
  max-width: 380px;
  padding: 32px 36px;
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
  color: var(--linkx-text);
}

.info-card ul {
  margin: 0 0 20px;
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

.info-link {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid var(--linkx-border);
  color: var(--linkx-primary);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: var(--linkx-transition-fast);
}

.info-link:hover {
  border-color: var(--linkx-primary);
  background: var(--linkx-primary-glow);
}

.profile-header {  /* 行注：定义 .profile-header 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  height: 100px;  /* 行注：设置 height 样式 */
}  /* 行注：结束当前样式块 */

.profile-cover {  /* 行注：定义 .profile-cover 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 50%, #0095ff 100%);  /* 行注：设置 background 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.profile-cover::before {  /* 行注：定义 .profile-cover::before 样式 */
  content: '';  /* 行注：设置 content 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar-wrapper {  /* 行注：定义 .profile-avatar-wrapper 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  bottom: -40px;  /* 行注：设置 bottom 样式 */
  left: 50%;  /* 行注：设置 left 样式 */
  transform: translateX(-50%);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar {  /* 行注：定义 .profile-avatar 样式 */
  width: 80px;  /* 行注：设置 width 样式 */
  height: 80px;  /* 行注：设置 height 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  font-size: 32px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: white;  /* 行注：设置 color 样式 */
  border: 4px solid var(--linkx-bg-card);  /* 行注：设置 border 样式 */
  box-shadow: 0 4px 16px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar:hover {  /* 行注：定义 .profile-avatar:hover 样式 */
  transform: scale(1.05);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.avatar-overlay {  /* 行注：定义 .avatar-overlay 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  background: rgba(0, 0, 0, 0.6);  /* 行注：设置 background 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  opacity: 0;  /* 行注：设置 opacity 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.profile-avatar:hover .avatar-overlay {  /* 行注：定义 .profile-avatar:hover .avatar-overlay 样式 */
  opacity: 1;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.profile-content {  /* 行注：定义 .profile-content 样式 */
  padding: 48px 24px 24px;  /* 行注：设置 padding 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 24px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.profile-name {  /* 行注：定义 .profile-name 样式 */
  text-align: center;  /* 行注：设置 text-align 样式 */
}  /* 行注：结束当前样式块 */

.profile-name h2 {  /* 行注：定义 .profile-name h2 样式 */
  font-size: 20px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  margin-bottom: 4px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.profile-username {  /* 行注：定义 .profile-username 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-section {
  background: var(--linkx-bg);
  border-radius: var(--linkx-radius);
  overflow: hidden;
  border: 1px solid var(--linkx-border);
}

.info-group {  /* 行注：定义 .info-group 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  padding: 14px 16px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.info-label {  /* 行注：定义 .info-label 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-label svg {  /* 行注：定义 .info-label svg 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-input {  /* 行注：定义 .info-input 样式 */
  width: 160px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  padding: 0 12px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.info-input:focus {  /* 行注：定义 .info-input:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.info-input::placeholder {  /* 行注：定义 .info-input::placeholder 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-select {  /* 行注：定义 .info-select 样式 */
  width: 120px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  padding: 0 12px;  /* 行注：设置 padding 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.info-select:focus {  /* 行注：定义 .info-select:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.info-value {  /* 行注：定义 .info-value 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-value.readonly {  /* 行注：定义 .info-value.readonly 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.info-divider {
  height: 1px;
  background: var(--linkx-border);
  margin: 0 16px;
}

.profile-settings-hint {
  margin: 0;
  font-size: 12px;
  color: var(--linkx-text-muted);
  text-align: center;
  line-height: 1.6;
}

.profile-settings-link {
  color: var(--linkx-primary);
  font-weight: 600;
  text-decoration: none;
}

.profile-settings-link:hover {
  text-decoration: underline;
}

.save-btn {
  width: 100%;  /* 行注：设置 width 样式 */
  height: 44px;  /* 行注：设置 height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  border: none;  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
  transition: var(--linkx-transition);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.save-btn:hover:not(:disabled) {  /* 行注：定义 .save-btn:hover:not(:disabled) 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
  box-shadow: 0 4px 16px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  transform: translateY(-1px);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.save-btn:active:not(:disabled) {  /* 行注：定义 .save-btn:active:not(:disabled) 样式 */
  transform: translateY(0);  /* 行注：设置 transform 样式 */
}  /* 行注：结束当前样式块 */

.save-btn:disabled {  /* 行注：定义 .save-btn:disabled 样式 */
  opacity: 0.7;  /* 行注：设置 opacity 样式 */
  cursor: not-allowed;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.save-btn.saving {  /* 行注：定义 .save-btn.saving 样式 */
  background: var(--linkx-primary-pressed);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.btn-loading {  /* 行注：定义 .btn-loading 样式 */
  width: 16px;  /* 行注：设置 width 样式 */
  height: 16px;  /* 行注：设置 height 样式 */
  border: 2px solid rgba(255, 255, 255, 0.3);  /* 行注：设置 border 样式 */
  border-top-color: white;  /* 行注：设置 border-top-color 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  to { transform: rotate(360deg); }  /* 行注：定义 to 样式 */
}  /* 行注：结束当前样式块 */

@media (max-width: 960px) {
  .profile-panel {
    width: 360px;
    max-width: 52%;
  }
}

@media (max-width: 760px) {
  .content-area {
    flex-direction: column;
  }

  .profile-panel {
    width: 100%;
    max-width: none;
    max-height: 58%;
    border-right: none;
    border-bottom: 1px solid var(--linkx-border);
  }

  .right-panel {
    flex: 1;
    align-items: stretch;
    padding: 16px;
  }

  .info-card {
    max-width: none;
    text-align: left;
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
  .info-select,
  .info-value {
    width: 100%;
    word-break: break-word;
  }
}
</style>
