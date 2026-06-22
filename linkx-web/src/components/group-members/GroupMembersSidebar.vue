<template>
  <aside class="side-column">
    <section class="group-hero-card">
      <div class="group-hero-top">
        <div class="group-avatar">
          <img v-if="groupDetail.groupAvatar" :src="groupDetail.groupAvatar" class="avatar-img" />
          <span v-else>{{ groupDetail.groupName?.charAt(0) || '群' }}</span>
        </div>
        <div class="group-hero-main">
          <div class="group-name-row">
            <h1>{{ groupDisplayName }}</h1>
            <span class="role-chip self-role" :class="roleClass(groupDetail.myRole)">我是{{ roleText(groupDetail.myRole) }}</span>
          </div>
          <div v-if="groupDetail.groupRemark" class="group-alias-hint">原群名：{{ groupDetail.groupName }}</div>
          <div class="group-meta-row">
            <span>群号 {{ groupDetail.id }}</span>
            <span>{{ groupDetail.memberCount }} / {{ groupDetail.maxMembers }} 人</span>
          </div>
          <div class="group-notice">
            {{ groupDetail.notice?.trim() || '暂无群公告' }}
          </div>
        </div>
      </div>
      <div class="group-stat-row">
        <div class="stat-pill">
          <span class="stat-label">群主</span>
          <strong>{{ ownerCount }}</strong>
        </div>
        <div class="stat-pill">
          <span class="stat-label">管理员</span>
          <strong>{{ adminCount }}</strong>
        </div>
        <div class="stat-pill">
          <span class="stat-label">普通成员</span>
          <strong>{{ memberCount }}</strong>
        </div>
        <div class="stat-pill warn">
          <span class="stat-label">已禁言</span>
          <strong>{{ mutedCount }}</strong>
        </div>
      </div>
    </section>

    <GroupMembersSidebarProfile
      :group-detail="groupDetail"
      :can-edit-group-profile="canEditGroupProfile"
      :can-edit-notice="canEditNotice"
      :updating-group-profile="updatingGroupProfile"
      :updating-notice="updatingNotice"
      :group-profile-draft="groupProfileDraft"
      :notice-draft="noticeDraft"
      :is-group-profile-changed="isGroupProfileChanged"
      :is-group-notice-changed="isGroupNoticeChanged"
      @trigger-group-avatar-upload="$emit('trigger-group-avatar-upload')"
      @update:group-profile-name="$emit('update:group-profile-name', $event)"
      @update:notice-draft="$emit('update:notice-draft', $event)"
      @discard-group-profile-drafts="$emit('discard-group-profile-drafts')"
      @submit-update-notice="$emit('submit-update-notice')"
      @submit-update-group-profile="$emit('submit-update-group-profile')"
    />

    <GroupMembersSidebarPreferences
      :group-preference-draft="groupPreferenceDraft"
      :is-group-preference-changed="isGroupPreferenceChanged"
      :saving-group-preferences="savingGroupPreferences"
      @update:group-remark="$emit('update:group-remark', $event)"
      @update:group-notification-muted="$emit('update:group-notification-muted', $event)"
      @restore-group-preferences="$emit('restore-group-preferences')"
      @submit-group-preferences="$emit('submit-group-preferences')"
    />

    <GroupMembersSidebarFilter
      :search-text="searchText"
      :role-filters="roleFilters"
      :role-filter="roleFilter"
      :filtered-member-count="filteredMemberCount"
      :total-member-count="groupDetail.members.length"
      @update:search-text="$emit('update:search-text', $event)"
      @update:role-filter="$emit('update:role-filter', $event)"
    />

    <GroupMembersSidebarManage
      :current-group-role="currentGroupRole"
      :available-friends-count="availableFriendsCount"
      :can-manage-members="canManageMembers"
      :is-group-owner="isGroupOwner"
      :can-dissolve-group="canDissolveGroup"
      @open-add-members-modal="$emit('open-add-members-modal')"
      @open-transfer-owner-modal="$emit('open-transfer-owner-modal')"
      @leave-group="$emit('leave-group')"
      @dissolve-group="$emit('dissolve-group')"
    />
  </aside>
</template>

<script setup lang="ts">
import type { GroupDetail, GroupPreferenceDraftState, GroupRoleFilter, GroupRoleFilterOption } from '../../types/chat'
import type { GroupProfileDraftState } from '../../utils/group-draft'
import { roleClass, roleText } from '../../utils/chat'
import GroupMembersSidebarFilter from './GroupMembersSidebarFilter.vue'
import GroupMembersSidebarManage from './GroupMembersSidebarManage.vue'
import GroupMembersSidebarPreferences from './GroupMembersSidebarPreferences.vue'
import GroupMembersSidebarProfile from './GroupMembersSidebarProfile.vue'

defineProps<{
  groupDetail: GroupDetail
  groupDisplayName: string
  ownerCount: number
  adminCount: number
  memberCount: number
  mutedCount: number
  canEditGroupProfile: boolean
  canEditNotice: boolean
  updatingGroupProfile: boolean
  updatingNotice: boolean
  groupProfileDraft: GroupProfileDraftState
  noticeDraft: string
  isGroupProfileChanged: boolean
  isGroupNoticeChanged: boolean
  groupPreferenceDraft: GroupPreferenceDraftState
  isGroupPreferenceChanged: boolean
  savingGroupPreferences: boolean
  searchText: string
  roleFilters: GroupRoleFilterOption[]
  roleFilter: GroupRoleFilter
  filteredMemberCount: number
  currentGroupRole: number
  availableFriendsCount: number
  canManageMembers: boolean
  isGroupOwner: boolean
  canDissolveGroup: boolean
}>()

defineEmits<{
  (event: 'trigger-group-avatar-upload'): void
  (event: 'update:group-profile-name', value: string): void
  (event: 'update:notice-draft', value: string): void
  (event: 'discard-group-profile-drafts'): void
  (event: 'submit-update-notice'): void
  (event: 'submit-update-group-profile'): void
  (event: 'update:group-remark', value: string): void
  (event: 'update:group-notification-muted', value: boolean): void
  (event: 'restore-group-preferences'): void
  (event: 'submit-group-preferences'): void
  (event: 'update:search-text', value: string): void
  (event: 'update:role-filter', value: GroupRoleFilter): void
  (event: 'open-add-members-modal'): void
  (event: 'open-transfer-owner-modal'): void
  (event: 'leave-group'): void
  (event: 'dissolve-group'): void
}>()
</script>

<style scoped>
.side-column {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.group-hero-card,
.panel-card {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  border-radius: var(--linkx-radius-lg);
  box-shadow: var(--linkx-shadow-md);
  padding: 22px;
}

.group-hero-card {
  position: relative;
  overflow: hidden;
}

.group-hero-card::after {
  content: '';
  position: absolute;
  inset: auto -60px -60px auto;
  width: 180px;
  height: 180px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(0, 214, 143, 0.14), transparent 68%);
  pointer-events: none;
}

.group-hero-top,
.group-meta-row,
.group-stat-row,
.panel-title-row,
.profile-action-row {
  display: flex;
  align-items: center;
}

.group-hero-top {
  align-items: flex-start;
  gap: 18px;
}

.group-avatar {
  width: 68px;
  height: 68px;
  border-radius: 20px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: linear-gradient(135deg, #00d68f 0%, #00c9a7 100%);
  color: white;
  font-size: 24px;
  font-weight: 700;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.group-hero-main,
.profile-fields {
  min-width: 0;
  flex: 1;
}

.group-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.group-name-row h1 {
  margin: 0;
  font-size: 28px;
  line-height: 1.2;
  color: var(--linkx-text);
}

.group-meta-row {
  gap: 14px;
  margin-top: 10px;
  color: var(--linkx-text-muted);
  font-size: 13px;
  flex-wrap: wrap;
}

.group-alias-hint {
  margin-top: 8px;
  color: var(--linkx-text-secondary);
  font-size: 12px;
  line-height: 1.6;
}

.group-notice {
  margin-top: 14px;
  max-width: 680px;
  color: var(--linkx-text-secondary);
  font-size: 14px;
  line-height: 1.7;
}

.stat-pill,
.role-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-weight: 600;
}

.group-stat-row {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.stat-pill {
  min-width: 0;
  padding: 14px 16px;
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  border-radius: 18px;
}

.stat-pill strong {
  color: var(--linkx-text);
  font-size: 20px;
}

.stat-pill.warn strong {
  color: #f0a020;
}

.stat-label {
  font-size: 12px;
}

.role-chip {
  height: 24px;
  padding: 0 10px;
  font-size: 12px;
}

.role-chip.owner,
.self-role.owner {
  background: rgba(255, 187, 80, 0.18);
  color: #d88914;
}

.role-chip.admin,
.self-role.admin {
  background: rgba(80, 145, 255, 0.16);
  color: #4f86ff;
}

.role-chip.member,
.self-role.member {
  background: var(--linkx-bg-hover);
  color: var(--linkx-text-secondary);
}

@media (max-width: 1100px) {
  .side-column {
    position: static;
  }
}

@media (max-width: 768px) {
  .group-hero-top {
    flex-direction: column;
    align-items: stretch;
  }

  .group-stat-row {
    grid-template-columns: 1fr;
  }
}
</style>
