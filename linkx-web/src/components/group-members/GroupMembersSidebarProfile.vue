<template>
  <section class="panel-card profile-card">
    <div class="panel-title-row">
      <div>
        <div class="panel-title">群资料</div>
        <div class="panel-note">{{ canEditGroupProfile ? '支持直接编辑群名称、头像与公告' : '仅群主和管理员可编辑群资料' }}</div>
      </div>
    </div>

    <div class="profile-editor">
      <button
        class="profile-avatar-editor"
        :class="{ editable: canEditGroupProfile }"
        :disabled="!canEditGroupProfile || updatingGroupProfile"
        @click="$emit('trigger-group-avatar-upload')"
      >
        <img
          v-if="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
          :src="groupProfileDraft.avatarPreview || groupDetail.groupAvatar"
          class="avatar-img"
        />
        <span v-else>{{ groupProfileDraft.groupName?.charAt(0) || groupDetail.groupName?.charAt(0) || '群' }}</span>
        <span v-if="canEditGroupProfile" class="profile-avatar-mask">更换头像</span>
      </button>

      <div class="profile-fields">
        <label class="field-label" for="groupNameInput">群名称</label>
        <input
          id="groupNameInput"
          :value="groupProfileDraft.groupName"
          type="text"
          class="text-input filled-input"
          maxlength="30"
          :disabled="!canEditGroupProfile || updatingGroupProfile"
          placeholder="请输入群名称"
          @input="$emit('update:group-profile-name', ($event.target as HTMLInputElement).value || '')"
        />

        <label class="field-label" for="groupNoticeInput">群公告</label>
        <textarea
          id="groupNoticeInput"
          :value="noticeDraft"
          class="text-area"
          rows="4"
          maxlength="255"
          :disabled="!canEditNotice || updatingNotice"
          :placeholder="canEditNotice ? '请输入群公告' : '暂无群公告'"
          @input="$emit('update:notice-draft', ($event.target as HTMLTextAreaElement).value || '')"
        ></textarea>
      </div>
    </div>

    <div class="profile-action-row">
      <button
        class="secondary-btn"
        :disabled="(!isGroupProfileChanged && !isGroupNoticeChanged) || updatingGroupProfile || updatingNotice"
        @click="$emit('discard-group-profile-drafts')"
      >
        还原修改
      </button>
      <button
        v-if="canEditNotice"
        class="secondary-btn"
        :disabled="!isGroupNoticeChanged || updatingNotice"
        @click="$emit('submit-update-notice')"
      >
        {{ updatingNotice ? '保存公告中...' : '保存公告' }}
      </button>
      <button
        v-if="canEditGroupProfile"
        class="primary-btn"
        :disabled="!isGroupProfileChanged || updatingGroupProfile"
        @click="$emit('submit-update-group-profile')"
      >
        {{ updatingGroupProfile ? '保存资料中...' : '保存资料' }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { GroupDetail } from '../../types/chat'
import type { GroupProfileDraftState } from '../../utils/group-draft'

defineProps<{
  groupDetail: GroupDetail
  canEditGroupProfile: boolean
  canEditNotice: boolean
  updatingGroupProfile: boolean
  updatingNotice: boolean
  groupProfileDraft: Pick<GroupProfileDraftState, 'groupName' | 'avatarPreview'>
  noticeDraft: string
  isGroupProfileChanged: boolean
  isGroupNoticeChanged: boolean
}>()

defineEmits<{
  (event: 'trigger-group-avatar-upload'): void
  (event: 'update:group-profile-name', value: string): void
  (event: 'update:notice-draft', value: string): void
  (event: 'discard-group-profile-drafts'): void
  (event: 'submit-update-notice'): void
  (event: 'submit-update-group-profile'): void
}>()
</script>

<style scoped>
.panel-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-md);
  padding: 22px;
}

.profile-card {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.panel-title-row,
.profile-action-row {
  display: flex;
  align-items: center;
}

.panel-title-row {
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.panel-note {
  margin-top: 6px;
  color: var(--linkx-text-muted);
  font-size: 12px;
  line-height: 1.6;
}

.profile-editor {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: start;
  gap: 16px;
}

.profile-avatar-editor {
  position: relative;
  width: 96px;
  height: 96px;
  border: 1px solid var(--linkx-border);
  border-radius: 28px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 30px;
  font-weight: 700;
  cursor: default;
}

.profile-avatar-editor.editable {
  cursor: pointer;
}

.profile-avatar-mask {
  position: absolute;
  inset: auto 0 0;
  padding: 8px 10px;
  background: rgba(0, 0, 0, 0.48);
  color: white;
  font-size: 12px;
  text-align: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-fields {
  min-width: 0;
  flex: 1;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.field-label + .filled-input,
.field-label + .text-area {
  margin-bottom: 14px;
}

.text-input,
.text-area {
  width: 100%;
  color: var(--linkx-text);
  font-size: 14px;
  outline: none;
}

.filled-input {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
}

.text-input {
  border: none;
  background: transparent;
}

.text-area {
  min-height: 92px;
  padding: 12px 14px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg);
  resize: vertical;
  line-height: 1.6;
}

.profile-action-row {
  justify-content: flex-end;
  gap: 12px;
  flex-wrap: wrap;
}

.secondary-btn,
.primary-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  font-size: 13px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.secondary-btn {
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
}

.secondary-btn:hover {
  background: var(--linkx-bg-hover);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

@media (max-width: 1100px) {
  .profile-editor {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .profile-action-row {
    flex-direction: column;
    align-items: stretch;
  }

  .secondary-btn,
  .primary-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
