<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div v-if="visible" class="drawer-overlay" @click.self="$emit('close')">
    <!-- 行注：渲染容器 -->
    <div class="group-drawer">
      <!-- 行注：渲染容器 -->
      <div class="drawer-header">
        <!-- 行注：渲染容器 -->
        <div>
          <!-- 行注：展示“群设置”文案 -->
          <div class="drawer-title">群设置</div>
          <!-- 行注：展示“管理群资料、成员和公告”文案 -->
          <div class="drawer-subtitle">管理群资料、成员和公告</div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：展示“x”文案 -->
        <button class="modal-close" @click="$emit('close')">x</button>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：开始定义模板区域 -->
      <template v-if="groupDetail">
        <!-- 行注：渲染容器 -->
        <div class="drawer-body">
          <!-- 行注：渲染容器 -->
          <div class="group-summary-card">
            <!-- 行注：渲染容器 -->
            <div
              class="group-summary-avatar"
              :class="{ editable: canEditGroupProfile }"
              @click="canEditGroupProfile && $emit('trigger-group-profile-avatar-upload')"
            >
              <!-- 行注：渲染图片 -->
              <ProtectedImage
                v-if="groupProfileAvatarPreview || groupDetail.groupAvatar"
                :src="groupProfileAvatarPreview || groupDetail.groupAvatar"
                class="avatar-img"
              />
              <!-- 行注：渲染文本节点 -->
              <span v-else>{{ groupProfileName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
              <!-- 行注：展示“更换”文案 -->
              <div v-if="canEditGroupProfile" class="group-summary-avatar-mask">更换</div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="group-summary-info">
              <!-- 行注：渲染容器 -->
              <div class="group-profile-name-row">
                <!-- 行注：渲染容器 -->
                <div class="group-profile-name-shell">
                  <!-- 行注：渲染输入框 -->
                  <input
                    :value="groupProfileName"
                    class="group-profile-name-input"
                    :disabled="!canEditGroupProfile || updatingGroupProfile"
                    maxlength="100"
                    placeholder="请输入群名称"
                    @input="handleGroupNameInput"
                  />
                  <!-- 行注：渲染容器 -->
                  <div class="group-profile-hint">
                    <!-- 行注：渲染动态文本 -->
                    {{ canEditGroupProfile ? '点击头像可更换群头像' : '仅群主和管理员可编辑群资料' }}
                  <!-- 行注：结束容器 -->
                  </div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染按钮 -->
                <button
                  v-if="canEditGroupProfile"
                  class="text-btn group-profile-save-btn"
                  :disabled="updatingGroupProfile || !isGroupProfileChanged"
                  :title="!isGroupProfileChanged ? '修改群名称或头像后才能保存' : '保存群资料'"
                  @click="$emit('save-group-profile')"
                >
                  <!-- 行注：渲染动态文本 -->
                  {{ updatingGroupProfile ? '保存中...' : '保存资料' }}
                <!-- 行注：结束按钮 -->
                </button>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-summary-badges">
                <!-- 行注：渲染容器 -->
                <div class="group-summary-pill">{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</div>
                <!-- 行注：展示“我的角色：{{ roleText(g”文案 -->
                <div class="group-summary-pill role">我的角色：{{ roleText(groupDetail.myRole) }}</div>
              <!-- 行注：结束容器 -->
              </div>
              <!-- 行注：渲染容器 -->
              <div class="group-summary-id-row">
                <!-- 行注：展示“群号：{{ groupDetail.”文案 -->
                <span class="group-summary-id">群号：{{ groupDetail.id }}</span>
                <!-- 行注：展示“复制群号”文案 -->
                <button class="text-btn group-id-copy-btn" @click="$emit('copy-group-id', groupDetail.id)">复制群号</button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="drawer-section">
            <!-- 行注：渲染容器 -->
            <div class="section-title-row">
              <!-- 行注：展示“群公告”文案 -->
              <span class="drawer-section-title">群公告</span>
              <!-- 行注：渲染按钮 -->
              <button
                v-if="canEditNotice"
                class="text-btn"
                :disabled="updatingNotice || !isGroupNoticeChanged"
                :title="!isGroupNoticeChanged ? '修改群公告后才能保存' : '保存群公告'"
                @click="$emit('save-notice')"
              >
                <!-- 行注：渲染动态文本 -->
                {{ updatingNotice ? '保存中...' : '保存公告' }}
              <!-- 行注：结束按钮 -->
              </button>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染文本域 -->
            <textarea
              :value="noticeDraft"
              class="drawer-textarea"
              rows="4"
              :disabled="!canEditNotice"
              :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
              @input="handleNoticeInput"
            ></textarea>
            <!-- 行注：展示“最近更新：{{ formatDate”文案 -->
            <div v-if="groupDetail.noticeUpdateTime" class="section-hint">最近更新：{{ formatDateTime(groupDetail.noticeUpdateTime) }}</div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div class="drawer-section">
            <!-- 行注：渲染容器 -->
            <div class="section-title-row">
              <!-- 行注：展示“成员管理”文案 -->
              <span class="drawer-section-title">成员管理</span>
              <!-- 行注：渲染容器 -->
              <div class="section-actions">
                <!-- 行注：展示“独立页查看”文案 -->
                <button class="text-btn" @click="$emit('open-group-members-page')">独立页查看</button>
                <!-- 行注：展示“邀请进群”文案 -->
                <button v-if="canManageMembers" class="text-btn" @click="$emit('open-add-members-modal')">邀请进群</button>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="member-manage-list">
              <!-- 行注：渲染容器 -->
              <div v-for="member in groupDetail.members" :key="member.userId" class="member-row">
                <!-- 行注：渲染容器 -->
                <div class="member-main">
                  <!-- 行注：渲染容器 -->
                  <div class="member-avatar">
                    <!-- 行注：渲染图片 -->
                    <ProtectedImage v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                    <!-- 行注：渲染文本节点 -->
                    <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
                  <!-- 行注：结束容器 -->
                  </div>
                  <!-- 行注：渲染容器 -->
                  <div class="member-info">
                    <!-- 行注：渲染容器 -->
                    <div class="member-name-row">
                      <!-- 行注：渲染文本节点 -->
                      <span class="member-name">{{ member.nickname || member.username }}</span>
                      <!-- 行注：渲染容器 -->
                      <div class="member-badges">
                        <!-- 行注：渲染文本节点 -->
                        <span class="member-role-tag" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
                        <!-- 行注：展示“我”文案 -->
                        <span v-if="String(member.userId) === currentUserId" class="member-self-tag">我</span>
                      <!-- 行注：结束容器 -->
                      </div>
                    <!-- 行注：结束容器 -->
                    </div>
                    <!-- 行注：渲染容器 -->
                    <div class="member-meta">
                      <!-- 行注：展示“@{{ member.usernam”文案 -->
                      <span class="member-username">@{{ member.username }}</span>
                      <!-- 行注：渲染文本节点 -->
                      <span v-if="member.muted && member.muteTime" class="member-mute-text">
                        <!-- 行注：展示“已禁言至 formatDateTim”文案 -->
                        已禁言至 {{ formatDateTime(member.muteTime) }}
                      <!-- 行注：结束文本节点 -->
                      </span>
                    <!-- 行注：结束容器 -->
                    </div>
                  <!-- 行注：结束容器 -->
                  </div>
                <!-- 行注：结束容器 -->
                </div>
                <!-- 行注：渲染容器 -->
                <div v-if="canOperateMember(member)" class="member-actions">
                  <!-- 行注：渲染按钮 -->
                  <button v-if="canToggleAdmin(member)" class="mini-btn" @click="$emit('toggle-admin-role', member)">
                    <!-- 行注：渲染动态文本 -->
                    {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                  <!-- 行注：结束按钮 -->
                  </button>
                  <!-- 行注：渲染按钮 -->
                  <button class="mini-btn" @click="$emit('toggle-mute-member', member)">{{ member.muted ? '解除禁言' : '禁言' }}</button>
                  <!-- 行注：展示“踢出”文案 -->
                  <button class="mini-btn danger" @click="$emit('remove-member', member)">踢出</button>
                <!-- 行注：结束容器 -->
                </div>
              <!-- 行注：结束容器 -->
              </div>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
          <!-- 行注：渲染容器 -->
          <div v-if="canDissolveGroup || !isGroupOwner" class="drawer-section danger-zone">
            <!-- 行注：渲染容器 -->
            <div class="section-title-row">
              <!-- 行注：展示“危险操作”文案 -->
              <span class="drawer-section-title">危险操作</span>
            <!-- 行注：结束容器 -->
            </div>
            <!-- 行注：渲染容器 -->
            <div class="danger-actions">
              <!-- 行注：展示“解散群聊”文案 -->
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('dissolve-group')">解散群聊</button>
              <!-- 行注：展示“转让群主”文案 -->
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('open-transfer-owner-modal')">转让群主</button>
              <!-- 行注：展示“退出群聊”文案 -->
              <button v-if="!isGroupOwner" class="danger-action-btn" @click="$emit('leave-group')">退出群聊</button>
            <!-- 行注：结束容器 -->
            </div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束模板区域 -->
      </template>
      <!-- 行注：展示“正在加载群详情...”文案 -->
      <div v-else class="panel-placeholder drawer-placeholder">正在加载群详情...</div>
    <!-- 行注：结束容器 -->
    </div>
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * GroupDetailPanel 组件，负责当前界面片段的展示与交互。
 */
import ProtectedImage from '../ProtectedImage.vue'
import {
  GROUP_ROLE_ADMIN,
  type GroupDetail,
  type GroupMember
} from '../../types/chat'
import { formatDateTime, roleClass, roleText } from '../../utils/chat'  // 行注：引入 formatDateTime, roleClass, roleText 能力

defineProps<{  // 行注：开始当前逻辑块
  visible: boolean  // 行注：设置 visible 配置项
  groupDetail: GroupDetail | null  // 行注：设置 groupDetail 配置项
  groupProfileName: string  // 行注：设置 groupProfileName 配置项
  groupProfileAvatarPreview: string  // 行注：设置 groupProfileAvatarPreview 配置项
  noticeDraft: string  // 行注：设置 noticeDraft 配置项
  currentUserId: string  // 行注：设置 currentUserId 配置项
  canEditGroupProfile: boolean  // 行注：设置 canEditGroupProfile 配置项
  updatingGroupProfile: boolean  // 行注：设置 updatingGroupProfile 配置项
  isGroupProfileChanged: boolean  // 行注：设置 isGroupProfileChanged 配置项
  canEditNotice: boolean  // 行注：设置 canEditNotice 配置项
  updatingNotice: boolean  // 行注：设置 updatingNotice 配置项
  isGroupNoticeChanged: boolean  // 行注：设置 isGroupNoticeChanged 配置项
  canManageMembers: boolean  // 行注：设置 canManageMembers 配置项
  canDissolveGroup: boolean  // 行注：设置 canDissolveGroup 配置项
  isGroupOwner: boolean  // 行注：设置 isGroupOwner 配置项
  canOperateMember: (member: GroupMember) => boolean  // 行注：设置 canOperateMember 配置项
  canToggleAdmin: (member: GroupMember) => boolean  // 行注：设置 canToggleAdmin 配置项
}>()  // 行注：执行当前调用逻辑

const emit = defineEmits<{  // 行注：开始解构当前返回值
  (event: 'close'): void  // 行注：执行当前调用逻辑
  (event: 'trigger-group-profile-avatar-upload'): void  // 行注：执行当前调用逻辑
  (event: 'update:groupProfileName', value: string): void  // 行注：执行当前调用逻辑
  (event: 'update:noticeDraft', value: string): void  // 行注：执行当前调用逻辑
  (event: 'save-group-profile'): void  // 行注：执行当前调用逻辑
  (event: 'copy-group-id', value: string | number): void  // 行注：执行当前调用逻辑
  (event: 'save-notice'): void  // 行注：执行当前调用逻辑
  (event: 'open-group-members-page'): void  // 行注：执行当前调用逻辑
  (event: 'open-add-members-modal'): void  // 行注：执行当前调用逻辑
  (event: 'toggle-admin-role', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'toggle-mute-member', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'remove-member', member: GroupMember): void  // 行注：执行当前调用逻辑
  (event: 'dissolve-group'): void  // 行注：执行当前调用逻辑
  (event: 'open-transfer-owner-modal'): void  // 行注：执行当前调用逻辑
  (event: 'leave-group'): void  // 行注：执行当前调用逻辑
}>()  // 行注：执行当前调用逻辑

function handleGroupNameInput(event: Event) {  // 行注：定义 handleGroupNameInput 方法
  const target = event.target as HTMLInputElement  // 行注：初始化 target 变量
  emit('update:groupProfileName', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块

function handleNoticeInput(event: Event) {  // 行注：定义 handleNoticeInput 方法
  const target = event.target as HTMLTextAreaElement  // 行注：初始化 target 变量
  emit('update:noticeDraft', target.value || '')  // 行注：调用 emit 方法
}  // 行注：结束当前代码块
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.drawer-overlay {  /* 行注：定义 .drawer-overlay 样式 */
  position: fixed;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(0, 0, 0, 0.5);  /* 行注：设置 background 样式 */
  backdrop-filter: blur(4px);  /* 行注：设置 backdrop-filter 样式 */
  z-index: 1800;  /* 行注：设置 z-index 样式 */
}  /* 行注：结束当前样式块 */

.group-drawer {  /* 行注：定义 .group-drawer 样式 */
  width: min(420px, 100%);  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  margin-left: auto;  /* 行注：设置 margin-left 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  border-radius: 0;  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  box-shadow: var(--linkx-shadow-lg);  /* 行注：设置 box-shadow 样式 */
}  /* 行注：结束当前样式块 */

.drawer-header,  /* 行注：补充 .drawer-header 选择器 */
.section-title-row {  /* 行注：定义 .section-title-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.drawer-header {  /* 行注：定义 .drawer-header 样式 */
  padding: 20px 20px 16px;  /* 行注：设置 padding 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.drawer-title {  /* 行注：定义 .drawer-title 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.drawer-subtitle,  /* 行注：补充 .drawer-subtitle 选择器 */
.section-hint,  /* 行注：补充 .section-hint 选择器 */
.member-meta {  /* 行注：定义 .member-meta 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.drawer-subtitle {  /* 行注：定义 .drawer-subtitle 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.drawer-body {  /* 行注：定义 .drawer-body 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  padding: 20px;  /* 行注：设置 padding 样式 */
  overflow: auto;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.modal-close,  /* 行注：补充 .modal-close 选择器 */
.text-btn,  /* 行注：补充 .text-btn 选择器 */
.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.modal-close {  /* 行注：定义 .modal-close 样式 */
  width: 32px;  /* 行注：设置 width 样式 */
  height: 32px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.modal-close:hover,  /* 行注：补充 .modal-close:hover 选择器 */
.text-btn:hover,  /* 行注：补充 .text-btn:hover 选择器 */
.mini-btn:hover {  /* 行注：定义 .mini-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-card {  /* 行注：定义 .group-summary-card 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  gap: 14px;  /* 行注：设置 gap 样式 */
  padding: 16px;  /* 行注：设置 padding 样式 */
  margin-bottom: 18px;  /* 行注：设置 margin-bottom 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar,  /* 行注：补充 .group-summary-avatar 选择器 */
.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  overflow: hidden;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar {  /* 行注：定义 .group-summary-avatar 样式 */
  position: relative;  /* 行注：设置 position 样式 */
  width: 72px;  /* 行注：设置 width 样式 */
  height: 72px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 28px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  border: 1px solid rgba(255, 255, 255, 0.06);  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar.editable {  /* 行注：定义 .group-summary-avatar.editable 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar-mask {  /* 行注：定义 .group-summary-avatar-mask 样式 */
  position: absolute;  /* 行注：设置 position 样式 */
  inset: 0;  /* 行注：设置 inset 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: rgba(15, 23, 42, 0.52);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  opacity: 0;  /* 行注：设置 opacity 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-avatar.editable:hover .group-summary-avatar-mask {  /* 行注：定义 .group-summary-avatar.editable:hover .group-summary-avatar-mask 样式 */
  opacity: 1;  /* 行注：设置 opacity 样式 */
}  /* 行注：结束当前样式块 */

.avatar-img {  /* 行注：定义 .avatar-img 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  object-fit: cover;  /* 行注：设置 object-fit 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-info,  /* 行注：补充 .group-summary-info 选择器 */
.member-info {  /* 行注：定义 .member-info 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-info {  /* 行注：定义 .group-summary-info 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-row {  /* 行注：定义 .group-profile-name-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-shell {  /* 行注：定义 .group-profile-name-shell 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-input {  /* 行注：定义 .group-profile-name-input 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  min-width: 0;  /* 行注：设置 min-width 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 12px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius-md);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 18px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  line-height: 1.2;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-input:disabled {  /* 行注：定义 .group-profile-name-input:disabled 样式 */
  background: transparent;  /* 行注：设置 background 样式 */
  border-color: transparent;  /* 行注：设置 border-color 样式 */
  cursor: default;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-name-input:focus,  /* 行注：补充 .group-profile-name-input:focus 选择器 */
.drawer-textarea:focus {  /* 行注：定义 .drawer-textarea:focus 样式 */
  border-color: var(--linkx-primary);  /* 行注：设置 border-color 样式 */
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);  /* 行注：设置 box-shadow 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-hint {  /* 行注：定义 .group-profile-hint 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
  line-height: 1.4;  /* 行注：设置 line-height 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.text-btn {  /* 行注：定义 .text-btn 样式 */
  padding: 8px 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-save-btn {  /* 行注：定义 .group-profile-save-btn 样式 */
  min-width: 84px;  /* 行注：设置 min-width 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid rgba(77, 107, 255, 0.12);  /* 行注：设置 border 样式 */
  background: rgba(77, 107, 255, 0.06);  /* 行注：设置 background 样式 */
  color: var(--linkx-primary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-profile-save-btn:hover {  /* 行注：定义 .group-profile-save-btn:hover 样式 */
  background: rgba(77, 107, 255, 0.12);  /* 行注：设置 background 样式 */
  color: #a8b8ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-badges {  /* 行注：定义 .group-summary-badges 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-pill,  /* 行注：补充 .group-summary-pill 选择器 */
.member-role-tag,  /* 行注：补充 .member-role-tag 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  display: inline-flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  border-radius: var(--linkx-radius-full);  /* 行注：设置 border-radius 样式 */
  font-size: 11px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-pill {  /* 行注：定义 .group-summary-pill 样式 */
  padding: 4px 10px;  /* 行注：设置 padding 样式 */
  background: rgba(255, 255, 255, 0.06);  /* 行注：设置 background 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-pill.role {  /* 行注：定义 .group-summary-pill.role 样式 */
  background: rgba(77, 107, 255, 0.14);  /* 行注：设置 background 样式 */
  color: #9cb0ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-id-row,  /* 行注：补充 .group-summary-id-row 选择器 */
.member-main,  /* 行注：补充 .member-main 选择器 */
.member-name-row,  /* 行注：补充 .member-name-row 选择器 */
.member-badges {  /* 行注：定义 .member-badges 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.group-summary-id {  /* 行注：定义 .group-summary-id 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.group-id-copy-btn {  /* 行注：定义 .group-id-copy-btn 样式 */
  padding: 0;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.drawer-section {  /* 行注：定义 .drawer-section 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 10px;  /* 行注：设置 gap 样式 */
  margin-bottom: 18px;  /* 行注：设置 margin-bottom 样式 */
}  /* 行注：结束当前样式块 */

.drawer-section-title {  /* 行注：定义 .drawer-section-title 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.drawer-textarea {  /* 行注：定义 .drawer-textarea 样式 */
  width: 100%;  /* 行注：设置 width 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: var(--linkx-bg);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  resize: vertical;  /* 行注：设置 resize 样式 */
  outline: none;  /* 行注：设置 outline 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
  font-family: inherit;  /* 行注：设置 font-family 样式 */
}  /* 行注：结束当前样式块 */

.section-actions,  /* 行注：补充 .section-actions 选择器 */
.danger-actions,  /* 行注：补充 .danger-actions 选择器 */
.member-actions {  /* 行注：定义 .member-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  gap: 8px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-manage-list {  /* 行注：定义 .member-manage-list 样式 */
  overflow: auto;  /* 行注：设置 overflow 样式 */
}  /* 行注：结束当前样式块 */

.member-row {  /* 行注：定义 .member-row 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: flex-start;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
  padding: 12px 14px;  /* 行注：设置 padding 样式 */
}  /* 行注：结束当前样式块 */

.member-row:not(:last-child) {  /* 行注：定义 .member-row:not(:last-child) 样式 */
  border-bottom: 1px solid var(--linkx-border);  /* 行注：设置 border-bottom 样式 */
}  /* 行注：结束当前样式块 */

.member-avatar {  /* 行注：定义 .member-avatar 样式 */
  width: 36px;  /* 行注：设置 width 样式 */
  height: 36px;  /* 行注：设置 height 样式 */
  flex-shrink: 0;  /* 行注：设置 flex-shrink 样式 */
  border-radius: var(--linkx-radius-sm);  /* 行注：设置 border-radius 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.member-info {  /* 行注：定义 .member-info 样式 */
  gap: 4px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.member-name {  /* 行注：定义 .member-name 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  line-height: 1.35;  /* 行注：设置 line-height 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag,  /* 行注：补充 .member-role-tag 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  min-height: 20px;  /* 行注：设置 min-height 样式 */
  padding: 0 8px;  /* 行注：设置 padding 样式 */
  line-height: 20px;  /* 行注：设置 line-height 样式 */
  letter-spacing: 0.01em;  /* 行注：设置 letter-spacing 样式 */
  border: 1px solid transparent;  /* 行注：设置 border 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.owner {  /* 行注：定义 .member-role-tag.owner 样式 */
  background: rgba(77, 107, 255, 0.14);  /* 行注：设置 background 样式 */
  border-color: rgba(144, 167, 255, 0.16);  /* 行注：设置 border-color 样式 */
  color: #9cb0ff;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.admin {  /* 行注：定义 .member-role-tag.admin 样式 */
  background: rgba(0, 214, 143, 0.1);  /* 行注：设置 background 样式 */
  border-color: rgba(55, 216, 170, 0.14);  /* 行注：设置 border-color 样式 */
  color: #43ddb1;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-role-tag.member,  /* 行注：补充 .member-role-tag.member 选择器 */
.member-self-tag {  /* 行注：定义 .member-self-tag 样式 */
  background: rgba(255, 255, 255, 0.06);  /* 行注：设置 background 样式 */
  border-color: rgba(255, 255, 255, 0.08);  /* 行注：设置 border-color 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.member-meta {  /* 行注：定义 .member-meta 样式 */
  font-size: 12px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.member-actions {  /* 行注：定义 .member-actions 样式 */
  flex-wrap: wrap;  /* 行注：设置 flex-wrap 样式 */
  justify-content: flex-end;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn {  /* 行注：定义 .mini-btn 样式 */
  padding: 6px 10px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  color: var(--linkx-text-secondary);  /* 行注：设置 color 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.mini-btn.danger,  /* 行注：补充 .mini-btn.danger 选择器 */
.danger-action-btn {  /* 行注：定义 .danger-action-btn 样式 */
  color: #ff8b8b;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.danger-zone {  /* 行注：定义 .danger-zone 样式 */
  padding-top: 4px;  /* 行注：设置 padding-top 样式 */
}  /* 行注：结束当前样式块 */

.danger-action-btn {  /* 行注：定义 .danger-action-btn 样式 */
  height: 40px;  /* 行注：设置 height 样式 */
  padding: 0 18px;  /* 行注：设置 padding 样式 */
  border: 1px solid rgba(255, 107, 107, 0.18);  /* 行注：设置 border 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  background: rgba(255, 107, 107, 0.08);  /* 行注：设置 background 样式 */
  font-size: 14px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  cursor: pointer;  /* 行注：设置 cursor 样式 */
}  /* 行注：结束当前样式块 */

.danger-action-btn:hover {  /* 行注：定义 .danger-action-btn:hover 样式 */
  background: rgba(255, 107, 107, 0.14);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.panel-placeholder.drawer-placeholder {  /* 行注：定义 .panel-placeholder.drawer-placeholder 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex: 1;  /* 行注：设置 flex 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */
</style>
