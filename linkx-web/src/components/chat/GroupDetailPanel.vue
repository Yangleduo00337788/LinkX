<template>
  <div v-if="visible" class="drawer-overlay" @click.self="$emit('close')">
    <div class="group-drawer">
      <div class="drawer-header">
        <div>
          <div class="drawer-title">群设置</div>
          <div class="drawer-subtitle">管理群资料、成员和公告</div>
        </div>
        <button class="modal-close" @click="$emit('close')">x</button>
      </div>

      <template v-if="groupDetail">
        <div class="drawer-body">
          <div class="group-summary-card">
            <div
              class="group-summary-avatar"
              :class="{ editable: canEditGroupProfile }"
              @click="canEditGroupProfile && $emit('trigger-group-profile-avatar-upload')"
            >
              <img v-if="groupProfileAvatarPreview || groupDetail.groupAvatar" :src="groupProfileAvatarPreview || groupDetail.groupAvatar" class="avatar-img" />
              <span v-else>{{ groupProfileName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
              <div v-if="canEditGroupProfile" class="group-summary-avatar-mask">更换</div>
            </div>
            <div class="group-summary-info">
              <div class="group-profile-name-row">
                <div class="group-profile-name-shell">
                  <input
                    :value="groupProfileName"
                    class="group-profile-name-input"
                    :disabled="!canEditGroupProfile || updatingGroupProfile"
                    maxlength="100"
                    placeholder="请输入群名称"
                    @input="handleGroupNameInput"
                  />
                  <div class="group-profile-hint">
                    {{ canEditGroupProfile ? '点击头像可更换群头像' : '仅群主和管理员可编辑群资料' }}
                  </div>
                </div>
                <button
                  v-if="canEditGroupProfile"
                  class="text-btn group-profile-save-btn"
                  :disabled="updatingGroupProfile || !isGroupProfileChanged"
                  :title="!isGroupProfileChanged ? '修改群名称或头像后才能保存' : '保存群资料'"
                  @click="$emit('save-group-profile')"
                >
                  {{ updatingGroupProfile ? '保存中...' : '保存资料' }}
                </button>
              </div>
              <div class="group-summary-badges">
                <div class="group-summary-pill">{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</div>
                <div class="group-summary-pill role">我的角色：{{ roleText(groupDetail.myRole) }}</div>
              </div>
              <div class="group-summary-id-row">
                <span class="group-summary-id">群号：{{ groupDetail.id }}</span>
                <button class="text-btn group-id-copy-btn" @click="$emit('copy-group-id', groupDetail.id)">复制群号</button>
              </div>
            </div>
          </div>

          <div class="drawer-section">
            <div class="section-title-row">
              <span class="drawer-section-title">群公告</span>
              <button
                v-if="canEditNotice"
                class="text-btn"
                :disabled="updatingNotice || !isGroupNoticeChanged"
                :title="!isGroupNoticeChanged ? '修改群公告后才能保存' : '保存群公告'"
                @click="$emit('save-notice')"
              >
                {{ updatingNotice ? '保存中...' : '保存公告' }}
              </button>
            </div>
            <textarea
              :value="noticeDraft"
              class="drawer-textarea"
              rows="4"
              :disabled="!canEditNotice"
              :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
              @input="handleNoticeInput"
            ></textarea>
            <div v-if="groupDetail.noticeUpdateTime" class="section-hint">最近更新：{{ formatDateTime(groupDetail.noticeUpdateTime) }}</div>
          </div>

          <div class="drawer-section">
            <div class="section-title-row">
              <span class="drawer-section-title">成员管理</span>
              <div class="section-actions">
                <button class="text-btn" @click="$emit('open-group-members-page')">独立页查看</button>
                <button v-if="canManageMembers" class="text-btn" @click="$emit('open-add-members-modal')">邀请进群</button>
              </div>
            </div>
            <div class="member-manage-list">
              <div v-for="member in groupDetail.members" :key="member.userId" class="member-row">
                <div class="member-main">
                  <div class="member-avatar">
                    <img v-if="member.avatar" :src="member.avatar" class="avatar-img" />
                    <span v-else>{{ member.nickname?.charAt(0) || '群' }}</span>
                  </div>
                  <div class="member-info">
                    <div class="member-name-row">
                      <span class="member-name">{{ member.nickname || member.username }}</span>
                      <div class="member-badges">
                        <span class="member-role-tag" :class="roleClass(member.role)">{{ roleText(member.role) }}</span>
                        <span v-if="String(member.userId) === currentUserId" class="member-self-tag">我</span>
                      </div>
                    </div>
                    <div class="member-meta">
                      <span class="member-username">@{{ member.username }}</span>
                      <span v-if="member.muted && member.muteTime" class="member-mute-text">
                        已禁言至 {{ formatDateTime(member.muteTime) }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-if="canOperateMember(member)" class="member-actions">
                  <button v-if="canToggleAdmin(member)" class="mini-btn" @click="$emit('toggle-admin-role', member)">
                    {{ member.role === GROUP_ROLE_ADMIN ? '取消管理员' : '设为管理员' }}
                  </button>
                  <button class="mini-btn" @click="$emit('toggle-mute-member', member)">{{ member.muted ? '解除禁言' : '禁言' }}</button>
                  <button class="mini-btn danger" @click="$emit('remove-member', member)">踢出</button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="canDissolveGroup || !isGroupOwner" class="drawer-section danger-zone">
            <div class="section-title-row">
              <span class="drawer-section-title">危险操作</span>
            </div>
            <div class="danger-actions">
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('dissolve-group')">解散群聊</button>
              <button v-if="canDissolveGroup" class="danger-action-btn" @click="$emit('open-transfer-owner-modal')">转让群主</button>
              <button v-if="!isGroupOwner" class="danger-action-btn" @click="$emit('leave-group')">退出群聊</button>
            </div>
          </div>
        </div>
      </template>
      <div v-else class="panel-placeholder drawer-placeholder">正在加载群详情...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  GROUP_ROLE_ADMIN,
  type GroupDetail,
  type GroupMember
} from '../../types/chat'
import { formatDateTime, roleClass, roleText } from '../../utils/chat'

defineProps<{
  visible: boolean
  groupDetail: GroupDetail | null
  groupProfileName: string
  groupProfileAvatarPreview: string
  noticeDraft: string
  currentUserId: string
  canEditGroupProfile: boolean
  updatingGroupProfile: boolean
  isGroupProfileChanged: boolean
  canEditNotice: boolean
  updatingNotice: boolean
  isGroupNoticeChanged: boolean
  canManageMembers: boolean
  canDissolveGroup: boolean
  isGroupOwner: boolean
  canOperateMember: (member: GroupMember) => boolean
  canToggleAdmin: (member: GroupMember) => boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'trigger-group-profile-avatar-upload'): void
  (event: 'update:groupProfileName', value: string): void
  (event: 'update:noticeDraft', value: string): void
  (event: 'save-group-profile'): void
  (event: 'copy-group-id', value: string | number): void
  (event: 'save-notice'): void
  (event: 'open-group-members-page'): void
  (event: 'open-add-members-modal'): void
  (event: 'toggle-admin-role', member: GroupMember): void
  (event: 'toggle-mute-member', member: GroupMember): void
  (event: 'remove-member', member: GroupMember): void
  (event: 'dissolve-group'): void
  (event: 'open-transfer-owner-modal'): void
  (event: 'leave-group'): void
}>()

function handleGroupNameInput(event: Event) {
  const target = event.target as HTMLInputElement
  emit('update:groupProfileName', target.value || '')
}

function handleNoticeInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:noticeDraft', target.value || '')
}
</script>

<style scoped>
.drawer-overlay {
  position: fixed;
  inset: 0;
  display: flex;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 1800;
}

.group-drawer {
  width: min(420px, 100%);
  height: 100%;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  border-radius: 0;
  background: var(--linkx-bg-card);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-lg);
}

.drawer-header,
.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.drawer-header {
  padding: 20px 20px 16px;
  border-bottom: 1px solid var(--linkx-border);
}

.drawer-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--linkx-text);
}

.drawer-subtitle,
.section-hint,
.member-meta {
  color: var(--linkx-text-muted);
}

.drawer-subtitle {
  font-size: 13px;
}

.drawer-body {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.modal-close,
.text-btn,
.mini-btn {
  border: none;
  background: transparent;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.modal-close:hover,
.text-btn:hover,
.mini-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.group-summary-card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px;
  margin-bottom: 18px;
  background: var(--linkx-bg);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
}

.group-summary-avatar,
.member-avatar {
  overflow: hidden;
}

.group-summary-avatar {
  position: relative;
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  border-radius: var(--linkx-radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
  color: white;
  font-size: 28px;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.group-summary-avatar.editable {
  cursor: pointer;
}

.group-summary-avatar-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.52);
  color: white;
  font-size: 12px;
  font-weight: 600;
  opacity: 0;
  transition: var(--linkx-transition-fast);
}

.group-summary-avatar.editable:hover .group-summary-avatar-mask {
  opacity: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-summary-info,
.member-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.group-summary-info {
  gap: 8px;
}

.group-profile-name-row {
  display: flex;
  align-items: flex-start;
  width: 100%;
  gap: 10px;
  min-width: 0;
}

.group-profile-name-shell {
  flex: 1;
  min-width: 0;
}

.group-profile-name-input {
  width: 100%;
  min-width: 0;
  height: 40px;
  padding: 0 12px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-md);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.group-profile-name-input:disabled {
  background: transparent;
  border-color: transparent;
  cursor: default;
}

.group-profile-name-input:focus,
.drawer-textarea:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
  outline: none;
}

.group-profile-hint {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.4;
  color: var(--linkx-text-muted);
}

.text-btn {
  padding: 8px 10px;
  border-radius: var(--linkx-radius);
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.group-profile-save-btn {
  min-width: 84px;
  height: 40px;
  padding: 0 14px;
  border: 1px solid rgba(77, 107, 255, 0.12);
  background: rgba(77, 107, 255, 0.06);
  color: var(--linkx-primary);
}

.group-profile-save-btn:hover {
  background: rgba(77, 107, 255, 0.12);
  color: #a8b8ff;
}

.group-summary-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.group-summary-pill,
.member-role-tag,
.member-self-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--linkx-radius-full);
  font-size: 11px;
  font-weight: 700;
}

.group-summary-pill {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.06);
  color: var(--linkx-text-secondary);
}

.group-summary-pill.role {
  background: rgba(77, 107, 255, 0.14);
  color: #9cb0ff;
}

.group-summary-id-row,
.member-main,
.member-name-row,
.member-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-summary-id {
  color: var(--linkx-text-secondary);
  font-size: 13px;
}

.group-id-copy-btn {
  padding: 0;
}

.drawer-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 18px;
}

.drawer-section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.drawer-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  resize: vertical;
  outline: none;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
}

.section-actions,
.danger-actions,
.member-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-manage-list {
  overflow: auto;
}

.member-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
}

.member-row:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.member-avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  border-radius: var(--linkx-radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-weight: 700;
}

.member-info {
  gap: 4px;
}

.member-name {
  color: var(--linkx-text);
  font-weight: 700;
  font-size: 14px;
  line-height: 1.35;
}

.member-role-tag,
.member-self-tag {
  min-height: 20px;
  padding: 0 8px;
  line-height: 20px;
  letter-spacing: 0.01em;
  border: 1px solid transparent;
}

.member-role-tag.owner {
  background: rgba(77, 107, 255, 0.14);
  border-color: rgba(144, 167, 255, 0.16);
  color: #9cb0ff;
}

.member-role-tag.admin {
  background: rgba(0, 214, 143, 0.1);
  border-color: rgba(55, 216, 170, 0.14);
  color: #43ddb1;
}

.member-role-tag.member,
.member-self-tag {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.08);
  color: var(--linkx-text-secondary);
}

.member-meta {
  font-size: 12px;
}

.member-actions {
  flex-wrap: wrap;
  justify-content: flex-end;
}

.mini-btn {
  padding: 6px 10px;
  border-radius: var(--linkx-radius);
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.mini-btn.danger,
.danger-action-btn {
  color: #ff8b8b;
}

.danger-zone {
  padding-top: 4px;
}

.danger-action-btn {
  height: 40px;
  padding: 0 18px;
  border: 1px solid rgba(255, 107, 107, 0.18);
  border-radius: var(--linkx-radius);
  background: rgba(255, 107, 107, 0.08);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.danger-action-btn:hover {
  background: rgba(255, 107, 107, 0.14);
}

.panel-placeholder.drawer-placeholder {
  display: flex;
  flex: 1;
  align-items: center;
  justify-content: center;
  color: var(--linkx-text-muted);
}
</style>
