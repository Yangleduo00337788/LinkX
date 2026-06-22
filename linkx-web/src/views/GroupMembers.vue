<template>
  <div class="group-members-page">
    <input ref="groupAvatarInputRef" type="file" accept="image/*" class="hidden-file-input" @change="handleGroupAvatarSelected" />

    <div class="page-shell">
      <div class="page-header">
        <div class="page-header-main">
          <button class="back-btn" @click="() => openGroupChat()">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M15 18l-6-6 6-6" />
            </svg>
            返回群聊
          </button>
          <div>
            <div class="page-title">群成员</div>
            <div class="page-subtitle">独立浏览群成员，并在这里直接完成成员权限管理</div>
          </div>
        </div>
        <div class="page-header-actions">
          <button class="secondary-btn" @click="copyGroupId">复制群号</button>
          <button class="primary-btn" :disabled="pageLoading || loadingGroupRequests" @click="refreshPageData">
            {{ pageLoading || loadingGroupRequests ? '刷新中...' : '刷新页面' }}
          </button>
        </div>
      </div>

      <div v-if="groupDetail" class="page-content">
        <GroupMembersSidebar
          :group-detail="groupDetail"
          :group-display-name="groupDisplayName"
          :owner-count="ownerCount"
          :admin-count="adminCount"
          :member-count="memberCount"
          :muted-count="mutedCount"
          :can-edit-group-profile="canEditGroupProfile"
          :can-edit-notice="canEditNotice"
          :updating-group-profile="updatingGroupProfile"
          :updating-notice="updatingNotice"
          :group-profile-draft="groupProfileDraft"
          :notice-draft="noticeDraft"
          :is-group-profile-changed="isGroupProfileChanged"
          :is-group-notice-changed="isGroupNoticeChanged"
          :group-preference-draft="groupPreferenceDraft"
          :is-group-preference-changed="isGroupPreferenceChanged"
          :saving-group-preferences="savingGroupPreferences"
          :search-text="searchText"
          :role-filters="roleFilters"
          :role-filter="roleFilter"
          :filtered-member-count="filteredMembers.length"
          :current-group-role="currentGroupRole"
          :available-friends-count="availableFriendsForCurrentGroup.length"
          :can-manage-members="canManageMembers"
          :is-group-owner="isGroupOwner"
          :can-dissolve-group="canDissolveGroup"
          @trigger-group-avatar-upload="triggerGroupAvatarUpload"
          @update:group-profile-name="groupProfileDraft.groupName = $event"
          @update:notice-draft="noticeDraft = $event"
          @discard-group-profile-drafts="discardGroupProfileDrafts"
          @submit-update-notice="submitUpdateNotice"
          @submit-update-group-profile="submitUpdateGroupProfile"
          @update:group-remark="groupPreferenceDraft.groupRemark = $event"
          @update:group-notification-muted="groupPreferenceDraft.notificationMuted = $event"
          @restore-group-preferences="syncGroupPreferenceDraft(groupDetail)"
          @submit-group-preferences="submitGroupPreferences"
          @update:search-text="searchText = $event"
          @update:role-filter="roleFilter = $event"
          @open-add-members-modal="openAddMembersModal"
          @open-transfer-owner-modal="openTransferOwnerModal"
          @leave-group="handleLeaveGroup"
          @dissolve-group="handleDissolveGroup"
        />

        <div class="main-column">
          <GroupMembersWorkspace
            :active-role-filter-label="activeRoleFilterLabel"
            :manageable-member-count="manageableMemberCount"
            :filtered-members="filteredMembers"
            :search-text="searchText"
            :can-manage-members="canManageMembers"
            :current-group-requests="currentGroupRequests"
            :request-action-loading-id="requestActionLoadingId"
            :action-loading="actionLoading"
            :current-user-id="userStore.userId || ''"
            :group-request-type-text="groupRequestTypeText"
            :group-request-tag-class="groupRequestTagClass"
            :build-group-request-message="buildGroupRequestMessage"
            :format-request-time="formatRequestTime"
            :can-operate-member="canOperateMember"
            :can-toggle-admin="canToggleAdmin"
            @accept-group-request="handleAcceptGroupRequest"
            @reject-group-request="handleRejectGroupRequest"
            @toggle-admin-role="toggleAdminRole"
            @toggle-mute-member="toggleMuteMember"
            @remove-member="handleRemoveMember"
          />

          <GroupMembersResources
            :group-message-search-loading="groupMessageSearchLoading"
            :message-search-keyword="messageSearchKeyword"
            :group-message-search-results="groupMessageSearchResults"
            :get-media-type-text="getMediaTypeText"
            :get-message-search-preview="getMessageSearchPreview"
            :group-media-loading="groupMediaLoading"
            :media-type="mediaType"
            :media-keyword="mediaKeyword"
            :group-media-items="groupMediaItems"
            :is-image-media="isImageMedia"
            @update:message-search-keyword="messageSearchKeyword = $event"
            @search-group-messages="searchGroupMessages"
            @open-group-chat="openGroupChat"
            @open-media-resource="openMediaResource"
            @update:media-type="mediaType = $event; void loadGroupMedia()"
            @update:media-keyword="mediaKeyword = $event"
            @load-group-media="loadGroupMedia"
            @copy-media-link="copyMediaLink"
          />
        </div>
      </div>

      <div v-else class="loading-panel">
        <div class="loading-spinner"></div>
        <div class="loading-text">正在加载群成员...</div>
      </div>
    </div>

    <GroupMembersDialogs
      :show-add-members-modal="showAddMembersModal"
      :available-friends-for-current-group="availableFriendsForCurrentGroup"
      :add-members-selection="addMembersSelection"
      :add-members-message="addMembersMessage"
      :adding-members="addingMembers"
      :show-transfer-owner-modal="showTransferOwnerModal"
      :transferable-members="transferableMembers"
      :transfer-owner-selection="transferOwnerSelection"
      :transferring-owner="transferringOwner"
      :show-mute-member-modal="showMuteMemberModal"
      :mute-target-member="muteTargetMember"
      :mute-minutes-input="muteMinutesInput"
      :action-loading="actionLoading"
      :confirm-dialog="confirmDialog"
      :show-notice-reminder="showNoticeReminder"
      :group-name="groupDetail?.groupName"
      :group-notice="groupDetail?.notice"
      :group-notice-update-time-text="formatDateTime(groupDetail?.noticeUpdateTime)"
      :acknowledging-notice-reminder="acknowledgingNoticeReminder"
      @close-add-members-modal="closeAddMembersModal"
      @update:add-members-selection="addMembersSelection = $event"
      @update:add-members-message="addMembersMessage = $event"
      @submit-add-members="submitAddMembers"
      @close-transfer-owner-modal="closeTransferOwnerModal"
      @update:transfer-owner-selection="transferOwnerSelection = $event"
      @submit-transfer-owner="submitTransferOwner"
      @close-mute-member-modal="closeMuteMemberModal"
      @update:mute-minutes-input="muteMinutesInput = $event"
      @submit-mute-member="submitMuteMember"
      @update:confirm-dialog-visible="confirmDialog.visible = $event"
      @cancel-confirm-dialog="cancelConfirmDialog"
      @confirm-confirm-dialog="confirmConfirmDialog"
      @update:show-notice-reminder="showNoticeReminder = $event"
      @acknowledge-notice-reminder="acknowledgeNoticeReminder"
    />
  </div>
</template>

<script setup lang="ts">
import GroupMembersDialogs from '../components/group-members/GroupMembersDialogs.vue'
import GroupMembersResources from '../components/group-members/GroupMembersResources.vue'
import GroupMembersSidebar from '../components/group-members/GroupMembersSidebar.vue'
import GroupMembersWorkspace from '../components/group-members/GroupMembersWorkspace.vue'
import { useGroupMembersPage } from '../hooks/useGroupMembersPage'

const {
  formatDateTime,
  confirmDialog,
  cancelConfirmDialog,
  confirmConfirmDialog,
  userStore,
  groupAvatarInputRef,
  groupDetail,
  pageLoading,
  loadingGroupRequests,
  actionLoading,
  requestActionLoadingId,
  searchText,
  roleFilter,
  updatingGroupProfile,
  updatingNotice,
  showNoticeReminder,
  acknowledgingNoticeReminder,
  noticeDraft,
  showAddMembersModal,
  addMembersSelection,
  addMembersMessage,
  addingMembers,
  showTransferOwnerModal,
  transferOwnerSelection,
  transferringOwner,
  showMuteMemberModal,
  muteTargetMember,
  muteMinutesInput,
  groupPreferenceDraft,
  savingGroupPreferences,
  mediaType,
  mediaKeyword,
  groupMediaItems,
  groupMediaLoading,
  messageSearchKeyword,
  groupMessageSearchResults,
  groupMessageSearchLoading,
  groupProfileDraft,
  roleFilters,
  currentGroupRole,
  canManageMembers,
  canEditGroupProfile,
  canEditNotice,
  isGroupOwner,
  canDissolveGroup,
  ownerCount,
  adminCount,
  mutedCount,
  memberCount,
  activeRoleFilterLabel,
  manageableMemberCount,
  groupDisplayName,
  isGroupPreferenceChanged,
  isGroupProfileChanged,
  isGroupNoticeChanged,
  availableFriendsForCurrentGroup,
  transferableMembers,
  currentGroupRequests,
  filteredMembers,
  discardGroupProfileDrafts,
  syncGroupPreferenceDraft,
  refreshPageData,
  copyGroupId,
  openGroupChat,
  getMediaTypeText,
  getMessageSearchPreview,
  copyMediaLink,
  openMediaResource,
  submitGroupPreferences,
  loadGroupMedia,
  searchGroupMessages,
  triggerGroupAvatarUpload,
  handleGroupAvatarSelected,
  groupRequestTypeText,
  groupRequestTagClass,
  buildGroupRequestMessage,
  formatRequestTime,
  handleAcceptGroupRequest,
  handleRejectGroupRequest,
  submitUpdateNotice,
  acknowledgeNoticeReminder,
  submitUpdateGroupProfile,
  openAddMembersModal,
  closeAddMembersModal,
  submitAddMembers,
  openTransferOwnerModal,
  closeTransferOwnerModal,
  submitTransferOwner,
  canOperateMember,
  canToggleAdmin,
  isImageMedia,
  toggleAdminRole,
  toggleMuteMember,
  closeMuteMemberModal,
  submitMuteMember,
  handleRemoveMember,
  handleDissolveGroup,
  handleLeaveGroup
} = useGroupMembersPage()

void groupAvatarInputRef
</script>

<style scoped>
.group-members-page {
  height: 100%;
  overflow-y: auto;
  padding: 24px;
  background:
    radial-gradient(circle at top right, rgba(0, 214, 143, 0.08), transparent 28%),
    radial-gradient(circle at left center, rgba(80, 145, 255, 0.08), transparent 24%),
    var(--linkx-bg);
}

.hidden-file-input {
  display: none;
}

.page-shell {
  width: min(1180px, 74vw, 100%);
  margin: 0 auto;
}

.page-header {
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);
  border: 1px solid var(--linkx-border);
  box-shadow: var(--linkx-shadow-md);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border-radius: var(--linkx-radius-lg);
}

.page-header-main,
.page-header-actions {
  display: flex;
  align-items: center;
}

.page-header-main {
  gap: 16px;
}

.page-header-actions {
  gap: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--linkx-text);
}

.page-subtitle,
.filter-hint {
  color: var(--linkx-text-muted);
}

.page-subtitle {
  margin-top: 6px;
  font-size: 13px;
}

.back-btn,
.primary-btn,
.secondary-btn {
  height: 38px;
  padding: 0 16px;
  border-radius: var(--linkx-radius);
  font-size: 13px;
  font-weight: 600;
  transition: var(--linkx-transition-fast);
}

.back-btn,
.secondary-btn {
  border: 1px solid var(--linkx-border);
  background: var(--linkx-bg-card);
  color: var(--linkx-text);
}

.primary-btn {
  border: none;
  background: var(--linkx-primary);
  color: white;
}

.back-btn:hover,
.secondary-btn:hover {
  background: var(--linkx-bg-hover);
}

.primary-btn:hover {
  background: var(--linkx-primary-hover);
}

.page-content {
  margin-top: 20px;
  display: grid;
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);
  align-items: start;
  gap: 24px;
}

.main-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.loading-panel {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.loading-text {
  color: var(--linkx-text);
  font-size: 16px;
  font-weight: 700;
}

.loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid var(--linkx-border);
  border-top-color: var(--linkx-primary);
  animation: spin 0.8s linear infinite;
}

.loading-text {
  margin-top: 14px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 1400px) {
  .page-shell {
    width: min(1120px, 84vw, 100%);
  }
}

@media (max-width: 1100px) {
  .page-shell {
    width: min(920px, 92vw, 100%);
  }

  .page-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .group-members-page {
    padding: 16px;
  }

  .page-shell {
    width: 100%;
  }

  .page-header,
  .page-header-main,
  .page-header-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header-actions {
    width: 100%;
  }

  .back-btn,
  .primary-btn,
  .secondary-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>
