<template>
  <div v-if="visible" class="overlay-panel" @click.self="$emit('close')">
    <div class="modal-card create-group-modal">
      <div class="modal-header">
        <div>
          <div class="modal-title">新建群聊</div>
          <div class="modal-subtitle">选择好友并填写群信息</div>
        </div>
        <button class="modal-close" @click="$emit('close')">x</button>
      </div>

      <div class="form-section">
        <label class="form-label">群头像</label>
        <div class="group-avatar-selector" @click="$emit('trigger-avatar-upload')">
          <img v-if="avatarPreview" :src="avatarPreview" class="group-avatar-preview" />
          <span v-else>{{ groupName?.charAt(0) || '群' }}</span>
        </div>
      </div>

      <div class="form-section">
        <label class="form-label">群名称</label>
        <input
          :value="groupName"
          class="modal-input"
          placeholder="请输入群名称"
          maxlength="100"
          @input="handleGroupNameInput"
        />
      </div>

      <div class="form-section">
        <label class="form-label">群公告</label>
        <textarea
          :value="notice"
          class="modal-textarea"
          rows="3"
          placeholder="选填，创建后可在群设置继续编辑"
          @input="handleNoticeInput"
        ></textarea>
      </div>

      <div class="form-section">
        <div class="section-title-row">
          <label class="form-label">选择成员</label>
          <span class="section-count">已选 {{ memberIds.length }} 人</span>
        </div>
        <div v-if="friends.length > 0" class="member-picker-list">
          <label v-for="friend in friends" :key="friend.friendId" class="member-picker-item">
            <input
              :checked="isSelected(friend.friendId)"
              type="checkbox"
              @change="toggleMemberSelection(friend.friendId, ($event.target as HTMLInputElement).checked)"
            />
            <div class="member-picker-avatar">
              <img v-if="friend.friendAvatar" :src="friend.friendAvatar" class="avatar-img" />
              <span v-else>{{ friend.friendNickname?.charAt(0) || '友' }}</span>
            </div>
            <div class="member-picker-info">
              <span class="member-picker-name">{{ friend.friendNickname }}</span>
              <span class="member-picker-meta">@{{ friend.friendUsername }}</span>
            </div>
          </label>
        </div>
        <div v-else class="panel-placeholder">暂无好友，请先添加好友再创建群聊。</div>
      </div>

      <div class="modal-actions">
        <button class="secondary-btn" @click="$emit('close')">取消</button>
        <button class="primary-btn" :disabled="creatingGroup" @click="$emit('submit')">
          {{ creatingGroup ? '创建中...' : '创建群聊' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FriendItem } from '../../types/chat'

const props = defineProps<{
  visible: boolean
  groupName: string
  notice: string
  memberIds: Array<string | number>
  avatarPreview: string
  friends: FriendItem[]
  creatingGroup: boolean
}>()

const emit = defineEmits<{
  (event: 'close'): void
  (event: 'trigger-avatar-upload'): void
  (event: 'update:groupName', value: string): void
  (event: 'update:notice', value: string): void
  (event: 'update:memberIds', value: Array<string | number>): void
  (event: 'submit'): void
}>()

function handleGroupNameInput(event: Event) {
  const target = event.target as HTMLInputElement
  emit('update:groupName', target.value || '')
}

function handleNoticeInput(event: Event) {
  const target = event.target as HTMLTextAreaElement
  emit('update:notice', target.value || '')
}

function isSelected(friendId: string | number) {
  return props.memberIds.some(item => String(item) === String(friendId))
}

function toggleMemberSelection(friendId: string | number, checked: boolean) {
  const nextSelection = checked
    ? [...props.memberIds, friendId]
    : props.memberIds.filter(item => String(item) !== String(friendId))
  emit('update:memberIds', nextSelection)
}
</script>

<style scoped>
.overlay-panel {
  position: fixed;
  inset: 0;
  z-index: 2200;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 56px 24px 24px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-card {
  width: min(640px, 100%);
  max-width: calc(100vw - 24px);
  max-height: min(720px, calc(100vh - 56px));
  display: flex;
  flex-direction: column;
  padding: 24px;
  overflow-y: auto;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  background: var(--linkx-bg-card);
  box-shadow: var(--linkx-shadow-lg);
}

.modal-header,
.section-title-row,
.modal-actions,
.member-picker-item {
  display: flex;
  align-items: center;
}

.modal-header,
.section-title-row,
.modal-actions {
  justify-content: space-between;
}

.modal-header {
  margin-bottom: 16px;
}

.modal-title {
  color: var(--linkx-text);
  font-size: 18px;
  font-weight: 700;
}

.modal-subtitle,
.section-count,
.member-picker-meta,
.panel-placeholder {
  color: var(--linkx-text-muted);
}

.modal-subtitle,
.section-count,
.panel-placeholder {
  font-size: 13px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--linkx-text-secondary);
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: var(--linkx-text-secondary);
  cursor: pointer;
}

.modal-close:hover,
.secondary-btn:hover {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text);
}

.group-avatar-selector {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--linkx-radius-lg);
  background: linear-gradient(135deg, #4d6bff 0%, #7f57ff 100%);
  color: white;
  font-size: 24px;
  font-weight: 700;
  cursor: pointer;
}

.group-avatar-preview,
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.modal-input,
.modal-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
  color: var(--linkx-text);
  outline: none;
  resize: vertical;
  transition: var(--linkx-transition-fast);
  font-family: inherit;
}

.modal-input:focus,
.modal-textarea:focus {
  border-color: var(--linkx-primary);
  box-shadow: 0 0 0 3px var(--linkx-primary-glow);
}

.member-picker-list {
  max-height: 220px;
  overflow: auto;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
}

.member-picker-item {
  gap: 12px;
  padding: 10px 12px;
  cursor: pointer;
}

.member-picker-item:not(:last-child) {
  border-bottom: 1px solid var(--linkx-border);
}

.member-picker-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: var(--linkx-radius-sm);
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-weight: 700;
}

.member-picker-info {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.member-picker-name {
  color: var(--linkx-text);
  font-size: 14px;
  font-weight: 700;
}

.panel-placeholder {
  padding: 18px 14px;
  border: 1px dashed var(--linkx-border);
  border-radius: var(--linkx-radius);
}

.modal-actions {
  margin-top: 4px;
  gap: 12px;
}

.secondary-btn,
.primary-btn {
  height: 40px;
  padding: 0 18px;
  border-radius: var(--linkx-radius);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: var(--linkx-transition-fast);
}

.secondary-btn {
  border: 1px solid var(--linkx-border);
  background: transparent;
  color: var(--linkx-text);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.primary-btn:disabled,
.secondary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .overlay-panel {
    padding: 20px 10px;
  }

  .modal-card {
    width: calc(100vw - 20px);
    padding: 20px 16px;
  }

  .modal-actions {
    flex-wrap: wrap;
  }

  .secondary-btn,
  .primary-btn {
    width: 100%;
  }
}
</style>
