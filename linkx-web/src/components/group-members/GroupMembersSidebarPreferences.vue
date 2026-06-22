<template>
  <section class="panel-card preference-card">
    <div class="panel-title-row">
      <div>
        <div class="panel-title">我的群偏好</div>
        <div class="panel-note">只对当前账号生效，支持群备注和消息免打扰。</div>
      </div>
    </div>

    <div class="preference-form">
      <label class="field-label" for="groupRemarkInput">群备注</label>
      <input
        id="groupRemarkInput"
        :value="groupPreferenceDraft.groupRemark"
        type="text"
        class="text-input filled-input"
        maxlength="100"
        placeholder="给这个群设置一个你自己的备注名"
        @input="$emit('update:group-remark', ($event.target as HTMLInputElement).value || '')"
      />

      <label class="preference-switch">
        <input
          :checked="groupPreferenceDraft.notificationMuted"
          type="checkbox"
          @change="$emit('update:group-notification-muted', ($event.target as HTMLInputElement).checked)"
        />
        <span>群免打扰</span>
        <small>开启后该群新消息不再弹出桌面通知</small>
      </label>
    </div>

    <div class="profile-action-row">
      <button
        class="secondary-btn"
        :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
        @click="$emit('restore-group-preferences')"
      >
        还原偏好
      </button>
      <button
        class="primary-btn"
        :disabled="!isGroupPreferenceChanged || savingGroupPreferences"
        @click="$emit('submit-group-preferences')"
      >
        {{ savingGroupPreferences ? '保存中...' : '保存偏好' }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { GroupPreferenceDraftState } from '../../types/chat'

defineProps<{
  groupPreferenceDraft: GroupPreferenceDraftState
  isGroupPreferenceChanged: boolean
  savingGroupPreferences: boolean
}>()

defineEmits<{
  (event: 'update:group-remark', value: string): void
  (event: 'update:group-notification-muted', value: boolean): void
  (event: 'restore-group-preferences'): void
  (event: 'submit-group-preferences'): void
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

.preference-card {
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

.preference-form {
  display: grid;
  gap: 12px;
}

.field-label {
  display: block;
  margin-bottom: 8px;
  color: var(--linkx-text-secondary);
  font-size: 13px;
  font-weight: 600;
}

.text-input {
  width: 100%;
  color: var(--linkx-text);
  font-size: 14px;
  outline: none;
  border: none;
  background: transparent;
}

.filled-input {
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius);
  background: var(--linkx-bg);
}

.preference-switch {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px 10px;
  align-items: start;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
  color: var(--linkx-text);
}

.preference-switch input {
  margin-top: 3px;
}

.preference-switch span,
.preference-switch small {
  grid-column: 2;
}

.preference-switch small {
  color: var(--linkx-text-muted);
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
