<template>
  <section class="panel-card manage-card">
    <div class="panel-title">群管理</div>
    <div class="manage-subtitle">把常用群操作集中到这里处理，减少在聊天页和成员页之间来回切换。</div>

    <div class="manage-summary-grid">
      <div class="manage-summary-item">
        <span class="manage-summary-label">我的权限</span>
        <strong>{{ roleText(currentGroupRole) }}</strong>
      </div>
      <div class="manage-summary-item">
        <span class="manage-summary-label">可邀请好友</span>
        <strong>{{ availableFriendsCount }}</strong>
      </div>
    </div>

    <div class="manage-action-list">
      <button v-if="canManageMembers" class="secondary-btn action-btn" @click="$emit('open-add-members-modal')">
        邀请成员
      </button>
      <button v-if="isGroupOwner" class="secondary-btn action-btn" @click="$emit('open-transfer-owner-modal')">
        转让群主
      </button>
      <button v-if="!isGroupOwner" class="secondary-btn action-btn warn" @click="$emit('leave-group')">
        退出群聊
      </button>
      <button v-if="canDissolveGroup" class="secondary-btn action-btn danger" @click="$emit('dissolve-group')">
        解散群聊
      </button>
    </div>

    <div class="manage-tip">
      <span v-if="canManageMembers">你可以直接邀请好友或处理群管理动作。</span>
      <span v-else>当前仅支持浏览成员信息，如需更多操作请联系群管理员。</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { roleText } from '../../utils/chat'

defineProps<{
  currentGroupRole: number
  availableFriendsCount: number
  canManageMembers: boolean
  isGroupOwner: boolean
  canDissolveGroup: boolean
}>()

defineEmits<{
  (event: 'open-add-members-modal'): void
  (event: 'open-transfer-owner-modal'): void
  (event: 'leave-group'): void
  (event: 'dissolve-group'): void
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

.panel-title {
  color: var(--linkx-text);
  font-size: 17px;
  font-weight: 700;
}

.manage-card {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.manage-subtitle {
  color: var(--linkx-text-muted);
  font-size: 13px;
  line-height: 1.7;
}

.manage-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.manage-summary-item {
  padding: 14px 16px;
  border-radius: 18px;
  background: var(--linkx-bg-hover);
  border: 1px solid color-mix(in srgb, var(--linkx-border) 86%, transparent);
}

.manage-summary-label {
  display: block;
  color: var(--linkx-text-muted);
  font-size: 12px;
}

.manage-summary-item strong {
  display: block;
  margin-top: 8px;
  color: var(--linkx-text);
  font-size: 18px;
}

.manage-action-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.secondary-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
  font-size: 13px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.secondary-btn:hover {
  background: var(--linkx-bg-hover);
}

.action-btn {
  width: 100%;
  justify-content: center;
}

.action-btn.warn {
  color: #d88914;
}

.action-btn.danger {
  color: #ff6b6b;
}

.manage-tip {
  color: var(--linkx-text-secondary);
  font-size: 12px;
  line-height: 1.7;
  padding: 12px 14px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--linkx-bg-hover) 68%, transparent);
}

@media (max-width: 1100px) {
  .manage-summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .secondary-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
