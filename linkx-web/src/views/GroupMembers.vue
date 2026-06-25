<!-- 行注：开始定义模板区域 -->
<template>
  <!-- 行注：渲染容器 -->
  <div class="group-members-page">
    <!-- 行注：渲染输入框 -->
    <input ref="groupAvatarInputRef" type="file" accept="image/*" class="hidden-file-input" @change="handleGroupAvatarSelected" />
    <!-- 行注：渲染容器 -->
    <div class="page-shell">
      <!-- 行注：渲染容器 -->
      <div class="page-header">
        <!-- 行注：渲染容器 -->
        <div class="page-header-main">
          <!-- 行注：渲染按钮 -->
          <button class="back-btn" @click="() => openGroupChat()">
            <!-- 行注：渲染图标容器 -->
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <!-- 行注：补充图标路径 -->
              <path d="M15 18l-6-6 6-6" />
            <!-- 行注：结束图标容器 -->
            </svg>
            <!-- 行注：展示“返回群聊”文案 -->
            返回群聊
          <!-- 行注：结束按钮 -->
          </button>
          <!-- 行注：渲染容器 -->
          <div>
            <!-- 行注：展示“群成员”文案 -->
            <div class="page-title">群成员</div>
            <!-- 行注：展示“独立浏览群成员，并在这里直接完成成员”文案 -->
            <div class="page-subtitle">独立浏览群成员，并在这里直接完成成员权限管理</div>
          <!-- 行注：结束容器 -->
          </div>
        <!-- 行注：结束容器 -->
        </div>
        <!-- 行注：渲染容器 -->
        <div class="page-header-actions">
          <!-- 行注：展示“复制群号”文案 -->
          <button class="secondary-btn" @click="copyGroupId">复制群号</button>
          <!-- 行注：渲染按钮 -->
          <button class="primary-btn" :disabled="pageLoading || loadingGroupRequests" @click="refreshPageData">
            <!-- 行注：渲染动态文本 -->
            {{ pageLoading || loadingGroupRequests ? '刷新中...' : '刷新页面' }}
          <!-- 行注：结束按钮 -->
          </button>
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-if="groupDetail" class="page-content">
        <!-- 行注：渲染 GroupMembersSidebar 组件 -->
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
        <!-- 行注：渲染容器 -->
        <div class="main-column">
          <!-- 行注：渲染 GroupMembersWorkspace 组件 -->
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
          <!-- 行注：渲染 GroupMembersResources 组件 -->
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
        <!-- 行注：结束容器 -->
        </div>
      <!-- 行注：结束容器 -->
      </div>
      <!-- 行注：渲染容器 -->
      <div v-else class="loading-panel">
        <!-- 行注：渲染容器 -->
        <div class="loading-spinner"></div>
        <!-- 行注：展示“正在加载群成员...”文案 -->
        <div class="loading-text">正在加载群成员...</div>
      <!-- 行注：结束容器 -->
      </div>
    <!-- 行注：结束容器 -->
    </div>
    <!-- 行注：渲染 GroupMembersDialogs 组件 -->
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
  <!-- 行注：结束容器 -->
  </div>
<!-- 行注：结束模板区域 -->
</template>
<!-- 行注：开始定义脚本逻辑区域 -->
<script setup lang="ts">
/**
 * 群成员详情页面，处理成员、资源和申请相关信息。
 */
import GroupMembersDialogs from '../components/group-members/GroupMembersDialogs.vue'  // 行注：引入 GroupMembersDialogs 组件
import GroupMembersResources from '../components/group-members/GroupMembersResources.vue'  // 行注：引入 GroupMembersResources 组件
import GroupMembersSidebar from '../components/group-members/GroupMembersSidebar.vue'  // 行注：引入 GroupMembersSidebar 组件
import GroupMembersWorkspace from '../components/group-members/GroupMembersWorkspace.vue'  // 行注：引入 GroupMembersWorkspace 组件
import { useGroupMembersPage } from '../hooks/useGroupMembersPage'  // 行注：引入 useGroupMembersPage 能力

const {  // 行注：开始解构当前返回值
  formatDateTime,  // 行注：解构 formatDateTime 方法
  confirmDialog,  // 行注：解构 confirmDialog 状态
  cancelConfirmDialog,  // 行注：解构 cancelConfirmDialog 状态
  confirmConfirmDialog,  // 行注：解构 confirmConfirmDialog 状态
  userStore,  // 行注：解构 userStore 状态
  groupAvatarInputRef,  // 行注：解构 groupAvatarInputRef 状态
  groupDetail,  // 行注：解构 groupDetail 状态
  pageLoading,  // 行注：解构 pageLoading 状态
  loadingGroupRequests,  // 行注：解构 loadingGroupRequests 方法
  actionLoading,  // 行注：解构 actionLoading 状态
  requestActionLoadingId,  // 行注：解构 requestActionLoadingId 状态
  searchText,  // 行注：解构 searchText 状态
  roleFilter,  // 行注：解构 roleFilter 状态
  updatingGroupProfile,  // 行注：解构 updatingGroupProfile 状态
  updatingNotice,  // 行注：解构 updatingNotice 状态
  showNoticeReminder,  // 行注：解构 showNoticeReminder 方法
  acknowledgingNoticeReminder,  // 行注：解构 acknowledgingNoticeReminder 状态
  noticeDraft,  // 行注：解构 noticeDraft 状态
  showAddMembersModal,  // 行注：解构 showAddMembersModal 状态
  addMembersSelection,  // 行注：解构 addMembersSelection 状态
  addMembersMessage,  // 行注：解构 addMembersMessage 状态
  addingMembers,  // 行注：解构 addingMembers 状态
  showTransferOwnerModal,  // 行注：解构 showTransferOwnerModal 状态
  transferOwnerSelection,  // 行注：解构 transferOwnerSelection 状态
  transferringOwner,  // 行注：解构 transferringOwner 状态
  showMuteMemberModal,  // 行注：解构 showMuteMemberModal 状态
  muteTargetMember,  // 行注：解构 muteTargetMember 状态
  muteMinutesInput,  // 行注：解构 muteMinutesInput 状态
  groupPreferenceDraft,  // 行注：解构 groupPreferenceDraft 状态
  savingGroupPreferences,  // 行注：解构 savingGroupPreferences 状态
  mediaType,  // 行注：解构 mediaType 状态
  mediaKeyword,  // 行注：解构 mediaKeyword 状态
  groupMediaItems,  // 行注：解构 groupMediaItems 状态
  groupMediaLoading,  // 行注：解构 groupMediaLoading 状态
  messageSearchKeyword,  // 行注：解构 messageSearchKeyword 状态
  groupMessageSearchResults,  // 行注：解构 groupMessageSearchResults 状态
  groupMessageSearchLoading,  // 行注：解构 groupMessageSearchLoading 状态
  groupProfileDraft,  // 行注：解构 groupProfileDraft 状态
  roleFilters,  // 行注：解构 roleFilters 状态
  currentGroupRole,  // 行注：解构 currentGroupRole 状态
  canManageMembers,  // 行注：解构 canManageMembers 状态
  canEditGroupProfile,  // 行注：解构 canEditGroupProfile 状态
  canEditNotice,  // 行注：解构 canEditNotice 状态
  isGroupOwner,  // 行注：解构 isGroupOwner 状态
  canDissolveGroup,  // 行注：解构 canDissolveGroup 状态
  ownerCount,  // 行注：解构 ownerCount 状态
  adminCount,  // 行注：解构 adminCount 状态
  mutedCount,  // 行注：解构 mutedCount 状态
  memberCount,  // 行注：解构 memberCount 状态
  activeRoleFilterLabel,  // 行注：解构 activeRoleFilterLabel 状态
  manageableMemberCount,  // 行注：解构 manageableMemberCount 状态
  groupDisplayName,  // 行注：解构 groupDisplayName 状态
  isGroupPreferenceChanged,  // 行注：解构 isGroupPreferenceChanged 状态
  isGroupProfileChanged,  // 行注：解构 isGroupProfileChanged 状态
  isGroupNoticeChanged,  // 行注：解构 isGroupNoticeChanged 状态
  availableFriendsForCurrentGroup,  // 行注：解构 availableFriendsForCurrentGroup 状态
  transferableMembers,  // 行注：解构 transferableMembers 状态
  currentGroupRequests,  // 行注：解构 currentGroupRequests 状态
  filteredMembers,  // 行注：解构 filteredMembers 状态
  discardGroupProfileDrafts,  // 行注：解构 discardGroupProfileDrafts 状态
  syncGroupPreferenceDraft,  // 行注：解构 syncGroupPreferenceDraft 方法
  refreshPageData,  // 行注：解构 refreshPageData 方法
  copyGroupId,  // 行注：解构 copyGroupId 方法
  openGroupChat,  // 行注：解构 openGroupChat 方法
  getMediaTypeText,  // 行注：解构 getMediaTypeText 方法
  getMessageSearchPreview,  // 行注：解构 getMessageSearchPreview 方法
  copyMediaLink,  // 行注：解构 copyMediaLink 方法
  openMediaResource,  // 行注：解构 openMediaResource 方法
  submitGroupPreferences,  // 行注：解构 submitGroupPreferences 方法
  loadGroupMedia,  // 行注：解构 loadGroupMedia 方法
  searchGroupMessages,  // 行注：解构 searchGroupMessages 状态
  triggerGroupAvatarUpload,  // 行注：解构 triggerGroupAvatarUpload 方法
  handleGroupAvatarSelected,  // 行注：解构 handleGroupAvatarSelected 方法
  groupRequestTypeText,  // 行注：解构 groupRequestTypeText 状态
  groupRequestTagClass,  // 行注：解构 groupRequestTagClass 状态
  buildGroupRequestMessage,  // 行注：解构 buildGroupRequestMessage 方法
  formatRequestTime,  // 行注：解构 formatRequestTime 方法
  handleAcceptGroupRequest,  // 行注：解构 handleAcceptGroupRequest 方法
  handleRejectGroupRequest,  // 行注：解构 handleRejectGroupRequest 方法
  submitUpdateNotice,  // 行注：解构 submitUpdateNotice 方法
  acknowledgeNoticeReminder,  // 行注：解构 acknowledgeNoticeReminder 状态
  submitUpdateGroupProfile,  // 行注：解构 submitUpdateGroupProfile 方法
  openAddMembersModal,  // 行注：解构 openAddMembersModal 方法
  closeAddMembersModal,  // 行注：解构 closeAddMembersModal 方法
  submitAddMembers,  // 行注：解构 submitAddMembers 方法
  openTransferOwnerModal,  // 行注：解构 openTransferOwnerModal 方法
  closeTransferOwnerModal,  // 行注：解构 closeTransferOwnerModal 方法
  submitTransferOwner,  // 行注：解构 submitTransferOwner 方法
  canOperateMember,  // 行注：解构 canOperateMember 状态
  canToggleAdmin,  // 行注：解构 canToggleAdmin 状态
  isImageMedia,  // 行注：解构 isImageMedia 状态
  toggleAdminRole,  // 行注：解构 toggleAdminRole 方法
  toggleMuteMember,  // 行注：解构 toggleMuteMember 方法
  closeMuteMemberModal,  // 行注：解构 closeMuteMemberModal 方法
  submitMuteMember,  // 行注：解构 submitMuteMember 方法
  handleRemoveMember,  // 行注：解构 handleRemoveMember 方法
  handleDissolveGroup,  // 行注：解构 handleDissolveGroup 方法
  handleLeaveGroup  // 行注：解构 handleLeaveGroup 方法
} = useGroupMembersPage()  // 行注：从 useGroupMembersPage 中解构所需能力

void groupAvatarInputRef  // 行注：补充当前表达式
</script>
<!-- 行注：开始定义样式区域 -->
<style scoped>
.group-members-page {  /* 行注：定义 .group-members-page 样式 */
  height: 100%;  /* 行注：设置 height 样式 */
  overflow-y: auto;  /* 行注：设置 overflow-y 样式 */
  padding: 24px;  /* 行注：设置 padding 样式 */
  background:  /* 行注：开始设置 background 样式 */
    radial-gradient(circle at top right, rgba(0, 214, 143, 0.08), transparent 28%),  /* 行注：补充 radial-gradient(circle at top right, rgba(0, 214, 143, 0.08), transparent 28%) 选择器 */
    radial-gradient(circle at left center, rgba(80, 145, 255, 0.08), transparent 24%),  /* 行注：补充 radial-gradient(circle at left center, rgba(80, 145, 255, 0.08), transparent 24%) 选择器 */
    var(--linkx-bg);  /* 行注：补充 background 的取值 */
}  /* 行注：结束当前样式块 */

.hidden-file-input {  /* 行注：定义 .hidden-file-input 样式 */
  display: none;  /* 行注：设置 display 样式 */
}  /* 行注：结束当前样式块 */

.page-shell {  /* 行注：定义 .page-shell 样式 */
  width: min(1180px, 74vw, 100%);  /* 行注：设置 width 样式 */
  margin: 0 auto;  /* 行注：设置 margin 样式 */
}  /* 行注：结束当前样式块 */

.page-header {  /* 行注：定义 .page-header 样式 */
  background: color-mix(in srgb, var(--linkx-bg-card) 92%, transparent);  /* 行注：设置 background 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  box-shadow: var(--linkx-shadow-md);  /* 行注：设置 box-shadow 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: space-between;  /* 行注：设置 justify-content 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
  padding: 20px 24px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius-lg);  /* 行注：设置 border-radius 样式 */
}  /* 行注：结束当前样式块 */

.page-header-main,  /* 行注：补充 .page-header-main 选择器 */
.page-header-actions {  /* 行注：定义 .page-header-actions 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
}  /* 行注：结束当前样式块 */

.page-header-main {  /* 行注：定义 .page-header-main 样式 */
  gap: 16px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.page-header-actions {  /* 行注：定义 .page-header-actions 样式 */
  gap: 12px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.page-title {  /* 行注：定义 .page-title 样式 */
  font-size: 24px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.page-subtitle,  /* 行注：补充 .page-subtitle 选择器 */
.filter-hint {  /* 行注：定义 .filter-hint 样式 */
  color: var(--linkx-text-muted);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.page-subtitle {  /* 行注：定义 .page-subtitle 样式 */
  margin-top: 6px;  /* 行注：设置 margin-top 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
}  /* 行注：结束当前样式块 */

.back-btn,  /* 行注：补充 .back-btn 选择器 */
.primary-btn,  /* 行注：补充 .primary-btn 选择器 */
.secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
  height: 38px;  /* 行注：设置 height 样式 */
  padding: 0 16px;  /* 行注：设置 padding 样式 */
  border-radius: var(--linkx-radius);  /* 行注：设置 border-radius 样式 */
  font-size: 13px;  /* 行注：设置 font-size 样式 */
  font-weight: 600;  /* 行注：设置 font-weight 样式 */
  transition: var(--linkx-transition-fast);  /* 行注：设置 transition 样式 */
}  /* 行注：结束当前样式块 */

.back-btn,  /* 行注：补充 .back-btn 选择器 */
.secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
  border: 1px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  background: var(--linkx-bg-card);  /* 行注：设置 background 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn {  /* 行注：定义 .primary-btn 样式 */
  border: none;  /* 行注：设置 border 样式 */
  background: var(--linkx-primary);  /* 行注：设置 background 样式 */
  color: white;  /* 行注：设置 color 样式 */
}  /* 行注：结束当前样式块 */

.back-btn:hover,  /* 行注：补充 .back-btn:hover 选择器 */
.secondary-btn:hover {  /* 行注：定义 .secondary-btn:hover 样式 */
  background: var(--linkx-bg-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.primary-btn:hover {  /* 行注：定义 .primary-btn:hover 样式 */
  background: var(--linkx-primary-hover);  /* 行注：设置 background 样式 */
}  /* 行注：结束当前样式块 */

.page-content {  /* 行注：定义 .page-content 样式 */
  margin-top: 20px;  /* 行注：设置 margin-top 样式 */
  display: grid;  /* 行注：设置 display 样式 */
  grid-template-columns: minmax(300px, 360px) minmax(0, 1fr);  /* 行注：设置 grid-template-columns 样式 */
  align-items: start;  /* 行注：设置 align-items 样式 */
  gap: 24px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.main-column {  /* 行注：定义 .main-column 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  gap: 24px;  /* 行注：设置 gap 样式 */
}  /* 行注：结束当前样式块 */

.loading-panel {  /* 行注：定义 .loading-panel 样式 */
  min-height: 240px;  /* 行注：设置 min-height 样式 */
  display: flex;  /* 行注：设置 display 样式 */
  flex-direction: column;  /* 行注：设置 flex-direction 样式 */
  align-items: center;  /* 行注：设置 align-items 样式 */
  justify-content: center;  /* 行注：设置 justify-content 样式 */
}  /* 行注：结束当前样式块 */

.loading-text {  /* 行注：定义 .loading-text 样式 */
  color: var(--linkx-text);  /* 行注：设置 color 样式 */
  font-size: 16px;  /* 行注：设置 font-size 样式 */
  font-weight: 700;  /* 行注：设置 font-weight 样式 */
}  /* 行注：结束当前样式块 */

.loading-spinner {  /* 行注：定义 .loading-spinner 样式 */
  width: 28px;  /* 行注：设置 width 样式 */
  height: 28px;  /* 行注：设置 height 样式 */
  border-radius: 50%;  /* 行注：设置 border-radius 样式 */
  border: 3px solid var(--linkx-border);  /* 行注：设置 border 样式 */
  border-top-color: var(--linkx-primary);  /* 行注：设置 border-top-color 样式 */
  animation: spin 0.8s linear infinite;  /* 行注：设置 animation 样式 */
}  /* 行注：结束当前样式块 */

.loading-text {  /* 行注：定义 .loading-text 样式 */
  margin-top: 14px;  /* 行注：设置 margin-top 样式 */
}  /* 行注：结束当前样式块 */

@keyframes spin {  /* 行注：声明关键帧动画 */
  to {  /* 行注：设置动画结束状态 */
    transform: rotate(360deg);  /* 行注：设置 transform 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1400px) {  /* 行注：声明响应式样式区块 */
  .page-shell {  /* 行注：定义 .page-shell 样式 */
    width: min(1120px, 84vw, 100%);  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 1100px) {  /* 行注：声明响应式样式区块 */
  .page-shell {  /* 行注：定义 .page-shell 样式 */
    width: min(920px, 92vw, 100%);  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .page-content {  /* 行注：定义 .page-content 样式 */
    grid-template-columns: 1fr;  /* 行注：设置 grid-template-columns 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */

@media (max-width: 768px) {  /* 行注：声明响应式样式区块 */
  .group-members-page {  /* 行注：定义 .group-members-page 样式 */
    padding: 16px;  /* 行注：设置 padding 样式 */
  }  /* 行注：结束当前样式块 */

  .page-shell {  /* 行注：定义 .page-shell 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .page-header,  /* 行注：补充 .page-header 选择器 */
  .page-header-main,  /* 行注：补充 .page-header-main 选择器 */
  .page-header-actions {  /* 行注：定义 .page-header-actions 样式 */
    flex-direction: column;  /* 行注：设置 flex-direction 样式 */
    align-items: stretch;  /* 行注：设置 align-items 样式 */
  }  /* 行注：结束当前样式块 */

  .page-header-actions {  /* 行注：定义 .page-header-actions 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
  }  /* 行注：结束当前样式块 */

  .back-btn,  /* 行注：补充 .back-btn 选择器 */
  .primary-btn,  /* 行注：补充 .primary-btn 选择器 */
  .secondary-btn {  /* 行注：定义 .secondary-btn 样式 */
    width: 100%;  /* 行注：设置 width 样式 */
    justify-content: center;  /* 行注：设置 justify-content 样式 */
  }  /* 行注：结束当前样式块 */
}  /* 行注：结束当前样式块 */
</style>
